package kk.myfile.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.leaf.Leaf;
import kk.myfile.util.Logger;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ClipData.Item;
import android.net.Uri;

public class ClipBoard {
	public static enum ClipType {
		Copy, Cut,
	}

	public static boolean put(Context context, ClipType clipType, List<Leaf> list) {
		try {
			ClipData cd = ClipData.newUri(context.getContentResolver(), clipType.name(), Uri
				.fromFile(list.get(0).getFile()));

			for (int i = 1; i < list.size(); i++) {
				cd.addItem(new Item(Uri.fromFile(list.get(i).getFile())));
			}

			ClipboardManager cbm = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
			cbm.setPrimaryClip(cd);

			return true;
		} catch (Exception e) {
			Logger.print(null, e);
			return false;
		}
	}

	public static boolean hasFile(Context context) {
		try {
			ClipboardManager cbm = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);

			if (cbm.hasPrimaryClip()) {
				ClipData cd = cbm.getPrimaryClip();

				for (int i = 0; i < cd.getItemCount(); i++) {
					Item item = cd.getItemAt(i);
					Uri uri = item.getUri();

					if (uri != null && ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
						try {
							if (new File(uri.getPath()).exists()) {
								return true;
							}
						} catch (Exception e) {
							Logger.print(null, e);
						}
					}
				}
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}

		return false;
	}

	public static List<String> getFiles(Context context) {
		List<String> list = new ArrayList<String>();

		try {
			ClipboardManager cbm = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);

			if (cbm.hasPrimaryClip()) {
				ClipData cd = cbm.getPrimaryClip();

				for (int i = 0; i < cd.getItemCount(); i++) {
					Item item = cd.getItemAt(i);
					Uri uri = item.getUri();

					if (uri != null && ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
						try {
							if (new File(uri.getPath()).exists()) {
								list.add(uri.getPath());
							}
						} catch (Exception e) {
							Logger.print(null, e);
						}
					}
				}
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}

		return list;
	}

	public static ClipType getType(Context context) {
		try {
			ClipboardManager cbm = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);

			if (cbm.hasPrimaryClip()) {
				ClipData cd = cbm.getPrimaryClip();
				String label = cd.getDescription().getLabel().toString();
				return ClipType.valueOf(label);
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}

		return null;
	}
}
