package kk.myfile.tree;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.Unknown;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;

public class Tree {
	private static JSONObject sTypeMap;
	
	private static final Direct sRoot = new Direct("/");
	
	public static void init(Context context) {
		if (sTypeMap != null) {
			return;
		}
		
		AssetManager sm = context.getAssets();
		try {
			InputStream is = sm.open("file_type.json");
			String string = FileUtil.readString(is, 1024 * 1024);
			sTypeMap = new JSONObject(string);
		} catch (Exception e) {
			Logger.print(null, e);
		}
		
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				sRoot.loadChilrenRec();
			}
		});
	}
	
	public static Leaf getLeaf(File file) {
		String path = file.getAbsolutePath();
		if (file.isDirectory()) {
			return new Direct(path);
		}
		
		String type = null;
		
		String name = file.getName();
		int pointIndex = name.lastIndexOf('.');
		if (pointIndex != -1) {
			String subfix = name.substring(pointIndex + 1, name.length())
				.toLowerCase(Locale.ENGLISH);
			
			try {
				JSONObject map = sTypeMap.getJSONObject(subfix);
				if (map != null) {
					type = map.getString("type");
					
					String cls = map.getString("cls");
					cls = String.format("%c%s", Character.toUpperCase(cls.charAt(0)),
						cls.substring(1));
					Class<?> clazz = Class.forName(String.format("kk.myfile.leaf.%s", cls));
					if (clazz != null) {
						Constructor<?> ct = clazz.getConstructor(String.class);
						Leaf leaf = (Leaf) ct.newInstance(path);
						leaf.setType(type);
						
						return leaf;
					}
				}
			} catch (Exception e) {
			}
		}
		
		Leaf leaf = new Unknown(path);
		leaf.setType(type);
		return leaf;
	}
	
	public static List<Leaf> getAll(String path) {
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
		
		List<Leaf> list = new ArrayList<Leaf>();
		getAllInter(dir, list);
		return list;
	}
	
	private static void getAllInter(Direct direct, List<Leaf> list) {
		for (Leaf leaf : direct.getChildrenLocked()) {
			list.add(leaf);
			
			if (leaf instanceof Direct) {
				getAllInter((Direct) leaf, list);
			}
		}
	}
}
