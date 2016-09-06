package kk.myfile.activity;

import kk.myfile.tree.Tree;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Setting;
import android.app.Application;

public class App extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		
		AppUtil.init(this);
		Setting.init(this);
		AppUtil.init(this);
		Tree.init(this);
	}
}
