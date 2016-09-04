package kk.myfile.leaf;

import kk.myfile.R;

public class Apk extends Leaf {
	public Apk(String path) {
		super(path);
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_apk;
	}
}
