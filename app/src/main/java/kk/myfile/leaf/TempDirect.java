package kk.myfile.leaf;

import java.util.List;

public class TempDirect extends Direct {
	public TempDirect(String path) {
		super(path);
	}

	public void setChildren(List<Leaf> list) {
		synchronized (mChildren) {
			mChildren.clear();
			mChildren.addAll(list);
		}
	}

	@Override
	public void loadChildren(boolean visible) {
	}

	@Override
	public void loadChildren(List<Leaf> list, boolean visible, boolean canon) {
	}
}
