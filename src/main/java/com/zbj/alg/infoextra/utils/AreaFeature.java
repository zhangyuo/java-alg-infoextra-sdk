package com.zbj.alg.infoextra.utils;


public class AreaFeature {
	
	private String province;
	private String city;
	private String district;
	private String provinceAbbr;
	private int level;				// recognize the level among province, city, district
	private int sentNumber;			// the segment sentence number of current area in a long sentence
	private int strPosiNumber;		// the emerging position of current area in a sentence
	private int flagNumber;			// the emerging number of area name among province, city, district
	private int levelNameLen;		// the length of max level position name in a sentence
	private double score;
	
	public String getProvince(){
		return province;
	}
	
	public void setProvince(String province){
		this.province = province;
	}
	
	public String getCity(){
		return city;
	}
	
	public void setCity(String city){
		this.city = city;
	}
	
	public String getDistrict(){
		return district;
	}
	
	public void setDistrict(String district){
		this.district = district;
	}
	
	public String getProvinceAbbr(){
		return provinceAbbr;
	}
	
	public void setProvinceAbbr(String provinceAbbr){
		this.provinceAbbr = provinceAbbr;
	}
	
	public int getSentNumber(){
		return sentNumber;
	}
	
	public void setSentNumber(int sentNumber){
		this.sentNumber = sentNumber;
	}
	
	public int getLevel(){
		return level;
	}
	
	public void setLevel(int level){
		this.level = level;
	}
	
	public int getStrPosiNumber(){
		return strPosiNumber;
	}
	
	public void setStrPosiNumber(int strPosiNumber){
		this.strPosiNumber = strPosiNumber;
	}
	
	public int getFlagNumber(){
		return flagNumber;
	}
	
	public void setFlagNumber(int flagNumber){
		this.flagNumber = flagNumber;
	}
	
	public int getLevelNameLen(){
		return levelNameLen;
	}
	
	public void setLevelNameLen(int levelNameLen){
		this.levelNameLen = levelNameLen;
	}
	
	public double getScore(){
		return score;
	}
	
	public void setScore(double score){
		this.score = score;
	}
	
	@Override
	public boolean equals(Object obj){
		AreaFeature af = (AreaFeature) obj;
		if(city == null){
			city = "null";
		}
		if(district == null){
			district = "null";
		}
		if(af.city == null){
			af.city = "null";
		}
		if(af.district == null){
			af.district = "null";
		}
		if(province.equals(af.province)
				&& city.equals(af.city)
				&& district.equals(af.district)
				&& sentNumber == af.sentNumber
				&& strPosiNumber == af.strPosiNumber
				&& flagNumber == af.flagNumber){
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result +((province == null) ? 1 : province.hashCode());
		return result;
	}
	
	@Override
	public String toString(){
		return "province="+province+"#"+"city="+city+"#"+"district="+district+"#"+"level="+level+
				"#"+"sentNumber="+sentNumber+"#"+"strPosiNumber="+strPosiNumber+
				"#"+"flagNumber="+flagNumber+"#"+"levelNameLen="+levelNameLen+"#"+"score="+score+"\n";
	}

}
