/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/10/8 1:05</create-date>
 *
 * <copyright file="Occurrence.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.zbj.alg.infoextra.multiinfo.lrentropy;

import java.util.*;

import com.zbj.alg.seg.domain.Term;

/**
 * 词共现统计，最多统计到三阶共现
 *
 * @author 
 */
public class Occurrence
{
    /**
     * 两个词的正向连接符 中国 RIGHT 人民
     */
    public static final String RIGHT = "——>";
    /**
     * 两个词的逆向连接符 人民 LEFT 中国
     */
    static final String LEFT = "<——";

    /**
     * 全部单词数量
     */
    double totalTerm;
    /**
     * 全部接续数量，包含正向和逆向
     */
    double totalPair;

    /**
     * 2 gram的pair
     */
    BinTrie<PairFrequency> triePair;
    /**
     * 词频统计用的储存结构
     */
    BinTrie<TermFrequency> trieSingle;
    /**
     * 三阶储存结构
     */
    BinTrie<TriaFrequency> trieTria;

    /**
     * 软缓存一个pair的setset
     */
    private Set<Map.Entry<String, PairFrequency>> entrySetPair;
    
    /**
     * 软缓存一个tria的setset
     */
    private Set<Map.Entry<String, TriaFrequency>> entrySetTria;

    public Occurrence(){
        triePair = new BinTrie<PairFrequency>();
        trieSingle = new BinTrie<TermFrequency>();
        trieTria = new BinTrie<TriaFrequency>();
        totalTerm = totalPair = 0;
    }

	public void addAll(List<Term> realSentWords) {
		String[] termList = new String[realSentWords.size()];
		int i = 0;
		for (Term t : realSentWords){
			termList[i] = t.getName()+"/"+t.getNatureStr();
			++i;
		}
		addAll(termList);
	}

	public void addAll(String[] termList) {
		for (String term : termList){
			addTerm(term);
		}
		
		String first = null;
        for (String current : termList){
            if (first != null){
                addPair(first, current);
            }
            first = current;
        }
        
        for (int i = 2; i < termList.length; ++i){
            addTria(termList[i - 2], termList[i - 1], termList[i]);
        }
	}

	public void addTria(String first, String second, String third){
        String key = first + RIGHT + second + RIGHT + third;
        TriaFrequency value = trieTria.get(key);
        if (value == null)
        {
            value = TriaFrequency.createR(first, RIGHT, second, third);
            trieTria.put(key, value);
        }
        else
        {
            value.increase();
        }
        key = second + RIGHT + third + LEFT + first;    // 其实两个key只有最后一个连接符方向不同
        value = trieTria.get(key);
        if (value == null)
        {
            value = TriaFrequency.createL(second, third, LEFT, first);
            trieTria.put(key, value);
        }
        else
        {
            value.increase();
        }
    }

	/**
     * 添加一个共现
     *
     * @param first  第一个词
     * @param second 第二个词
     */
    public void addPair(String first, String second){
        addPair(first, RIGHT, second);
    }

	private void addPair(String first, String right2, String second) {
		String key = first + right2 + second;
        PairFrequency value = triePair.get(key);
        if (value == null)
        {
            value = PairFrequency.create(first, right2, second);
            triePair.put(key, value);
        }
        else
        {
            value.increase();
        }
        ++totalPair;
	}

	/**
     * 统计词频
     *
     * @param key 增加一个词
     */
	private void addTerm(String key) {
		TermFrequency value = trieSingle.get(key.split("/")[0]);
		if (value == null){
			value = new TermFrequency(key.split("/")[0]);
            trieSingle.put(key.split("/")[0], value);
		}
		else{
			value.increase();
		}
		++totalTerm;
	}

	/**
     * 输入数据完毕，执行计算
     */
    public void compute(Map<String,String> zbjsmall){
    	/** Pair(二阶共现) **/
        entrySetPair = triePair.entrySet();
        double total_mi = 0;
        double total_le = 0;
        double total_re = 0;
        for (Map.Entry<String, PairFrequency> entry : entrySetPair){
            PairFrequency value = entry.getValue();
            value.mi = computeMutualInformation(value,zbjsmall);
            value.le = computeLeftEntropy(value);
            value.re = computeRightEntropy(value);
            total_mi += value.mi;
            total_le += value.le;
            total_re += value.re;
        }

        for (Map.Entry<String, PairFrequency> entry : entrySetPair){
            PairFrequency value = entry.getValue();
            value.score = value.mi / total_mi + value.le / total_le+ value.re / total_re;   // 归一化
            value.score *= entrySetPair.size();
        }
        
        /** Tair(三阶共现) **/
//        entrySetTria = trieTria.entrySet();
//        for (Map.Entry<String, TriaFrequency> entry : entrySetTria){
//        	TriaFrequency value = entry.getValue();
//        }
        
        /** Tair(一阶共现) **/
//        entrySetSingle = trieSingle.entrySet();
    }

    /**
     * 计算右熵
     *
     * @param pair
     * @return
     */
    public double computeRightEntropy(PairFrequency pair){
        Set<Map.Entry<String, TriaFrequency>> entrySet = trieTria.prefixSearch(pair.getKey() + RIGHT);
        return computeEntropy(entrySet);
    }

	/**
     * 计算左熵
     *
     * @param pair
     * @return
     */
    public double computeLeftEntropy(PairFrequency pair){
        Set<Map.Entry<String, TriaFrequency>> entrySet = trieTria.prefixSearch(pair.getKey() + LEFT);
        return computeEntropy(entrySet);
    }

    private double computeEntropy(Set<Map.Entry<String, TriaFrequency>> entrySet){
        double totalFrequency = 0;
        for (Map.Entry<String, TriaFrequency> entry : entrySet){
            totalFrequency += entry.getValue().getValue();
        }
        double le = 0;
        for (Map.Entry<String, TriaFrequency> entry : entrySet){
            double p = entry.getValue().getValue() / totalFrequency;
            le += -p * Math.log(p);
        }
        return le;
    }

	public double computeMutualInformation(PairFrequency pair, Map<String,String> zbjsmall){
    	double totalFrequency = 6877576.0;
    	int firstTermFrequency = 0;
    	int secondTermFrequency = 0;
    	if(zbjsmall.containsKey(pair.first)){
    		firstTermFrequency = Integer.parseInt(zbjsmall.get(pair.first).split("/")[1]);
    	}
    	if(zbjsmall.containsKey(pair.second)){
    		secondTermFrequency = Integer.parseInt(zbjsmall.get(pair.second).split("/")[1]);
    	}
        return Math.log(Math.max(Predefine.MIN_PROBABILITY, pair.getValue() / totalPair)
        		/ Math.max(Predefine.MIN_PROBABILITY, (firstTermFrequency / totalFrequency
        				* secondTermFrequency / totalFrequency)));
    }

	public List<PairFrequency> getPhraseByScore(){
        List<PairFrequency> pairFrequencyList = new ArrayList<PairFrequency>(entrySetPair.size());
        for (Map.Entry<String, PairFrequency> entry : entrySetPair){
            pairFrequencyList.add(entry.getValue());
        }
        Collections.sort(pairFrequencyList, new Comparator<PairFrequency>(){
            @Override
            public int compare(PairFrequency o1, PairFrequency o2){
                return -Double.compare(o1.score, o2.score);
            }
        });
        return pairFrequencyList;
    }

}
