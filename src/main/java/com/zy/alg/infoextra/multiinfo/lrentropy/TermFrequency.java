package com.zy.alg.infoextra.multiinfo.lrentropy;

import java.util.AbstractMap;

public class TermFrequency extends AbstractMap.SimpleEntry<String, Integer> implements Comparable<TermFrequency>{
	
	public TermFrequency(String term)
    {
        this(term, 1);
    }

	public TermFrequency(String term, Integer frequency)
    {
        super(term, frequency);
    }

	@Override
	public int compareTo(TermFrequency o) {
		if (this.getFrequency().compareTo(o.getFrequency()) == 0) return getKey().compareTo(o.getKey());
        return this.getFrequency().compareTo(o.getFrequency());
	}

	private Integer getFrequency() {
		return getValue();
	}

    public int increase()
    {
        return increase(1);
    }

    public int increase(int number)
    {
        setValue(getValue() + number);
        return getValue();
    }

}
