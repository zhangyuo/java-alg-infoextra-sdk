package com.zy.alg.infoextra.service;

import java.util.List;
import java.util.Map.Entry;

public interface LongSentenceRank {

    /**
     * 长句排序分析(基于词向量)
     *
     * @param corpus：长句
     * @return 分句和权值
     */
    List<Entry<String, Double>> wordvecRank(String corpus);

    /**
     * 长句排序分析(基于TextRank)
     *
     * @param corpus
     * @return
     */
    List<Entry<String, Double>> textRankLongSent(String corpus);

}
