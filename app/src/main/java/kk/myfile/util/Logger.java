package kk.myfile.util;

import android.util.Log;

import java.util.Arrays;

public class Logger {
	private static void insertElement(StringBuilder sb, StackTraceElement element) {
		sb.append(element.getFileName()).append('-').append(element.getLineNumber())
			.append(':').append(element.getMethodName()).append("()\n");
	}

	public static void print(Object... args) {
		if (args != null && args.length > 0 && args[0] instanceof Throwable) {
			Object[] temp = Arrays.copyOfRange(args, 1, args.length);
			prints((Throwable) args[0], temp);
		} else {
			prints(null, args);
		}
	}

	private static void prints(Throwable throwable, Object... args) {
		StringBuilder sb = new StringBuilder();

		StackTraceElement[] stack = new Throwable().getStackTrace();
		if (stack.length > 2) {
			insertElement(sb, stack[2]);
		} else {
			sb.append("-------------------------------\n");
			for (int i = stack.length - 1; i > 0; i--) {
				insertElement(sb, stack[i]);
			}
			sb.append("-------------------------------\n");
		}

		for (Object obj : args) {
			sb.append(obj).append(' ');
		}

		String tag = "MyFile";

		Log.d(tag, sb.toString());

		if (throwable != null) {
			Log.d(tag, "", throwable);
		}
	}
}
