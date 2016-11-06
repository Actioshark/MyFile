package kk.myfile.leaf;

import kk.myfile.R;

public class Office extends Leaf {
	public static final int COLOR = 0xffcc8800;

	public Office(String path) {
		super(path);
	}

	@Override
	public int getIcon() {
		return R.drawable.file_office;
	}

	@Override
	public int getTypeName() {
		return R.string.type_office;
	}
}
