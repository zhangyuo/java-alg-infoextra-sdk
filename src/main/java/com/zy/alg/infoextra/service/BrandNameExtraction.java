package com.zy.alg.infoextra.service;

/**
 * shop or company brand name extraction
 * @author zhangyu
 *
 */
public interface BrandNameExtraction {
	
	/**
	 * search index demand: shop brand name extration
	 * @param oriLabel (text)
	 * @param realType (shop type: "0-无、1-个人、2-企业、3-个体经营、4-事业单位团体、5-政府、6-学校、7-媒体")
	 * @param ability (shop ability value >= 0.0)
	 * @return brand word
	 */
	String getBrandWord(String oriLabel, String realType, Double ability);

}
