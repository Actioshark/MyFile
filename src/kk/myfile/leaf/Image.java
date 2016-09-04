package kk.myfile.leaf;

import kk.myfile.R;
import android.graphics.Bitmap;

public class Image extends Leaf {
	public Image(String path) {
		super(path);
	}
	
	public Bitmap getThum() {
		// TODO
		return null;
	}
	
	public Bitmap getImage() {
		// TODO
		return null;
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_image;
	}
}
