package com.zy.alg.infoextra.library.auto;

/**
 * 
 * create by zhangyu on 2018/5/15
 *
 */
public interface ShopTrain {
	
	/**
	 * 模型自动更新初始化
	 * @param rawPath
	 * @param resourcePath
	 * @param outputPath
	 */
	public void init(String rawPath,String resourcePath,String outputPath);
	/**
	 * 模型自动更新训练方法
	 * @return
	 */
	public boolean train();
	/**
	 * 模型自动更新校验
	 * @return
	 */
	public boolean check();

}
