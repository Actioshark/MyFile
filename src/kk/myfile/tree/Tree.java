package kk.myfile.tree;

import java.util.ArrayList;
import java.util.List;

import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Setting;

public class Tree {
	public static final String HIDDEN_FILE = ".nomedia";
	
	public static Direct load(String path) {
		final Direct direct = new Direct(path);
		direct.setTag(direct);
		
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				if (Setting.getShowHidden()) {
					direct.loadChildrenRecAll();
				} else {
					direct.loadChildrenRecVis();
				}
				
				direct.setTag(null);
			}
		});
		
		return direct;
	}
	
	public static List<Leaf> search(Direct direct, String input) {
		List<Leaf> ret = new ArrayList<Leaf>();
		
		search(direct, input.toLowerCase(Setting.LOCALE), ret);
		
		return ret;
	}
	
	private static void search(Direct direct, String input, List<Leaf> ret) {
		try {
			List<Leaf> children = direct.getChildren();
			
			synchronized (children) {
				for (Leaf leaf : children) {
					if (leaf.getFile().getName().toLowerCase(Setting.LOCALE).contains(input)) {
						ret.add(leaf);
					}
					
					if (leaf instanceof Direct) {
						search((Direct) leaf, input, ret);
					}
				}
			}
		} catch (Exception e) {
		}
	}
}
