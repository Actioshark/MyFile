package kk.myfile.leaf;

import kk.myfile.R;

public class Video extends Leaf {
	public static final String TYPE = "video/*";
	
	public Video(String path) {
		super(path);
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_video;
	}
}
