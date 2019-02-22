package com.zbj.alg.infoextra.instance;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.nlpcn.commons.lang.tire.domain.Forest;

import com.zbj.alg.infoextra.utils.InitialDictionary;
import com.zbj.alg.infoextra.utils.Stop;
import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.UserDefineLibrary;
import com.zbj.alg.seg.service.ServiceSegModelEnhance;
import com.zbj.alg.seg.splitWord.ToAnalysis;

	
public class GetInstanceWord {
	
	private static Forest zbjdic = new Forest();
	
	public GetInstanceWord(String resourcePath) throws IOException {
		
		String ZBJDicPath = resourcePath + "zbjsmall.dic";
		ServiceSegModelEnhance.getInstance();
		InitialDictionary.insertZBJDic(zbjdic,ZBJDicPath);
	}
	
	/**
	 * 提取实体词
	 * @param query
	 * @return
	 */
	public List<Term> getInstWords(String query) {
		//词性过滤
		Result sentWords = ToAnalysis.parse(query,UserDefineLibrary.FOREST,zbjdic);
		List<Term> realSentWords = new LinkedList<Term>();
		for(Term t: sentWords){
			if(!Stop.stopNature.contains(t.getNatureStr())){
				realSentWords.add(t);
			}
		}
		
		return realSentWords;
	}

}
