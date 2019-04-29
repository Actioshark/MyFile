package kk.myfile.file;

import java.util.HashSet;
import java.util.Set;

public class MyCache<K, V> {
	public static class Entry<K, V> {
		public K key;
		public V value;
		public int size;
		
		public Entry<K, V> prev;
		public Entry<K, V> next;
	}
	
	private final Entry<K, V> mHeader;
	private int mCount = 0;
	
	private int mCurSize = 0;
	private int mMaxSize = 1024 * 1024 * 16;
	
	public MyCache() {
		mHeader = new Entry<>();
		mHeader.prev = mHeader.next = mHeader;
	}
	
	public int size() {
		return mCount;
	}
	
	public void setMaxSize(int max) {
		mMaxSize = max;
		checkSize();
	}
	
	public int getMaxSize() {
		return mMaxSize;
	}
	
	public int getCurSize() {
		return mCurSize;
	}
	
	public void checkSize() {
		for (Entry<K, V> node = mHeader.prev; node != mHeader && mMaxSize < mCurSize; node = node.prev) {
			removeEntry(node);
		}
	}
	
	private boolean keyEquals(K a, K b) {
		if (a == null) {
			return b == null;
		} else {
			return a.equals(b);
		}
	}
	
	private void putEntry(Entry<K, V> node) {
		node.prev = mHeader;
		node.next = mHeader.next;
		mHeader.next.prev = node;
		mHeader.next = node;
		
		mCount++;
		mCurSize += node.size;
	}
	
	private void removeEntry(Entry<K, V> node) {
		node.prev.next = node.next;
		node.next.prev = node.prev;
		
		mCount--;
		mCurSize -= node.size;
	}
	
	public void put(K key, V value, int size) {
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			if (keyEquals(node.key, key)) {
				removeEntry(node);
				break;
			}
		}
		
		Entry<K, V> node = new Entry<>();
		node.key = key;
		node.value = value;
		node.size = size;
		
		putEntry(node);
		checkSize();
	}
	
	public V get(K key) {
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			if (keyEquals(node.key, key)) {
				removeEntry(node);
				putEntry(node);
				return node.value;
			}
		}
		
		return null;
	}
	
	public boolean contains(K key) {
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			if (keyEquals(node.key, key)) {
				removeEntry(node);
				putEntry(node);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean remove(K key) {
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			if (keyEquals(node.key, key)) {
				removeEntry(node);
				return true;
			}
		}
		
		return false;
	}
	
	public Set<Entry<K, V>> entrys() {
		Set<Entry<K, V>> set = new HashSet<>();
		
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			set.add(node);
		}
		
		return set;
	}
	
	public Set<K> keys() {
		Set<K> set = new HashSet<>();
		
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			set.add(node.key);
		}
		
		return set;
	}
	
	public Set<V> values() {
		Set<V> set = new HashSet<>();
		
		for (Entry<K, V> node = mHeader.next; node != mHeader; node = node.next) {
			set.add(node.value);
		}
		
		return set;
	}
}
