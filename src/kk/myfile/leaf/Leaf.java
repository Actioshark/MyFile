package kk.myfile.leaf;

import java.io.File;

public abstract class Leaf {
	protected final String mPath;
	
	protected String mType;
	
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
	
	public abstract int getIcon();
}
