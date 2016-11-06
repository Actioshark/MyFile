package kk.myfile.util;

import android.util.Log;

public class Logger {
	public static boolean DEBUG_ENABLE = true;

	public static void print(String tag, Throwable throwable, Object... args) {
		if (!DEBUG_ENABLE) {
			return;
		}

		if (tag == null) {
			tag = "DEBUG";
		}

		StringBuilder sb = new StringBuilder();

		StackTraceElement[] stack = new Throwable().getStackTrace();
		if (stack.length > 1) {
			StackTraceElement element = stack[1];

			sb.append(element.getFileName()).append('-').append(element.getLineNumber())
				.append(':').append(element.getMethodName()).append("()\n");
		}

		for (Object obj : args) {
			sb.append(String.valueOf(obj)).append(' ');
		}

		Log.d(tag, sb.toString());

		Log.d(tag, "", throwable);
	}

	public static void print(String tag, Object... args) {
		if (!DEBUG_ENABLE) {
			return;
		}

		if (tag == null) {
			tag = "DEBUG";
		}

		StringBuilder sb = new StringBuilder();

		StackTraceElement[] stack = new Throwable().getStackTrace();
		if (stack.length > 1) {
			StackTraceElement element = stack[1];

			sb.append(element.getFileName()).append('-').append(element.getLineNumber())
				.append(':').append(element.getMethodName()).append("()\n");
		}

		for (Object obj : args) {
			sb.append(String.valueOf(obj)).append(' ');
		}

		Log.d(tag, sb.toString());
	}
}
