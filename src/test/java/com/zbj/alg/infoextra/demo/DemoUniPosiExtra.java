package com.zbj.alg.infoextra.demo;

import java.io.IOException;
import java.util.List;

import com.zbj.alg.infoextra.service.PositionExtraction;
import com.zbj.alg.infoextra.service.PositionExtractionEnhancer;
import com.zbj.alg.infoextra.utils.OutputPosiInfo;


public class DemoUniPosiExtra {
	
	public static void main(String[] args) throws IOException{
		
		String originalCorpusPath = "G://project/地域信息抽取/";
		PositionExtraction pe = null;
		try{
			pe = new PositionExtractionEnhancer(originalCorpusPath);
		} catch (IOException e) {
			System.out.println(e);
		}
		
//		String query = "北京朝阳区的表演真精彩,关于新建张家界经吉首至怀化铁路可行性研究报告的批复发改基础20162076号,北京有限公司，成都，綦江";
		String query = "广西壮族自治区还有美丽的桂林山水";
		List<OutputPosiInfo> posiMap = pe.uniPosiExtra(query);
		for(OutputPosiInfo o: posiMap){
			System.out.println("地域："+o.getPositionName()+"\t"+o.getScore());
		}
		
	}

}
