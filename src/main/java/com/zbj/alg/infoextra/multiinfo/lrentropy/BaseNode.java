package com.zbj.alg.infoextra.multiinfo.lrentropy;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public abstract class BaseNode<V>{

	protected BaseNode[] child;
	
    protected Status status;

    protected V value;
    
    protected char c;
    
    public enum Status
    {
        UNDEFINED_0,

        NOT_WORD_1,

        WORD_MIDDLE_2,

        WORD_END_3,
    }

	public abstract BaseNode<V> getChild(char aChar);

	public V getValue() {
		return value;
	}

	public int compareTo(BaseNode other) {
		return compareTo(other.getChar());
	}

	protected char getChar(){
        return c;
    }

    public int compareTo(char other)
    {
        if (this.c > other)
        {
            return 1;
        }
        if (this.c < other)
        {
            return -1;
        }
        return 0;
    }

	protected abstract boolean addChild(BaseNode node);

    public final void setValue(V value)
    {
        this.value = value;
    }
    
	protected void walk(StringBuilder sb, Set<Map.Entry<String, V>> entrySet){
        sb.append(c);
        if (status == Status.WORD_MIDDLE_2 || status == Status.WORD_END_3){
            entrySet.add(new TrieEntry(sb.toString(), value));
        }
        if (child == null) return;
        for (BaseNode node : child){
            if (node == null) continue;
            node.walk(new StringBuilder(sb.toString()), entrySet);
        }
    }
    
    public class TrieEntry extends AbstractMap.SimpleEntry<String, V> implements Comparable<TrieEntry>{
        public TrieEntry(String key, V value){
            super(key, value);
        }
        @Override
        public int compareTo(TrieEntry o){
            return getKey().compareTo(o.getKey());
        }
    }
    
}
