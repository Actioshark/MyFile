package kk.myfile.leaf;

import java.util.Map;

import android.media.MediaPlayer;
import android.net.Uri;
import kk.myfile.R;
import kk.myfile.util.AppUtil;

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
	
	@SuppressWarnings("deprecation")
	public Map<String, String> getDetail() {
		Map<String, String> map = super.getDetail();
		
		try {
			MediaPlayer mp = MediaPlayer.create(AppUtil.getContext(), Uri.fromFile(getFile()));
			int dur = mp.getDuration();
			int second = (dur /= 1000) % 60;
			int minute = (dur /= 60) % 60;
			int hour = dur /= 60;
			map.put(AppUtil.getString(R.string.word_duration), String.format(
					"%02d:%02d:%02d", hour, minute, second));
		} catch (Exception e) {
		}
		
		return map;
	}
}
