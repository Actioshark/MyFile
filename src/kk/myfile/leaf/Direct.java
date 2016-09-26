package kk.myfile.leaf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.R;
import kk.myfile.tree.FileUtil;

public class Direct extends Leaf {
	protected final List<Leaf> mChildren = new ArrayList<Leaf>();
	
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
			synchronized (mChildren) {
				mChildren.clear();
				
				for (File file : getFile().listFiles()) {
					mChildren.add(FileUtil.createLeaf(file));
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void loadChildrenVis() {
		try {
			synchronized (mChildren) {
				mChildren.clear();
			
				for (File file : getFile().listFiles()) {
					if (FileUtil.HIDDEN_FILE.equals(file.getName())) {
						mChildren.clear();
						return;
					}
					
					if (file.isHidden() == false) {
						mChildren.add(FileUtil.createLeaf(file));
					}
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void loadChildrenRecAll() {
		try {
			List<Leaf> children = new ArrayList<Leaf>();
			for (File file : getFile().listFiles()) {
				children.add(FileUtil.createLeaf(file));
			}
			
			synchronized (mChildren) {
				mChildren.clear();
				mChildren.addAll(children);
			}
			
			for (Leaf leaf : children) {
				if (leaf instanceof Direct) {
					Direct direct = (Direct) leaf;
					direct.loadChildrenRecAll();
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void loadChildrenRecVis() {
		try {
			List<Leaf> children = new ArrayList<Leaf>();
			for (File file : getFile().listFiles()) {
				if (FileUtil.HIDDEN_FILE.equals(file.getName())) {
					synchronized (mChildren) {
						mChildren.clear();
						return;
					}
				}
				
				if (file.isHidden() == false) {
					children.add(FileUtil.createLeaf(file));
				}
			}
			
			synchronized (mChildren) {
				mChildren.clear();
				mChildren.addAll(children);
			}
			
			for (Leaf leaf : children) {
				if (leaf instanceof Direct) {
					Direct direct = (Direct) leaf;
					direct.loadChildrenRecVis();
				}
			}
		} catch (Exception e) {
		}
	}
	
	public List<Leaf> getChildren() {
		return mChildren;
	}
}
