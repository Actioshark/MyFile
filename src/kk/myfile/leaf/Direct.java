package kk.myfile.leaf;

import java.io.File;

import kk.myfile.R;
import kk.myfile.tree.FileUtil;
import kk.myfile.tree.Tree;

public class Direct extends Leaf {
	protected Leaf[] mChildren = new Leaf[] {};
	
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
	
	public void loadChildrenAll() {
		try {
			File[] list = getFile().listFiles();
			mChildren = new Leaf[list.length];
			
			for (int i = 0; i < list.length; i++) {
				mChildren[i] = FileUtil.createLeaf(list[i]);
			}
		} catch (Exception e) {
		}
	}
	
	public void loadChildrenVisible() {
		try {
			File[] list = getFile().listFiles();
			Leaf[] children = new Leaf[list.length];
			int count = 0;
			
			for (int i = 0; i < list.length; i++) {
				File file = list[i];
				if (FileUtil.HIDDEN_FILE.equals(file.getName())) {
					mChildren = new Leaf[0];
					return;
				}
				
				if (file.isHidden() == false) {
					children[i] = FileUtil.createLeaf(file);
					count++;
				}
			}
			
			mChildren = new Leaf[count];
			int c = 0;
			for (int i = 0; i < count && c < count; i++) {
				Leaf leaf = children[i];
				
				if (leaf != null) {
					mChildren[c++] = leaf;
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void loadChildrenRecAll() {
		try {
			File file = getFile();
			
			if (file.getCanonicalPath().equals(mPath) == false) {
				return;
			}
			
			File[] list = file.listFiles();
			
			Leaf[] children = new Leaf[list.length];
			for (int i = 0; i < list.length; i++) {
				children[i] = FileUtil.createLeaf(list[i]);
			}
			
			mChildren = children;
			Tree.addTypedLeaves(children);
			
			for (Leaf leaf : children) {
				if (leaf instanceof Direct) {
					Direct direct = (Direct) leaf;
					direct.loadChildrenRecAll();
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void loadChildrenRecVisible() {
		try {
			File file = getFile();
			
			if (file.getCanonicalPath().equals(mPath) == false) {
				return;
			}
			
			File[] list = file.listFiles();
			Leaf[] children = new Leaf[list.length];
			int count = 0;
			
			for (int i = 0; i < list.length; i++) {
				File temp = list[i];
				
				if (FileUtil.HIDDEN_FILE.equals(temp.getName())) {
					mChildren = new Leaf[0];
					return;
				}
				
				if (temp.isHidden() == false) {
					children[i] = FileUtil.createLeaf(temp);
					count++;
				}
			}
			
			Leaf[] temp = new Leaf[count];
			int c = 0;
			for (int i = 0; i < children.length && c < count; i++) {
				Leaf leaf = children[i];
				
				if (leaf != null) {
					temp[c++] = leaf;
				}
			}
			
			mChildren = temp;
			Tree.addTypedLeaves(temp);
			
			for (Leaf leaf : temp) {
				if (leaf instanceof Direct) {
					Direct direct = (Direct) leaf;
					direct.loadChildrenRecVisible();
				}
			}
		} catch (Exception e) {
		}
	}
	
	public Leaf[] getChildren() {
		return mChildren;
	}
}
