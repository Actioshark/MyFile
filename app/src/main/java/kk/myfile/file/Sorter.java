package kk.myfile.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import kk.myfile.activity.BaseActivity.Classify;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.util.AppUtil;
import kk.myfile.util.DataUtil;
import kk.myfile.util.Logger;
import kk.myfile.util.Setting;

public class Sorter {
	public enum SortType {
		Directory, ModifyTime, Name, Path, Size, Subfix,
	}

	public static abstract class SortFactor implements Comparator<Leaf>, Cloneable {
		public final SortType type;
		public final String text;
		public boolean up;

		public SortFactor(SortType type) {
			this.type = type;

			this.text = AppUtil.getString("sort_" + type.name());
		}

		@Override
		public int compare(Leaf a, Leaf b) {
			int ret = cmp(a, b);

			if (up) {
				return ret;
			} else {
				return -ret;
			}
		}

		abstract protected int cmp(Leaf a, Leaf b);

		@Override
		public SortFactor clone() {
			SortFactor temp = createFactor(type.name());
			if (temp == null) {
				temp = new SortFactorDirectory();
			}

			temp.up = up;

			return temp;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SortFactor) {
				SortFactor temp = (SortFactor) obj;

				return type == temp.type && up == temp.up;
			}

			return false;
		}
	}

	public static class SortFactorDirectory extends SortFactor {
		public SortFactorDirectory() {
			super(SortType.Directory);
		}

		@Override
		public int cmp(Leaf a, Leaf b) {
			boolean ad = a instanceof Direct;
			boolean bd = b instanceof Direct;

			if (ad == bd) {
				return 0;
			} else if (ad) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	public static class SortFactorModifyTime extends SortFactor {
		public SortFactorModifyTime() {
			super(SortType.ModifyTime);
		}

		@Override
		public int cmp(Leaf a, Leaf b) {
			long at = a.getFile().lastModified();
			long bt = b.getFile().lastModified();

			if (at < bt) {
				return -1;
			} else if (at > bt) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public static class SortFactorName extends SortFactor {
		public SortFactorName() {
			super(SortType.Name);
		}

		@Override
		public int cmp(Leaf a, Leaf b) {
			String an = DataUtil.getFileName(a.getPath()).toLowerCase(Setting.LOCALE);
			String bn = DataUtil.getFileName(b.getPath()).toLowerCase(Setting.LOCALE);

			return an.compareTo(bn);
		}
	}

	public static class SortFactorPath extends SortFactor {
		public SortFactorPath() {
			super(SortType.Path);
		}

		@Override
		public int cmp(Leaf a, Leaf b) {
			String ap = a.getPath().toLowerCase(Setting.LOCALE);
			String bp = b.getPath().toLowerCase(Setting.LOCALE);

			return ap.compareTo(bp);
		}
	}

	public static class SortFactorSize extends SortFactor {
		public SortFactorSize() {
			super(SortType.Size);
		}

		@Override
		public int cmp(Leaf a, Leaf b) {
			long al = a.getFile().length();
			long bl = b.getFile().length();

			if (al < bl) {
				return -1;
			} else if (al > bl) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public static class SortFactorSubfix extends SortFactor {
		public SortFactorSubfix() {
			super(SortType.Subfix);
		}

		@Override
		public int cmp(Leaf a, Leaf b) {
			String as = DataUtil.getSubfix(a.getPath());
			if (as == null) {
				as = "";
			}

			String bs = DataUtil.getSubfix(b.getPath());
			if (bs == null) {
				bs = "";
			}

			return as.compareTo(bs);
		}
	}

	private static final Map<Classify, List<SortFactor>> sFactors = new HashMap<Classify, List<SortFactor>>();

	private static SortFactor createFactor(String type) {
		SortFactor factor = null;

		try {
			factor = (SortFactor) Class.forName("kk.myfile.file.Sorter$SortFactor" + type)
				.newInstance();
		} catch (Exception e) {
			Logger.print(e, type);
		}

		return factor;
	}

	private static List<SortFactor> cloneFactors(List<SortFactor> factors) {
		List<SortFactor> list = new ArrayList<SortFactor>(factors.size());

		for (SortFactor factor : factors) {
			SortFactor temp = factor.clone();

			list.add(temp);
		}

		return list;
	}

	public static synchronized void setFactors(Classify classify, List<SortFactor> factors) {
		sFactors.put(classify, cloneFactors(factors));

		try {
			JSONArray ja = new JSONArray();

			int len = factors.size();
			for (int i = 0; i < len; i++) {
				SortFactor factor = factors.get(i);
				JSONObject jo = new JSONObject();

				jo.put("type", factor.type.name());
				jo.put("up", factor.up);

				ja.put(i, jo);
			}

			Setting.setSortFactor(classify, ja.toString());
		} catch (Exception e) {
			Logger.print(e);
		}
	}

	public static synchronized List<SortFactor> getFactors(Classify classify) {
		List<SortFactor> list = sFactors.get(classify);

		if (list == null) {
			list = new ArrayList<SortFactor>();

			SortFactor[] array;
			if (classify == Classify.Type) {
				array = new SortFactor[] {
					new SortFactorPath(), new SortFactorName(), new SortFactorModifyTime(),
					new SortFactorSize(), new SortFactorSubfix(),
				};
			} else {
				array = new SortFactor[] {
					new SortFactorDirectory(), new SortFactorName(), new SortFactorModifyTime(),
					new SortFactorSize(), new SortFactorSubfix(),
				};
			}

			try {
				String str = Setting.getSortFactor(classify);
				JSONArray ja = new JSONArray(str);

				int len = ja.length();
				for (int i = 0; i < len; i++) {
					JSONObject jo = ja.getJSONObject(i);

					String type = jo.getString("type");
					boolean up = jo.getBoolean("up");

					SortFactor factor = createFactor(type);
					if (factor != null) {
						factor.up = up;

						for (int j = 0; j < array.length; j++) {
							if (array[j] != null && array[j].type == factor.type) {
								list.add(factor);
								array[j] = null;
							}
						}
					}
				}
			} catch (Exception e) {
				Logger.print(e);
			}

			for (int j = 0; j < array.length; j++) {
				if (array[j] != null) {
					list.add(array[j]);
				}
			}

			sFactors.put(classify, list);
		}

		return cloneFactors(list);
	}

	private static int compare(List<SortFactor> factors, Leaf a, Leaf b) {
		for (SortFactor factor : factors) {
			int ret = factor.compare(a, b);
			if (ret != 0) {
				return ret;
			}
		}

		return 0;
	}

	public static void sort(Classify classify, List<? extends Leaf> list) {
		final List<SortFactor> factors = getFactors(classify);

		Collections.sort(list, new Comparator<Leaf>() {
			@Override
			public int compare(Leaf a, Leaf b) {
				return Sorter.compare(factors, a, b);
			}
		});
	}

	public static void sort(Classify classify, Leaf[] list) {
		final List<SortFactor> factors = getFactors(classify);

		Arrays.sort(list, new Comparator<Leaf>() {
			@Override
			public int compare(Leaf a, Leaf b) {
				return Sorter.compare(factors, a, b);
			}
		});
	}

	public static <T extends Leaf> void insert(Classify classify, List<T> list, T data) {
		final List<SortFactor> factors = getFactors(classify);
		int len = list.size();

		for (int i = 0; i < len; i++) {
			if (compare(factors, data, list.get(i)) < 0) {
				list.add(i, data);
				return;
			}
		}

		list.add(data);
	}
}
