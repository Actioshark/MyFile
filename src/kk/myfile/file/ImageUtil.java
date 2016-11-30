package kk.myfile.file;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import kk.myfile.leaf.Leaf;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;

public class ImageUtil {
	public static interface IThumable {
		public Drawable getThum(int width, int height) throws Exception;
	}

	public static interface IThumListener {
		public void onThumGot(Drawable drawable);
	}

	private static class DrawableNode {
		public Leaf leaf;
		public int width;
		public int height;
		public long token;
		public Drawable drawable;
		public List<IThumListener> listeners;
	}
	
	public static final int DEF_WIDTH = 256;
	public static final int MAX_WIDTH = 40960;
	
	public static final int DEF_HEIGHT = 256;
	public static final int MAX_HEIGHT = 40960;

	public static final int THUM_CACHE_SIZE = 1024 * 1024 * 32;
	private static final MyCache<String, DrawableNode> THUM_CACHE =
		new MyCache<String, DrawableNode>();

	private static boolean sIsRunning = false;
	
	static {
		THUM_CACHE.setMaxSize(THUM_CACHE_SIZE);
	}

	public static void getThum(final Leaf leaf, final int width, final int height,
		final IThumListener listener) {
		
		if (leaf instanceof IThumable == false) {
			return;
		}

		AppUtil.runOnNewThread(new Runnable() {
			public void run() {
				synchronized (THUM_CACHE) {
					DrawableNode node = THUM_CACHE.get(leaf.getPath());

					if (node != null && node.drawable != null) {
						if (listener != null) {
							final Drawable drawable = node.drawable;

							AppUtil.runOnUiThread(new Runnable() {
								public void run() {
									listener.onThumGot(drawable);
								}
							});
						}

						return;
					}

					if (node == null) {
						node = new DrawableNode();
						node.listeners = new ArrayList<IThumListener>();
						THUM_CACHE.put(leaf.getPath(), node, 0);
					}

					node.leaf = leaf;
					node.width = (width > 0 && width < MAX_WIDTH) ? width : DEF_WIDTH;
					node.height = (height > 0 && height < MAX_HEIGHT) ? height : DEF_HEIGHT;
					node.token = SystemClock.elapsedRealtime();
					
					if (listener != null) {
						node.listeners.add(listener);
					}

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

						final DrawableNode nd = new DrawableNode();
						synchronized (THUM_CACHE) {
							nd.leaf = node.leaf;
							nd.width = node.width;
							nd.height = node.height;
							nd.token = node.token;
						}

						try {
							nd.drawable = ((IThumable) nd.leaf).getThum(nd.width, nd.height);
						} catch (Exception e) {
							Logger.print(null, e);

							nd.drawable = AppUtil.getRes().getDrawable(nd.leaf.getIcon());
						}
						
						synchronized (THUM_CACHE) {
							node.drawable = nd.drawable;
							
							nd.listeners = node.listeners;
							node.listeners = null;
							
							int size = node.drawable.getIntrinsicWidth() *
								node.drawable.getIntrinsicHeight() * 4;
							THUM_CACHE.put(node.leaf.getPath(), node, size);
						}

						AppUtil.runOnUiThread(new Runnable() {
							public void run() {
								for (IThumListener listener : nd.listeners) {
									listener.onThumGot(nd.drawable);
								}
							}
						});
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
