package kk.myfile.leaf;

import java.io.File;

import android.graphics.Bitmap;

public abstract class Leaf {
	protected final String mPath;
	
	protected String mType;
	
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
	
	public void setType(String type) {
		if (mType == null) {
			mType = type;
		}
	}
	
	public String getType() {
		return mType;
	}
	
	public void setTag(Object tag) {
		mTag = tag;
	}
	
	public Object getTag() {
		return mTag;
	}
	
	public abstract int getIcon();
	
	public Bitmap getThum(int width, int height) {
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Leaf == false) {
			return false;
		}
		
		Leaf other = (Leaf) obj;
		return mPath.equals(other.mPath);
	}
}
