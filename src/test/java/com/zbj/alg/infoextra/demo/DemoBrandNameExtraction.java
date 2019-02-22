package com.zbj.alg.infoextra.demo;

import java.io.IOException;

import com.zbj.alg.infoextra.service.BrandNameExtraction;
import com.zbj.alg.infoextra.service.BrandNameExtractionEnhancer;

public class DemoBrandNameExtraction {
	
	public static void main(String[] args) throws IOException{
		
		String originalFilePath = "E:/项目/猪八戒店铺品牌名标注抽取与自动更新机制/";
		
		BrandNameExtraction bne = new BrandNameExtractionEnhancer(originalFilePath);
		
		String oriLabel = "重庆知行地理信息有限责任公司";
		String realtype = "0";
		double ability = 0.0;
		String label = bne.getBrandWord(oriLabel, realtype, ability);
		System.out.print(label);
	}

}
