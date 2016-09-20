package kk.myfile.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import kk.myfile.leaf.Leaf;

public class IntentUtil {
	public static boolean view(Context context, Leaf leaf, String type) {
		try {
			if (type == null) {
				type = leaf.getType();
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
			return false;
		}
	}
}
