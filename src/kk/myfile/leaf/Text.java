package kk.myfile.leaf;

import kk.myfile.R;

public class Text extends Leaf {
	public static final String TYPE = "text/*";

	public static final int COLOR = 0xffaaaa00;

	public Text(String path) {
		super(path);
	}

	@Override
	public int getIcon() {
		return R.drawable.file_text;
	}

	@Override
	public int getTypeName() {
		return R.string.type_text;
	}
}
