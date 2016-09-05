package kk.myfile.leaf;

import java.io.File;

import kk.myfile.R;
import kk.myfile.tree.Tree;

public class Direct extends Leaf {
	private Leaf[] mChildren = new Leaf[] {};
	
	public Direct(String path) {
		super(path);
	}
	
	public void loadChilren() {
		try {
			File file = getFile();
			
			File[] children = file.listFiles();
			if (children == null) {
				return;
			}
			
			mChildren = new Leaf[children.length];
			for (int i = 0; i < children.length; i++) {
				Leaf leaf = Tree.getLeaf(children[i]);
				mChildren[i] = leaf;
			}
		} catch (Exception e) {
		}
	}
	
	public void loadChilrenRec() {
		try {
			File file = getFile();
			
			if (file.getCanonicalPath().equals(mPath) == false) {
				return;
			}
			
			File[] children = file.listFiles();
			if (children == null) {
				return;
			}
			
			synchronized (this) {
				mChildren = new Leaf[children.length];
				for (int i = 0; i < children.length; i++) {
					mChildren[i] = Tree.getLeaf(children[i]);
				}
			}

			for (Leaf leaf : mChildren) {
				if (leaf instanceof Direct) {
					((Direct) leaf).loadChilrenRec();
				}
			}
		} catch (Exception e) {
		}
	}
	
	public Leaf[] getChildren() {
		return mChildren;
	}
	
	public synchronized Leaf[] getChildrenLocked() {
		return mChildren;
	}
	
	@Override
	public void setType(String type) {
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_directory;
	}
}
