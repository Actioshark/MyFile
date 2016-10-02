package kk.myfile.file;

import java.util.ArrayList;
import java.util.List;

import kk.myfile.leaf.Leaf;

public class ClipPad {
	public static enum Mode {
		Copy, Cut,
	}
	
	private static Mode sMode;
	private static final List<Leaf> sList = new ArrayList<Leaf>();
	
	public static synchronized void setClip(Mode mode, List<Leaf> list) {
		sMode = mode;
		
		sList.clear();
		sList.addAll(list);
	}
	
	public static synchronized void setClip(Mode mode, Leaf leaf) {
		sMode = mode;
		
		sList.clear();
		sList.add(leaf);
	}
	
	public static synchronized Mode getMode() {
		return sMode;
	}
	
	public static synchronized List<Leaf> getClip() {
		return new ArrayList<Leaf>(sList);
	}
	
	public static synchronized int size() {
		return sList.size();
	}
	
	public static synchronized void clear() {
		sMode = null;
		sList.clear();
	}
}
