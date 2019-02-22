package com.zbj.alg.infoextra.demo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.zbj.alg.infoextra.service.KeyWord;
import com.zbj.alg.infoextra.service.KeyWordEnhancer;

public class DemoKeyWordExtraction {
	
	public static void main(String[] args)  throws IOException {
		
		String resourcePath = "E:/代码模型文件/OntologyModel/";
		KeyWord me = null;
		try{
			me = new KeyWordEnhancer(resourcePath);
		} catch (IOException e) {
			System.out.print(e);
		}
		
		String content = "里约热内卢（葡萄牙语：Rio de Janeiro，意即“一月的河”），"
				+ "简称“里约”，曾经是巴西的首都（1763年－1960年），位于巴西东南部沿海地区，"
				+ "东南濒临大西洋，海岸线长636公里。里约热内卢属于热带海洋性气候，终年高温，气温年、日较差都小，"
				+ "季节分配比较均匀。里约热内卢是巴西乃至南美的重要门户，同时也是巴西及南美经济最发达的地区之一"
				+ "，素以巴西重要交通枢纽和信息通讯、旅游、文化、金融和保险中心而闻名。里约热内卢是巴西第二大工业基地。"
				+ "市境内的里约热内卢港是世界三大天然良港之一，里约热内卢基督像是该市的标志，也是世界新七大奇迹之一。"
				+ "里约热内卢也是巴西联邦共和国的第二大城市，仅次于圣保罗，又被称为巴西联邦共和国的第二首都，"
				+ "拥有全国最大进口港、是全国经济中心，同时也是全国重要的交通中心。背山面水，港湾优良。工业主要有纺织、印刷、汽车等，"
				+ "有七百多家银行和最大的股票交易所；有世界最大的马拉卡纳球场。海滨风景优美，为南美洲著名旅游胜地。"
				+ "里约热内卢曾在马拉卡纳球场举办过1950年巴西世界杯和2014年巴西世界杯两次世界杯。[1]2009年10月02日，"
				+ "里约热内卢获得2016年第31届夏季奥林匹克运动会举办权，2016年8月5日，2016年里约热内卢奥运会在马拉卡纳球场开幕，"
				+ "里约热内卢成为奥运史上首个主办奥运会的南美洲城市，同时也是首个主办奥运会的葡萄牙语城市";
		Map<String,Double> keywordMap = new HashMap<String,Double>();
		// 互信息左右熵
		keywordMap = me.extractPhrase(content, 5);
		for(Map.Entry<String, Double> t : keywordMap.entrySet()){
			System.out.println(t.getKey()+"\t"+t.getValue());
		}
		
	}

}
