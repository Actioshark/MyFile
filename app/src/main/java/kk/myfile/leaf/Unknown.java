package kk.myfile.leaf;

import kk.myfile.R;

public class Unknown extends Leaf {
	public static final String TYPE = "*/*";

	public static final int COLOR = 0xff333333;

	public Unknown(String path) {
		super(path);
	}

	@Override
	public int getIcon() {
		return R.drawable.file_unknown;
	}
}
