package kk.myfile.leaf;

import java.io.File;

import kk.myfile.R;
import kk.myfile.tree.FileUtil;

public class Direct extends Leaf {
	private Leaf[] mChildren = new Leaf[] {};
	
	public Direct(String path) {
		super(path);
	}
	
	@Override
	public void setType(String type) {
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_directory;
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
				mChildren[i] = FileUtil.createLeaf(children[i]);
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
			
			Leaf[] temp = new Leaf[children.length];
			for (int i = 0; i < children.length; i++) {
				temp[i] = FileUtil.createLeaf(children[i]);
			}
			
			mChildren = temp;
			
			for (Leaf leaf : mChildren) {
				if (leaf instanceof Direct) {
					Direct direct = (Direct) leaf;
					direct.loadChilrenRec();
				}
			}
		} catch (Exception e) {
		}
	}
	
	public Leaf[] getChildren() {
		return mChildren;
	}
}
