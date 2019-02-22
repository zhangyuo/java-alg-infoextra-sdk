package com.zy.alg.infoextra.multiinfo.lrentropy;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class BinTrie<V> extends BaseNode<V>{

	private int size;

    public BinTrie()
    {
        child = new BaseNode[65535 + 1];    // (int)Character.MAX_VALUE
        size = 0;
        status = Status.NOT_WORD_1;
    }

	public V get(String key) {
		BaseNode branch = this;
		char[] chars = key.toCharArray();
		for (char aChar : chars){
			if (branch == null) return null;
            branch = branch.getChild(aChar);
		}
		if (branch == null) return null;
        if (!(branch.status == Status.WORD_END_3 || branch.status == Status.WORD_MIDDLE_2)) return null;
        return (V) branch.getValue();
	}

	@Override
	public BaseNode<V> getChild(char c) {
		return child[c];
	}
	
	public void put(String key, V value) {
		if (key.length() == 0) return; 
        BaseNode branch = this;
        char[] chars = key.toCharArray();
        for (int i = 0; i < chars.length - 1; ++i){
            branch.addChild(new Node(chars[i], Status.NOT_WORD_1, null));
            branch = branch.getChild(chars[i]);
        }
        if (branch.addChild(new Node<V>(chars[chars.length - 1], Status.WORD_END_3, value))){
            ++size;
        }
	}

	protected boolean addChild(BaseNode node) {
		boolean add = false;
        char c = node.getChar();
        BaseNode target = getChild(c);
        if (target == null)
        {
            child[c] = node;
            add = true;
        }
        else
        {
            switch (node.status)
            {
                case UNDEFINED_0:
                    if (target.status != Status.NOT_WORD_1)
                    {
                        target.status = Status.NOT_WORD_1;
                        add = true;
                    }
                    break;
                case NOT_WORD_1:
                    if (target.status == Status.WORD_END_3)
                    {
                        target.status = Status.WORD_MIDDLE_2;
                    }
                    break;
                case WORD_END_3:
                    if (target.status == Status.NOT_WORD_1)
                    {
                        target.status = Status.WORD_MIDDLE_2;
                    }
                    if (target.getValue() == null)
                    {
                        add = true;
                    }
                    target.setValue(node.getValue());
                    break;
            }
        }
        return add;
	}

	public Set<Entry<String, V>> entrySet() {
		Set<Map.Entry<String, V>> entrySet = new TreeSet<Map.Entry<String, V>>();
        StringBuilder sb = new StringBuilder();
        for (BaseNode node : child)
        {
            if (node == null) continue;
            node.walk(new StringBuilder(sb.toString()), entrySet);
        }
        return entrySet;
	}

    public Set<Map.Entry<String, V>> prefixSearch(String key){
        Set<Map.Entry<String, V>> entrySet = new TreeSet<Map.Entry<String, V>>();
        StringBuilder sb = new StringBuilder(key.substring(0, key.length() - 1));
        BaseNode branch = this;
        char[] chars = key.toCharArray();
        for (char aChar : chars)
        {
            if (branch == null) return entrySet;
            branch = branch.getChild(aChar);
        }

        if (branch == null) return entrySet;
        branch.walk(sb, entrySet);
        return entrySet;
    }
}
