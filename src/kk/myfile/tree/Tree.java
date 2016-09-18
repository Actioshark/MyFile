package kk.myfile.tree;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Broadcast;

public class Tree {
	public static final String BC_START = "tree_start";
	public static final String BC_UPDATE = "tree_update";
	public static final String BC_COMPLETED = "tree_completed";
	
	private static final Direct sRoot = new Direct("/");
	private static boolean sIsRefreshing = false;
	private static boolean sNeedRefresh = false;
	
	public static void init(Context context) {
		refresh();
	}
	
	public static void refresh() {
		synchronized (sRoot) {
			if (sIsRefreshing) {
				sNeedRefresh = true;
				return;
			}
			
			sIsRefreshing = true;
			sNeedRefresh = false;
		}
		
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				Broadcast.send(BC_START, null);
				sRoot.loadChilrenRec();
				Broadcast.send(BC_COMPLETED, null);
				
				synchronized (sRoot) {
					sIsRefreshing = false;
					
					if (sNeedRefresh) {
						refresh();
					}
				}
			}
		});
		
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				while (true) {		
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
					
					synchronized (sRoot) {
						if (sIsRefreshing) {
							Broadcast.send(BC_UPDATE, null);
						} else {
							break;
						}
					}
				}
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
