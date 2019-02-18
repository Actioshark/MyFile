package kk.myfile.leaf;

import kk.myfile.R;

public class Archive extends Leaf {
	public static final int COLOR = 0xff00cccc;

	public Archive(String path) {
		super(path);
	}

	@Override
	public int getIcon() {
		return R.drawable.file_archive;
	}
}
