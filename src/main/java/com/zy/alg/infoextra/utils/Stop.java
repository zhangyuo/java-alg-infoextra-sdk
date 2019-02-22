package com.zy.alg.infoextra.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

public class Stop {
	
	/**
	 * 三元组停用上位词
	 */
	public static Set<String> stopSuperword = new HashSet<String>(){{
		add("设计");add("策划");add("推广");add("制作");add("开发");add("服务");
		add("定制");add("简介");add("国际");add("逼格");add("效果");add("大气");
		add("位置");add("价位");add("注册");add("部门");add("极简");add("方案");
	}};
	
	/**
	 * 通用停用词性
	 */
	public static Set<String> stopNature = new HashSet<String>(){{
		add("o");//拟声词(哈哈/o 、隆隆/o)
		add("t");//时间词(1997年/t  3月/t  19日/t  下午/t  2时/t  18分/t)
		add("tj");//时语素(３日/t  晚/tg，尊重/v  现/tg  执政/vn  )
		add("j");//简称略语(德、文教)
		add("z");//状态词(短短、扎扎实实)
		add("r");//代词(本报/r、本/r 、各/r )
		add("l");//习用语(少年儿童/l 落到实处/l)
		add("ag");//(私/ag 酷/ag)
		add("a");//形容词(重要/a 、美丽/a、抽象/a )
		add("d");//副词(进一步/d  )
		add("ad");//副形词(积极/ad、 易/ad )
		add("an");//名形词(外交/n  和/c  安全/an， 麻烦/an)
		add("p");//介词(对/p  以/p  为/p   把/p)
		add("m");//数词(一个/m 、一些/m)
		add("q");//量词(首/m  批/q 、一/m  年/q)
		add("mq");//数词(三/m 个/q、10/m  公斤/q、一个/m 、一些/m)
		add("s");//处所词(家里/s、 西部/s)
		add("i");//成语(一言一行/i  义无反顾/i  )
		add("f");//方位词(眼睛/n  里/f  )
		add("u");//助词(了/u 的/u)
		add("ul");//(了/ul)
		add("uj");
		add("udh");//(的话/udh)
		add("ud");//(得/ud)
		add("c");//连词(与/c )
		add("w");//标点符号
		add("k");//后接成分(取名/v 者/k)
		add("b");//区别词(女/b 司机/n，  金/b 手镯/n，  慢性/b 胃炎/n，总/b  占地/v  面积/n)
		add("dg");//(善/dg)
		add("null");
	}};
	
	/**
	 * 三元组停用下位词
	 */
	public static Set<String> stopSubword = new HashSet<String>(){{
		add("项目");add("功能");add("背景");add("信息");
	}};
	
	/**
	 * 通用停用词
	 */
	public Set<String> stopUniversalWord = new HashSet<String>();
	
	public Stop(String path ) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(path), "utf-8"));
		String line = null;
		while((line = br.readLine()) != null){
			stopUniversalWord.add(line);
		}
		br.close();
	}

}
