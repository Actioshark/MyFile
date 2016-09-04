package kk.myfile.leaf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.R;
import kk.myfile.tree.Tree;

public class Direct extends Leaf {
	private Leaf[] mChildren;
	
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
			
			mChildren.clear();
			for (File child : children) {
				Leaf node = Tree.getLeaf(child);
				
				mChildren.add(node);
			}
		} catch (Exception e) {
		}
	}
	
	public synchronized void loadChilrenRec() {
		try {
			File file = getFile();
			
			File[] children = file.listFiles();
			if (children == null) {
				return;
			}
			
			if (file.getCanonicalPath().equals(mPath) == false) {
				return;
			}
			
			mChildren.clear();
			for (File child : children) {
				Leaf node = Tree.getLeaf(child);
				
				if (node instanceof Direct) {
					((Direct) node).loadChilrenRec();
				}
				
				mChildren.add(node);
			}
		} catch (Exception e) {
		}
	}
	
	public List<Leaf> getChildren() {
		return mChildren;
	}
	
	public synchronized List<Leaf> getChildrenCopy(boolean copy) {
		if (copy) {
			return new ArrayList<Leaf>(mChildren);
		} else {
			return mChildren;
		}
	}
	
	@Override
	public void setType(String type) {
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_directory;
	}
}
