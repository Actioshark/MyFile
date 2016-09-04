package kk.myfile.leaf;

import kk.myfile.R;

public class Audio extends Leaf {
	public Audio(String path) {
		super(path);
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_audio;
	}
}
