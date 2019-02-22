package com.zy.alg.infoextra.service;

import java.util.List;
import java.util.Map;

public interface KeyWord {
	
	/**
	 * TextRank extract keyword
	 * @param corpus
	 * @param wordNum (return word number)
	 * @return
	 */
	List<Map.Entry<String, Float>> extractKeyword(String corpus, int wordNum);
	
	/**
	 * MultiInfomation left right Entropy extract keyword
	 * @param corpus
	 * @param wordNum
	 * @return
	 */
	Map<String,Double> extractPhrase(String corpus, int wordNum);

}
