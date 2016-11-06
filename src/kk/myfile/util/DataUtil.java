package kk.myfile.util;

import java.util.ArrayList;
import java.util.List;

import kk.myfile.leaf.Leaf;

public class DataUtil {
	public static ArrayList<CharSequence> leaf2PathCs(List<Leaf> leafs) {
		ArrayList<CharSequence> paths = new ArrayList<CharSequence>();
		
		for (Leaf leaf : leafs) {
			paths.add(leaf.getPath());
		}
		
		return paths;
	}
	
	public static List<String> leaf2PathString(List<Leaf> leafs) {
		List<String> paths = new ArrayList<String>();
		
		for (Leaf leaf : leafs) {
			paths.add(leaf.getPath());
		}
		
		return paths;
	}
}
