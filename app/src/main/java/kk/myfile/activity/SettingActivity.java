package kk.myfile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

import kk.myfile.R;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Setting;

public class SettingActivity extends BaseActivity {
	static final long NUM_LIMIT_DELAY = 500;
	static final long NUM_LIMIT_REPEAT = 20;

	private Runnable mNumLimitToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_setting);

		// 返回
		findViewById(R.id.ll_menu).findViewById(R.id.iv_back).setOnClickListener(
			new OnClickListener() {
				@Override
				public void onClick(View view) {
					finish();
				}
			});

		// 目录文件排序
		findViewById(R.id.ll_sort_direct).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SettingSortActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Direct.name());
				startActivity(intent);
			}
		});

		// 分类文件排序
		findViewById(R.id.ll_sort_type).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SettingSortActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Type.name());
				startActivity(intent);
			}
		});

		// 目录文件列表视图
		findViewById(R.id.ll_list_style_direct).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SettingListStyleActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Direct.name());
				startActivity(intent);
			}
		});

		// 分类文件列表视图
		findViewById(R.id.ll_list_style_type).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SettingListStyleActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Type.name());
				startActivity(intent);
			}
		});

		// 大文件列表视图
		findViewById(R.id.ll_list_style_big).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SettingListStyleActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Big.name());
				startActivity(intent);
			}
		});

		// 最近文件列表视图
		findViewById(R.id.ll_list_style_recent).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SettingListStyleActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Recent.name());
				startActivity(intent);
			}
		});

		// 隐藏文件
		View llHidden = findViewById(R.id.ll_hidden);
		final ImageView ivHiddenSelect = llHidden.findViewById(R.id.iv_select);
		ivHiddenSelect.setImageResource(Setting.getShowHidden() ? R.drawable.single_select_pre
			: R.drawable.single_select_nor);
		llHidden.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean show = !Setting.getShowHidden();
				Setting.setShowHidden(show);
				ivHiddenSelect.setImageResource(show ? R.drawable.single_select_pre
					: R.drawable.single_select_nor);
			}
		});

		// 大文件数量限制
		View llBigLimit = findViewById(R.id.ll_num_limit_big);
		final TextView tvBigLimitNum = llBigLimit.findViewById(R.id.tv_num);
		tvBigLimitNum.setText(String.format(Setting.LOCALE, "%d", Setting.getNumLimit(Classify.Big)));
		llBigLimit.findViewById(R.id.iv_up).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			}
		});
		llBigLimit.findViewById(R.id.iv_up).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Setting.setNumLimit(Classify.Big, Setting.getNumLimit(Classify.Big) + 1);
					tvBigLimitNum.setText(String.format(Setting.LOCALE, "%d", Setting.getNumLimit(Classify.Big)));

					mNumLimitToken = AppUtil.runOnUiThread(new Runnable() {
						public void run() {
							Setting
								.setNumLimit(Classify.Big, Setting.getNumLimit(Classify.Big) + 1);
							tvBigLimitNum.setText(String.format(Setting.LOCALE, "%d", Setting
								.getNumLimit(Classify.Big)));
						}
					}, NUM_LIMIT_DELAY, NUM_LIMIT_REPEAT);
				} else if (event.getAction() == MotionEvent.ACTION_UP
					|| event.getAction() == MotionEvent.ACTION_CANCEL) {

					AppUtil.removeUiThread(mNumLimitToken);
					mNumLimitToken = null;
				}

				return view.onTouchEvent(event);
			}
		});
		llBigLimit.findViewById(R.id.iv_dn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			}
		});
		llBigLimit.findViewById(R.id.iv_dn).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Setting.setNumLimit(Classify.Big, Setting.getNumLimit(Classify.Big) - 1);
					tvBigLimitNum.setText(String.format(Setting.LOCALE, "%d", Setting.getNumLimit(Classify.Big)));

					mNumLimitToken = AppUtil.runOnUiThread(new Runnable() {
						public void run() {
							Setting
								.setNumLimit(Classify.Big, Setting.getNumLimit(Classify.Big) - 1);
							tvBigLimitNum.setText(String.format(Setting.LOCALE, "%d", Setting
								.getNumLimit(Classify.Big)));
						}
					}, NUM_LIMIT_DELAY, NUM_LIMIT_REPEAT);
				} else if (event.getAction() == MotionEvent.ACTION_UP
					|| event.getAction() == MotionEvent.ACTION_CANCEL) {

					AppUtil.removeUiThread(mNumLimitToken);
					mNumLimitToken = null;
				}

				return view.onTouchEvent(event);
			}
		});

		// 最近文件数量限制
		View llRecentLimit = findViewById(R.id.ll_num_limit_recent);
		final TextView tvRecentLimitNum = llRecentLimit.findViewById(R.id.tv_num);
		tvRecentLimitNum.setText(String.format(Setting.LOCALE, "%d", Setting.getNumLimit(Classify.Recent)));
		llRecentLimit.findViewById(R.id.iv_up).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			}
		});
		llRecentLimit.findViewById(R.id.iv_up).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Setting.setNumLimit(Classify.Recent, Setting.getNumLimit(Classify.Recent) + 1);
					tvRecentLimitNum.setText(String.format(Setting.LOCALE, "%d", Setting
						.getNumLimit(Classify.Recent)));

					mNumLimitToken = AppUtil.runOnUiThread(new Runnable() {
						public void run() {
							Setting.setNumLimit(Classify.Recent, Setting
								.getNumLimit(Classify.Recent) + 1);
							tvRecentLimitNum.setText(String.format(Setting.LOCALE, "%d", Setting
								.getNumLimit(Classify.Recent)));
						}
					}, NUM_LIMIT_DELAY, NUM_LIMIT_REPEAT);
				} else if (event.getAction() == MotionEvent.ACTION_UP
					|| event.getAction() == MotionEvent.ACTION_CANCEL) {

					AppUtil.removeUiThread(mNumLimitToken);
					mNumLimitToken = null;
				}

				return view.onTouchEvent(event);
			}
		});
		llRecentLimit.findViewById(R.id.iv_dn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			}
		});
		llRecentLimit.findViewById(R.id.iv_dn).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Setting.setNumLimit(Classify.Recent, Setting.getNumLimit(Classify.Recent) - 1);
					tvRecentLimitNum.setText(String.format(Setting.LOCALE, "%d", Setting
						.getNumLimit(Classify.Recent)));

					mNumLimitToken = AppUtil.runOnUiThread(new Runnable() {
						public void run() {
							Setting.setNumLimit(Classify.Recent, Setting
								.getNumLimit(Classify.Recent) - 1);
							tvRecentLimitNum.setText(String.format(Setting.LOCALE, "%d", Setting
								.getNumLimit(Classify.Recent)));
						}
					}, NUM_LIMIT_DELAY, NUM_LIMIT_REPEAT);
				} else if (event.getAction() == MotionEvent.ACTION_UP
					|| event.getAction() == MotionEvent.ACTION_CANCEL) {

					AppUtil.removeUiThread(mNumLimitToken);
					mNumLimitToken = null;
				}

				return view.onTouchEvent(event);
			}
		});
	}
}
