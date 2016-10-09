package kk.myfile.file;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.SystemClock;

import kk.myfile.util.AppUtil;
import kk.myfile.util.Broadcast;
import kk.myfile.util.Logger;

public class ImageUtil {
	public static final String BRO_THUM_GOT = "image_util_thum_got";
	
	private static class BitmapNode {
		public int width;
		public int height;
		public int defResId;
		public long token;
		public Bitmap bitmap;
	}

	private static final int THUM_CACHE_SIZE = 60;
	private static final LinkedHashMap<String, BitmapNode> THUM_CACHE =
			new LinkedHashMap<String, BitmapNode>(THUM_CACHE_SIZE, 0.75f, true) {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, BitmapNode> eldest) {
			return size() > THUM_CACHE_SIZE;
		}
	};
	
	private static boolean sIsRunning = false;

	public static Options getSize(String path) {
		Options options = new Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(path, options);

		return options;
	}

	public static Bitmap getThum(String path, int width, int height, int defResId) {
		if (width <= 0 || width > 40960) {
			width = 128;
		}
		
		if (height <= 0 || height > 40960) {
			height = 128;
		}
		
		synchronized (THUM_CACHE) {
			BitmapNode bn = THUM_CACHE.get(path);
			
			if (bn == null) {
				bn = new BitmapNode();
				THUM_CACHE.put(path, bn);
			} else if (bn.bitmap != null) {
				return bn.bitmap;
			}
			
			bn.width = width;
			bn.height = height;
			bn.defResId = defResId;
			bn.token = SystemClock.elapsedRealtime();
			
			if (sIsRunning) {
				return null;
			}
			
			sIsRunning = true;
		}

		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						String path = null;
						BitmapNode bn = null;
						
						synchronized (THUM_CACHE) {
							for (Entry<String, BitmapNode> entry : THUM_CACHE.entrySet()) {
								BitmapNode node = entry.getValue();
								if (node.bitmap == null) {
									if (bn == null || node.token > bn.token) {
										path = entry.getKey();
										bn = node;
									}
								}
							}
						}
						
						if (bn == null) {
							return;
						}
						
						Bitmap bmp;
						
						try {
							Options size = getSize(path);
							int sw = (size.outWidth + bn.width - 1) / bn.width;
							int sh = (size.outHeight + bn.height - 1) / bn.height;
							
							Options op = new Options();
							op.inSampleSize = Math.min(sw, sh);
							op.inScaled = true;
							
							bmp = BitmapFactory.decodeFile(path, op);
							int bw = bmp.getWidth();
							int bh = bmp.getHeight();
							int rw = Math.min(bw, bn.width);
							int rh = Math.min(bh, bn.height);
							int x = (bw - rw) / 2;
							int y = (bh - rh) / 2;
							
							int[] pixels = new int[rw * rh];
							bmp.getPixels(pixels, 0, rw, x, y, rw, rh);
							
							Config config = bmp.getConfig();
							if (config == null) {
								config = Config.ARGB_8888;
							}
							bmp = Bitmap.createBitmap(pixels, rw, rh, config);
						} catch (Exception e) {
							Logger.print(null, e);
							
							bmp = BitmapFactory.decodeResource(AppUtil.getRes(), bn.defResId);
						}
						
						synchronized (THUM_CACHE) {
							bn.bitmap = bmp;
						}
		
						Broadcast.send(BRO_THUM_GOT, bmp);
					}
				} catch (Exception e) {
					Logger.print(null, e);
				} finally {
					synchronized (THUM_CACHE) {
						sIsRunning = false;
					}
				}
			}
		});
		
		return null;
	}
}
