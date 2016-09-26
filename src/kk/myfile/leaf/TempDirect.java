package kk.myfile.leaf;

import java.util.List;

public class TempDirect extends Direct {
	public TempDirect(String path) {
		super(path);
	}
	
	public void setChildren(List<Leaf> list) {
		synchronized (mChildren) {
			mChildren.clear();
			mChildren.addAll(list);
		}
	}
	
	@Override
	public void loadChildrenAll() {
	}
	
	@Override
	public void loadChildrenVis() {
	}
	
	@Override
	public void loadChildrenRecAll() {
	}
	
	@Override
	public void loadChildrenRecVis() {
	}
}
