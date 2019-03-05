package com.zy.alg.infoextra.update.util;

import java.io.File;

public class TrainFileUtil {
	
	/**
	 * 获取文件大小
	 * @param file
	 * @return
	 */
	public static long GetFileSize(File file){
		long fileS = 0;
	    if(file.exists() && file.isFile()) {
	    	fileS = file.length();
	    }
	    return fileS;
	}

}
