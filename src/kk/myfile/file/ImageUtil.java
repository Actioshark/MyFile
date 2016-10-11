package kk.myfile.file;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.Leaf.IThumListenner;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;

public class ImageUtil {
	private static class DrawableNode {
		public Leaf leaf;
		public int width;
		public int height;
		public long token;
		public Drawable drawable;
		public IThumListenner listenner;
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
							for (Entry<String, DrawableNode> entry : THUM_CACHE.entrySet()) {
								DrawableNode n = entry.getValue();
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
						
						try {
							Drawable drawable = node.leaf.getThum(node.width, node.height);
						
							synchronized (THUM_CACHE) {
								node.drawable = drawable;
							}
						} catch (Exception e) {
							Logger.print(null, e);
							
							synchronized (THUM_CACHE) {
								node.drawable = AppUtil.getRes().getDrawable(leaf.getIcon());
							}
						}
						
						if (node.listenner != null) {
							final DrawableNode BN = node;
							
							AppUtil.runOnUiThread(new Runnable() {
								public void run() {
									BN.listenner.onThumGot(BN.drawable);
									BN.leaf = null;
									BN.listenner = null;
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
