package kk.myfile.leaf;

import java.io.File;

public abstract class Leaf {
	protected final String mPath;
	
	private Object mTag;
	
	public Leaf(String path) {
		if (path == null || path.startsWith("/") == false) {
			mPath = "/";
		} else {
			mPath = path;
		}
	}
	
	public File getFile() {
		return new File(mPath);
	}
	
	public String getPath() {
		return mPath;
	}
	
	public void setTag(Object tag) {
		mTag = tag;
	}
	
	public Object getTag() {
		return mTag;
	}
	
	public abstract int getIcon();
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Leaf == false) {
			return false;
		}
		
		Leaf other = (Leaf) obj;
		return mPath.equals(other.mPath);
	}
}
