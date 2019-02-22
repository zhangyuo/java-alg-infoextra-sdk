package com.zy.alg.infoextra.demo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import com.zbj.alg.infoextra.service.PositionExtraction;
import com.zbj.alg.infoextra.service.PositionExtractionEnhancer;
import com.zbj.alg.infoextra.utils.OutputPosiInfo;

public class DemoLocalPosiRecog {
	
	public static void main(String[] args) throws IOException{
		
		String originalCorpusPath = "E:/项目/地域信息抽取/";
		PositionExtraction pe = null;
		try{
			pe = new PositionExtractionEnhancer(originalCorpusPath);
		} catch (IOException e) {
			System.out.println(e);
		}

		test1(pe);
//		test2(pe);
		
	}

	private static void test1(PositionExtraction pe) {
		String query = "我需要北京朝阳区互联网思维酒店隆重庆祝合作市需要完成的合作河南蒙古族自治县海南藏族自治州，需要给我一个高科技在酒店的详细实行方案"
				+ "准备开家互联网思维酒店,用户外出前，需网上录入目的地，在酒店网站进行客房预订，"
				+ "通过移动支付预付订与微信选房，微信客服等等相结合的应用详细方案。"
				+ "高科技在酒店的运用，贯穿在整个酒店各个细节方面的实行方案。重庆江北区服务商优先。";
		String category =  "品牌设计/策划#品牌咨询服务#品牌架构";
		String position = "上海&上海&松江";
		List<OutputPosiInfo> posiMap = pe.taskPosiRecog(query,category,position);
		for(OutputPosiInfo o: posiMap){
			System.out.println(o.getPositionName()+"\t"+o.getScore());
		}
	}
	
	private static void test2(PositionExtraction pe) throws IOException {
		String originalCorpusPath = "E:/项目/地域信息抽取/";
		String testCorpusPath = originalCorpusPath+"taskCorpus.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(testCorpusPath), "utf-8"));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(originalCorpusPath + "Y_Position1.txt"), "utf-8"), true);
		PrintWriter pv = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(originalCorpusPath + "N_Position1.txt"), "utf-8"), true);
		
		String line = null;
		int num = 0;
		while ((line = br.readLine()) != null){
			num++;
//			if(num == 60){
//				num++;
//				num--;
//			}
			String[] seg = line.split(",");
			if(seg.length == 13){
				String category = seg[2]+"#"+seg[4]+"#"+seg[6];
				String position = seg[8]+"&"+seg[9]+"&"+seg[10];
				String corpus = seg[11]+"。"+seg[12];
				System.out.println(num);
				List<OutputPosiInfo> posiMap = pe.taskPosiRecog(corpus.toLowerCase(),category.toLowerCase(),position);
				if(posiMap.size() == 0){
					pv.println(num + "#\t" + position + "\t" + corpus);
				}
				else{
					pw.println(num + "#\t" + position + "\t" + corpus);
					for(OutputPosiInfo o: posiMap){
						pw.println(o.getPositionName()+"\t"+o.getScore());
					}
					pw.println();
				}
			}
		}
		br.close();
		pw.close();
		pv.close();
	}

}
