package kk.myfile.leaf;

import kk.myfile.R;
import kk.myfile.file.ImageUtil;

public class Image extends Leaf {
	public static final String TYPE = "image/*";
	
	public Image(String path) {
		super(path);
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_image;
	}
	
	@Override
	public void getThum(int width, int height, IThumListenner listenner) {
		ImageUtil.getThum(mPath, width, height, listenner);
	}
}
