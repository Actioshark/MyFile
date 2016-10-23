package kk.myfile.leaf;

import java.util.Map;

import android.annotation.SuppressLint;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import kk.myfile.R;
import kk.myfile.util.Logger;
import kk.myfile.util.MathUtil;

public class Audio extends Leaf {
	public static final String TYPE = "audio/*";
	
	public static final int COLOR = 0xffcc0000;
	
	public Audio(String path) {
		super(path);
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_audio;
	}
	
	@Override
	public int getTypeName() {
		return R.string.type_audio;
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
		} catch (Exception e) {
			Logger.print(null, e);
		}
		
		return map;
	}
}
