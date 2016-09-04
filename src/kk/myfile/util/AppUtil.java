package kk.myfile.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class AppUtil {
	private static Resources sRes;
	
	private static Handler sHandler;
	
	private static String PACKAGE_NAME;

	private static int SCREEN_WIDTH;
	private static int SCREEN_HEIGHT;
	private static int sStatusBarHeight = 0;

	public static void init(Context context) {
		if (sRes != null) {
			return;
		}
		
		sRes = context.getResources();
		
		sHandler = new Handler(context.getMainLooper());
		
		PACKAGE_NAME = context.getPackageName();

		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		manager.getDefaultDisplay().getMetrics(metrics);
		SCREEN_WIDTH = metrics.widthPixels;
		SCREEN_HEIGHT = metrics.heightPixels;

		int id = sRes.getIdentifier("status_bar_height", "dimen", "android");
		if (id > 0) {
			sStatusBarHeight = sRes.getDimensionPixelSize(id);
		}
	}

	public static String getPackageName() {
		return PACKAGE_NAME;
	}

	public static Resources getRes() {
		return sRes;
	}

	public static int getId(String type, String name) {
		return sRes.getIdentifier(name, type, PACKAGE_NAME);
	}

	public static String getString(int id, Object... args) {
		return sRes.getString(id, args);
	}

	public static String getString(String name, Object... args) {
		return sRes.getString(getId("string", name), args);
	}

	public static float getDimen(int id) {
		return sRes.getDimension(id);
	}

	public static float getDimen(String name) {
		return sRes.getDimension(getId("dimen", name));
	}

	public static int getDimenInt(int id) {
		return (int) getDimen(id);
	}

	public static int getDimenInt(String name) {
		return (int) getDimen(name);
	}

	public static int getColor(int id) {
		return sRes.getColor(id);
	}

	public static int getColor(String name) {
		return getColor(getId("color", name));
	}

	public static int getScreenWidth(boolean full) {
		return SCREEN_WIDTH;
	}

	public static int getScreenHeight(boolean full) {
		return full ? SCREEN_HEIGHT : SCREEN_HEIGHT - sStatusBarHeight;
	}

	public static int getStatusBarHeight() {
		return sStatusBarHeight;
	}

	public static void exitApp() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public static void runOnUiThread(Runnable runnable) {
		runOnUiThread(runnable, 0);
	}

	public static void runOnUiThread(final Runnable runnable, long delay) {
		if (delay > 0) {
			sHandler.postDelayed(runnable, delay);
		} else {
			sHandler.post(runnable);
		}
	}

	public static void runOnNewThread(Runnable runnable) {
		runOnNewThread(runnable, 0);
	}

	public static void runOnNewThread(final Runnable runnable, final long delay) {
		if (delay > 0) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(delay);
					} catch (Exception e) {
					}

					runnable.run();
				}
			}).start();
		} else {
			new Thread(runnable).start();
		}
	}
}
