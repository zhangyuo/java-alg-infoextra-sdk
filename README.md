# 关键信息提取项目说明文档

## 接口说明

1.品牌名提取

* BrandNameExtraction.getBrandWord(String oriLabel, String realtype, Double ability)方法
 * 方法描述：根据输入店铺名或者公司名提取品牌名字
 * 输入参数：
 
	| 名称 | 含义 | 备注 |
	|:-------:|:-------:|:-------:|
	|oriLabel|店铺名|text|
	|realtype|店铺类型|0-无、1-个人、2-企业、3-个体经营、4-事业单位团体、5-政府、6-学校、7-媒体|
	|ability|店铺能力值|>0.0|
 * 返回值说明：String类
 
2.关键词提取

* KeyWord.extractKeyword(String corpus, int wordNum)方法
 * 方法描述：根据输入文本提取文本的关键词-TextRank
 * 输入参数：
 
 	| 名称 | 含义 | 备注 |
 	|:-------:|:-------:|:-------:|
 	|corpus|输入文本|无|
 	|wordNum|返回词个数|无|
 * 返回值说明：List<Map.Entry<String, Float>>
 
3.关键句排序

* LongSentenceRank.wordvecRank(String corpus)方法
 * 方法描述：根据输入文本排序文本关键句-词向量
 * 输入参数：
 
 	| 名称 | 含义 | 备注 |
 	|:-------:|:-------:|:-------:|
 	|corpus|输入文本|无|
 * 返回值说明：List<Entry<String, Double>>
 
* LongSentenceRank.textRankLongSent(String corpus)方法
 * 方法描述：根据输入文本排序文本关键句-TextRank
 * 输入参数：
 
 	| 名称 | 含义 | 备注 |
 	|:-------:|:-------:|:-------:|
 	|corpus|输入文本|无|
 * 返回值说明：List<Entry<String, Double>>

4.地理位置信息提取
 
* PositionExtraction.uniPosiExtra(String corpus)方法
 * 方法描述：根据输入文本提取通用地理位置信息
 * 输入参数：
 
 	| 名称 | 含义 | 备注 |
 	|:-------:|:-------:|:-------:|
 	|querys|输入文本|无|
 * 返回值说明：List<OutputPosiInfo>
 
## 模型自动训练

1.shop品牌词自动更新

* ShopTrain.init(String rawPath,String resourcePath,String outputPath);
* ShopTrain.train();
* ShopTrain.check();
 * 分别对品牌词自动更新进行初始化、训练和检查