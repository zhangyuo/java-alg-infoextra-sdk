package com.zy.alg.infoextra.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.zbj.alg.def.common.Model;
import com.zy.alg.infoextra.seg.LongSentenceSegment;
import com.zy.alg.infoextra.utils.InitialDictionary;
import com.zy.alg.infoextra.utils.Sort;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;

import com.zy.alg.infoextra.utils.AreaFeature;
import com.zy.alg.infoextra.utils.MyEntry;
import com.zy.alg.infoextra.utils.OutputPosiInfo;
import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.LoadDic;
import com.zbj.alg.seg.service.ServiceSegModel;
import com.zbj.alg.seg.service.ServiceSegModelEnhance;
import com.zbj.alg.seg.splitWord.DicAnalysis;

public class PositionExtractionEnhancer implements PositionExtraction, Model {

    /**
     * AreaTagLirbry + 地域缩写词典：AbbreviationWord.txt
     */
    private static Forest areadic = new Forest();
    /**
     * 服务标签库
     */
    private static Forest serdic = new Forest();
    /**
     * 服务类目标签权重:类目标签+权值
     */
    private static Map<String, Double> serCatTagWeight = new HashMap<String, Double>();
    /**
     * 地域缩写词表
     */
    private static Map<String, Set<String>> areaAbbrCity = new HashMap<String, Set<String>>();
    /**
     * 服务类目标签分词:seg词+权重
     */
    private static Map<String, Double> cateSegWordWeight = new HashMap<String, Double>();
    /**
     * 地域索引词典
     */
    private static Map<String, String> wordArea = new HashMap<String, String>();
    /**
     * 地域标签库-(area,(attr,(tag)))
     */
    private static Map<String, Map<String, Set<String>>> areaAttrTag = new HashMap<String, Map<String, Set<String>>>();

    ServiceSegModel ssme = null;

    public PositionExtractionEnhancer(String resourcePath) throws IOException {

        String serCatTagWeightPath = resourcePath + "SerCatTagWeight.txt";
        String cityTablePath = resourcePath + "AreaTagLibrary";
        String abbreviationWordPath = resourcePath + "AbbreviationWord.txt";

        String SerLibPath = resourcePath + "ServiceTagLibrary";

        ssme = ServiceSegModelEnhance.getInstance();
        InitialDictionary.insertSerDic(serdic, SerLibPath);

        BufferedReader sr = new BufferedReader(new InputStreamReader(
                new FileInputStream(serCatTagWeightPath), "utf-8"));
        BufferedReader cr = new BufferedReader(new InputStreamReader(
                new FileInputStream(cityTablePath), "utf-8"));
        BufferedReader ar = new BufferedReader(new InputStreamReader(
                new FileInputStream(abbreviationWordPath), "utf-8"));
        // 服务类目标签权重加载
        String sline = null;
        while ((sline = sr.readLine()) != null) {
            String[] seg = sline.split("\t");
            if (seg.length == 2) {
                serCatTagWeight.put(seg[0].toLowerCase(),
                        Double.parseDouble(seg[1]));
                // 类目分词
                Result words = ssme.parserQuery(seg[0].toLowerCase(), "2");
                for (Term t : words) {
                    if (cateSegWordWeight.containsKey(t.getName())) {
                        cateSegWordWeight.put(t.getName(), 0.5
                                * cateSegWordWeight.get(t.getName()) + 0.5
                                * serCatTagWeight.get(seg[0].toLowerCase()));
                    } else {
                        cateSegWordWeight.put(t.getName(),
                                serCatTagWeight.get(seg[0].toLowerCase()));
                    }
                }
            }
        }
        sr.close();
        if (cateSegWordWeight.containsKey("设计")) {
            cateSegWordWeight.put("设计", 1.0);
        }
        // 地域标签库加载-<area,<attr,<tag>>>
        String cline = null;
        while ((cline = cr.readLine()) != null) {
            String[] seg = cline.split("\t");
            if (seg.length >= 2) {
                String area = seg[1];
                Map<String, Set<String>> attrTag = areaAttrTag.get(area);
                if (attrTag == null) {
                    attrTag = new HashMap<String, Set<String>>();
                    areaAttrTag.put(area, attrTag);
                }
                if (seg.length > 2) {
                    String attr = seg[2];
                    Set<String> tag = attrTag.get(attr);
                    if (tag == null) {
                        tag = new HashSet<String>();
                        attrTag.put(attr, tag);
                    }
                    for (int i = 3; i < seg.length; i++) {
                        tag.add(seg[i]);
                    }
                }
            }
        }
        cr.close();
        // 构建地域索引词典
        for (Map.Entry<String, Map<String, Set<String>>> q : areaAttrTag.entrySet()) {
            String areaOne = q.getKey().split("&")[0];
            String areaTwo = q.getKey().split("&")[1];
            String areaThree = q.getKey().split("&")[2];
            // province
            String provinceName = areaOne.split("/")[0];
            if (!wordArea.containsKey(provinceName)) {
                wordArea.put(provinceName, areaOne);
            } else if (!wordArea.get(provinceName).contains(areaOne)) {
                wordArea.put(provinceName, wordArea.get(provinceName) + "#" + areaOne);
            }
            String[] provinceSeg = areaOne.split("/");
            String provinceFullName = provinceSeg[0] + provinceSeg[1].replaceAll("直辖", "");
            if (!wordArea.containsKey(provinceFullName)) {
                wordArea.put(provinceFullName, areaOne);
            } else if (!wordArea.get(provinceFullName).contains(areaOne)) {
                wordArea.put(provinceFullName, wordArea.get(provinceFullName) + "#" + areaOne);
            }

            String cityName = "";
            String cityFullName = "";
            String districtName = "";
            String districtFullName = "";
            String areaOneTwo = "";
            String areaRealOne = areaOne.split("/")[0] + "/" + areaOne.split("/")[1];
            if (areaRealOne.equals(areaTwo)) {
                areaOneTwo = areaOne;
            } else {
                areaOneTwo = areaOne + "&" + areaTwo;
            }
            if (q.getValue().size() != 0) {
                if (q.getValue().containsKey("二级地名同名")) {
                    // city
                    cityName = areaTwo.split("/")[0];
                    if (!wordArea.containsKey(cityName)) {
                        wordArea.put(cityName, areaOneTwo);
                    } else if (!wordArea.get(cityName).contains(areaOneTwo)) {
                        wordArea.put(cityName, wordArea.get(cityName) + "#" + areaOneTwo);
                    }
                    cityFullName = areaTwo.replaceAll("/|直辖", "");
                    if (!wordArea.containsKey(cityFullName)) {
                        wordArea.put(cityFullName, areaOneTwo);
                    } else if (!wordArea.get(cityFullName).contains(areaOneTwo)) {
                        wordArea.put(cityFullName, wordArea.get(cityFullName) + "#" + areaOneTwo);
                    }
                    for (String tag : q.getValue().get("二级地名同名")) {
                        if (!wordArea.containsKey(tag)) {
                            wordArea.put(tag, areaOneTwo);
                        } else if (!wordArea.get(tag).contains(areaOneTwo)) {
                            wordArea.put(tag, wordArea.get(tag) + "#" + areaOneTwo);
                        }
                    }
                    // district
                    if (q.getValue().containsKey("三级地名同名")) {
                        districtName = areaThree.split("/")[0];
                        if (!wordArea.containsKey(districtName)) {
                            wordArea.put(districtName, q.getKey());
                        } else if (!wordArea.get(districtName).contains(q.getKey())) {
                            wordArea.put(districtName, wordArea.get(districtName) + "#" + q.getKey());
                        }
                        districtFullName = areaThree.replace("/", "");
                        if (!wordArea.containsKey(districtFullName)) {
                            wordArea.put(districtFullName, q.getKey());
                        } else if (!wordArea.get(districtFullName).contains(q.getKey())) {
                            wordArea.put(districtFullName, wordArea.get(districtFullName) + "#" + q.getKey());
                        }
                        for (String tag : q.getValue().get("三级地名同名")) {
                            if (!wordArea.containsKey(tag)) {
                                wordArea.put(tag, q.getKey());
                            } else if (!wordArea.get(tag).contains(q.getKey())) {
                                wordArea.put(tag, wordArea.get(tag) + "#" + q.getKey());
                            }
                        }
                    } else if (q.getValue().containsKey("三级地名全名查询")) {
                        for (String tag : q.getValue().get("三级地名全名查询")) {
                            if (!wordArea.containsKey(tag)) {
                                wordArea.put(tag, q.getKey());
                            } else if (!wordArea.get(tag).contains(q.getKey())) {
                                wordArea.put(tag, wordArea.get(tag) + "#" + q.getKey());
                            }
                        }
                    } else {
                        districtName = areaThree.split("/")[0];
                        if (!wordArea.containsKey(districtName)) {
                            wordArea.put(districtName, q.getKey());
                        } else if (!wordArea.get(districtName).contains(q.getKey())) {
                            wordArea.put(districtName, wordArea.get(districtName) + "#" + q.getKey());
                        }
                        districtFullName = areaThree.replace("/", "");
                        if (!wordArea.containsKey(districtFullName)) {
                            wordArea.put(districtFullName, q.getKey());
                        } else if (!wordArea.get(districtFullName).contains(q.getKey())) {
                            wordArea.put(districtFullName, wordArea.get(districtFullName) + "#" + q.getKey());
                        }
                    }
                } else if (q.getValue().containsKey("二级地名全名查询")) {
                    // city
                    for (String tag : q.getValue().get("二级地名全名查询")) {
                        if (!wordArea.containsKey(tag)) {
                            wordArea.put(tag, areaOneTwo);
                        } else if (!wordArea.get(tag).contains(areaOneTwo)) {
                            wordArea.put(tag, wordArea.get(tag) + "#" + areaOneTwo);
                        }
                    }
                    // district
                    if (q.getValue().containsKey("三级地名同名")) {
                        districtName = areaThree.split("/")[0];
                        if (!wordArea.containsKey(districtName)) {
                            wordArea.put(districtName, q.getKey());
                        } else if (!wordArea.get(districtName).contains(q.getKey())) {
                            wordArea.put(districtName, wordArea.get(districtName) + "#" + q.getKey());
                        }
                        districtFullName = areaThree.replace("/", "");
                        if (!wordArea.containsKey(districtFullName)) {
                            wordArea.put(districtFullName, q.getKey());
                        } else if (!wordArea.get(districtFullName).contains(q.getKey())) {
                            wordArea.put(districtFullName, wordArea.get(districtFullName) + "#" + q.getKey());
                        }
                        for (String tag : q.getValue().get("三级地名同名")) {
                            if (!wordArea.containsKey(tag)) {
                                wordArea.put(tag, q.getKey());
                            } else if (!wordArea.get(tag).contains(q.getKey())) {
                                wordArea.put(tag, wordArea.get(tag) + "#" + q.getKey());
                            }
                        }
                    } else if (q.getValue().containsKey("三级地名全名查询")) {
                        for (String tag : q.getValue().get("三级地名全名查询")) {
                            if (!wordArea.containsKey(tag)) {
                                wordArea.put(tag, q.getKey());
                            } else if (!wordArea.get(tag).contains(q.getKey())) {
                                wordArea.put(tag, wordArea.get(tag) + "#" + q.getKey());
                            }
                        }
                    } else {
                        districtName = areaThree.split("/")[0];
                        if (!wordArea.containsKey(districtName)) {
                            wordArea.put(districtName, q.getKey());
                        } else if (!wordArea.get(districtName).contains(q.getKey())) {
                            wordArea.put(districtName, wordArea.get(districtName) + "#" + q.getKey());
                        }
                        districtFullName = areaThree.replace("/", "");
                        if (!wordArea.containsKey(districtFullName)) {
                            wordArea.put(districtFullName, q.getKey());
                        } else if (!wordArea.get(districtFullName).contains(q.getKey())) {
                            wordArea.put(districtFullName, wordArea.get(districtFullName) + "#" + q.getKey());
                        }
                    }
                } else {
                    // city
                    cityName = areaTwo.split("/")[0];
                    if (!wordArea.containsKey(cityName)) {
                        wordArea.put(cityName, areaOneTwo);
                    } else if (!wordArea.get(cityName).contains(areaOneTwo)) {
                        wordArea.put(cityName, wordArea.get(cityName) + "#" + areaOneTwo);
                    }
                    cityFullName = areaTwo.replaceAll("/|直辖", "");
                    if (!wordArea.containsKey(cityFullName)) {
                        wordArea.put(cityFullName, areaOneTwo);
                    } else if (!wordArea.get(cityFullName).contains(areaOneTwo)) {
                        wordArea.put(cityFullName, wordArea.get(cityFullName) + "#" + areaOneTwo);
                    }
                    // district
                    if (q.getValue().containsKey("三级地名同名")) {
                        districtName = areaThree.split("/")[0];
                        if (!wordArea.containsKey(districtName)) {
                            wordArea.put(districtName, q.getKey());
                        } else if (!wordArea.get(districtName).contains(q.getKey())) {
                            wordArea.put(districtName, wordArea.get(districtName) + "#" + q.getKey());
                        }
                        districtFullName = areaThree.replace("/", "");
                        if (!wordArea.containsKey(districtFullName)) {
                            wordArea.put(districtFullName, q.getKey());
                        } else if (!wordArea.get(districtFullName).contains(q.getKey())) {
                            wordArea.put(districtFullName, wordArea.get(districtFullName) + "#" + q.getKey());
                        }
                        for (String tag : q.getValue().get("三级地名同名")) {
                            if (!wordArea.containsKey(tag)) {
                                wordArea.put(tag, q.getKey());
                            } else if (!wordArea.get(tag).contains(q.getKey())) {
                                wordArea.put(tag, wordArea.get(tag) + "#" + q.getKey());
                            }
                        }
                    } else if (q.getValue().containsKey("三级地名全名查询")) {
                        for (String tag : q.getValue().get("三级地名全名查询")) {
                            if (!wordArea.containsKey(tag)) {
                                wordArea.put(tag, q.getKey());
                            } else if (!wordArea.get(tag).contains(q.getKey())) {
                                wordArea.put(tag, wordArea.get(tag) + "#" + q.getKey());
                            }
                        }
                    }
                }
            } else {
                // city
                cityName = areaTwo.split("/")[0];
                if (!wordArea.containsKey(cityName)) {
                    wordArea.put(cityName, areaOneTwo);
                } else if (!wordArea.get(cityName).contains(areaOneTwo)) {
                    wordArea.put(cityName, wordArea.get(cityName) + "#" + areaOneTwo);
                }
                cityFullName = areaTwo.replaceAll("/|直辖", "");
                if (!wordArea.containsKey(cityFullName)) {
                    wordArea.put(cityFullName, areaOneTwo);
                } else if (!wordArea.get(cityFullName).contains(areaOneTwo)) {
                    wordArea.put(cityFullName, wordArea.get(cityFullName) + "#" + areaOneTwo);
                }
                // district
                districtName = areaThree.split("/")[0];
                if (!wordArea.containsKey(districtName)) {
                    wordArea.put(districtName, q.getKey());
                } else if (!wordArea.get(districtName).contains(q.getKey())) {
                    wordArea.put(districtName, wordArea.get(districtName) + "#" + q.getKey());
                }
                districtFullName = areaThree.replace("/", "");
                if (!wordArea.containsKey(districtFullName)) {
                    wordArea.put(districtFullName, q.getKey());
                } else if (!wordArea.get(districtFullName).contains(q.getKey())) {
                    wordArea.put(districtFullName, wordArea.get(districtFullName) + "#" + q.getKey());
                }
            }
        }
        // 初始化地域词典
        List<Value> areaWords = new ArrayList<Value>();
        for (Map.Entry<String, String> q : wordArea.entrySet()) {
            Value value = new Value(q.getKey(), q.getValue(), "2000");
            areaWords.add(value);
        }
        LoadDic.insertZbjDic1(areadic, areaWords);
        // 城市缩写词加载
        String aline = null;
        while ((aline = ar.readLine()) != null) {
            String[] seg = aline.split("\t");
            if (seg.length == 2) {
                Set<String> area = new LinkedHashSet<String>();
                String[] place = seg[1].split(",");
                for (int i = 0; i < place.length; i++) {
                    area.add(place[i]);
                }
                areaAbbrCity.put(seg[0], area);
            }
        }
        ar.close();
    }

    /**
     * universal position infomation extraction
     */
    public List<OutputPosiInfo> uniPosiExtra(String querys) {
        if (querys == null || querys.equals("") || querys.equals("null")) {
            return null;
        }
        querys = querys.toLowerCase();
        // 分句
        List<String> sent = LongSentenceSegment.longSentSpiltPunctuation(querys);
        List<AreaFeature> posiList = new ArrayList<AreaFeature>(); // 输出地名
        // 标准城市描述(非省略缩写)
        for (int i = 0; i < sent.size(); i++) {
            if (sent.get(i).length() >= 2) {
                String query = sent.get(i);
                int segNum = i;
                // 分词获取地名
                Map<String, Set<String>> areaWord = new LinkedHashMap<String, Set<String>>(); // 有效地名
                Result areaParse = DicAnalysis.parse(query, areadic); // 单个词分词用户自定义词典不生效:成都、重庆
                for (Term t : areaParse) {
                    if (t.getNatureStr().matches("^[^a-z]+$")) {
                        String[] key = t.getNatureStr().split("#");
                        for (int k = 0; k < key.length; k++) {
                            if (!areaWord.containsKey(key[k])) {
                                Set<String> set = new HashSet<String>(); // 同一分句相同地域过滤
                                set.add(t.getName());
                                areaWord.put(key[k], set);
                            } else {
                                Set<String> tmpSet = areaWord.get(key[k]);
                                tmpSet.add(t.getName());
                                areaWord.put(key[k], tmpSet);
                            }
                        }
                    } else if (t.getNatureStr().equals("ns")
                            && wordArea.containsKey(t.getName())) {
                        String word = t.getName();
                        String[] key = wordArea.get(word).split("#");
                        for (int k = 0; k < key.length; k++) {
                            if (!areaWord.containsKey(key[k])) {
                                Set<String> set = new HashSet<String>(); // 同一分句相同地域过滤
                                set.add(t.getName());
                                areaWord.put(key[k], set);
                            } else {
                                Set<String> tmpSet = areaWord.get(key[k]);
                                tmpSet.add(t.getName());
                                areaWord.put(key[k], tmpSet);
                            }
                        }
                    }
                }
                // 分词判断词是否为地理名词
                Result parse = ssme.parserQuery(query, "3");
                Map<String, String> wordparse = new LinkedHashMap<String, String>();
                int num = 0;
                for (Term t : parse) {
                    num++;
                    if (t.getName().length() <= 3) {
                        wordparse.put(num + "#" + t.getNatureStr(), t.getName());
                    }
                }
                Set<AreaFeature> tmpPosiSet = new HashSet<AreaFeature>(); // 输出地名
                // 四川/省/川&成都/市&金堂/县-位置分析
                for (Map.Entry<String, Set<String>> q : areaWord.entrySet()) {
                    String fullArea = q.getKey();
                    int areaLen = fullArea.split("&").length;
                    Set<String> location = q.getValue();
                    int locLen = location.size();
                    String maxLoc = "";
                    if (locLen <= 2) {
                        for (String l : location) {
                            if (l.length() >= maxLoc.length()) {
                                maxLoc = l;
                            }
                        }
                    }
                    // 位置-特征-权重计算
                    AreaFeature areafeature = new AreaFeature();
                    Boolean posiFlag = false;
                    if (areaLen == 1) {
                        // 四川/省/川
                        areafeature.setLevel(1);
                        areafeature.setSentNumber(segNum);
                        String[] proSeg = fullArea.split("/");
                        if (proSeg.length == 3) {
                            String province = proSeg[0] + "/" + proSeg[1];
                            String abbr = proSeg[2];
                            areafeature.setProvince(province);
                            areafeature.setProvinceAbbr(abbr);
                            int index1 = query.indexOf(maxLoc);
                            areafeature.setStrPosiNumber(index1);
                            areafeature.setFlagNumber(1);
                            areafeature.setLevelNameLen(maxLoc.length());
                            if (locLen == 2) {
                                areafeature.setScore(7.0);
                                posiFlag = true;
                                // 分词过滤不合理省份
                            } else if (wordparse.containsValue(maxLoc) || maxLoc.length() >= 4) {
                                areafeature.setScore(7.0);
                                posiFlag = true;
                            }
                        }
                    } else if (areaLen == 2) {
                        // 四川/省/川 + 成都/市
                        areafeature.setLevel(2);
                        areafeature.setSentNumber(segNum);
                        String[] areaSeg = fullArea.split("&");
                        if (areaSeg[0].split("/").length == 3
                                && areaSeg[1].split("/").length == 2) {
                            String province = areaSeg[0].split("/")[0] + "/" + areaSeg[0].split("/")[1];
                            String abbr = areaSeg[0].split("/")[2];
                            String city = areaSeg[1];
                            areafeature.setProvince(province);
                            areafeature.setProvinceAbbr(abbr);
                            areafeature.setCity(city);
                            int index2 = query.indexOf(maxLoc);
                            areafeature.setStrPosiNumber(index2);
                            areafeature.setFlagNumber(1);
                            areafeature.setLevelNameLen(maxLoc.length());
                            if (locLen == 2
                                    || maxLoc.length() >= 4
                                    || !fullArea.contains(maxLoc)) {
                                areafeature.setScore(7.0);
                                posiFlag = true;
                            } else if (wordparse.containsValue(maxLoc)) {
                                // 分词过滤不合理城市
                                for (Map.Entry<String, String> w : wordparse.entrySet()) {
                                    if (w.getValue().equals(maxLoc)) {
                                        String wordstr = w.getKey().split("#")[1];
                                        if (wordstr.equals("ns")) {
                                            areafeature.setScore(7.0);
                                        } else {
                                            areafeature.setScore(5.0);
                                        }
                                        posiFlag = true;
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (areaLen == 3) {
                        // 四川/省/川 + 成都/市 + 金堂/县
                        areafeature.setLevel(3);
                        areafeature.setSentNumber(segNum);
                        String[] areaSeg = fullArea.split("&");
                        if (areaSeg[0].split("/").length == 3
                                && areaSeg[1].split("/").length == 2
                                && areaSeg[2].split("/").length == 2) {
                            String province = areaSeg[0].split("/")[0] + "/" + areaSeg[0].split("/")[1];
                            String abbr = areaSeg[0].split("/")[2];
                            String city = areaSeg[1];
                            String district = areaSeg[2];
                            areafeature.setProvince(province);
                            areafeature.setProvinceAbbr(abbr);
                            areafeature.setCity(city);
                            areafeature.setDistrict(district);
                            int index3 = query.indexOf(maxLoc);
                            areafeature.setStrPosiNumber(index3);
                            areafeature.setFlagNumber(1);
                            areafeature.setLevelNameLen(maxLoc.length());
                            if (locLen == 2
                                    || maxLoc.length() >= 4
                                    || !fullArea.contains(maxLoc)) {
                                areafeature.setScore(7.0);
                                posiFlag = true;
                            } else if (wordparse.containsValue(maxLoc)) {
                                // 分词过滤不合理城市
                                for (Map.Entry<String, String> w : wordparse.entrySet()) {
                                    if (w.getValue().equals(maxLoc)) {
                                        String wordstr = w.getKey().split("#")[1];
                                        if (wordstr.equals("ns")) {
                                            areafeature.setScore(7.0);
                                        } else {
                                            areafeature.setScore(5.0);
                                        }
                                        posiFlag = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    // 句内分析
                    Double score = areafeature.getScore();
                    if (query.contains("公司") || query.contains("集团")
                            || query.contains("企业") || query.contains("机构")) {
                        score -= 100;
                    }
                    if (query.contains("服务商")
                            || query.contains("优先")
                            || query.contains("最好")
                            || (query.contains("限") & !query.contains("有限"))
                            || (query.contains("地") & !query.contains("地产") & !query
                            .contains("落地"))) {
                        score += 500;
                    }
                    if (query.contains("除了")) {
                        score -= 1000;
                    }
                    // 地点顺序分数
                    score += 5 * Math.exp(-0.02 * segNum);
                    areafeature.setScore(score);
                    // 地理位置装载
                    if (posiFlag) {
                        tmpPosiSet.add(areafeature);
                    }
                }
                // 同一句子中出现包含地名,去除长度短的地名: 重庆/市#渝&重庆/市&江北/区	重庆/市#渝
                List<AreaFeature> finalList = rvScore(tmpPosiSet);
                for (AreaFeature a : finalList) {
                    posiList.add(a);
                }
            }
        }
        // posiList过滤
        Map<String, MyEntry<String, Double>> tmpposMap = posiFilter(posiList);
        Map<String, Double> posMap = new HashMap<String, Double>();
        for (Map.Entry<String, MyEntry<String, Double>> v : tmpposMap
                .entrySet()) {
            posMap.put(v.getKey(), v.getValue().getValue());
        }
        // 城市省略缩写描述(江浙沪)
        for (int i = 0; i < sent.size() & i < 100; i++) {
            for (Entry<String, Set<String>> a : areaAbbrCity.entrySet()) {
                if (sent.get(i).contains(a.getKey())) {
                    double abbScore = 0.0;
                    abbScore += 5;
                    // 句内分析
                    if (sent.get(i).contains("公司")
                            || sent.get(i).contains("集团")
                            || sent.get(i).contains("企业")
                            || sent.get(i).contains("机构")) {
                        abbScore -= 100;
                    }
                    if (sent.get(i).contains("服务商")
                            || sent.get(i).contains("优先")
                            || sent.get(i).contains("最好")
                            || (sent.get(i).contains("限") & !sent.get(i)
                            .contains("有限"))
                            || (sent.get(i).contains("地")
                            & !sent.get(i).contains("地产") & !sent
                            .get(i).contains("落地"))) {
                        abbScore += 500;
                    }
                    if (sent.get(i).contains("除了")) {
                        abbScore -= 1000;
                    }

                    // 地点顺序分数
                    abbScore += 4 * Math.exp(-0.02 * i);

                    for (String aa : a.getValue()) {
                        if (posMap.containsKey(aa)) {
                            posMap.put(aa, abbScore + posMap.get(aa));
                        } else {
                            posMap.put(aa, abbScore);
                        }
                    }
                }
            }
        }
        // Rank and out
        List<Entry<String, Double>> posRankMap = new LinkedList<Entry<String, Double>>();
        posRankMap = Sort.sortMap(posMap);
        List<OutputPosiInfo> posiList1 = new ArrayList<OutputPosiInfo>();
        for (Map.Entry<String, Double> p : posRankMap) {
            OutputPosiInfo pi = new OutputPosiInfo();
            pi.setPositionName(p.getKey());
            pi.setScore(p.getValue());
            posiList1.add(pi);
        }

        return posiList1;
    }

    /**
     * localize position recongition in task corpus
     */
    public List<OutputPosiInfo> taskPosiRecog(String querys, String category, String origPos) {
        if (querys == null || querys.equals("") || querys.equals("null")) {
            return null;
        }
        querys = querys.toLowerCase();
        category = category.toLowerCase();
        // 类目权重计算
        String[] cate = category.split("#");
        double weight = 1.0;
        if (cate.length == 3) {
            for (Entry<String, Double> w : serCatTagWeight.entrySet()) {
                if (w.getKey().equals(cate[0])) {
                    weight *= w.getValue();
                }
                if (w.getKey().equals(cate[1])) {
                    weight *= w.getValue();
                }
                if (w.getKey().equals(cate[2])) {
                    weight *= w.getValue();
                }
            }
        }
        // 分句
        List<String> sent = LongSentenceSegment.longSentSpiltPunctuation(querys);
        // 类目对seg[0]+seg[1]+seg[2]的影响
        double cateScore = 0.0;
        for (int s = 0; s < sent.size() & s < 3; s++) {
            Result sentWords = ssme.parserQueryUser(sent.get(s), "2", serdic);
            for (Term w : sentWords) {
                if (cateSegWordWeight.containsKey(w.getName())) {
                    if (cateSegWordWeight.get(w.getName()) > 1) {
                        cateScore += cateSegWordWeight.get(w.getName()) * 10;
                    } else if (cateSegWordWeight.get(w.getName()) == 1) {
                        cateScore += 1;
                    } else if (cateSegWordWeight.get(w.getName()) < 1) {
                        cateScore -= (1 - cateSegWordWeight.get(w.getName())) * 50;
                    }
                }
            }
        }
        if (cateScore == 0.0) {
            cateScore = weight - 1.01;
        }
        List<AreaFeature> posiList = new ArrayList<AreaFeature>(); // 输出地名
        // 标准城市描述(非省略缩写)
        for (int i = 0; i < sent.size(); i++) {
            if (sent.get(i).length() >= 2) {
                String query = sent.get(i);
                int segNum = i;
                Set<AreaFeature> tmpPosiSet = posiExtra(query, segNum, weight, cateScore);
                // 同一句子中出现包含地名,去除长度短的地名: 重庆/市#渝&重庆/市&江北/区	重庆/市#渝
                List<AreaFeature> finalList = rvScore(tmpPosiSet);
                for (AreaFeature a : finalList) {
                    posiList.add(a);
                }
            }
        }
        // posiList过滤
        Map<String, MyEntry<String, Double>> tmpposMap = posiFilter(posiList);
        Map<String, Double> posMap = new HashMap<String, Double>();
        for (Map.Entry<String, MyEntry<String, Double>> v : tmpposMap
                .entrySet()) {
            posMap.put(v.getKey(), v.getValue().getValue());
        }
        // 城市省略缩写描述(江浙沪)
        for (int i = 0; i < sent.size() & i < 100; i++) {
            for (Entry<String, Set<String>> a : areaAbbrCity.entrySet()) {
                if (sent.get(i).contains(a.getKey())) {
                    double abbScore = 0.0;
                    abbScore += 5;
                    // 句内分析
                    if (sent.get(i).contains("公司")
                            || sent.get(i).contains("集团")
                            || sent.get(i).contains("企业")
                            || sent.get(i).contains("机构")) {
                        abbScore -= 100;
                    }
                    if (sent.get(i).contains("服务商")
                            || sent.get(i).contains("优先")
                            || sent.get(i).contains("最好")
                            || (sent.get(i).contains("限") & !sent.get(i)
                            .contains("有限"))
                            || (sent.get(i).contains("地")
                            & !sent.get(i).contains("地产") & !sent
                            .get(i).contains("落地"))) {
                        abbScore += 500;
                    }
                    if (sent.get(i).contains("除了")) {
                        abbScore -= 1000;
                    }

                    // 地点顺序分数
                    abbScore += 4 * Math.exp(-0.02 * i);

                    // 类目权重过滤
                    abbScore *= weight;
                    abbScore += cateScore;

                    for (String aa : a.getValue()) {
                        if (posMap.containsKey(aa)) {
                            posMap.put(aa, abbScore + posMap.get(aa));
                        } else {
                            posMap.put(aa, abbScore);
                        }
                    }
                }
            }
        }
        // original position from locating
        String[] origPosSeg = origPos.split("&");
        int posLength = origPosSeg.length;
        if (posLength == 1) { // 四川/省
            for (String q : areaAttrTag.keySet()) {
                String[] area = q.split("&");
                String province = area[0].split("/")[0] + "/" + area[0].split("/")[1];
                if (province.contains(origPosSeg[0])) {
                    if (!posMap.containsKey(province)) {
                        posMap.put(province, cateScore);
                    }
                    break;
                }
            }
        } else if (posLength == 2) { // 成都/市
            if (origPosSeg[0].equals(origPosSeg[1])) {
                for (String q : areaAttrTag.keySet()) {
                    String[] area = q.split("&");
                    String province = area[0].split("/")[0] + "/" + area[0].split("/")[1];
                    if (province.contains(origPosSeg[0])) {
                        if (!posMap.containsKey(province)) {
                            posMap.put(province, cateScore);
                        }
                        break;
                    }
                }
            } else {
                for (String q : areaAttrTag.keySet()) {
                    String[] area = q.split("&");
                    if (area.length == 3) {
                        String province = area[0].split("/")[0] + "/" + area[0].split("/")[1];
                        String city = area[1];
                        String district = area[2];
                        String key = province + "&" + city + "&" + district;
                        if (province.contains(origPosSeg[0])
                                && (city.contains(origPosSeg[1])
                                || city.replace("/", "").contains(origPosSeg[1]))) {
                            if (!posMap.containsKey(province + "&" + city)) {
                                posMap.put(province + "&" + city, cateScore);
                            }
                            break;
                        } else if (province.contains(origPosSeg[0])
                                && (city.contains(origPosSeg[1])
                                || district.replace("/", "").contains(origPosSeg[1]))) {
                            if (!posMap.containsKey(key)) {
                                posMap.put(key, cateScore);
                            }
                            break;
                        }
                    }
                }
            }
        } else if (posLength == 3) { // 金堂/县
            Boolean flag = false;
            for (String q : areaAttrTag.keySet()) {
                String[] area = q.split("&");
                if (area.length == 3) {
                    String province = area[0].split("/")[0] + "/" + area[0].split("/")[1];
                    String city = area[1];
                    String district = area[2];
                    String key = province + "&" + city + "&" + district;
                    if (province.contains(origPosSeg[0])
                            && (city.contains(origPosSeg[1]) || city
                            .replace("/", "").contains(origPosSeg[1]))
                            && (district.contains(origPosSeg[2])
                            || district.replace("/", "").contains(origPosSeg[2]))) {
                        if (!posMap.containsKey(key)) {
                            posMap.put(key, cateScore);
                            flag = true;
                        }
                        break;
                    }
                }
            }
            if (!flag) {
                for (String q : areaAttrTag.keySet()) {
                    String[] area = q.split("&");
                    if (area.length == 3) {
                        String province = area[0].split("/")[0] + "/" + area[0].split("/")[1];
                        String city = area[1];
                        String key = province + "&" + city;
                        if (province.contains(origPosSeg[0])
                                && (city.contains(origPosSeg[1])
                                || city.replace("/", "").contains(origPosSeg[1]))) {
                            if (!posMap.containsKey(key)) {
                                posMap.put(key, cateScore);
                            }
                            break;
                        }
                    }
                }
            }
        }
        // Rank and out
        List<Entry<String, Double>> posRankMap = new LinkedList<Entry<String, Double>>();
        posRankMap = Sort.sortMap(posMap);
        List<OutputPosiInfo> posiList1 = new ArrayList<OutputPosiInfo>();
        for (Map.Entry<String, Double> p : posRankMap) {
            OutputPosiInfo pi = new OutputPosiInfo();
            pi.setPositionName(p.getKey());
            pi.setScore(p.getValue());
            posiList1.add(pi);
        }

        return posiList1;
    }

    /**
     * find standard position in sentence and compute position score
     *
     * @param query
     * @param segNum
     * @param weight
     * @param cateScore
     * @return
     */
    private Set<AreaFeature> posiExtra(String query, int segNum, Double weight, Double cateScore) {

        // 分词获取地名
        Map<String, Set<String>> areaWord = new LinkedHashMap<String, Set<String>>(); // 有效地名
        Result areaParse = DicAnalysis.parse(query, areadic); // 单个词分词用户自定义词典不生效:成都、重庆
        for (Term t : areaParse) {
            if (t.getNatureStr().matches("^[^a-z]+$")) {
                String[] key = t.getNatureStr().split("#");
                for (int k = 0; k < key.length; k++) {
                    if (!areaWord.containsKey(key[k])) {
                        Set<String> set = new HashSet<String>(); // 同一分句相同地域过滤
                        set.add(t.getName());
                        areaWord.put(key[k], set);
                    } else {
                        Set<String> tmpSet = areaWord.get(key[k]);
                        tmpSet.add(t.getName());
                        areaWord.put(key[k], tmpSet);
                    }
                }
            } else if (t.getNatureStr().equals("ns")
                    && wordArea.containsKey(t.getName())) {
                String word = t.getName();
                String[] key = wordArea.get(word).split("#");
                for (int k = 0; k < key.length; k++) {
                    if (!areaWord.containsKey(key[k])) {
                        Set<String> set = new HashSet<String>(); // 同一分句相同地域过滤
                        set.add(t.getName());
                        areaWord.put(key[k], set);
                    } else {
                        Set<String> tmpSet = areaWord.get(key[k]);
                        tmpSet.add(t.getName());
                        areaWord.put(key[k], tmpSet);
                    }
                }
            }
        }
        // 分词判断词是否为地理名词
        Result parse = ssme.parserQuery(query, "3");
        Map<String, String> wordparse = new LinkedHashMap<String, String>();
        int num = 0;
        for (Term t : parse) {
            num++;
            if (t.getName().length() <= 3) {
                wordparse.put(num + "#" + t.getNatureStr(), t.getName());
            }
        }
        Set<AreaFeature> tmpPosiSet = new HashSet<AreaFeature>(); // 输出地名
        // 四川/省/川&成都/市&金堂/县-位置分析
        for (Map.Entry<String, Set<String>> q : areaWord.entrySet()) {
            String fullArea = q.getKey();
            int areaLen = fullArea.split("&").length;
            Set<String> location = q.getValue();
            int locLen = location.size();
            String maxLoc = "";
            if (locLen <= 2) {
                for (String l : location) {
                    if (l.length() >= maxLoc.length()) {
                        maxLoc = l;
                    }
                }
            }
            // 位置-特征-权重计算
            AreaFeature areafeature = new AreaFeature();
            Boolean posiFlag = false;
            if (areaLen == 1) {
                // 四川/省/川
                areafeature.setLevel(1);
                areafeature.setSentNumber(segNum);
                String[] proSeg = fullArea.split("/");
                if (proSeg.length == 3) {
                    String province = proSeg[0] + "/" + proSeg[1];
                    String abbr = proSeg[2];
                    areafeature.setProvince(province);
                    areafeature.setProvinceAbbr(abbr);
                    int index1 = query.indexOf(maxLoc);
                    areafeature.setStrPosiNumber(index1);
                    areafeature.setFlagNumber(1);
                    areafeature.setLevelNameLen(maxLoc.length());
                    if (locLen == 2) {
                        areafeature.setScore(7.0);
                        posiFlag = true;
                        // 分词过滤不合理省份
                    } else if (wordparse.containsValue(maxLoc) || maxLoc.length() >= 4) {
                        areafeature.setScore(7.0);
                        posiFlag = true;
                    }
                }
            } else if (areaLen == 2) {
                // 四川/省/川 + 成都/市
                areafeature.setLevel(2);
                areafeature.setSentNumber(segNum);
                String[] areaSeg = fullArea.split("&");
                if (areaSeg[0].split("/").length == 3
                        && areaSeg[1].split("/").length == 2) {
                    String province = areaSeg[0].split("/")[0] + "/" + areaSeg[0].split("/")[1];
                    String abbr = areaSeg[0].split("/")[2];
                    String city = areaSeg[1];
                    areafeature.setProvince(province);
                    areafeature.setProvinceAbbr(abbr);
                    areafeature.setCity(city);
                    int index2 = query.indexOf(maxLoc);
                    areafeature.setStrPosiNumber(index2);
                    areafeature.setFlagNumber(1);
                    areafeature.setLevelNameLen(maxLoc.length());
                    if (locLen == 2
                            || maxLoc.length() >= 4
                            || !fullArea.contains(maxLoc)) {
                        areafeature.setScore(7.0);
                        posiFlag = true;
                    } else if (wordparse.containsValue(maxLoc)) {
                        // 分词过滤不合理城市
                        for (Map.Entry<String, String> w : wordparse.entrySet()) {
                            if (w.getValue().equals(maxLoc)) {
                                String wordstr = w.getKey().split("#")[1];
                                if (wordstr.equals("ns")) {
                                    areafeature.setScore(7.0);
                                } else {
                                    areafeature.setScore(5.0);
                                }
                                posiFlag = true;
                                break;
                            }
                        }
                    }
                }
            } else if (areaLen == 3) {
                // 四川/省/川 + 成都/市 + 金堂/县
                areafeature.setLevel(3);
                areafeature.setSentNumber(segNum);
                String[] areaSeg = fullArea.split("&");
                if (areaSeg[0].split("/").length == 3
                        && areaSeg[1].split("/").length == 2
                        && areaSeg[2].split("/").length == 2) {
                    String province = areaSeg[0].split("/")[0] + "/" + areaSeg[0].split("/")[1];
                    String abbr = areaSeg[0].split("/")[2];
                    String city = areaSeg[1];
                    String district = areaSeg[2];
                    areafeature.setProvince(province);
                    areafeature.setProvinceAbbr(abbr);
                    areafeature.setCity(city);
                    areafeature.setDistrict(district);
                    int index3 = query.indexOf(maxLoc);
                    areafeature.setStrPosiNumber(index3);
                    areafeature.setFlagNumber(1);
                    areafeature.setLevelNameLen(maxLoc.length());
                    if (locLen == 2
                            || maxLoc.length() >= 4
                            || !fullArea.contains(maxLoc)) {
                        areafeature.setScore(7.0);
                        posiFlag = true;
                    } else if (wordparse.containsValue(maxLoc)) {
                        // 分词过滤不合理城市
                        for (Map.Entry<String, String> w : wordparse.entrySet()) {
                            if (w.getValue().equals(maxLoc)) {
                                String wordstr = w.getKey().split("#")[1];
                                if (wordstr.equals("ns")) {
                                    areafeature.setScore(7.0);
                                } else {
                                    areafeature.setScore(5.0);
                                }
                                posiFlag = true;
                                break;
                            }
                        }
                    }
                }
            }

            // 句内分析
            Double score = areafeature.getScore();
            if (query.contains("公司") || query.contains("集团")
                    || query.contains("企业") || query.contains("机构")) {
                score -= 100;
            }
            if (query.contains("服务商")
                    || query.contains("优先")
                    || query.contains("最好")
                    || (query.contains("限") & !query.contains("有限"))
                    || (query.contains("地") & !query.contains("地产") & !query
                    .contains("落地"))) {
                score += 500;
            }
            if (query.contains("除了")) {
                score -= 1000;
            }
            // 地点顺序分数
            score += 5 * Math.exp(-0.02 * segNum);
            // 类目权重过滤
            score *= weight;
            score += cateScore;
            areafeature.setScore(score);
            // 地理位置装载
            if (posiFlag) {
                tmpPosiSet.add(areafeature);
            }
        }

        return tmpPosiSet;
    }

    /**
     * 同一句子中出现包含地名,去除长度短的地名: 重庆/市#渝&重庆/市&江北/区	重庆/市#渝
     *
     * @param posiSet
     * @return
     */
    private List<AreaFeature> rvScore(Set<AreaFeature> posiSet) {
        Map<String, MyEntry<String, Double>> nearMap = new HashMap<String, MyEntry<String, Double>>();
        Map<String, AreaFeature> nearRealMap = new HashMap<String, AreaFeature>();
        for (AreaFeature v : posiSet) {
            String level = "";
            if (v.getLevel() == 1) {
                level = v.getProvince();
            } else if (v.getLevel() == 2) {
                level = v.getProvince() + "&" + v.getCity();
            } else if (v.getLevel() == 3) {
                level = v.getProvince() + "&" + v.getCity() + "&"
                        + v.getDistrict();
            }
            MyEntry<String, Double> me = new MyEntry<String, Double>(
                    v.getSentNumber() + "#" + v.getStrPosiNumber(),
                    v.getScore());
            nearMap.put(level, me);
            nearRealMap.put(level, v);
        }
        Set<String> tmp2d = new HashSet<String>();
        Set<String> tmp1d = new HashSet<String>();
        for (String v : nearMap.keySet()) {
            if (!v.contains("&")) {
                tmp1d.add(v);
            } else if (v.split("&").length == 2) {
                tmp2d.add(v);
            }
        }
        Set<String> filter = new HashSet<String>();
        for (Map.Entry<String, MyEntry<String, Double>> v : nearMap
                .entrySet()) {
            String key = v.getKey();
            int keysentnum = Integer
                    .parseInt(v.getValue().getKey().split("#")[0]);
            int keyposinum = Integer
                    .parseInt(v.getValue().getKey().split("#")[1]);
            if (key.split("&").length == 3) {
                for (String m : tmp1d) {
                    if (key.contains(m)) {
                        int msentnum = Integer.parseInt(nearMap.get(m)
                                .getKey().split("#")[0]);
                        int mposinum = Integer.parseInt(nearMap.get(m)
                                .getKey().split("#")[1]);
                        if (msentnum == keysentnum
                                && Math.abs(mposinum - keyposinum) < 9) {
                            filter.add(m);
                        }
                    }
                }
                for (String n : tmp2d) {
                    if (key.contains(n)) {
                        int nsentnum = Integer.parseInt(nearMap.get(n)
                                .getKey().split("#")[0]);
                        int nposinum = Integer.parseInt(nearMap.get(n)
                                .getKey().split("#")[1]);
                        if (nsentnum == keysentnum
                                && Math.abs(nposinum - keyposinum) < 9) {
                            filter.add(n);
                        }
                    }
                }
            }
            if (key.split("&").length == 2) {
                for (String t : tmp1d) {
                    if (key.contains(t)) {
                        int tsentnum = Integer.parseInt(nearMap.get(t)
                                .getKey().split("#")[0]);
                        int tposinum = Integer.parseInt(nearMap.get(t)
                                .getKey().split("#")[1]);
                        if (tsentnum == keysentnum
                                && Math.abs(tposinum - keyposinum) < 9) {
                            filter.add(t);
                        }
                    }
                }
            }
        }
        for (String v : filter) {
            nearRealMap.remove(v);
        }
        // 更新flagNum
        List<AreaFeature> finalList = new ArrayList<AreaFeature>();
        for (AreaFeature a : nearRealMap.values()) {
            int flagNum = a.getFlagNumber();
            for (String v : filter) {
                String[] vArea = v.split("&");
                if (vArea.length == 1
                        && a.getProvince().equals(vArea[0])) {
                    flagNum += 1;
                } else if (vArea.length == 2
                        && a.getProvince().equals(vArea[0])
                        && a.getCity().equals(vArea[1])) {
                    flagNum += 2;
                }
            }
            if (flagNum > 3) {
                flagNum = 3;
            }
            a.setFlagNumber(flagNum);
            finalList.add(a);
        }

        return finalList;
    }

    /**
     * standard position filtering
     *
     * @param posiList
     * @return
     */
    private Map<String, MyEntry<String, Double>> posiFilter(List<AreaFeature> posiList) {
        // 浙江/省#浙&宁波/市&江北/区		香港/特别行政区#港&新界&北/区
        Map<Integer, Set<Integer>> sentstr = new HashMap<Integer, Set<Integer>>();
        for (AreaFeature v : posiList) {
            Set<Integer> tmp = sentstr.get(v.getSentNumber());
            if (tmp == null) {
                tmp = new TreeSet<Integer>();
                sentstr.put(v.getSentNumber(), tmp);
            }
            tmp.add(v.getStrPosiNumber());
        }
        Set<AreaFeature> tmpFilter = new HashSet<AreaFeature>();
        for (Map.Entry<Integer, Set<Integer>> vv : sentstr.entrySet()) {
            int num = 0;
            int lastnum = -1;
            for (Integer vvv : vv.getValue()) {
                num++;
                if (num == 1) {
                    lastnum = vvv;
                    continue;
                }
                int difference = vvv - lastnum;
                if (difference == 1) {
                    for (AreaFeature v : posiList) {
                        if (vv.getKey() == v.getSentNumber()
                                && vvv == v.getStrPosiNumber()) {
                            tmpFilter.add(v);
                        }
                    }
                }
                lastnum = vvv;
            }
        }
        for (AreaFeature f : tmpFilter) {
            posiList.remove(f);
        }
        // 北京/市#京&北京/市&朝阳/区		吉林/省#吉&长春/市&朝阳/区
        Map<String, Integer> leftFilter = new HashMap<String, Integer>();
        for (AreaFeature v : posiList) {
            String key = v.getSentNumber() + "#" + v.getStrPosiNumber();
            int value = v.getFlagNumber();
            if (leftFilter.containsKey(key)) {
                if (value > leftFilter.get(key)) {
                    leftFilter.put(key, value);
                }
            } else {
                leftFilter.put(key, value);
            }
        }
        List<AreaFeature> posiList1 = new ArrayList<AreaFeature>();
        for (Map.Entry<String, Integer> vv : leftFilter.entrySet()) {
            int sentNum = Integer.parseInt(vv.getKey().split("#")[0]);
            int strPosiNum = Integer.parseInt(vv.getKey().split("#")[1]);
            int flagNum = vv.getValue();
            for (AreaFeature a : posiList) {
                if (a.getSentNumber() == sentNum
                        && a.getStrPosiNumber() == strPosiNum
                        && a.getFlagNumber() == flagNum) {
                    posiList1.add(a);
                }
            }
        }
        // 区分两个含同名的地名：青海/省#海南/自治州		海南/省
        Map<String, Set<AreaFeature>> tempMap = new HashMap<String, Set<AreaFeature>>();
        for (AreaFeature v : posiList1) {
            String key = v.getSentNumber() + "#" + v.getStrPosiNumber() + "#"
                    + v.getFlagNumber();
            Set<AreaFeature> tmpSet = tempMap.get(key);
            if (tmpSet == null) {
                tmpSet = new HashSet<AreaFeature>();
                tempMap.put(key, tmpSet);
            }
            tmpSet.add(v);
        }
        List<AreaFeature> tmpFilter1 = new ArrayList<AreaFeature>();
        for (Set<AreaFeature> q : tempMap.values()) {
            if (q.size() > 1) {
                int maxLen = 0;
                for (AreaFeature a : q) {
                    if (a.getLevelNameLen() > maxLen) {
                        maxLen = a.getLevelNameLen();
                    }
                }
                for (AreaFeature a : q) {
                    if (a.getLevelNameLen() < maxLen) {
                        tmpFilter1.add(a);
                    }
                }
            }
        }
        for (AreaFeature f : tmpFilter1) {
            posiList1.remove(f);
        }
        // 相同地名(不同句子):score分数求和
        Map<String, MyEntry<String, Double>> tmpposMap = new HashMap<String, MyEntry<String, Double>>();
        for (AreaFeature v : posiList1) {
            String areaNum = "";
            if (v.getLevel() == 1) {
                areaNum = v.getProvince();
            } else if (v.getLevel() == 2) {
                areaNum = v.getProvince() + "&" + v.getCity();
            } else if (v.getLevel() == 3) {
                areaNum = v.getProvince() + "&" + v.getCity() + "&"
                        + v.getDistrict();
            }
            MyEntry<String, Double> me = new MyEntry<String, Double>(
                    v.getSentNumber() + "#" + v.getStrPosiNumber(),
                    v.getScore());
            if (tmpposMap.containsKey(areaNum)) {
                double s = v.getScore() + tmpposMap.get(areaNum).getValue();
                MyEntry<String, Double> me1 = new MyEntry<String, Double>(
                        v.getSentNumber() + "#" + v.getStrPosiNumber(), s);
                tmpposMap.put(areaNum, me1);
                // if(v.getScore() > tmpposMap.get(areaNum).getValue()){
                // tmpposMap.put(areaNum, me);
                // }
            } else {
                tmpposMap.put(areaNum, me);
            }
        }
        // 过滤近邻位置包含关系：重庆/市#渝&重庆/市&江北/区	 重庆/市#渝
        Set<String> tmp2d = new HashSet<String>();
        Set<String> tmp1d = new HashSet<String>();
        for (String v : tmpposMap.keySet()) {
            if (!v.contains("&")) {
                tmp1d.add(v);
            } else if (v.split("&").length == 2) {
                tmp2d.add(v);
            }
        }
        Set<String> filter = new HashSet<String>();
        for (Map.Entry<String, MyEntry<String, Double>> v : tmpposMap.entrySet()) {
            String key = v.getKey();
            int keysentnum = Integer.parseInt(v.getValue().getKey().split("#")[0]);
            int keyposinum = Integer.parseInt(v.getValue().getKey().split("#")[1]);
            if (key.split("&").length == 3) {
                for (String m : tmp1d) {
                    if (key.contains(m)) {
                        int msentnum = Integer.parseInt(tmpposMap.get(m).getKey().split("#")[0]);
                        int mposinum = Integer.parseInt(tmpposMap.get(m).getKey().split("#")[1]);
                        if (msentnum == keysentnum && Math.abs(mposinum - keyposinum) < 9) {
                            filter.add(m);
                        }
                    }
                }
                for (String n : tmp2d) {
                    if (key.contains(n)) {
                        int nsentnum = Integer.parseInt(tmpposMap.get(n).getKey().split("#")[0]);
                        int nposinum = Integer.parseInt(tmpposMap.get(n).getKey().split("#")[1]);
                        if (nsentnum == keysentnum && Math.abs(nposinum - keyposinum) < 9) {
                            filter.add(n);
                        }
                    }
                }
            }
            if (key.split("&").length == 2) {
                for (String t : tmp1d) {
                    if (key.contains(t)) {
                        int tsentnum = Integer.parseInt(tmpposMap.get(t).getKey().split("#")[0]);
                        int tposinum = Integer.parseInt(tmpposMap.get(t).getKey().split("#")[1]);
                        if (tsentnum == keysentnum && Math.abs(tposinum - keyposinum) < 9) {
                            filter.add(t);
                        }
                    }
                }
            }
        }
        for (String v : filter) {
            tmpposMap.remove(v);
        }

        return tmpposMap;
    }

    /**
     * policy position infomation extract
     *
     * @param querys (text corpus)
     * @return
     */
    public List<OutputPosiInfo> policyRegExtra(String querys) {

        if (querys == null || querys.equals("") || querys.equals("null")) {
            return null;
        }
        querys = querys.toLowerCase();
        // 分句
        List<String> sent = LongSentenceSegment.longSentSpiltPunctuation(querys);
        List<AreaFeature> posiList = new ArrayList<AreaFeature>(); // 输出地名
        // 标准城市描述(非省略缩写)
        for (int i = 0; i < sent.size(); i++) {
            if (sent.get(i).length() >= 2) {
                String query = sent.get(i);
                int segNum = i;
                // 分词获取地名
                Map<String, Set<String>> areaWord = new LinkedHashMap<String, Set<String>>(); // 有效地名
                Result areaParse = DicAnalysis.parse(query, areadic); // 单个词分词用户自定义词典不生效:成都、重庆
                for (Term t : areaParse) {
                    if (t.getNatureStr().matches("^[^a-z]+$")) {
                        String[] key = t.getNatureStr().split("#");
                        for (int k = 0; k < key.length; k++) {
                            if (!areaWord.containsKey(key[k])) {
                                Set<String> set = new HashSet<String>(); // 同一分句相同地域过滤
                                set.add(t.getName());
                                areaWord.put(key[k], set);
                            } else {
                                Set<String> tmpSet = areaWord.get(key[k]);
                                tmpSet.add(t.getName());
                                areaWord.put(key[k], tmpSet);
                            }
                        }
                    } else if (t.getNatureStr().equals("ns")
                            && wordArea.containsKey(t.getName())) {
                        String word = t.getName();
                        String[] key = wordArea.get(word).split("#");
                        for (int k = 0; k < key.length; k++) {
                            if (!areaWord.containsKey(key[k])) {
                                Set<String> set = new HashSet<String>(); // 同一分句相同地域过滤
                                set.add(t.getName());
                                areaWord.put(key[k], set);
                            } else {
                                Set<String> tmpSet = areaWord.get(key[k]);
                                tmpSet.add(t.getName());
                                areaWord.put(key[k], tmpSet);
                            }
                        }
                    }
                }
                // 分词判断词是否为地理名词
                Result parse = ssme.parserQuery(query, "3");
                Map<String, String> wordparse = new LinkedHashMap<String, String>();
                int num = 0;
                for (Term t : parse) {
                    num++;
                    if (t.getName().length() <= 3) {
                        wordparse.put(num + "#" + t.getNatureStr(), t.getName());
                    }
                }
                Set<AreaFeature> tmpPosiSet = new HashSet<AreaFeature>(); // 输出地名
                // 四川/省/川&成都/市&金堂/县-位置分析
                for (Map.Entry<String, Set<String>> q : areaWord.entrySet()) {
                    String fullArea = q.getKey();
                    int areaLen = fullArea.split("&").length;
                    Set<String> location = q.getValue();
                    int locLen = location.size();
                    String maxLoc = "";
                    if (locLen <= 2) {
                        for (String l : location) {
                            if (l.length() >= maxLoc.length()) {
                                maxLoc = l;
                            }
                        }
                    }
                    // 位置-特征-权重计算
                    AreaFeature areafeature = new AreaFeature();
                    Boolean posiFlag = false;
                    if (areaLen == 1) {
                        // 四川/省/川
                        areafeature.setLevel(1);
                        areafeature.setSentNumber(segNum);
                        String[] proSeg = fullArea.split("/");
                        if (proSeg.length == 3) {
                            String province = proSeg[0] + "/" + proSeg[1];
                            String abbr = proSeg[2];
                            areafeature.setProvince(province);
                            areafeature.setProvinceAbbr(abbr);
                            int index1 = query.indexOf(maxLoc);
                            areafeature.setStrPosiNumber(index1);
                            areafeature.setFlagNumber(1);
                            areafeature.setLevelNameLen(maxLoc.length());
                            if (locLen == 2) {
                                areafeature.setScore(7.0);
                                posiFlag = true;
                                // 分词过滤不合理省份
                            } else if (wordparse.containsValue(maxLoc) || maxLoc.length() >= 4) {
                                areafeature.setScore(7.0);
                                posiFlag = true;
                            }
                        }
                    } else if (areaLen == 2) {
                        // 四川/省/川 + 成都/市
                        areafeature.setLevel(2);
                        areafeature.setSentNumber(segNum);
                        String[] areaSeg = fullArea.split("&");
                        if (areaSeg[0].split("/").length == 3
                                && areaSeg[1].split("/").length == 2) {
                            String province = areaSeg[0].split("/")[0] + "/" + areaSeg[0].split("/")[1];
                            String abbr = areaSeg[0].split("/")[2];
                            String city = areaSeg[1];
                            areafeature.setProvince(province);
                            areafeature.setProvinceAbbr(abbr);
                            areafeature.setCity(city);
                            int index2 = query.indexOf(maxLoc);
                            areafeature.setStrPosiNumber(index2);
                            areafeature.setFlagNumber(1);
                            areafeature.setLevelNameLen(maxLoc.length());
                            if (locLen == 2
                                    || maxLoc.length() >= 4
                                    || !fullArea.contains(maxLoc)) {
                                areafeature.setScore(7.0);
                                posiFlag = true;
                            } else if (wordparse.containsValue(maxLoc)) {
                                // 分词过滤不合理城市
                                for (Map.Entry<String, String> w : wordparse.entrySet()) {
                                    if (w.getValue().equals(maxLoc)) {
                                        String wordstr = w.getKey().split("#")[1];
                                        if (wordstr.equals("ns")) {
                                            areafeature.setScore(7.0);
                                        } else {
                                            areafeature.setScore(5.0);
                                        }
                                        posiFlag = true;
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (areaLen == 3) {
                        // 四川/省/川 + 成都/市 + 金堂/县
                        areafeature.setLevel(3);
                        areafeature.setSentNumber(segNum);
                        String[] areaSeg = fullArea.split("&");
                        if (areaSeg[0].split("/").length == 3
                                && areaSeg[1].split("/").length == 2
                                && areaSeg[2].split("/").length == 2) {
                            String province = areaSeg[0].split("/")[0] + "/" + areaSeg[0].split("/")[1];
                            String abbr = areaSeg[0].split("/")[2];
                            String city = areaSeg[1];
                            String district = areaSeg[2];
                            areafeature.setProvince(province);
                            areafeature.setProvinceAbbr(abbr);
                            areafeature.setCity(city);
                            areafeature.setDistrict(district);
                            int index3 = query.indexOf(maxLoc);
                            areafeature.setStrPosiNumber(index3);
                            areafeature.setFlagNumber(1);
                            areafeature.setLevelNameLen(maxLoc.length());
                            if (locLen == 2
                                    || maxLoc.length() >= 4
                                    || !fullArea.contains(maxLoc)) {
                                areafeature.setScore(7.0);
                                posiFlag = true;
                            } else if (wordparse.containsValue(maxLoc)) {
                                // 分词过滤不合理城市
                                for (Map.Entry<String, String> w : wordparse.entrySet()) {
                                    if (w.getValue().equals(maxLoc)) {
                                        String wordstr = w.getKey().split("#")[1];
                                        if (wordstr.equals("ns")) {
                                            areafeature.setScore(7.0);
                                        } else {
                                            areafeature.setScore(5.0);
                                        }
                                        posiFlag = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    // 句内分析
                    Double score = areafeature.getScore();
                    if (query.contains("公司") || query.contains("集团")
                            || query.contains("企业") || query.contains("机构")
                            || query.contains("便民")
                            || query.contains("新华社")) {
                        score -= 100;
                    }
                    // 地点顺序分数
//					score += 5 * Math.exp(-0.02 * segNum);
                    score += 5 * Math.exp(10 / (segNum + 1));// 越靠前越好
                    areafeature.setScore(score);
                    // 地理位置装载
                    if (posiFlag) {
                        tmpPosiSet.add(areafeature);
                    }
                }
                // 同一句子中出现包含地名,去除长度短的地名: 重庆/市#渝&重庆/市&江北/区	重庆/市#渝
                List<AreaFeature> finalList = rvScore(tmpPosiSet);
                for (AreaFeature a : finalList) {
                    posiList.add(a);
                }
            }
        }
        // posiList过滤
        Map<String, MyEntry<String, Double>> tmpposMap = posiFilter(posiList);
        Map<String, Double> posMap = new HashMap<String, Double>();
        for (Map.Entry<String, MyEntry<String, Double>> v : tmpposMap
                .entrySet()) {
            posMap.put(v.getKey(), v.getValue().getValue());
        }
        // 城市省略缩写描述(江浙沪)
        for (int i = 0; i < sent.size() & i < 100; i++) {
            for (Entry<String, Set<String>> a : areaAbbrCity.entrySet()) {
                if (sent.get(i).contains(a.getKey())) {
                    double abbScore = 0.0;
                    abbScore += 5;
                    // 句内分析
                    if (sent.get(i).contains("公司")
                            || sent.get(i).contains("集团")
                            || sent.get(i).contains("企业")
                            || sent.get(i).contains("机构")
                            || sent.get(i).contains("便民")
                            || sent.get(i).contains("新华社")) {
                        abbScore -= 100;
                    }
                    // 地点顺序分数
                    abbScore += 4 * Math.exp(10 / (i + 1));
//					abbScore += 4 * Math.exp(-0.02 * i);

                    for (String aa : a.getValue()) {
                        if (posMap.containsKey(aa)) {
                            posMap.put(aa, abbScore + posMap.get(aa));
                        } else {
                            posMap.put(aa, abbScore);
                        }
                    }
                }
            }
        }
        // Rank and out
        List<Entry<String, Double>> posRankMap = new LinkedList<Entry<String, Double>>();
        posRankMap = Sort.sortMap(posMap);
        List<OutputPosiInfo> posiList1 = new ArrayList<OutputPosiInfo>();
        for (Map.Entry<String, Double> p : posRankMap) {
            OutputPosiInfo pi = new OutputPosiInfo();
            pi.setPositionName(p.getKey());
            pi.setScore(p.getValue());
            posiList1.add(pi);
        }

        return posiList1;
    }

    @Override
    public void destroy() {

    }
}
