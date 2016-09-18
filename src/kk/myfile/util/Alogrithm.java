package kk.myfile.util;

import java.util.Comparator;

public class Alogrithm {
	private static final int SELECT_THRESHOLD = 8;

	public static <T> void selectSort(T[] a, int from, int to, Comparator<? super T> cmp) {
		for (int i = from; i < to; i++) {
			int sel = i;

			for (int j = i + 1; j < to; j++) {
				if (cmp.compare(a[sel], a[j]) > 0) {
					sel = j;
				}
			}

			T temp = a[i];
			a[i] = a[sel];
			a[sel] = temp;
		}
	}

	public static <T> void quickSort(T[] a, int from, int to, Comparator<? super T> cmp) {
		if (to < from + SELECT_THRESHOLD) {
			selectSort(a, from, to, cmp);
			return;
		}

		int i = from, j = to - 1;
		boolean dir = true;

		while (i < j) {
			if (cmp.compare(a[i], a[j]) > 0) {
				T t = a[i];
				a[i] = a[j];
				a[j] = t;

				dir = !dir;
			}

			if (dir) {
				j--;
			} else {
				i++;
			}
		}

		quickSort(a, from, i, cmp);
		quickSort(a, i + 1, to, cmp);
	}
}
