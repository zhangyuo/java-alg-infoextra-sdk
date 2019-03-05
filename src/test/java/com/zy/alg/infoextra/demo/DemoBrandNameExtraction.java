package com.zy.alg.infoextra.demo;

import com.zy.alg.infoextra.service.BrandNameExtraction;
import com.zy.alg.infoextra.service.BrandNameExtractionEnhancer;

public class DemoBrandNameExtraction {

    public static void main(String[] args) {

        String originalFilePath = "G:\\project\\猪八戒店铺品牌名标注抽取与自动更新机制\\";

        BrandNameExtraction bne = new BrandNameExtractionEnhancer(originalFilePath);

        String oriLabel = "重庆知行地理信息有限责任公司";
        String realType = "0";
        double ability = 0.0;
        String label = bne.getBrandWord(oriLabel, realType, ability);
        System.out.print(label);
    }

}
