package com.zy.alg.infoextra.demo;

import java.util.List;
import java.util.Map.Entry;

import com.zy.alg.infoextra.service.LongSentenceRank;
import com.zy.alg.infoextra.service.LongSentenceRankEnhance;

public class DemoTextRankSentRank {

    public static void main(String[] arg) {

        String resourcePath = "G:\\project\\模型数据资源库-all\\";
        LongSentenceRank ts;
        try {
            ts = new LongSentenceRankEnhance(resourcePath);
            String corpus = "程序员(英文Programmer)是从事程序开发、维护的专业人员。" +
                    "一般将程序员分为程序设计人员和程序编码人员，" +
                    "但两者的界限并不非常清楚，特别是在中国。" +
                    "软件从业人员分为初级程序员、高级程序员、系统" +
                    "分析员和项目经理四大类。";
            List<Entry<String, Double>> sent = ts.textRankLongSent(corpus);
            for (Entry<String, Double> q : sent) {
                System.out.println(q.getKey() + "\t=\t" + q.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
