package kk.myfile.leaf;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import kk.myfile.R;
import kk.myfile.file.FileUtil;
import kk.myfile.file.Tree;

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
					if (Tree.HIDDEN_FILE.equals(file.getName())) {
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
		Queue<Direct> queue = new LinkedList<Direct>();
		queue.add(this);
		
		for (Direct dir = queue.poll(); dir != null; dir = queue.poll()) {
			try {
				List<Leaf> children = new ArrayList<Leaf>();
				for (File file : new File(dir.mPath).listFiles()) {
					children.add(FileUtil.createLeaf(file));
				}
				
				synchronized (dir.mChildren) {
					dir.mChildren.clear();
					dir.mChildren.addAll(children);
				}
				
				for (Leaf leaf : children) {
					if (leaf instanceof Direct) {
						queue.offer((Direct) leaf);
					}
				}
			} catch (Exception e) {
			}
		}
	}
	
	public void loadChildrenRecVis() {
		Queue<Direct> queue = new LinkedList<Direct>();
		queue.add(this);

loop:	for (Direct dir = queue.poll(); dir != null; dir = queue.poll()) {
			try {
				List<Leaf> children = new ArrayList<Leaf>();
				
				for (File file : new File(dir.mPath).listFiles()) {
					if (Tree.HIDDEN_FILE.equals(file.getName())) {
						continue loop;
					}
					
					if (file.isHidden() == false) {
						children.add(FileUtil.createLeaf(file));
					}
				}
				
				synchronized (dir.mChildren) {
					dir.mChildren.clear();
					dir.mChildren.addAll(children);
				}
				
				for (Leaf leaf : children) {
					if (leaf instanceof Direct) {
						queue.offer((Direct) leaf);
					}
				}
			} catch (Exception e) {
			}
		}
	}
	
	public List<Leaf> getChildren() {
		return mChildren;
	}
}
