package kk.myfile.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import kk.myfile.R;
import kk.myfile.file.FileUtil;
import kk.myfile.leaf.Audio;
import kk.myfile.leaf.Image;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.Text;
import kk.myfile.leaf.Video;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.SimpleDialog;

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
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

			Uri uri =  UriUtil.getUri(context, leaf.getFile());
			intent.setDataAndType(uri, type);

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
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			intent.setType(type);

			ArrayList<Uri> uris = new ArrayList<>();
			for (Leaf leaf : list) {
				Uri uri = UriUtil.getUri(context, leaf.getFile());
				uris.add(uri);
			}
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			Logger.print(null, e);
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
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			intent.setDataAndType(UriUtil.getUri(context, leaf.getFile()), type);

			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static interface IAsListener {
		public void onSelect(String type);
	}
	
	public static void showAsDialog(Context context, int msg, final IAsListener listener) {
		SimpleDialog sd = new SimpleDialog(context);
		sd.setCanceledOnTouchOutside(true);
		sd.setMessage(msg);
		sd.setButtons(R.string.type_text, R.string.type_image,
			R.string.type_audio, R.string.type_video,
			R.string.word_any);
		sd.setClickListener(new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index, ClickType type) {
				if (index == -1) {
					return;
				}
				
				String tp;
				
				switch (index) {
				case 0:
					tp = Text.TYPE;
					break;

				case 1:
					tp = Image.TYPE;
					break;

				case 2:
					tp = Audio.TYPE;
					break;

				case 3:
					tp = Video.TYPE;
					break;

				default:
					tp = "*/*";
					break;
				}
				
				listener.onSelect(tp);

				dialog.dismiss();
			}
		});
		sd.show();
	}
}
