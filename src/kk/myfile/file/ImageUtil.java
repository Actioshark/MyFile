package kk.myfile.file;

import java.util.LinkedHashMap;

import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import kk.myfile.leaf.Leaf;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;

public class ImageUtil {
	public static interface IThumable {
		public Drawable getThum(int width, int height);
	}
	
	public static interface IThumListenner {
		public void onThumGot(Drawable drawable);
	}
	
	private static class DrawableNode {
		public Leaf leaf;
		public int width;
		public int height;
		public long token;
		public Drawable drawable;
		public IThumListenner listenner;
		
		public DrawableNode clone() {
			DrawableNode node = new DrawableNode();
			
			node.leaf = leaf;
			node.width = width;
			node.height = height;
			node.token = token;
			node.drawable = drawable;
			node.listenner = listenner;
			
			return node;
		}
	}

	private static final int THUM_CACHE_SIZE = 60;
	private static final LinkedHashMap<String, DrawableNode> THUM_CACHE =
			new LinkedHashMap<String, DrawableNode>(THUM_CACHE_SIZE, 0.75f, true) {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, DrawableNode> eldest) {
			return size() > THUM_CACHE_SIZE;
		}
	};
	
	private static boolean sIsRunning = false;

	public static void getThum(final Leaf leaf, final int width, final int height, final IThumListenner listenner) {
		AppUtil.runOnNewThread(new Runnable() {
			public void run() {
				synchronized (THUM_CACHE) {
					DrawableNode node = THUM_CACHE.get(leaf.getPath());
					
					if (node != null && node.drawable != null) {
						if (listenner != null) {
							final Drawable drawable = node.drawable;
							
							AppUtil.runOnUiThread(new Runnable() {
								public void run() {
									listenner.onThumGot(drawable);
								}
							});
						}
						
						return;
					}
					
					if (node == null) {
						node = new DrawableNode();
						THUM_CACHE.put(leaf.getPath(), node);
					}
					
					node.leaf = leaf;
					node.width = (width > 0 && width < 40960) ? width : 128;
					node.height = (height > 0 && height < 40960) ? height : 128;
					node.token = SystemClock.elapsedRealtime();
					node.listenner = listenner;
					
					if (sIsRunning) {
						return;
					}
					sIsRunning = true;
				}
				
				try {
					while (true) {
						DrawableNode node = null;
						
						synchronized (THUM_CACHE) {
							for (DrawableNode n : THUM_CACHE.values()) {
								if (n.drawable == null) {
									if (node == null || n.token > node.token) {
										node = n;
									}
								}
							}
						}
						
						if (node == null) {
							return;
						}
						
						final DrawableNode nd;
						synchronized (THUM_CACHE) {
							nd = node.clone();
						}
						
						try {
							nd.drawable = ((IThumable) nd.leaf).getThum(nd.width, nd.height);
							synchronized (THUM_CACHE) {
								node.drawable = nd.drawable;
							}
						} catch (Exception e) {
							Logger.print(null, e);
							
							nd.drawable = AppUtil.getRes().getDrawable(nd.leaf.getIcon());
							synchronized (THUM_CACHE) {
								node.drawable = nd.drawable;
							}
						}
						
						if (nd.listenner != null) {
							AppUtil.runOnUiThread(new Runnable() {
								public void run() {
									nd.listenner.onThumGot(nd.drawable);
								}
							});
						}
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
	}
}
