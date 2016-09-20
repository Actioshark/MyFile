package kk.myfile.leaf;

import kk.myfile.R;

public class Text extends Leaf {
	public static final String TYPE = "text/*";
	
	public Text(String path) {
		super(path);
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_text;
	}
}
