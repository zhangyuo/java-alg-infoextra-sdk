package com.zy.alg.infoextra.demo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import com.zbj.alg.infoextra.service.BrandNameExtraction;
import com.zbj.alg.infoextra.service.BrandNameExtractionEnhancer;

public class DemoBrandNameExtraction1 {

	public static void main(String[] args) throws IOException {

		String originalFilePath = "E:\\项目\\猪八戒店铺品牌名标注抽取与自动更新机制\\";

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(originalFilePath
						+ "BrandNameAbilityCorpus.new.csv"), "utf-8"));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(originalFilePath
						+ "BrandNameLabel.new.txt"), "utf-8"));

		BrandNameExtraction bne = new BrandNameExtractionEnhancer(
				originalFilePath);

		Set<String> brandWords = new LinkedHashSet<String>();

		String line = null;
		int num = 0;
		while ((line = br.readLine()) != null) {
			if (line.charAt(0) == 0xFEFF) {
				line = line.substring(1, line.length());
			}
			System.out.println(++num);
			String[] seg = line.split(",");
			String oriLable = "";
			String realtype = "0";
			Double ability = 0.0;
			if (seg.length == 1) {
				oriLable = seg[0];
				String label = bne.getBrandWord(oriLable, realtype, ability);
				if (label != null) {
					brandWords.add(oriLable + "\t" + label);
				}
			} else if (seg.length == 2) {
				oriLable = seg[0];
				// (shop type: "0-无、1-个人、2-企业、3-个体经营、4-事业单位团体、5-政府、6-学校、7-媒体")
				switch (seg[1]) {
				case "":
					realtype = "0";
					break;
				case "个人":
					realtype = "1";
					break;
				case "企业":
					realtype = "2";
					break;
				case "个体经营":
					realtype = "3";
					break;
				case "事业单位团体":
					realtype = "4";
					break;
				case "政府":
					realtype = "5";
					break;
				case "学校":
					realtype = "6";
					break;
				case "媒体":
					realtype = "7";
					break;
				}

				String label = bne.getBrandWord(oriLable, realtype, ability);
				if (label != null) {
					brandWords.add(oriLable + "\t" + label);
				}
			} else if (seg.length == 3) {
				if (!seg[2].matches("^[.0-9]+")) {
					continue;
				}
				oriLable = seg[0];
				switch (seg[1]) {
				case "":
					realtype = "0";
					break;
				case "个人":
					realtype = "1";
					break;
				case "企业":
					realtype = "2";
					break;
				case "个体经营":
					realtype = "3";
					break;
				case "事业单位团体":
					realtype = "4";
					break;
				case "政府":
					realtype = "5";
					break;
				case "学校":
					realtype = "6";
					break;
				case "媒体":
					realtype = "7";
					break;
				}
				ability = Double.parseDouble(seg[2]);
				String label = bne.getBrandWord(oriLable, realtype, ability);
				if (label != null) {
					brandWords.add(oriLable + "\t" + label);
				}
			}
		}
		br.close();

		pw.println("total brand name number in 2000000: " + brandWords.size());
		for (String q : brandWords) {
			pw.println(q);
		}
		pw.close();
	}

}
