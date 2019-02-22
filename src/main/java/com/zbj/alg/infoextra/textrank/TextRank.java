package com.zbj.alg.infoextra.textrank;

import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class TextRank {
	
	/**
     * 最大迭代次数
     */
	public static int max_iter = 200;
	
	 /**
     * 阻尼系数（ＤａｍｐｉｎｇＦａｃｔｏｒ），一般取值为0.85
     */
    public static float d = 0.85f;
    
    public static float min_diff = 0.001f;
    
    /**
     * 文档句子的个数
     */
    int D;
    
    /**
     * 拆分为[句子[单词]]形式的文档
     */
    List<List<String>> docs;
    
    /**
     * 句子和其他句子的相关程度
     */
    double[][] weight;
    
    /**
     * 该句子和其他句子相关程度之和
     */
    double[] weight_sum;
    
    /**
     * 迭代之后收敛的权重
     */
    double[] vertex;
    
    /**
     * 排序后的最终结果 score <-> index
     */
    TreeMap<Double, Integer> top;
    
    BM25 bm25;
	
	public TextRank(List<List<String>> docs){
		
		this.docs = docs;
        bm25 = new BM25(docs);
        D = docs.size();
        weight = new double[D][D];
        weight_sum = new double[D];
        vertex = new double[D];
        top = new TreeMap<Double, Integer>(Collections.reverseOrder());
	}
	
	public TreeMap<Double, Integer> textRankSentence(){
		
        int cnt = 0;
        for (List<String> sentence : docs)
        {
            double[] scores = bm25.simAll(sentence);
//            System.out.println(Arrays.toString(scores));
            weight[cnt] = scores;
            weight_sum[cnt] = sum(scores) - scores[cnt]; // 减掉自己，自己跟自己肯定最相似
            vertex[cnt] = 1.0;
            ++cnt;
        }
        for (int _ = 0; _ < max_iter; ++_)
        {
            double[] m = new double[D];
            double max_diff = 0;
            for (int i = 0; i < D; ++i)
            {
                m[i] = 1 - d;
                for (int j = 0; j < D; ++j)
                {
                    if (j == i || weight_sum[j] == 0) continue;
                    m[i] += (d * weight[j][i] / weight_sum[j] * vertex[j]);
                }
                double diff = Math.abs(m[i] - vertex[i]);
                if (diff > max_diff)
                {
                    max_diff = diff;
                }
            }
            vertex = m;
            if (max_diff <= min_diff) break;
        }
        // 我们来排个序吧
        for (int i = 0; i < D; ++i)
        {
            top.put(vertex[i], i);
        }
        
        return top;
    }
	
	/**
     * 简单的求和
     *
     * @param array
     * @return
     */
    private static double sum(double[] array){
        double total = 0;
        for (double v : array)
        {
            total += v;
        }
        return total;
    }
	
}