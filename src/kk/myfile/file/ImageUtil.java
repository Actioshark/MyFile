package kk.myfile.file;

import java.util.LinkedHashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import kk.myfile.util.AppUtil;
import kk.myfile.util.Broadcast;
import kk.myfile.util.Logger;
import kk.myfile.util.MathUtil;

public class ImageUtil {
	public static final String BRO_THUM_GOT = "image_util_thum_got";
	public static final String BRO_IMAGE_GOT = "image_util_image_got";
	
	private static final int THUM_MAX_WIDTH = 256;
	private static final int THUM_MAX_HEIGHT = 256;
	private static final int MAX_MULTIPLY = THUM_MAX_WIDTH * THUM_MAX_HEIGHT * 2;

	private static class BitmapNode {
		public Bitmap bitmap;
		public long token;

		public BitmapNode(Bitmap bitmap, long token) {
			this.bitmap = bitmap;
			this.token = token;
		}
	}

	private static final int THUM_CACHE_SIZE = 50;
	@SuppressWarnings("serial")
	private static final LinkedHashMap<String, BitmapNode> THUM_CACHE =
			new LinkedHashMap<String, BitmapNode>(THUM_CACHE_SIZE, 0.75f, true) {

		@Override
		protected boolean removeEldestEntry(Entry<String, BitmapNode> eldest) {
			return size() > THUM_CACHE_SIZE;
		}
	};

	private static final int IMAGE_CACHE_SIZE = 4;
	@SuppressWarnings("serial")
	private static final LinkedHashMap<String, BitmapNode> IMAGE_CACHE =
			new LinkedHashMap<String, BitmapNode>(IMAGE_CACHE_SIZE, 0.75f, true) {

		@Override
		protected boolean removeEldestEntry(Entry<String, BitmapNode> eldest) {
			return size() > IMAGE_CACHE_SIZE;
		}
	};

	public static Options getOptions(String path) {
		Options options = new Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(path, options);

		return options;
	}

	public static Bitmap getThum(final String path) {
		synchronized (THUM_CACHE) {
			BitmapNode bitmapNode = THUM_CACHE.get(path);
			
			if (bitmapNode == null) {
				final BitmapNode bn = new BitmapNode(null, System.currentTimeMillis());
				THUM_CACHE.put(path, bn);

				AppUtil.runOnNewThread(new Runnable() {
					@Override
					public void run() {
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
								Options options = getOptions(path);
								int scaleWidth = (options.outWidth + THUM_MAX_WIDTH - 1)
										/ THUM_MAX_WIDTH;
								int scaleHeight = (options.outHeight + THUM_MAX_HEIGHT - 1)
										/ THUM_MAX_HEIGHT;
								options = new Options();
								options.inSampleSize = MathUtil.max(scaleWidth, scaleHeight, 2);

								Bitmap bmp = BitmapFactory.decodeFile(path, options);
								synchronized (THUM_CACHE) {
									bn.bitmap = bmp;
								}

								Broadcast.send(BRO_THUM_GOT, bmp);
								return;
							}
							
							try {
								Thread.sleep(100);
							} catch (Exception e) {
								Logger.print(null, e);
							}
						}
					}
				});

				return null;
			} else {
				return bitmapNode.bitmap;
			}
		}
	}

	public static Bitmap getImage(final String path) {
		synchronized (IMAGE_CACHE) {
			BitmapNode bitmapNode = IMAGE_CACHE.get(path);

			if (bitmapNode == null) {
				final BitmapNode bn = new BitmapNode(null, System.currentTimeMillis());
				IMAGE_CACHE.put(path, bn);

				AppUtil.runOnNewThread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							boolean selected = true;
							
							synchronized (IMAGE_CACHE) {
								if (IMAGE_CACHE.containsKey(path) == false) {
									return;
								}
								
								for (BitmapNode bitmapNode : IMAGE_CACHE.values()) {
									if (bitmapNode.bitmap == null && bn.token < bitmapNode.token) {
										selected = false;
										break;
									}
								}
							}
								
							if (selected) {
								Options options = getOptions(path);
								Options op = new Options();
								op.inSampleSize = (int) Math.ceil(Math.sqrt((options.outWidth * options.outHeight
										+ MAX_MULTIPLY - 1) / MAX_MULTIPLY));
								
								Bitmap bmp = BitmapFactory.decodeFile(path, op);
								
								synchronized (bn) {
									bn.bitmap = bmp;
								}
		
								Broadcast.send(BRO_IMAGE_GOT, bmp);
								return;
							}
							
							try {
								Thread.sleep(100);
							} catch (Exception e) {
								Logger.print(null, e);
							}
						}
					}
				});

				return null;
			} else {
				return bitmapNode.bitmap;
			}
		}
	}
}
