package kk.myfile.file;

import java.util.LinkedHashMap;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import kk.myfile.util.AppUtil;
import kk.myfile.util.Broadcast;
import kk.myfile.util.Logger;

public class ImageUtil {
	public static final String BRO_THUM_GOT = "image_util_thum_got";

	private static class BitmapNode {
		public Bitmap bitmap;
		public long token;

		public BitmapNode(Bitmap bitmap, long token) {
			this.bitmap = bitmap;
			this.token = token;
		}
	}

	private static final int THUM_CACHE_SIZE = 60;
	@SuppressWarnings("serial")
	private static final LinkedHashMap<String, BitmapNode> THUM_CACHE =
			new LinkedHashMap<String, BitmapNode>(THUM_CACHE_SIZE, 0.75f, true) {

		@Override
		protected boolean removeEldestEntry(Entry<String, BitmapNode> eldest) {
			return size() > THUM_CACHE_SIZE;
		}
	};

	public static Options getSize(String path) {
		Options options = new Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(path, options);

		return options;
	}

	public static Bitmap getThum(final String path, final int pw, final int ph) {
		synchronized (THUM_CACHE) {
			BitmapNode bitmapNode = THUM_CACHE.get(path);
			Bitmap bitmap = bitmapNode == null ? null : bitmapNode.bitmap;
			
			if (bitmap == null) {
				final BitmapNode bn = new BitmapNode(null, System.currentTimeMillis());
				THUM_CACHE.put(path, bn);

				AppUtil.runOnNewThread(new Runnable() {
					@Override
					public void run() {
						try {
							while (true) {
								boolean selected = true;
								
								synchronized (THUM_CACHE) {
									if (THUM_CACHE.containsKey(path) == false) {
										return;
									}
									
									for (BitmapNode bitmapNode : THUM_CACHE.values()) {
										if (bitmapNode.bitmap == null && bn.token < bitmapNode.token) {
											selected = false;
											break;
										}
									}
								}
									
								if (selected) {
									Options size = getSize(path);
									int sw = (size.outWidth + pw - 1) / pw;
									int sh = (size.outHeight + ph - 1) / ph;
									
									Options op = new Options();
									op.inSampleSize = Math.min(sw, sh);
									op.inScaled = true;
									
									Bitmap bmp = BitmapFactory.decodeFile(path, op);
									int bw = bmp.getWidth();
									int bh = bmp.getHeight();
									int rw = Math.min(bw, pw);
									int rh = Math.min(bh, ph);
									int x = (bw - rw) / 2;
									int y = (bh - rh) / 2;
									
									int[] pixels = new int[rw * rh];
									bmp.getPixels(pixels, 0, rw, x, y, rw, rh);
									
									Config config = bmp.getConfig();
									if (config == null) {
										config = Config.ARGB_8888;
									}
									bmp = Bitmap.createBitmap(pixels, rw, rh, config);
									
									synchronized (THUM_CACHE) {
										bn.bitmap = bmp;
									}
	
									Broadcast.send(BRO_THUM_GOT, bmp);
									return;
								}
								
								Thread.sleep(200);
							}
						} catch (Exception e) {
							Logger.print(null, e, path);
							
							synchronized (THUM_CACHE) {
								THUM_CACHE.remove(path);
							}
						}
					}
				});
			}
			
			return bitmap;
		}
	}
}
