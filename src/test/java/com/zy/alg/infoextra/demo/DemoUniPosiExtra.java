package com.zy.alg.infoextra.demo;

import java.io.IOException;
import java.util.List;

import com.zy.alg.infoextra.service.PositionExtraction;
import com.zy.alg.infoextra.service.PositionExtractionEnhancer;
import com.zy.alg.infoextra.utils.OutputPosiInfo;


public class DemoUniPosiExtra {

    public static void main(String[] args) {

        String originalCorpusPath = "G:/project/知识图谱2.0/";
        PositionExtraction pe;
        try {
            pe = new PositionExtractionEnhancer(originalCorpusPath);
            String query = "北京朝阳区的表演真精彩,关于新建张家界经吉首至怀化铁路可行性研究报告的批复发改基础20162076号,北京有限公司，成都，綦江";
//            String query = "新疆维吾尔自治区";
//            String query = "广西壮族自治区还有美丽的桂林山水";
            List<OutputPosiInfo> posiMap = pe.uniPosiExtra(query);
            for (OutputPosiInfo o : posiMap) {
                System.out.println("地域：" + o.getPositionName() + "\t" + o.getScore());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
