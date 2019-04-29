package kk.myfile.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.leaf.Leaf;
import kk.myfile.util.Logger;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ClipData.Item;

import org.json.JSONArray;

public class ClipBoard {
	public enum ClipType {
		Copy, Cut,
	}

	public enum DataType {
		Text, File,
	}

	private static class Label {
		public ClipType clipType;
		public DataType dataType;
	}

	private static final String SEPARATOR = ",";

	private static boolean put(Context context, String label, List<String> texts) {
		try {
			ClipData cd = ClipData.newPlainText(label, texts.get(0));
			for (int i = 1; i < texts.size(); i++) {
				Item item = new Item(texts.get(i));
				cd.addItem(item);
			}

			ClipboardManager cbm = (ClipboardManager) context
					.getSystemService(Context.CLIPBOARD_SERVICE);
			cbm.setPrimaryClip(cd);

			return true;
		} catch (Exception e) {
			Logger.print(e);
			return false;
		}
	}

	private static String encodeLabel(ClipType clipType, DataType dataType) {
		return String.format("%s%s%s", clipType.name(), SEPARATOR, dataType.name());
	}

	private static Label decodeLabel(String label) {
		Label ret = new Label();

		try {
			String[] ls = label.split(SEPARATOR);

			ret.clipType = ClipType.valueOf(ls[0]);
			ret.dataType = DataType.valueOf(ls[1]);
		} catch (Exception e) {
		}

		return ret;
	}

	public static boolean put(Context context, String text) {
		String label = encodeLabel(ClipType.Copy, DataType.Text);

		List<String> texts = new ArrayList<>();
		texts.add(text);

		return put(context, label, texts);
	}

	public static boolean put(Context context, ClipType clipType, List<Leaf> leafs) {
		try {
			String label = encodeLabel(clipType, DataType.File);

			List<String> texts = new ArrayList<>();
			for (Leaf leaf : leafs) {
				texts.add(leaf.getPath());
			}

			return put(context, label, texts);
		} catch (Exception e) {
			Logger.print(e);
			return false;
		}
	}

	public static boolean hasFile(Context context) {
		return getFiles(context).size() > 0;
	}

	public static List<String> getFiles(Context context) {
		List<String> list = new ArrayList<>();

		try {
			ClipboardManager cbm = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);

			if (cbm.hasPrimaryClip()) {
				ClipData cd = cbm.getPrimaryClip();

				String ls = cd.getDescription().getLabel().toString();
				Label label = decodeLabel(ls);
				if (label.clipType == null || label.dataType != DataType.File) {
					return list;
				}

				for (int i = 0; i < cd.getItemCount(); i++) {
					Item item = cd.getItemAt(i);
					try {
						String path = item.getText().toString();
						File file = new File(path);
						if (file.exists()) {
							list.add(path);
						}
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
			Logger.print(e);
		}

		return list;
	}

	public static ClipType getType(Context context) {
		try {
			ClipboardManager cbm = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);

			if (cbm.hasPrimaryClip()) {
				ClipData cd = cbm.getPrimaryClip();
				String ls = cd.getDescription().getLabel().toString();
				Label label = decodeLabel(ls);
				return label.clipType;
			}
		} catch (Exception e) {
			Logger.print(e);
		}

		return null;
	}
}
