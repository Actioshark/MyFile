package kk.myfile.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.leaf.Leaf;
import kk.myfile.util.Logger;
import kk.myfile.util.UriUtil;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ClipData.Item;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

public class ClipBoard {
	public enum ClipType {
		Copy, Cut,
	}

	public static boolean put(Context context, String label, String text) {
		try {
			ClipData cd = ClipData.newPlainText(label, text);
			ClipboardManager cbm = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
			cbm.setPrimaryClip(cd);

			return true;
		} catch (Exception e) {
			Logger.print(null, e);
			return false;
		}
	}

	public static boolean put(Context context, ClipType clipType, List<Leaf> list) {
		try {
			Uri uri = UriUtil.getUri(context, list.get(0).getFile());

			ClipData cd = ClipData.newUri(context.getContentResolver(), clipType.name(), uri);

			for (int i = 1; i < list.size(); i++) {
				cd.addItem(new Item(UriUtil.getUri(context, list.get(i).getFile())));
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
		return getFiles(context).size() > 0;
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

					if (uri == null) {
						continue;
					}

					File file = UriUtil.getFile(context, uri);
					if (file != null && file.exists()) {
						list.add(file.getPath());
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
