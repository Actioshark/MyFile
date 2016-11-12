package kk.myfile.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import kk.myfile.file.FileUtil;
import kk.myfile.leaf.Leaf;

public class IntentUtil {
	public static boolean view(Context context, Leaf leaf, String type) {
		try {
			if (type == null) {
				type = FileUtil.getType(leaf);
			}

			if (type == null) {
				return false;
			}

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.fromFile(leaf.getFile()), type);

			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			Logger.print(null, e);
			return false;
		}
	}

	public static boolean share(Context context, List<Leaf> list, String type) {
		try {
			if (type == null) {
				for (Leaf leaf : list) {
					String t = FileUtil.getType(leaf);

					if (type == null) {
						if (t == null) {
							type = "*/*";
							break;
						}

						type = t;
					} else {
						if (t == null) {
							type = "*/*";
							break;
						}

						if (type.equals(t)) {
							continue;
						}

						int index = type.indexOf('/');
						if (index != -1) {
							String head = type.substring(0, index + 1);
							if (t.startsWith(head)) {
								type = head + "*";
								continue;
							}
						}

						type = "*/*";
					}
				}
			}

			if (type == null) {
				return false;
			}

			Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setType(type);

			ArrayList<Uri> uris = new ArrayList<Uri>();
			for (Leaf leaf : list) {
				uris.add(Uri.fromFile(leaf.getFile()));
			}
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean edit(Context context, Leaf leaf, String type) {
		try {
			if (type == null) {
				type = FileUtil.getType(leaf);
			}

			if (type == null) {
				return false;
			}

			Intent intent = new Intent(Intent.ACTION_EDIT);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.fromFile(leaf.getFile()), type);

			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
