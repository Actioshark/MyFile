package kk.myfile.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import kk.myfile.leaf.Apk;
import kk.myfile.leaf.Audio;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Image;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.Office;
import kk.myfile.leaf.Text;
import kk.myfile.leaf.Video;
import kk.myfile.leaf.Zip;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Broadcast;
import kk.myfile.util.Setting;

public class Tree {
	public static final String BC_START = "tree_start";
	public static final String BC_UPDATE = "tree_update";
	public static final String BC_COMPLETED = "tree_completed";
	
	private static final Direct sRoot = new Direct("/");
	private static boolean sIsRefreshing = false;
	private static boolean sNeedRefresh = false;
	
	private static final Map<Class<?>, List<Leaf>> sTypedFile =
			new HashMap<Class<?>, List<Leaf>>();
	private static final List<Leaf> sBigFile = new ArrayList<Leaf>();
	private static final List<Leaf> sRecentFile = new ArrayList<Leaf>();
	
	public static void init(Context context) {
		sTypedFile.put(Text.class, new ArrayList<Leaf>());
		sTypedFile.put(Image.class, new ArrayList<Leaf>());
		sTypedFile.put(Audio.class, new ArrayList<Leaf>());
		sTypedFile.put(Video.class, new ArrayList<Leaf>());
		sTypedFile.put(Office.class, new ArrayList<Leaf>());
		sTypedFile.put(Zip.class, new ArrayList<Leaf>());
		sTypedFile.put(Apk.class, new ArrayList<Leaf>());
		
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
				
				for (List<Leaf> list : sTypedFile.values()) {
					list.clear();
				}
				sBigFile.clear();
				sRecentFile.clear();
				
				if (Setting.getShowHidden()) {
					sRoot.loadChildrenRecAll();
				} else {
					sRoot.loadChildrenRecVisible();
				}
				
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
	
	public static List<Leaf> getLeaves(Direct direct) {
		List<Leaf> list = new ArrayList<Leaf>();
		getLeavesInter(direct, list);
		return list;
	}
	
	private static void getLeavesInter(Direct direct, List<Leaf> list) {
		for (Leaf leaf : direct.getChildren()) {
			list.add(leaf);
			
			if (leaf instanceof Direct) {
				getLeavesInter((Direct) leaf, list);
			}
		}
	}
	
	public static void addTypedLeaves(Leaf[] leaves) {
		for (Leaf leaf : leaves) {
			for (Entry<Class<?>, List<Leaf>> entry : sTypedFile.entrySet()) {
				if (entry.getKey().isInstance(leaf)) {
					entry.getValue().add(leaf);
					break;
				}
			}
		}
	}
	
	public static List<Leaf> getTypedLeaves(Class<?> cls) {
		return sTypedFile.get(cls);
	}
}
