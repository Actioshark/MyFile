package kk.myfile.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import kk.myfile.R;
import kk.myfile.util.Setting;

public class SettingHiddenActivity extends BaseActivity {
	private ImageView mIvSelect;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_setting_hidden);
		
		View view = findViewById(R.id.ll_visible);
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean visible = !Setting.getShowHidden();
				Setting.setShowHidden(visible);
				mIvSelect.setImageResource(visible ?
					R.drawable.select_pre : R.drawable.select_nor);
			}
		});
		mIvSelect = (ImageView) view.findViewById(R.id.iv_select);
		mIvSelect.setImageResource(Setting.getShowHidden() ?
				R.drawable.select_pre : R.drawable.select_nor);
		
		View menu = findViewById(R.id.ll_menu);
		menu.findViewById(R.id.iv_back)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}
}
