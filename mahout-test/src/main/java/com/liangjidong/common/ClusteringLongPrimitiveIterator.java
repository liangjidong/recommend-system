package com.liangjidong.common;

import org.apache.mahout.cf.taste.impl.common.AbstractLongPrimitiveIterator;

/**
 * 不需要自定义，使用LongPrimitiveArrayIterator就可以了
 * 
 * @author ljd
 *
 */
public class ClusteringLongPrimitiveIterator extends AbstractLongPrimitiveIterator {

	private long userID;// 通过该用户id得出所需要的候选集。该id也就是目标用户

	public long nextLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long peek() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void skip(int n) {
		// TODO Auto-generated method stub

	}

	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

}
