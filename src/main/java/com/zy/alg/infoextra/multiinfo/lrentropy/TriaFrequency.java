package com.zy.alg.infoextra.multiinfo.lrentropy;

public class TriaFrequency extends PairFrequency{

	public String third;
	
	private TriaFrequency(String term){
        super(term);
    }
	
    public static TriaFrequency createR(String first, String delimiter, String second, String third)
    {
        TriaFrequency triaFrequency = new TriaFrequency(first + delimiter + second + Occurrence.RIGHT + third);
        triaFrequency.first = first;
        triaFrequency.second = second;
        triaFrequency.third = third;
        triaFrequency.delimiter = delimiter;
        return triaFrequency;
    }

    public static TriaFrequency createL(String second, String third, String delimiter, String first){
        TriaFrequency triaFrequency = new TriaFrequency(second + Occurrence.RIGHT + third + delimiter + first);
        triaFrequency.first = first;
        triaFrequency.second = second;
        triaFrequency.third = third;
        triaFrequency.delimiter = delimiter;
        return triaFrequency;
    }

}
