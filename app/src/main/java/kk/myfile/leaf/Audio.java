package kk.myfile.leaf;

import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;

import kk.myfile.R;
import kk.myfile.adapter.DetailItemAdapter.Data;
import kk.myfile.file.ImageUtil.IThumable;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;
import kk.myfile.util.MathUtil;

public class Audio extends Leaf implements IThumable {
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
	public Drawable getThum(int width, int height) throws Exception {
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		mmr.setDataSource(mPath);
		byte[] data = mmr.getEmbeddedPicture();
		Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
		return new BitmapDrawable(AppUtil.getRes(), bmp);
	}

	@SuppressLint("InlinedApi")
	public List<Data> getDetail() {
		List<Data> list = super.getDetail();

		try {
			MediaMetadataRetriever mmr = new MediaMetadataRetriever();
			mmr.setDataSource(mPath);

			putDetail(list, 2, R.string.word_album, mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
			putDetail(list, 2, R.string.word_artist, mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
			putDetail(list, 2, R.string.word_author, mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR));
			putDetail(list, 2, R.string.word_title, mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
			putDetail(list, 2, R.string.word_compilation, mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPILATION));
			putDetail(list, 2, R.string.word_composer, mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER));

			putDetail(list, 2, R.string.word_date, mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE));
			putDetail(list, 2, R.string.word_year, mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR));

			putDetail(list, 2, R.string.word_minetype, mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE));

			if (Build.VERSION.SDK_INT >= 14) {
				String bitrate = MathUtil.insertComma(mmr
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
				putDetail(list, 2, R.string.word_bitrate, "%s b/s", bitrate);
			}

			int dur = Integer.valueOf(mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
			int second = (dur /= 1000) % 60;
			int minute = (dur /= 60) % 60;
			int hour = dur /= 60;
			putDetail(list, 2, R.string.word_duration, "%02d:%02d:%02d", hour, minute, second);
		} catch (Exception e) {
			Logger.print(e);
		}

		return list;
	}
}
