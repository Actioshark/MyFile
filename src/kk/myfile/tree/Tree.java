package kk.myfile.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Broadcast;

public class Tree {
	public static final String BC_UPDATE = "tree_update";
	public static final String BC_COMPLETED = "tree_completed";
	
	private static final Direct sRoot = new Direct("/");
	
	public static void init(Context context) {
		final AtomicBoolean completed = new AtomicBoolean(false);
		
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				sRoot.loadChilrenRec();
				completed.set(true);
			}
		});
		
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				while (completed.get() == false) {
					Broadcast.send(BC_UPDATE, null);
					
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
				}
				
				Broadcast.send(BC_COMPLETED, null);
			}
		});
	}
	
	public static Direct findDirect(String path) {
		Direct dir = sRoot;
		String[] nodes = path.split("/");
		if (nodes != null) {
			for (int i = 1; i < nodes.length; i++) {
				String node = nodes[i];
				
				for (Leaf leaf : dir.getChildren()) {
					if (leaf instanceof Direct && leaf.getFile().getName().equals(node)) {
						dir = (Direct) leaf;
						break;
					}
				}
			}
		}
		
		return dir;
	}
	
	public static List<Leaf> getLeaves(Direct direct, Class<?> cls) {
		List<Leaf> list = new ArrayList<Leaf>();
		
		if (cls == null) {
			getLeavesInter(direct, list);
		} else {
			getLeavesInter(direct, cls, list);
		}
		
		return list;
	}
	
	private static void getLeavesInter(Direct direct, Class<?> cls, List<Leaf> list) {
		for (Leaf leaf : direct.getChildren()) {
			if (cls.isInstance(leaf)) {
				list.add(leaf);
			}
			
			if (leaf instanceof Direct) {
				getLeavesInter((Direct) leaf, cls, list);
			}
		}
	}
	
	private static void getLeavesInter(Direct direct, List<Leaf> list) {
		for (Leaf leaf : direct.getChildren()) {
			list.add(leaf);
			
			if (leaf instanceof Direct) {
				getLeavesInter((Direct) leaf, list);
			}
		}
	}
}
