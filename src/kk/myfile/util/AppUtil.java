package kk.myfile.util;

import java.io.DataOutputStream;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class AppUtil {
	private static Context sContext;
	private static Resources sRes;

	private static String sPackageName;

	private static Handler sHandler;

	private static float sDensity;

	private static int sScreenWidth;
	private static int sScreenHeight;
	public static int sStatusBarHeight = 0;

	public static void init(Context context) {
		if (sRes != null) {
			return;
		}

		sContext = context;
		sRes = context.getResources();

		sHandler = new Handler(context.getMainLooper());

		sPackageName = context.getPackageName();

		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		manager.getDefaultDisplay().getMetrics(metrics);

		sDensity = metrics.density;

		sScreenWidth = metrics.widthPixels;
		sScreenHeight = metrics.heightPixels;

		int id = sRes.getIdentifier("status_bar_height", "dimen", "android");
		if (id > 0) {
			sStatusBarHeight = sRes.getDimensionPixelSize(id);
		}
	}

	@Deprecated
	public static Context getContext() {
		return sContext;
	}

	public static Resources getRes() {
		return sRes;
	}

	public static String getPackageName() {
		return sPackageName;
	}

	public static int getId(String type, String name) {
		return sRes.getIdentifier(name, type, sPackageName);
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

	public static int getPixcel(float dp) {
		return (int) (dp * sDensity);
	}

	public static int getScreenWidth() {
		return sScreenWidth;
	}

	public static int getScreenHeight() {
		return sScreenHeight - sStatusBarHeight;
	}

	public static int getStatusBarHeight() {
		return sStatusBarHeight;
	}

	public static void exitApp() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public static void runOnUiThread(Runnable runnable) {
		sHandler.post(runnable);
	}

	public static void runOnUiThread(Runnable runnable, long delay) {
		sHandler.postDelayed(runnable, delay);
	}

	public static Runnable runOnUiThread(final Runnable runnable, long delay, final long repeat) {
		Runnable repeater = new Runnable() {
			@Override
			public void run() {
				sHandler.postDelayed(this, repeat);
				runnable.run();
			}
		};

		sHandler.postDelayed(repeater, delay);

		return repeater;
	}

	public static void removeUiThread(final Runnable runnable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				sHandler.removeCallbacks(runnable);
			}
		});
	}

	public static void runOnNewThread(Runnable runnable) {
		new Thread(runnable).start();
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

	public static void requestRoot() {
		Process process = null;
		DataOutputStream os = null;
		
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("chmod 777 " + sContext.getPackageCodePath() + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			Logger.print(null, e);
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
	}
}
