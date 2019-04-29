package kk.myfile.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.StatFs;
import android.webkit.MimeTypeMap;
import kk.myfile.R;
import kk.myfile.leaf.Audio;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Image;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.Text;
import kk.myfile.leaf.Unknown;
import kk.myfile.leaf.Video;
import kk.myfile.util.AppUtil;
import kk.myfile.util.DataUtil;
import kk.myfile.util.Logger;
import kk.myfile.util.Setting;

public class FileUtil {
	private static JSONObject sTypeMap;

	private static final char[] ILLEGAL_FILE_NAME_CHAR = {
		'/', '\0', '\\',
	};

	public static void init(Context context) {
		if (sTypeMap != null) {
			return;
		}

		AssetManager sm = context.getAssets();
		try {
			InputStream is = sm.open("file_type.json");
			String string = FileUtil.readString(is);
			sTypeMap = new JSONObject(string);
		} catch (Exception e) {
			Logger.print(null, e);
		}
	}

	public static String getType(Leaf leaf) {
		MimeTypeMap mtm = MimeTypeMap.getSingleton();
		String subfix = DataUtil.getSubfix(leaf.getPath());
		
		if (mtm.hasExtension(subfix)) {
			return mtm.getMimeTypeFromExtension(subfix);
		} else {
			return null;
		}
	}

	public static Leaf createLeaf(File file) {
		String path = file.getAbsolutePath();
		
		if (file.isDirectory()) {
			return new Direct(path);
		} else {
			return createTempLeaf(path);
		}
	}
	
	public static Leaf createTempLeaf(String path) {
		try {
			MimeTypeMap mtm = MimeTypeMap.getSingleton();
			String subfix = DataUtil.getSubfix(path);
			
			if (mtm.hasExtension(subfix)) {
				String type = mtm.getMimeTypeFromExtension(subfix);
				
				if (type.startsWith("text/")) {
					return new Text(path);
				}
				
				if (type.startsWith("image/")) {
					return new Image(path);
				}
				
				if (type.startsWith("audio/")) {
					return new Audio(path);
				}
				
				if (type.startsWith("video/")) {
					return new Video(path);
				}
				
				if (type.startsWith("application/")) {
					if (sTypeMap.has(subfix)) {
						String cls = sTypeMap.getString(subfix);
						Class<?> clazz = Class.forName(String.format("kk.myfile.leaf.%s", cls));
						Constructor<?> ct = clazz.getConstructor(String.class);
	
						Leaf leaf = (Leaf) ct.newInstance(path);
						return leaf;
					}
				}
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}
		
		Leaf leaf = new Unknown(path);
		return leaf;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static long getTotalSize() {
		StatFs sf = new StatFs(Setting.DEFAULT_PATH);

		if (Build.VERSION.SDK_INT < 18) {
			return sf.getBlockSize() * sf.getBlockCount();
		} else {
			return sf.getTotalBytes();
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static long getAvailSize() {
		StatFs sf = new StatFs(Setting.DEFAULT_PATH);

		if (Build.VERSION.SDK_INT < 18) {
			return sf.getBlockSize() * sf.getAvailableBlocks();
		} else {
			return sf.getAvailableBytes();
		}
	}

	public static boolean isLink(File file) {
		try {
			File canon;
			File par = file.getParentFile();
			if (par == null) {
				canon = file;
			} else {
				canon = new File(par.getCanonicalFile(), file.getName());
			}

			return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
		} catch (Exception e) {
			return false;
		}
	}

	public static String readString(InputStream is) throws Exception {
		byte[] buf = new byte[1024 * 1024];
		int len;
		StringBuilder sb = new StringBuilder();

		while ((len = is.read(buf)) > 0) {
			sb.append(new String(buf, 0, len));
		}

		return sb.toString();
	}

	public static String checkNewName(String parent, String name) {
		if (name == null || name.length() < 1 || name.length() > 255) {
			return AppUtil.getString(R.string.err_name_length_valid, 1, 255);
		}

		for (char ch : ILLEGAL_FILE_NAME_CHAR) {
			if (name.contains(String.valueOf(ch))) {
				return AppUtil.getString(R.string.err_illegal_file_name_char);
			}
		}

		if (new File(parent, name).exists()) {
			return AppUtil.getString(R.string.err_file_exist);
		}

		return null;
	}

	public static String createDirect(File file) {
		try {
			if (file.mkdirs()) {
				return null;
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}

		return AppUtil.getString(R.string.err_create_direct_failed);
	}

	public static String createFile(File file) {
		try {
			File parent = file.getParentFile();
			if (!parent.exists()) {
				if (!parent.mkdirs()) {
					return AppUtil.getString(R.string.err_path_not_valid);
				}
			}
			
			if (file.createNewFile()) {
				return null;
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}

		return AppUtil.getString(R.string.err_create_file_failed);
	}

	public static String rename(File o, File n) {
		try {
			if (o.renameTo(n)) {
				return null;
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}

		return AppUtil.getString(R.string.err_rename_file_failed);
	}
	
	public static String delete(File file) {
		return delete(file, false);
	}

	public static String delete(File file, boolean childOnly) {
		try {
			List<File> list = new ArrayList<File>();
			list.add(file);

			for (int i = 0; i < list.size(); i++) {
				File temp = list.get(i);

				if (temp.isDirectory()) {
					for (File child : temp.listFiles()) {
						list.add(child);
					}
				}
			}
			
			if (childOnly) {
				list.remove(0);
			}

			boolean success = true;

			for (int i = list.size() - 1; i >= 0; i--) {
				success = list.get(i).delete() && success;
			}

			if (success) {
				return null;
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}

		String name;
		try {
			name = file.getName();
		} catch (Exception e) {
			name = "";
		}

		return AppUtil.getString(R.string.err_delete_file_failed, name);
	}

	public static boolean write(File from, File to) {
		try {
			InputStream is = new FileInputStream(from);
			OutputStream os = new FileOutputStream(to);
			byte[] buf = new byte[1024 * 1024];
			int len = 0;

			while ((len = is.read(buf)) != -1) {
				os.write(buf, 0, len);
			}

			is.close();
			os.close();

			return true;
		} catch (Exception e) {
			Logger.print(null, e);
		}

		return false;
	}

	public static boolean write(File from, File to, int xor) {
		try {
			InputStream is = new FileInputStream(from);
			OutputStream os = new FileOutputStream(to);
			byte[] buf = new byte[1024 * 1024];
			int len = 0;

			while ((len = is.read(buf)) != -1) {
				for (int i = 0; i < len; i++) {
					buf[i] = (byte) (buf[i] ^ xor);
				}

				os.write(buf, 0, len);
			}

			is.close();
			os.close();

			return true;
		} catch (Exception e) {
			Logger.print(null, e);
		}

		return false;
	}
}
