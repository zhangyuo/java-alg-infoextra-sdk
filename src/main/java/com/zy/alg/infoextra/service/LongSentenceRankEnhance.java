package com.zy.alg.infoextra.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;

import com.zy.alg.infoextra.seg.LongSentenceSegment;
import com.zy.alg.infoextra.utils.InitialDictionary;
import com.zy.alg.infoextra.utils.Sort;
import org.nlpcn.commons.lang.tire.domain.Forest;

import com.zbj.alg.infoextra.seg.LongSentenceSegment;
import com.zbj.alg.infoextra.textrank.TextRank;
import com.zbj.alg.infoextra.utils.InitialDictionary;
import com.zbj.alg.infoextra.utils.Sort;
import com.zbj.alg.infoextra.word2vec.Tokenizer;
import com.zbj.alg.infoextra.word2vec.VectorModelUTF;
import com.zbj.alg.infoextra.word2vec.Word2Vec;
import com.zbj.alg.infoextra.word2vec.WordNeuron;
import com.zbj.alg.nlp.crf.ModelParse;
import com.zbj.alg.nlp.server.CRF;
import com.zbj.alg.nlp.server.CRFEnhancer;
import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.UserDefineLibrary;
import com.zbj.alg.seg.service.ServiceSegModelEnhance;
import com.zbj.alg.seg.splitWord.IndexAnalysis;
import com.zbj.alg.seg.splitWord.ToAnalysis;
import com.zbj.alg.tag.model.VectorModel;


public  class LongSentenceRankEnhance implements LongSentenceRank{
	
	private static Forest zbjdic = new Forest();
	private static Forest serdic = new Forest();
	private static VectorModel vm = null;
	private static VectorModelUTF vm1 = null;
	//CRF模型解析
    private static ModelParse MP = new ModelParse();
	
    CRF crf = null;
    
	private Set<String> sertag = new HashSet<String>();
	
	public LongSentenceRankEnhance(String resourcePath)throws IOException{
		
		String vectorModelPath = resourcePath+"WordVec4GuideTag";
		String vectorModelPathUTF = resourcePath+"charvecmodel.txt";
		String ZBJDicPath = resourcePath + "zbjsmall.dic";
		String SerLibPath = resourcePath + "ServiceTagLibrary";
		String CRFmodelRealpath = resourcePath + "crfmodel.txt";
		
		ServiceSegModelEnhance.getInstance();
		InitialDictionary.insertZBJDic(zbjdic,ZBJDicPath);
		InitialDictionary.insertSerDic(serdic,SerLibPath);
		vm = VectorModel.loadFromFile(vectorModelPath);
		vm1 = VectorModelUTF.loadFromFile(vectorModelPathUTF);
		MP.parse(CRFmodelRealpath);
		crf = new CRFEnhancer();
		
		BufferedReader sr = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(resourcePath + "ServiceTagLibrary")), "utf-8"));
		String sline = null;
		while((sline = sr.readLine()) != null){
			String[] seg = sline.split("\t");
			if(seg.length >= 3 & (seg[2].contains("主题 ") || seg[2].contains("类型"))){
				String[] seg1 = seg[2].split("#");
				if(seg1.length == 1 & (seg1[0].equals("主题") || seg1[0].equals("类型"))){
					for(int i=3; i<seg.length; i++){
						String[] seg2 = seg[i].split("/");
						sertag.add(seg2[0]);
					}
				}
				else if(seg1[0].equals("主题") || seg1[0].equals("类型")){
					sertag.add(seg1[1]);
				}
			}
		}
		sr.close();
	}
	
	/**
	 * word embedding + crf
	 */
	public List<Entry<String, Double>> wordvecRank(String corpus) {
		
		float[] corpusvec = new float[vm.getVectorSize()];
		Map<String,Double> sentnceMap = new HashMap<String,Double>();
		Map<String,float[]> sentnceVecMap = new LinkedHashMap<String,float[]>();
		Result words = ToAnalysis.parse(corpus,UserDefineLibrary.FOREST,zbjdic);
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
		for(Entry<String, float[]> s:sentnceVecMap.entrySet()){
			if(s.getKey().split("#").length !=2){
				continue;
			}
			//double distance = 0.0;
			double number = Double.parseDouble(s.getKey().split("#")[0]);
			String sent = s.getKey().split("#")[1];
			Map<Term, String> centerWord = crf.getWordLabel(sent,zbjdic,MP);
			for(Entry<Term, String> w:centerWord.entrySet()){
				double para = 1.0;
				if(vm.getWordMap().containsKey(w.getKey().getName())){
					if(w.getValue().equals("TH")){
						//System.out.println(w.getKey());
						para = 5;
					}
					for(int i=0;i<vm.getVectorSize();i++){
						s.getValue()[i] += para*vm.getWordVector(w.getKey().getName())[i]/number;
						corpusvec[i] += para*vm.getWordVector(w.getKey().getName())[i]/sum;
						//distance += corpusvec[i] * s.getValue()[i];
					}
				}
			}
			
		}

		//int sentNumber = 0;
		for(Entry<String, float[]> s:sentnceVecMap.entrySet()){
			if(s.getKey().split("#").length !=2){
				continue;
			}
			//sentNumber++;
			String sent = s.getKey().split("#")[1];
			double distance = 0.0;
			for(int i=0;i<vm.getVectorSize();i++){
				distance += corpusvec[i] * s.getValue()[i];
			}
			sentnceMap.put(sent, /*Math.exp(-0.2*sentNumber)**/distance);
		}
		
		//rank sentence
		List<Map.Entry<String, Double>> sentnceMapSort = null;
        if (!sentnceMap.isEmpty()) {
        	sentnceMapSort = Sort.sortMap(sentnceMap);
        }
        if(sentnceMapSort == null){
        	sentnceMapSort = new ArrayList<Map.Entry<String, Double>>();
        	Map<String,Double> map = new HashMap<String,Double>();
        	map.put(corpus, 1.0);
        	for(Entry<String, Double> m:map.entrySet()){
        		sentnceMapSort.add(m);
        	}
        }
        return sentnceMapSort;
	}

	/**
	 * char embedding + crf + servicetaglibary
	 */
	public List<Entry<String, Double>> charvecRank(String corpus) {

		List<Entry<String, Double>> sentSortMap = null;
		if(corpus==null || corpus.equals("") || corpus.equals("null")){
			return sentSortMap;
		}
		//标点符号分句
		StringBuffer query = new StringBuffer();
		String[] seg = corpus.split("[^\u4e00-\u9fa5a-zA-Z0-9]");
		for(int i=0; i<seg.length; i++){
			String temp = seg[i].replaceAll("[^\u4e00-\u9fa5a-zA-Z0-9]", "");
			if(temp.equals("") 
					|| temp==null
					|| temp.length()<3
					|| temp.matches("^[0-9]+"))
				continue;
			query.append(temp.toLowerCase()+ "#");
		}

		Map<String,double[]> shortvec = new LinkedHashMap<String,double[]>();
		double[] longsent = new double[vm1.getVectorSize()];;
		String[] shortseg = query.toString().split("#");
		//短句
		for(int k=0; k<shortseg.length; k++){
			List<String> charseg = new LinkedList<String>();
			String[] charsegzh = shortseg[k].split("[^\u4e00-\u9fa5]");
			String[] charsegen = shortseg[k].split("[\u4e00-\u9fa5]");
			if(charsegen.length == 0){
				String[] charseg1 = charsegzh[0].split("");
				for(int m=0; m<charseg1.length; m++){
					if(charseg1[m].equals("")
							|| charseg1[m].equals("null")
							|| charseg1[m] == null){
						continue;
					}
					charseg.add(charseg1[m]);
				}
			}
			else{
				for(int i=0,j=0; i<charsegzh.length & j<charsegen.length; i++,j++){
					String[] charseg2 = charsegzh[i].split("");
					j += charsegzh[i].length();
					for(int m=0; m<charseg2.length; m++){
						if(charseg2[m].equals("")
								|| charseg2[m].equals("null")
								|| charseg2[m] == null){
							continue;
						}
						charseg.add(charseg2[m]);
					}
					if(j>=charsegen.length)
						break;
					if(charsegen[j].equals("")
							|| charsegen[j].equals("null")
							|| charsegen[j] == null){
						continue;
					}
					charseg.add(charsegen[j]);
					i += charsegen[j].length()-1;
					j--;
				}
			}

			double[] charvec = new double[vm1.getVectorSize()];
			
			//字向量
			for(int m=0; m<charseg.size(); m++){
				if(charseg.get(m).equals(""))
					continue;
				if(vm1.getWordMap().containsKey(charseg.get(m))){
					for(int n=0; n<vm1.getWordVector(charseg.get(m)).length; n++){
						charvec[n] += vm1.getWordVector(charseg.get(m))[n];
					}
				}
			}
			//重复分句处理
			if(shortvec.containsKey(shortseg[k])){
				for(int r=0; r<vm1.getVectorSize(); r++){
					shortvec.get(shortseg[k])[r] += charvec[r];
				}
				shortvec.put(shortseg[k], shortvec.get(shortseg[k]));
			}
			else{
				shortvec.put(shortseg[k], charvec);
			}
			
			//servicetaglibary
			Result words = IndexAnalysis.parse(shortseg[k],UserDefineLibrary.FOREST,zbjdic,serdic);
			double serweight = 1;
			for(Term w:words){
				if(sertag.contains(w.getName())){
					serweight++;
				}
			}
			
			//crf
			Map<Term, String> centerWord = crf.getWordLabel(shortseg[k],zbjdic,MP);
			double parse = 1;
			for(Entry<Term, String> q: centerWord.entrySet()){
				if(q.getValue().equals("TH")
						& q.getKey().getName().length() > 1
						& !q.getKey().getNatureStr().equals("nrfg")
						& !q.getKey().getNatureStr().equals("nrt")
						& !q.getKey().getNatureStr().equals("o")
						& !q.getKey().getNatureStr().equals("q")
						& !q.getKey().getNatureStr().equals("nba")
						& !q.getKey().getNatureStr().equals("t")
						& !q.getKey().getNatureStr().equals("ns")
						& !q.getKey().getNatureStr().equals("nr")
						& !q.getKey().getNatureStr().equals("m")
						& !q.getKey().getNatureStr().equals("j")
						& !q.getKey().getNatureStr().equals("z")
						& !q.getKey().getNatureStr().equals("a")
						& !q.getKey().getNatureStr().equals("r")
						& !q.getKey().getNatureStr().equals("l")
						& !q.getKey().getNatureStr().equals("d")
						& !q.getKey().getNatureStr().equals("mq")
						& !q.getKey().getNatureStr().equals("s")
						& !q.getKey().getNatureStr().equals("i")
						& !q.getKey().getNatureStr().equals("ad")
						& !q.getKey().getNatureStr().equals("nsg")
						& !q.getKey().getNatureStr().equals("nrf")
						& !q.getKey().getNatureStr().equals("f")
						& !q.getKey().getNatureStr().equals("nnd")
						& !q.getKey().getNatureStr().equals("nf")){
					parse += 3;
				}
			}
			
			for(int i=0; i<vm1.getVectorSize(); i++){
				longsent[i] += (0.7*parse*charvec[i] + 0.3*serweight*charvec[i])
						/shortseg.length;
			}
		}
		
		//cosin
		int sentNumber = 0;
		Map<String, Double> sentMap = new HashMap<String, Double>();
		for(Entry<String,double[]> v: shortvec.entrySet()){
			sentNumber++;
			Double distance = 0.0;
			for(int i=0; i<v.getValue().length; i++){
				distance += longsent[i]*v.getValue()[i];
			}
			sentMap.put(v.getKey(), Math.exp(-0.1*sentNumber)*distance);
		}
				
		//rank
		sentSortMap = Sort.sortMap(sentMap);
		
		return sentSortMap;
	}
	
	/**
	 * char embedding + crf + servicetaglibary 单句为整体处理
	 */
	public List<Entry<String, Double>> singleCharvecRank(String corpus) {
		
		List<Entry<String, Double>> sentSortMap = null;
		if(corpus==null || corpus.equals("") || corpus.equals("null")){
			return sentSortMap;
		}
		
		Word2Vec wv = new Word2Vec.Factory()
		.setMethod(Word2Vec.Method.Skip_Gram).setNumOfThread(10)
		.setVectorSize(200).setFreqThresold(1).build();
		Map<String, WordNeuron> neuronMap = null;
		StringBuffer query = new StringBuffer();
		//标点符号分句
		String[] seg = corpus.split("[^\u4e00-\u9fa5a-zA-Z0-9]");
		for(int i=0; i<seg.length; i++){
			String temp = seg[i].replaceAll("[^\u4e00-\u9fa5a-zA-Z0-9]", "");
			if(temp.equals("") 
					|| temp==null
					|| temp.length()<3
					|| temp.matches("^[0-9]+"))
				continue;
			query.append(temp.toLowerCase()+ "#");
		}
		//Word2vec
		wv.readTokens(new Tokenizer(query.toString(), ""));
		neuronMap = wv.training();
		
		Map<String,double[]> shortvec = new HashMap<String,double[]>();
		double[] longsent = new double[wv.getVectorSize()];;
		String[] shortseg = query.toString().split("#");
		//短句
		for(int k=0; k<shortseg.length; k++){
			String[] charseg = shortseg[k].split("");
			double[] charvec = new double[wv.getVectorSize()];
			//字向量
			for(int m=0; m<charseg.length; m++){
				if(charseg[m].equals(""))
					continue;
				for(int n=0; n<neuronMap.get(charseg[m]).vector.length; n++){
					charvec[n] += neuronMap.get(charseg[m]).vector[n];
				}
			}
			shortvec.put(shortseg[k], charvec);
			
			//crf
			Map<Term, String> centerWord = crf.getWordLabel(shortseg[k],zbjdic,MP);
			double parse = 1;
			for(Entry<Term, String> q: centerWord.entrySet()){
				if(q.getValue().equals("TH")){
					parse += 3;
				}
			}
			for(int i=0; i<wv.getVectorSize(); i++){
				longsent[i] += parse*charvec[i];
			}
		}
		
		//cosin
		Map<String, Double> sentMap = new HashMap<String, Double>();
		for(Entry<String,double[]> v: shortvec.entrySet()){
			Double distance = 0.0;
			for(int i=0; i<v.getValue().length; i++){
				distance += longsent[i]*v.getValue()[i];
			}
			sentMap.put(v.getKey(), distance);
		}
		
		//rank
		sentSortMap = Sort.sortMap(sentMap);
		
		return sentSortMap;
	}

	/**
	 * Textrank long sentence
	 */
	public List<Entry<String, Double>> textRankLongSent(String longquery) {

		List<String> segSentence = LongSentenceSegment.longSentSpiltPunctuation(longquery);
		List<List<String>> sentenceDocs = new LinkedList<List<String>>();
		List<Entry<String,Double>> listSentRank = new LinkedList<Entry<String,Double>>();
		Map<String,Double> sentRank = new LinkedHashMap<String,Double>();
		
		for(int i=0; i<segSentence.size(); i++){
			Result words = ToAnalysis.parse(segSentence.get(i),UserDefineLibrary.FOREST,zbjdic);
			List<String> docs = new LinkedList<String>();
			for(Term q: words){
				if(!q.getNatureStr().equals("w")
						& !q.getName().contains("/")
						& q.toString().contains("/")
						& q.getName().length()>1
						& q.getNatureStr().matches("^[n]+$|^[v]+$|^[g]+$|^[en]+$")){
					docs.add(q.getName());
				}
			}
			sentenceDocs.add(docs);
		}
		
		TreeMap<Double, Integer> top = new TreeMap<Double, Integer>();
		TextRank textRank = new TextRank(sentenceDocs);
		top = textRank.textRankSentence();
		
		for(Entry<Double, Integer> t: top.entrySet()){
			sentRank.put(segSentence.get(t.getValue()), t.getKey());
		}
		
		for(Entry<String,Double> s: sentRank.entrySet()){
			listSentRank.add(s);
		}
		
		return listSentRank;
	}

}
