package com.zy.alg.infoextra.service;

import java.util.List;

import com.zy.alg.infoextra.utils.OutputPosiInfo;

/**
 * Geographical location extract
 * 
 * @author zhangyu
 *
 */
public interface PositionExtraction {

	/**
	 * universal position infomation extract
	 * 
	 * @param querys
	 *            (text corpus)
	 * @return
	 */
	List<OutputPosiInfo> uniPosiExtra(String corpus);
	/**
	 * task localize position recognition
	 * 
	 * @param querys
	 *            (task corpus)
	 * @param category
	 *            (task category)
	 * @param origPos
	 *            (task publish location:province&city&district)
	 * @return
	 */
	 List<OutputPosiInfo> taskPosiRecog(String corpus, String category, String origPos);
	 /**
	  * policy position infomation extract
	  * @param corpus
	  * @return
	  */
	 List<OutputPosiInfo> policyRegExtra(String corpus);
}
