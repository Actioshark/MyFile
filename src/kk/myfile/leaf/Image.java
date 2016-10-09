package kk.myfile.leaf;

import kk.myfile.R;
import kk.myfile.file.ImageUtil;

import android.graphics.drawable.Drawable;

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
	public Drawable getThum(int width, int height) {
		return ImageUtil.getThum(mPath, width, height);
	}
}
