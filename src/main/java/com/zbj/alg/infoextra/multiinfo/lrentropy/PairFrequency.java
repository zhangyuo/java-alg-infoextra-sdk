package com.zbj.alg.infoextra.multiinfo.lrentropy;

public class PairFrequency extends TermFrequency{
	
    public double mi;

    public double le;

    public double re;

    public double score;
    public String first;
    public String second;
    public String delimiter;
    
    protected PairFrequency(String term){
        super(term);
    }

	public static PairFrequency create(String first, String delimiter,
			String second) {
		PairFrequency pairFrequency = new PairFrequency(first + delimiter + second);
        pairFrequency.first = first;
        pairFrequency.delimiter = delimiter;
        pairFrequency.second = second;
        return pairFrequency;
	}

}
