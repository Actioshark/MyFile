package kk.myfile.util;

import android.util.Log;

public class Logger {
	public static boolean DEBUG_ENABLE = true;
	
	public static String DEF_TAG = "DEBUG";
	
	private static void insertElement(StringBuilder sb, StackTraceElement element) {
		sb.append(element.getFileName()).append('-').append(element.getLineNumber())
			.append(':').append(element.getMethodName()).append("()\n");
	}

	public static void print(String tag, Object... args) {
		if (args != null && args.length > 0 && args[0] instanceof Throwable) {
			Object[] temp = new Object[args.length - 1];
			for (int i = 0; i < temp.length; i++) {
				temp[i] = args[i + 1];
			}
			
			prints(tag, (Throwable) args[0], 2, temp);
		} else {
			prints(tag, null, 2, args);
		}
	}

	private static void prints(String tag, Throwable throwable, int trackIndex, Object... args) {
		if (!DEBUG_ENABLE) {
			return;
		}

		if (tag == null) {
			tag = DEF_TAG;
		}

		StringBuilder sb = new StringBuilder();

		StackTraceElement[] stack = new Throwable().getStackTrace();
		if (trackIndex >= 0 && trackIndex < stack.length) {
			insertElement(sb, stack[trackIndex]);
		} else {
			sb.append("-------------------------------\n");
			for (int i = stack.length - 1; i > 0; i--) {
				insertElement(sb, stack[i]);
			}
			sb.append("-------------------------------\n");
		}

		for (Object obj : args) {
			sb.append(String.valueOf(obj)).append(' ');
		}

		Log.d(tag, sb.toString());

		if (throwable != null) {
			Log.d(tag, "", throwable);
		}
	}
}
