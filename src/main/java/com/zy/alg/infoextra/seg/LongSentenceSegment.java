package com.zy.alg.infoextra.seg;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nlpcn.commons.lang.tire.domain.Forest;

import com.zbj.alg.infoextra.utils.InitialDictionary;
import com.zbj.alg.infoextra.word2vec.VectorModelUTF;
import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.UserDefineLibrary;
import com.zbj.alg.seg.service.ServiceSegModelEnhance;
import com.zbj.alg.seg.splitWord.ToAnalysis;
import com.zbj.alg.tag.model.VectorModel;

public class LongSentenceSegment {
	
	private static Forest zbjdic = new Forest();
	private static VectorModel vm = null;
	private static VectorModelUTF vm1 = null;
	private static String punctuation = "[，。？！；“”、,?!;\"]";
	
	/**
	 * 初始化
	 * @param resourcePath
	 * @throws IOException
	 */
	public LongSentenceSegment(String resourcePath) throws IOException{
		
		String vectorModelPath = resourcePath+"WordVec4GuideTag";
		String vectorModelPathUTF = resourcePath+"charvecmodel.txt";
		String ZBJDicPath = resourcePath + "zbjsmall.dic";
		
		ServiceSegModelEnhance.getInstance();
		InitialDictionary.insertZBJDic(zbjdic,ZBJDicPath);
		vm = VectorModel.loadFromFile(vectorModelPath);
		vm1 = VectorModelUTF.loadFromFile(vectorModelPathUTF);
	}
	
	/**
	 * 长句，标点符号分句
	 * @param longquery
	 * @return
	 */
	public static List<String> longSentSpiltPunctuation(String longquery) {
		
		String[] segSentence = longquery.split(punctuation);
		List<String> segSent = new LinkedList<String>();
		for(int i=0; i<segSentence.length; i++){
			if(segSentence[i].length() >= 2){
				String realText = segSentence[i].trim();
				segSent.add(realText);
			}
		}
		
		return segSent;
	}
	
	/**
	 * 长句，词向量分句
	 * @param longquery
	 * @return
	 */
	public Map<String,float[]> longSentSegWordVec(String longquery) {
		
		Map<String,float[]> sentnceVecMap = new LinkedHashMap<String,float[]>();
		Result words = ToAnalysis.parse(longquery,UserDefineLibrary.FOREST,zbjdic);
		StringBuffer sentence = new StringBuffer();
		float[] sentenceVec = new float[vm.getVectorSize()];
		double sum = 1;
		double num = 1;
		for(Term w:words){
			if(w.getNatureStr().equals("w")
					|| !w.toString().contains("/")
					|| num == 10){
				sentnceVecMap.put(num+"#-"+sentence.toString(), sentenceVec);
				num =1;
				sentence = new StringBuffer();
				sentenceVec = new float[vm.getVectorSize()];
			}
			if(vm.getWordMap().containsKey(w.getName())){
				num++;
				sum++;
				sentence.append(w.getName());
			}
		}
		sentnceVecMap.put(num+"#"+sentence.toString(), sentenceVec);
		
		return sentnceVecMap;
	}
	
	/**
	 * 长句，字向量分句
	 * @param longquery
	 * @return
	 */
	public Map<String,float[]> longSentSegCharVec(String longquery) {
		
		Map<String,float[]> sentnceVecMap = new LinkedHashMap<String,float[]>();
		Result words = ToAnalysis.parse(longquery,UserDefineLibrary.FOREST,zbjdic);
		StringBuffer sentence = new StringBuffer();
		float[] sentenceVec = new float[vm1.getVectorSize()];
		double sum = 1;
		double num = 1;
		for(Term w:words){
			if(w.getNatureStr().equals("w")
					|| !w.toString().contains("/")
					|| num == 20){
				sentnceVecMap.put(num+"#-"+sentence.toString(), sentenceVec);
				num =1;
				sentence = new StringBuffer();
				sentenceVec = new float[vm1.getVectorSize()];
			}
			if(vm1.getWordMap().containsKey(w.getName())){
				num++;
				sum++;
				sentence.append(w.getName());
			}
		}
		sentnceVecMap.put(num+"#"+sentence.toString(), sentenceVec);
		
		return sentnceVecMap;
	}


	/**
	 * 正则：非中文字目数字，长句分句
	 * @param longquery
	 * @return
	 */
	public String [] longSentSpiltRegex(String longquery) {
		
		StringBuffer query = new StringBuffer();
		String[] seg = longquery.split("[^\u4e00-\u9fa5a-zA-Z0-9]");
		for(int i=0; i<seg.length; i++){
			String temp = seg[i].replaceAll("[^\u4e00-\u9fa5a-zA-Z0-9]", "");
			if(temp.equals("") 
					|| temp==null
					|| temp.length()<3
					|| temp.matches("^[0-9]+"))
				continue;
			query.append(temp.toLowerCase()+ "#");
		}
		
		String[] shortseg = query.toString().split("#");
		
		return shortseg;
	}
	
}
