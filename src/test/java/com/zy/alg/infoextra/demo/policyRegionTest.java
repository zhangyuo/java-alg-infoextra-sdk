package com.zy.alg.infoextra.demo;

import java.io.IOException;
import java.util.List;

import com.zy.alg.infoextra.service.PositionExtraction;
import com.zy.alg.infoextra.service.PositionExtractionEnhancer;
import com.zy.alg.infoextra.utils.OutputPosiInfo;
import com.zbj.alg.seg.service.ServiceSegModelEnhance;

public class policyRegionTest {
	
	public static void main(String[] arg) throws IOException{
		String modelPath = "E:/tr_project_one/project/technology-services-policy/PolModelResource/";
		ServiceSegModelEnhance.getInstance();
		PositionExtraction pe = new PositionExtractionEnhancer(modelPath);		
		String pubpro = "满洲里市";//宁乡县
		String pubmun = "";
		String title = "关于满洲里城北小区二期(B)区及三期项目规划批前公示,黄山";		
		List<OutputPosiInfo> posiMap = pe.policyRegExtra(pubpro+pubmun+title);
		System.out.print("地域： ");
		if(posiMap.size() != 0){
			for(OutputPosiInfo s:posiMap){
				System.out.println(s.getPositionName() +"\t"+ s.getScore());
			}
		}
		System.out.println();
	}
}
