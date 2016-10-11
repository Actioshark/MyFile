package kk.myfile.leaf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import kk.myfile.R;
import kk.myfile.file.ImageUtil.IThumable;
import kk.myfile.util.AppUtil;

public class Image extends Leaf implements IThumable {
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
		Options size = new Options();
		size.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mPath, size);
		
		int sw = (size.outWidth + width - 1) / width;
		int sh = (size.outHeight + height - 1) / height;
		
		Options op = new Options();
		op.inSampleSize = Math.min(sw, sh);
		op.inScaled = true;
		
		Bitmap bmp = BitmapFactory.decodeFile(mPath, op);
		int bw = bmp.getWidth();
		int bh = bmp.getHeight();
		int rw = Math.min(bw, width);
		int rh = Math.min(bh, height);
		int x = (bw - rw) / 2;
		int y = (bh - rh) / 2;
		
		int[] pixels = new int[rw * rh];
		bmp.getPixels(pixels, 0, rw, x, y, rw, rh);
		
		Config config = bmp.getConfig();
		if (config == null) {
			config = Config.ARGB_8888;
		}
		bmp = Bitmap.createBitmap(pixels, rw, rh, config);
	
		return new BitmapDrawable(AppUtil.getRes(), bmp);
	}
}
