package com.zy.alg.infoextra.demo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import com.zy.alg.infoextra.service.PositionExtraction;
import com.zy.alg.infoextra.service.PositionExtractionEnhancer;
import com.zy.alg.infoextra.utils.OutputPosiInfo;

public class DemoUniPosiExtra1 {
	public static void main(String[] args) throws IOException{
		
		String originalCorpusPath = "E:/项目/地域信息抽取/";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(originalCorpusPath
						+ "taskCorpus.txt"), "utf-8"));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(originalCorpusPath
						+ "taskCorpus.txt.area"), "utf-8"));
		PositionExtraction pe = null;
		try{
			pe = new PositionExtractionEnhancer(originalCorpusPath);
		} catch (IOException e) {
			System.out.println(e);
		}
		
		String line = null;
		int num = 0;
		while ((line = br.readLine()) != null) {
			num++;
			String[] seg = line.split(",");
			if(seg.length == 13){
				String category = seg[2]+"#"+seg[4]+"#"+seg[6];
				String position = seg[8]+"&"+seg[9]+"&"+seg[10];
				String corpus = seg[11]+"。"+seg[12];
				System.out.println(num);
				List<OutputPosiInfo> posiMap = pe.uniPosiExtra(corpus);
				pw.println(line);
				for(OutputPosiInfo o: posiMap){
					pw.println("地域："+o.getPositionName()+"\t"+o.getScore());
				}
				pw.println();
			}
		}
		br.close();
		pw.close();
		
	}
}
