package com.zbj.alg.infoextra.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zbj.alg.infoextra.utils.Sort;

public class BrandNameExtractionEnhancer implements BrandNameExtraction {

	private static final Logger logger = LoggerFactory
			.getLogger(BrandNameExtractionEnhancer.class);
	/**
	 * 常见英语关键词
	 */
	private static Set<String> EnglishWords = new HashSet<String>();
	/**
	 * 品牌词提取的过滤词
	 */
	private static Set<String> FilteringKeyWords = new HashSet<String>();
	/**
	 * 提取品牌词的关键词
	 */
	private static Set<String> BrandKeyWords = new HashSet<String>();
	/**
	 * 地名
	 */
	private static Set<String> posiWords = new HashSet<String>();

	public BrandNameExtractionEnhancer(String resourcePath) {

		if (resourcePath != null) {
			String EnglishDicPath = resourcePath + "EnglishDic.txt";
			String FilteringKeyWordPath = resourcePath + "FilteringKeyWord.txt";
			String PositionPath = resourcePath + "AreaTagLibrary";
			String BrandKeyWordPath = resourcePath + "BrandKeyWord.txt";

			BufferedReader br;
			try {
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(EnglishDicPath), "utf-8"));
				// 常见英语关键词加载
				String line = null;
				while ((line = br.readLine()) != null) {
					String[] seg = line.split("\t");
					if (seg.length == 2) {
						String[] natureSeg = seg[1].split("&");
						int flag = 0;
						for (int i = 0; i < natureSeg.length; i++) {
							if (natureSeg[i].equals("n")
									|| natureSeg[i].contains("v")) {
								flag++;
								break;
							}
						}
						if (flag > 0 & seg[0].length() > 2) {
							EnglishWords.add(seg[0].trim().toLowerCase());
						}
					}
				}
				br.close();
				logger.info("EnglishDic.txt load success!");
			} catch (IOException e) {
				logger.error("EnglishDic.txt load failed!", e);
			}

			BufferedReader bf;
			try {
				bf = new BufferedReader(new InputStreamReader(
						new FileInputStream(FilteringKeyWordPath), "utf-8"));
				// 品牌词过滤词加载
				String fline = null;
				while ((fline = bf.readLine()) != null) {
					if (fline.length() > 1) {
						FilteringKeyWords.add(fline.trim().toLowerCase());
					}
				}
				bf.close();
				logger.info("FilteringKeyWord.txt load success!");
			} catch (IOException e) {
				logger.error("FilteringKeyWord.txt load failed!", e);
			}

			BufferedReader bw;
			try {
				bw = new BufferedReader(new InputStreamReader(
						new FileInputStream(BrandKeyWordPath), "utf-8"));
				// 品牌词的关键词加载
				String wline = null;
				while ((wline = bw.readLine()) != null) {
					if (wline.length() > 1) {
						BrandKeyWords.add(wline.trim().toLowerCase());
					}
				}
				bw.close();
				logger.info("BrandKeyWord.txt load success!");
			} catch (IOException e) {
				logger.error("BrandKeyWord.txt load failed!", e);
			}

			BufferedReader pr;
			try {
				pr = new BufferedReader(new InputStreamReader(
						new FileInputStream(PositionPath), "utf-8"));
				// 地名加载
				String pline = null;
				while ((pline = pr.readLine()) != null) {
					String[] seg = pline.split("\t");
					if (seg.length >= 2) {
						String[] place = seg[1].split("&");
						if (place.length == 3) {
							String[] seg1 = place[0].split("/", 2);
							String pl1 = seg1[0] + "/" + seg1[1];
							posiWords.add(pl1);
							String pl2 = place[1];
							if (seg.length >= 4 && seg[2].equals("二级地名全名查询")) {
								for (int i = 3; i < seg.length; i++) {
									posiWords.add(seg[i]);
								}
							} else {
								posiWords.add(pl2);
							}
							String pl3 = place[2];
							if (seg.length >= 4 && seg[2].equals("三级地名全名查询")) {
								for (int i = 3; i < seg.length; i++) {
									posiWords.add(seg[i]);
								}
							} else {
								posiWords.add(pl3);
							}
						}
					}
				}
				pr.close();
				logger.info("AreaTagLibrary load success!");
			} catch (IOException e) {
				logger.error("AreaTagLibrary load failed!", e);
			}
		} else {
			logger.debug("resource path is empty, brand word extracton model initialize failed!");
		}
	}

	public String getBrandWord(String oriLabel, String realtype, Double ability) {

		Map<Integer, String> posi = new LinkedHashMap<Integer, String>();
		Map<Integer, String> orderfitKeyWords = new LinkedHashMap<Integer, String>();

		String brandName = null;
		String label = oriLabel.toLowerCase();

		Pattern pp = Pattern.compile("([a-z]+)_([0-9]+)_([0-9a-z]+)");
		Matcher m = pp.matcher(label);
		String strFlag = "";
		while (m.find()) {
			strFlag = m.group();
		}

		if (label.equals("") || label.equals("null") || label == null
				|| label.equals("\"\"") || !strFlag.equals("")) {
			return null;
		}

		// ** Shop能力限制 **//
		if ((!realtype.equals("0") && !realtype.equals("1")) || ability > 2000) {

			// ******** ONLY EN and Num ********//
			if (label.matches("^[^\u4e00-\u9fa5]+$")) {
				if (!label.contains("_") && label.matches("^[a-z]{2,}$")) {
					brandName = pureEnNumFilter(label); //
				}
			} else {
				// ******** ONLY CN (most important) ********//
				if (label.matches("^[^a-z]+$")) {
					label = label.replaceAll("[\\pP\\pS\\pZ0-9]", "");
					posi = initPosi(label);
					orderfitKeyWords = initFitKeyWord(label);
					if (label.length() > 1) {
						Boolean flag = false;
						if (orderfitKeyWords.size() == 0) {
							for (String q : BrandKeyWords) {
								if (label.contains(q)) {
									flag = true;
									break;
								}
							}
						}
						if (flag || orderfitKeyWords.size() != 0) {
							brandName = pureCnFilter(label, posi,
									orderfitKeyWords); //
						}
					}
				} else {
					// ******** contains CN and EN ********//
					posi = initPosi(label);
					for (String q : posi.values()) {
						label = label.replace(q, "");
					}
					label = label.replaceAll("[\\pP\\pS\\pZ&&[^ ]]", "");
					orderfitKeyWords = initFitKeyWord(label);
					if (label.length() > 1) {
						Boolean flag = false;
						if (orderfitKeyWords.size() == 0) {
							for (String q : BrandKeyWords) {
								if (label.contains(q)) {
									flag = true;
									break;
								}
							}
						}
						if (flag || orderfitKeyWords.size() != 0) {
							brandName = bothCnEnFilter(label, orderfitKeyWords); //
						}
					}
				}
			}

		}
		// ** 无Shop能力限制 **//
		else {
			// ** 关键词限制过滤 **//
			Boolean flag = false;
			for (String q : BrandKeyWords) {
				if (label.contains(q)) {
					flag = true;
					break;
				}
			}

			if (flag) {
				// ******** ONLY CN (most important) ********//
				if (label.matches("^[^a-z]+$")) {
					label = label.replaceAll("[\\pP\\pS\\pZ0-9]", "");
					posi = initPosi(label);
					orderfitKeyWords = initFitKeyWord(label);
					if (label.length() > 1 && orderfitKeyWords.size() != 0) {
						brandName = pureCnFilter(label, posi, orderfitKeyWords); //
					}
				} else {
					// ******** contains CN and EN ********//
					posi = initPosi(label);
					for (String q : posi.values()) {
						label = label.replace(q, "");
					}
					label = label.replaceAll("[\\pP\\pS\\pZ&&[^ ]]", "");
					orderfitKeyWords = initFitKeyWord(label);
					if (label.length() > 1 && orderfitKeyWords.size() != 0) {
						brandName = bothCnEnFilter(label, orderfitKeyWords); //
					}
				}
			}

		}

		return brandName;
	}

	/**
	 * init shop position
	 * 
	 * @param label
	 * @return
	 */
	private Map<Integer, String> initPosi(String label) {

		Map<Integer, String> oriPosi = new HashMap<Integer, String>();
		for (String p : posiWords) {
			String[] seg3 = p.split("/");
			if (label.contains(p.replace("/", ""))) {
				int olen = label.indexOf(p.replace("/", ""));
				oriPosi.put(olen, p.replace("/", ""));
			} else if (label.contains(seg3[0])) {
				int olen = label.indexOf(seg3[0]);
				oriPosi.put(olen, seg3[0]);
			}
		}

		Map<Integer, String> posi = new LinkedHashMap<Integer, String>();
		posi = Sort.sortIntMapChange(oriPosi);

		return posi;
	}

	/**
	 * init filtering key word
	 * 
	 * @param label
	 * @return
	 */
	private Map<Integer, String> initFitKeyWord(String label) {

		Map<Integer, String> fitKeyWords = new HashMap<Integer, String>();
		for (String b : FilteringKeyWords) {
			if (label.contains(b)) {
				Integer forelen = label.indexOf(b);
				Integer backlen = label.lastIndexOf(b);
				if (forelen == backlen) {
					if (fitKeyWords.containsKey(forelen)) {
						if (fitKeyWords.get(forelen).length() < b.length()) {
							fitKeyWords.put(forelen, b);
						}
					} else {
						fitKeyWords.put(forelen, b);
					}
				} else {
					if (fitKeyWords.containsKey(forelen)) {
						if (fitKeyWords.get(forelen).length() < b.length()) {
							fitKeyWords.put(forelen, b);
						}
					} else {
						fitKeyWords.put(forelen, b);
					}
					if (fitKeyWords.containsKey(backlen)) {
						if (fitKeyWords.get(backlen).length() < b.length()) {
							fitKeyWords.put(backlen, b);
						}
					} else {
						fitKeyWords.put(backlen, b);
					}
				}
			}
		}

		Map<Integer, String> orderfitKeyWords = new LinkedHashMap<Integer, String>();
		orderfitKeyWords = Sort.sortIntMapChange(fitKeyWords);

		return orderfitKeyWords;
	}

	/**
	 * pure English and number filtering
	 * 
	 * @param label
	 * @return
	 */
	public static String pureEnNumFilter(String label) {

		String brandName = null;

		// ** 网站 **//
		if (label.contains("www.")) {
			label = label.replace("www.", "");
			int len = label.indexOf(".");
			label = label.substring(0, len);
			brandName = label; //
			System.out.println(brandName);
		}
		// ** 其他 **//
		else {
			label = label.replaceAll("[\\pP\\pS\\pZ]", " ");
			String[] seg5 = label.split(" ");
			int eFlag = 0;
			for (int e = 0; e < seg5.length; e++) {
				if (EnglishWords.contains(seg5[e])) {
					eFlag++;
				}
			}
			if (eFlag == seg5.length) {
				brandName = label.trim(); //
			}
		}

		return brandName;
	}

	/**
	 * pure Chinese filtering
	 * 
	 * @param label
	 * @param oriLabel
	 * @param posi
	 * @param orderfitKeyWords
	 * @return
	 */
	public static String pureCnFilter(String label, Map<Integer, String> posi,
			Map<Integer, String> orderfitKeyWords) {

		String brandName = null;
		String foreSubString = "";
		int flag = 0;
		for (Map.Entry<Integer, String> o : orderfitKeyWords.entrySet()) {
			flag++;
			if (orderfitKeyWords.size() == 1) {
				foreSubString = label.substring(0, o.getKey());
				break;
			} else {
				if (flag == 1) {
					foreSubString = label.substring(0, o.getKey());
				}
			}
		}
		// 去地名词
		for (String p : posi.values()) {
			foreSubString = foreSubString.replace(p, "");
		}
		// 提取品牌词
		if (foreSubString.length() >= 2) {
			if (foreSubString.substring(foreSubString.length() - 1).equals("的")
					|| foreSubString.substring(foreSubString.length() - 1)
							.equals("の")
					|| foreSubString.substring(foreSubString.length() - 1)
							.equals("之")) {
				String oriforeSubString = foreSubString;
				foreSubString = foreSubString.substring(0,
						foreSubString.length() - 1);
				if (foreSubString.length() < 2) {
					brandName = oriforeSubString; //
				} else {
					brandName = foreSubString; //
				}
			} else {
				brandName = foreSubString; // ** 最多
			}
		}

		// 过滤特殊词
		if (brandName != null
				&& (brandName.contains("丨") || brandName.contains("灬") || brandName
						.contains("氵"))) {
			brandName = brandName.replaceAll("[|灬氵]", "");
		}
		if (brandName != null && brandName.trim().length() > 1) {
			if (brandName.matches("^[^\u4e00-\u9fa5]+$")) {
				if (brandName.matches("[a-z]+")
						&& (brandName.length() >= 4 || EnglishWords
								.contains(brandName.trim()))) {
					return brandName.trim();
				} else {
					return null;
				}
			} else {
				return brandName.trim();
			}
		} else {
			return null;
		}

	}

	/**
	 * both contain Chinese and English filtering
	 * 
	 * @param label
	 * @param orderfitKeyWords
	 * @param ability
	 * @return
	 */
	public static String bothCnEnFilter(String label,
			Map<Integer, String> orderfitKeyWords) {

		String brandName = null;
		String foreSubString = "";
		int flag = 0;
		for (Map.Entry<Integer, String> o : orderfitKeyWords.entrySet()) {
			flag++;
			if (orderfitKeyWords.size() == 1) {
				foreSubString = label.substring(0, o.getKey());
				break;
			} else {
				if (flag == 1) {
					foreSubString = label.substring(0, o.getKey());
				}
			}
		}
		if (foreSubString.length() >= 2) {
			brandName = foreSubString; //
		}

		// 过滤
		if (brandName != null && brandName.trim().length() > 1) {
			if (brandName.matches("^[^\u4e00-\u9fa5]+$")) {
				if (brandName.matches("[a-z]+")
						&& (brandName.length() >= 4 || EnglishWords
								.contains(brandName.trim()))) {
					return brandName.trim();
				} else {
					return null;
				}
			} else {
				return brandName.trim();
			}
		} else {
			return null;
		}
	}

}
