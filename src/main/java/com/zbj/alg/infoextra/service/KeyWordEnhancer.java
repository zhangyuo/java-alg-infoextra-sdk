package com.zbj.alg.infoextra.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.nlpcn.commons.lang.tire.domain.Forest;

import com.zbj.alg.infoextra.instance.GetInstanceWord;
import com.zbj.alg.infoextra.multiinfo.lrentropy.Occurrence;
import com.zbj.alg.infoextra.multiinfo.lrentropy.PairFrequency;
import com.zbj.alg.infoextra.seg.LongSentenceSegment;
import com.zbj.alg.infoextra.textrank.TextRank;
import com.zbj.alg.infoextra.utils.InitialDictionary;
import com.zbj.alg.infoextra.utils.Sort;
import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.UserDefineLibrary;
import com.zbj.alg.seg.service.ServiceSegModelEnhance;
import com.zbj.alg.seg.splitWord.ToAnalysis;

public class KeyWordEnhancer implements KeyWord {
	
    private static Forest zbjdic = new Forest();
    private static Map<String,String> zbjsmall = new HashMap<String,String>();
    GetInstanceWord gtw = null;
    
    public KeyWordEnhancer(String resourcePath) throws IOException {
    	
    	String ZBJDicPath = resourcePath + "zbjsmall.dic";
    	
    	ServiceSegModelEnhance.getInstance();
		InitialDictionary.insertZBJDic(zbjdic,ZBJDicPath);
		zbjsmall = InitialDictionary.readZBJDic(ZBJDicPath);
		gtw = new GetInstanceWord(resourcePath);
    }

    /**
     * TextRank extract keyword
     */
	public List<Entry<String, Float>> extractKeyword(String corpus, int wordNum) {
		
		if(corpus == null
				|| corpus.equals("")
				|| corpus.equals("null")){
			return null;
		}
		corpus = corpus.toLowerCase();
		Result splitWords = ToAnalysis.parse(corpus,UserDefineLibrary.FOREST,zbjdic);
		
		List<String> wordList = new ArrayList<String>(splitWords.size());
        for (Term q : splitWords)
        {
        	if(!q.getNatureStr().equals("w")
					& !q.getName().contains("/")
					& q.toString().contains("/")
					& q.getName().length()>1
					& q.getNatureStr().matches("^[n]+$|^[v]+$|^[g]+$|^[en]+$")){
                wordList.add(q.getName());
            }
        }
//        System.out.println(wordList);
        Map<String, Set<String>> words = new TreeMap<String, Set<String>>();
        Queue<String> que = new LinkedList<String>();
        for (String w : wordList)
        {
            if (!words.containsKey(w))
            {
                words.put(w, new TreeSet<String>());
            }
            if (que.size() >= 5)
            {
                que.poll();
            }
            for (String qWord : que)
            {
                if (w.equals(qWord))
                {
                    continue;
                }
                words.get(w).add(qWord);
                words.get(qWord).add(w);
            }
            que.offer(w);
        }
//        System.out.println(words);
        Map<String, Float> score = new HashMap<String, Float>();
        for (int i = 0; i < TextRank.max_iter; ++i)
        {
            Map<String, Float> m = new HashMap<String, Float>();
            float max_diff = 0;
            for (Map.Entry<String, Set<String>> entry : words.entrySet())
            {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                m.put(key, 1 - TextRank.d);
                for (String element : value)
                {
                    int size1 = words.get(element).size();
                    if (key.equals(element) || size1 == 0) continue;
                    m.put(key, m.get(key) + TextRank.d / size1 * (score.get(element) == null ? 0 : score.get(element)));
                }
                max_diff = Math.max(max_diff, Math.abs(m.get(key) - (score.get(key) == null ? 0 : score.get(key))));
            }
            score = m;
            if (max_diff <= TextRank.min_diff) break;
        }

        List<Map.Entry<String, Float>> keywordMapSort = null;
        keywordMapSort = Sort.sortMapFloat(score);
        
        List<Map.Entry<String, Float>> keywordRankSort = new LinkedList<Entry<String, Float>>();
        int limitNum = 0;
        for(Entry<String, Float> q: keywordMapSort){
        	limitNum++;
        	if(limitNum<=wordNum){
        		keywordRankSort.add(q);
        	}
        }
        return keywordRankSort;
	}

	/**
	 * MultiInfomation left right Entropy extract keyword
	 */
	public Map<String, Double> extractPhrase(String corpus, int wordNum) {
		
		if(corpus == null
				|| corpus.equals("")
				|| corpus.equals("null")){
			return null;
		}
		corpus = corpus.toLowerCase();
		Map<String,Double> phraseMap = new LinkedHashMap<String,Double>();
		Occurrence occurrence = new Occurrence();
		List<String> sentSeg = LongSentenceSegment.longSentSpiltPunctuation(corpus); 
		for(String s: sentSeg){
			List<Term> realSentWords = new LinkedList<Term>();
			realSentWords = gtw.getInstWords(s);
			occurrence.addAll(realSentWords);
		}
		occurrence.compute(zbjsmall);
		
		for (PairFrequency phrase : occurrence.getPhraseByScore()){
			if (phraseMap.size() == wordNum){
				break;
			}
			phraseMap.put(phrase.first + phrase.second, (Double) phrase.score);
		}
		
		return phraseMap;
	}
	
}
