package kk.myfile.leaf;

import kk.myfile.R;

public class Zip extends Leaf {
	public Zip(String path) {
		super(path);
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_zip;
	}
}