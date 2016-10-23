package kk.myfile.leaf;

import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;

import kk.myfile.R;
import kk.myfile.file.ImageUtil.IThumable;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;
import kk.myfile.util.MathUtil;

public class Video extends Leaf implements IThumable {
	public static final String TYPE = "video/*";
	
	public static final int COLOR = 0xffcc00cc;
	
	public Video(String path) {
		super(path);
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_video;
	}
	
	@Override
	public Drawable getThum(int width, int height) throws Exception {
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		mmr.setDataSource(mPath);
		Bitmap bmp = mmr.getFrameAtTime();
		return new BitmapDrawable(AppUtil.getRes(), bmp);
	}
	
	@Override
	public int getTypeName() {
		return R.string.type_video;
	}
	
	@SuppressLint("InlinedApi")
	public Map<String, String> getDetail() {
		Map<String, String> map = super.getDetail();
		
		try {
			MediaMetadataRetriever mmr = new MediaMetadataRetriever();
			mmr.setDataSource(mPath);
			
			putDetail(map, R.string.word_album, mmr.extractMetadata(
				MediaMetadataRetriever.METADATA_KEY_ALBUM));
			putDetail(map, R.string.word_artist, mmr.extractMetadata(
				MediaMetadataRetriever.METADATA_KEY_ARTIST));
			putDetail(map, R.string.word_author, mmr.extractMetadata(
				MediaMetadataRetriever.METADATA_KEY_AUTHOR));
			putDetail(map, R.string.word_title, mmr.extractMetadata(
				MediaMetadataRetriever.METADATA_KEY_TITLE));
			putDetail(map, R.string.word_compilation, mmr.extractMetadata(
				MediaMetadataRetriever.METADATA_KEY_COMPILATION));
			putDetail(map, R.string.word_composer, mmr.extractMetadata(
				MediaMetadataRetriever.METADATA_KEY_COMPOSER));
			
			putDetail(map, R.string.word_date, mmr.extractMetadata(
				MediaMetadataRetriever.METADATA_KEY_DATE));
			putDetail(map, R.string.word_year, mmr.extractMetadata(
				MediaMetadataRetriever.METADATA_KEY_YEAR));
			
			putDetail(map, R.string.word_minetype, mmr.extractMetadata(
				MediaMetadataRetriever.METADATA_KEY_MIMETYPE));
			
			if (Build.VERSION.SDK_INT >= 14) {
				String bitrate = MathUtil.insertComma(mmr.extractMetadata(
					MediaMetadataRetriever.METADATA_KEY_BITRATE));
				putDetail(map, R.string.word_bitrate, "%s b/s", bitrate);
			}
			
			int dur = Integer.valueOf(mmr.extractMetadata(
				MediaMetadataRetriever.METADATA_KEY_DURATION));
			int second = (dur /= 1000) % 60;
			int minute = (dur /= 60) % 60;
			int hour = dur /= 60;
			putDetail(map, R.string.word_duration, "%02d:%02d:%02d", hour, minute, second);
			
			if (Build.VERSION.SDK_INT >= 14) {
				putDetail(map, R.string.word_width,mmr.extractMetadata(
					MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
				putDetail(map, R.string.word_height,mmr.extractMetadata(
					MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
				putDetail(map, R.string.word_rotation, mmr.extractMetadata(
					MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}
		
		return map;
	}
}
