package com.zy.alg.infoextra.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;

import com.zy.alg.domain.Result;
import com.zy.alg.domain.Term;
import com.zy.alg.infoextra.seg.LongSentenceSegment;
import com.zy.alg.infoextra.textrank.TextRank;
import com.zy.alg.infoextra.utils.Sort;
import com.zy.alg.nlp.crf.ModelParse;
import com.zy.alg.nlp.server.CRF;
import com.zy.alg.nlp.server.CRFEnhancer;
import com.zy.alg.service.AnsjSeg;
import com.zy.alg.service.AnsjSegImpl;
import com.zy.alg.util.LoadDic;
import com.zy.alg.word2vec.VectorModel;
import org.nlpcn.commons.lang.tire.domain.Forest;


public class LongSentenceRankEnhance implements LongSentenceRank {

    private static Forest zbjdic = new Forest();
    private static Forest serdic = new Forest();
    private static VectorModel vm = null;
    //CRF模型解析
    private static ModelParse MP = new ModelParse();

    CRF crf;
    AnsjSeg ansjSeg;

    private Set<String> sertag = new HashSet<String>();

    public LongSentenceRankEnhance(String resourcePath) throws IOException {

        String vectorModelPath = resourcePath + "WordVec4GuideTag";
        String ZBJDicPath = resourcePath + "zbjsmall.dic";
        String SerLibPath = resourcePath + "ServiceTagLibrary";
        String CRFmodelRealpath = resourcePath + "crfmodel.txt";

        ansjSeg = AnsjSegImpl.getSingleton();
        LoadDic.insertUserDefineDic(zbjdic,ZBJDicPath);
        LoadDic.insertUserDefineDic(serdic,SerLibPath);
        vm = VectorModel.loadFromFile(vectorModelPath);
        MP.parse(CRFmodelRealpath);
        crf = new CRFEnhancer();

        BufferedReader sr = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(resourcePath + "ServiceTagLibrary")), "utf-8"));
        String sline = null;
        while ((sline = sr.readLine()) != null) {
            String[] seg = sline.split("\t");
            if (seg.length >= 3 & (seg[2].contains("主题 ") || seg[2].contains("类型"))) {
                String[] seg1 = seg[2].split("#");
                if (seg1.length == 1 & (seg1[0].equals("主题") || seg1[0].equals("类型"))) {
                    for (int i = 3; i < seg.length; i++) {
                        String[] seg2 = seg[i].split("/");
                        sertag.add(seg2[0]);
                    }
                } else if (seg1[0].equals("主题") || seg1[0].equals("类型")) {
                    sertag.add(seg1[1]);
                }
            }
        }
        sr.close();
    }

    /**
     * word embedding + crf
     */
    @Override
    public List<Entry<String, Double>> wordvecRank(String corpus) {

        float[] corpusvec = new float[vm.getVectorSize()];
        Map<String, Double> sentnceMap = new HashMap<String, Double>();
        Map<String, float[]> sentnceVecMap = new LinkedHashMap<String, float[]>();
        Result words = ansjSeg.textTokenizerUser(corpus, "1", zbjdic);
        StringBuffer sentence = new StringBuffer();
        float[] sentenceVec = new float[vm.getVectorSize()];
        double sum = 1;
        double num = 1;
        for (Term w : words) {
            if (w.getNatureStr().equals("w")
                    || !w.toString().contains("/")
                    || num == 10) {
                sentnceVecMap.put(num + "#-" + sentence.toString(), sentenceVec);
                num = 1;
                sentence = new StringBuffer();
                sentenceVec = new float[vm.getVectorSize()];
            }
            if (vm.getWordMap().containsKey(w.getName())) {
                num++;
                sum++;
                sentence.append(w.getName());
            }
        }
        sentnceVecMap.put(num + "#" + sentence.toString(), sentenceVec);
        for (Entry<String, float[]> s : sentnceVecMap.entrySet()) {
            if (s.getKey().split("#").length != 2) {
                continue;
            }
            //double distance = 0.0;
            double number = Double.parseDouble(s.getKey().split("#")[0]);
            String sent = s.getKey().split("#")[1];
            Map<Term, String> centerWord = crf.getWordLabel(sent, zbjdic, MP);
            for (Entry<Term, String> w : centerWord.entrySet()) {
                double para = 1.0;
                if (vm.getWordMap().containsKey(w.getKey().getName())) {
                    if (w.getValue().equals("TH")) {
                        //System.out.println(w.getKey());
                        para = 5;
                    }
                    for (int i = 0; i < vm.getVectorSize(); i++) {
                        s.getValue()[i] += para * vm.getWordVector(w.getKey().getName())[i] / number;
                        corpusvec[i] += para * vm.getWordVector(w.getKey().getName())[i] / sum;
                        //distance += corpusvec[i] * s.getValue()[i];
                    }
                }
            }

        }

        //int sentNumber = 0;
        for (Entry<String, float[]> s : sentnceVecMap.entrySet()) {
            if (s.getKey().split("#").length != 2) {
                continue;
            }
            //sentNumber++;
            String sent = s.getKey().split("#")[1];
            double distance = 0.0;
            for (int i = 0; i < vm.getVectorSize(); i++) {
                distance += corpusvec[i] * s.getValue()[i];
            }
            sentnceMap.put(sent, /*Math.exp(-0.2*sentNumber)**/distance);
        }

        //rank sentence
        List<Map.Entry<String, Double>> sentnceMapSort = null;
        if (!sentnceMap.isEmpty()) {
            sentnceMapSort = Sort.sortMap(sentnceMap);
        }
        if (sentnceMapSort == null) {
            sentnceMapSort = new ArrayList<Map.Entry<String, Double>>();
            Map<String, Double> map = new HashMap<String, Double>();
            map.put(corpus, 1.0);
            for (Entry<String, Double> m : map.entrySet()) {
                sentnceMapSort.add(m);
            }
        }
        return sentnceMapSort;
    }

    /**
     * Textrank long sentence
     */
    @Override
    public List<Entry<String, Double>> textRankLongSent(String longquery) {

        List<String> segSentence = LongSentenceSegment.longSentSpiltPunctuation(longquery);
        List<List<String>> sentenceDocs = new LinkedList<List<String>>();
        List<Entry<String, Double>> listSentRank = new LinkedList<Entry<String, Double>>();
        Map<String, Double> sentRank = new LinkedHashMap<String, Double>();

        for (int i = 0; i < segSentence.size(); i++) {
            Result words = ansjSeg.textTokenizerUser(segSentence.get(i), "1", zbjdic);
            List<String> docs = new LinkedList<String>();
            for (Term q : words) {
                if (!q.getNatureStr().equals("w")
                        & !q.getName().contains("/")
                        & q.toString().contains("/")
                        & q.getName().length() > 1
                        & q.getNatureStr().matches("^[n]+$|^[v]+$|^[g]+$|^[en]+$")) {
                    docs.add(q.getName());
                }
            }
            sentenceDocs.add(docs);
        }

        TreeMap<Double, Integer> top = new TreeMap<Double, Integer>();
        TextRank textRank = new TextRank(sentenceDocs);
        top = textRank.textRankSentence();

        for (Entry<Double, Integer> t : top.entrySet()) {
            sentRank.put(segSentence.get(t.getValue()), t.getKey());
        }

        for (Entry<String, Double> s : sentRank.entrySet()) {
            listSentRank.add(s);
        }

        return listSentRank;
    }

}
