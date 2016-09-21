package kk.myfile.leaf;

public class TempDirect extends Direct {
	public TempDirect(String path) {
		super(path);
	}
	
	public void setChildren(Leaf[] list) {
		mChildren = list;
	}
	
	@Override
	public void loadChildrenAll() {
	}
	
	@Override
	public void loadChildrenVisible() {
	}
	
	@Override
	public void loadChildrenRecAll() {
	}
	
	@Override
	public void loadChildrenRecVisible() {
	}
}
