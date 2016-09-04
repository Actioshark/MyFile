package kk.myfile.leaf;

import kk.myfile.R;

public class Unknown extends Leaf {
	public Unknown(String path) {
		super(path);
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_unknown;
	}
}
