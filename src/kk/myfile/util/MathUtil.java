package kk.myfile.util;

import android.graphics.PointF;

public class MathUtil {
	public static float distance(float x0, float y0, float x1, float y1) {
		return distance(x1 - x0, y1 - y0);
	}

	public static float distance(float dx, float dy) {
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	public static float getRotation(float x0, float y0, float x1, float y1) {
		return getRotation(x1 - x0, y1 - y0);
	}

	public static float getRotation(float dx, float dy) {
		return (float) Math.atan2(dy, dx);
	}

	public static PointF getPoint(float x, float y, float[] matrix) {
		PointF point = new PointF();
		point.x = matrix[0] * x + matrix[1] * y + matrix[2];
		point.y = matrix[3] * x + matrix[4] * y + matrix[5];
		return point;
	}

	public static int min(int... values) {
		int m = Integer.MAX_VALUE;

		for (int v : values) {
			if (v < m) {
				m = v;
			}
		}

		return m;
	}

	public static long min(long... values) {
		long m = Long.MAX_VALUE;

		for (long v : values) {
			if (v < m) {
				m = v;
			}
		}

		return m;
	}

	public static float min(float... values) {
		float m = Float.MAX_VALUE;

		for (float v : values) {
			if (v < m) {
				m = v;
			}
		}

		return m;
	}

	public static double min(double... values) {
		double m = Double.MAX_VALUE;

		for (double v : values) {
			if (v < m) {
				m = v;
			}
		}

		return m;
	}

	public static int max(int... values) {
		int m = Integer.MIN_VALUE;

		for (int v : values) {
			if (v > m) {
				m = v;
			}
		}

		return m;
	}

	public static long max(long... values) {
		long m = Long.MIN_VALUE;

		for (long v : values) {
			if (v > m) {
				m = v;
			}
		}

		return m;
	}

	public static float max(float... values) {
		float m = -Float.MAX_VALUE;

		for (float v : values) {
			if (v > m) {
				m = v;
			}
		}

		return m;
	}

	public static double max(double... values) {
		double m = -Double.MAX_VALUE;

		for (double v : values) {
			if (v > m) {
				m = v;
			}
		}

		return m;
	}
}
