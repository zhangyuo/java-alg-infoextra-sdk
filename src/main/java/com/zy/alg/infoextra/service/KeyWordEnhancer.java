package com.zy.alg.infoextra.service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.zy.alg.domain.Result;
import com.zy.alg.domain.Term;
import com.zy.alg.infoextra.textrank.TextRank;
import com.zy.alg.infoextra.utils.Sort;
import com.zy.alg.service.AnsjSeg;
import com.zy.alg.service.AnsjSegImpl;
import com.zy.alg.splitword.IndexAnalysis;
import com.zy.alg.util.LoadDic;
import org.nlpcn.commons.lang.tire.domain.Forest;

public class KeyWordEnhancer implements KeyWord {

    private static Forest zbjdic = new Forest();
    private static Map<String, String> zbjsmall = new HashMap<String, String>();
    AnsjSeg ansjSeg;

    public KeyWordEnhancer(String resourcePath) throws IOException {

        String ZBJDicPath = resourcePath + "zbjsmall.dic";

        ansjSeg = AnsjSegImpl.getSingleton();
        LoadDic.insertUserDefineDic(zbjdic, ZBJDicPath);
        zbjsmall = readZBJDic(ZBJDicPath);
    }

    /**
     * TextRank extract keyword
     */
    @Override
    public List<Entry<String, Float>> extractKeyword(String corpus, int wordNum) {

        if (corpus == null
                || corpus.equals("")
                || corpus.equals("null")) {
            return null;
        }
        corpus = corpus.toLowerCase();
        Result splitWords = ansjSeg.textTokenizerUser(corpus, "1", zbjdic);

        List<String> wordList = new ArrayList<String>(splitWords.size());
        for (Term q : splitWords) {
            if (!q.getNatureStr().equals("w")
                    & !q.getName().contains("/")
                    & q.toString().contains("/")
                    & q.getName().length() > 1
                    & q.getNatureStr().matches("^[n]+$|^[v]+$|^[g]+$|^[en]+$")) {
                wordList.add(q.getName());
            }
        }
//        System.out.println(wordList);
        Map<String, Set<String>> words = new TreeMap<String, Set<String>>();
        Queue<String> que = new LinkedList<String>();
        for (String w : wordList) {
            if (!words.containsKey(w)) {
                words.put(w, new TreeSet<String>());
            }
            if (que.size() >= 5) {
                que.poll();
            }
            for (String qWord : que) {
                if (w.equals(qWord)) {
                    continue;
                }
                words.get(w).add(qWord);
                words.get(qWord).add(w);
            }
            que.offer(w);
        }
//        System.out.println(words);
        Map<String, Float> score = new HashMap<String, Float>();
        for (int i = 0; i < TextRank.max_iter; ++i) {
            Map<String, Float> m = new HashMap<String, Float>();
            float max_diff = 0;
            for (Map.Entry<String, Set<String>> entry : words.entrySet()) {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                m.put(key, 1 - TextRank.d);
                for (String element : value) {
                    int size1 = words.get(element).size();
                    if (key.equals(element) || size1 == 0) {
                        continue;
                    }
                    m.put(key, m.get(key) + TextRank.d / size1 * (score.get(element) == null ? 0 : score.get(element)));
                }
                max_diff = Math.max(max_diff, Math.abs(m.get(key) - (score.get(key) == null ? 0 : score.get(key))));
            }
            score = m;
            if (max_diff <= TextRank.min_diff) {
                break;
            }
        }

        List<Map.Entry<String, Float>> keywordMapSort = null;
        keywordMapSort = Sort.sortMapFloat(score);

        List<Map.Entry<String, Float>> keywordRankSort = new LinkedList<Entry<String, Float>>();
        int limitNum = 0;
        for (Entry<String, Float> q : keywordMapSort) {
            limitNum++;
            if (limitNum <= wordNum) {
                keywordRankSort.add(q);
            }
        }
        return keywordRankSort;
    }

    /**
     * read zbj dic
     *
     * @param zbjdicPath
     * @return
     * @throws IOException
     */
    public static Map<String, String> readZBJDic(String zbjdicPath)
            throws IOException {
        BufferedReader ar = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(zbjdicPath)), "utf-8"));
        Map<String, String> zbjsmall = new HashMap<String, String>();
        String Line;
        while ((Line = ar.readLine()) != null) {
            String[] seg = Line.split("\t");
            if (seg.length == 3) {
                zbjsmall.put(seg[0], seg[1] + "/" + seg[2]);
            }
        }
        ar.close();
        System.out.println(IndexAnalysis.parse("reading zbjdic over"));
        return zbjsmall;
    }

}
