package kk.myfile.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;

import kk.myfile.file.FileUtil;
import kk.myfile.leaf.Leaf;

public class UriUtil {
    public static Uri getUri(Context context, File file) {
        Leaf leaf = FileUtil.createLeaf(file);
        String type = FileUtil.getType(leaf);
        if (type != null && type.startsWith("image/")) {
            try {
                String url = MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getPath(), "", "");
                return Uri.parse(url);
            } catch (Exception e) {
                Logger.print(e);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    public static File getFile(Context context, Uri uri) {
        String scheme = uri.getScheme();

        if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            try {
                String[] filePathColumn = {MediaStore.MediaColumns.DATA};
                String filePath = null;

                Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    filePath = cursor.getString(columnIndex);
                }

                cursor.close();

                return new File(filePath);
            } catch (Exception e) {
                Logger.print(e);
            }
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            try {
                String filePath = uri.getPath();
                return new File(filePath);
            } catch (Exception e) {
                Logger.print(e);
            }
        }

        return null;
    }
}
