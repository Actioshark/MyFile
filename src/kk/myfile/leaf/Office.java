package kk.myfile.leaf;

import kk.myfile.R;

public class Office extends Leaf {
	public Office(String path) {
		super(path);
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_office;
	}
}
