package kk.myfile.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Dialog;
import android.content.Context;
import kk.myfile.R;
import kk.myfile.activity.App;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.InputDialog;
import kk.myfile.ui.SimpleDialog;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;
import kk.myfile.util.Setting;

public class Tree {
	public static final String HIDDEN_FILE = ".nomedia";
	
	public static abstract class ProgressCallback {
		public void onConfirm() {
		}
		
		public void onCancel() {
		}
		
		public void onFinish() {
		}
	}
	
	public static Direct load(String path) {
		final Direct direct = new Direct(path);
		direct.setTag(direct);
		
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				if (Setting.getShowHidden()) {
					direct.loadChildrenRecAll();
				} else {
					direct.loadChildrenRecVis();
				}
				
				direct.setTag(null);
			}
		});
		
		return direct;
	}
	
	public static List<Leaf> search(Direct direct, String input) {
		List<Leaf> ret = new ArrayList<Leaf>();
		
		search(direct, input.toLowerCase(Setting.LOCALE), ret);
		
		return ret;
	}
	
	private static void search(Direct direct, String input, List<Leaf> ret) {
		try {
			List<Leaf> children = direct.getChildren();
			
			synchronized (children) {
				for (Leaf leaf : children) {
					if (leaf.getFile().getName().toLowerCase(Setting.LOCALE).contains(input)) {
						ret.add(leaf);
					}
					
					if (leaf instanceof Direct) {
						search((Direct) leaf, input, ret);
					}
				}
			}
		} catch (Exception e) {
		}
	}
	
	public static void createDirect(Context context, final String parent, final ProgressCallback cb) {
		final InputDialog id = new InputDialog(context);
		id.setMessage(R.string.msg_input_direct_name);
		
		String name = "";
		for (int i = 1; i < 1000; i++) {
			String tmp = AppUtil.getString(R.string.def_direct_name, i);
			String err = FileUtil.checkNewName(parent, tmp);
			if (err == null) {
				name = tmp;
				break;
			}
		}
		id.setInput(name);
		id.setSelection(name.length());
		
		id.setClickListener(new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index) {
				if (index == 1) {
					String input = id.getInput();
					String err = FileUtil.checkNewName(parent, input);
					if (err != null) {
						App.showToast(err);
						return;
					}
					
					err = FileUtil.createDirect(new File(parent, input));
					if (err == null) {
						err = AppUtil.getString(R.string.err_create_direct_success);
					}
						
					App.showToast(err);
				}
				
				dialog.dismiss();
				
				if (cb != null) {
					cb.onFinish();
				}
			}
		});
		id.show();
	}
	
	public static void createFile(Context context, final String parent, final ProgressCallback cb) {
		final InputDialog id = new InputDialog(context);
		id.setMessage(R.string.msg_input_file_name);
		
		String name = "";
		for (int i = 1; i < 1000; i++) {
			String tmp = AppUtil.getString(R.string.def_file_name, i);
			String err = FileUtil.checkNewName(parent, tmp);
			if (err == null) {
				name = tmp;
				break;
			}
		}
		id.setInput(name);
		id.setSelection(name.length());
		
		id.setClickListener(new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index) {
				if (index == 1) {
					String input = id.getInput();
					String err = FileUtil.checkNewName(parent, input);
					if (err != null) {
						App.showToast(err);
						return;
					}
					
					err = FileUtil.createDirect(new File(parent, input));
					if (err == null) {
						err = AppUtil.getString(R.string.err_create_file_success);
					}

					App.showToast(err);
				}
				
				dialog.dismiss();
				
				if (cb != null) {
					cb.onFinish();
				}
			}
		});
		id.show();
	}
	
	public static void delete(final Context context, final List<Leaf> list, final ProgressCallback cb) {
		SimpleDialog sd = new SimpleDialog(context);
		sd.setMessage(AppUtil.getString(R.string.msg_delete_file_confirm,
				list.size()));
		sd.setButtons(new int[] {R.string.word_cancel, R.string.word_confirm});
		sd.setClickListener(new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index) {
				if (index == 1) {
					final SimpleDialog sd = new SimpleDialog(context);
					sd.setMessage(AppUtil.getString(R.string.msg_delete_file_progress,
							0, list.size(), 0, 0));
					sd.setButtons(new int[] {R.string.word_cancel});
					sd.setClickListener(new IDialogClickListener() {
						@Override
						public void onClick(Dialog dialog, int index) {
							dialog.dismiss();
						}
					});
					sd.setCanceledOnTouchOutside(false);
					sd.show();
		
					AppUtil.runOnNewThread(new Runnable() {
						public void run() {
							final AtomicInteger success = new AtomicInteger(0);
							final AtomicInteger failed = new AtomicInteger(0);
							
							for (Leaf leaf : list) {
								String err = FileUtil.delete(leaf.getFile());
								if (err == null) {
									success.addAndGet(1);
								} else {
									failed.addAndGet(1);
								}
								
								if (sd.isShowing() == false) {
									return;
								}
									
								AppUtil.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (sd.isShowing()) {
											int s = success.get();
											int f = failed.get();
											int t = list.size();
											
											sd.setMessage(AppUtil.getString(
												R.string.msg_delete_file_progress,
												s + f, t, s, f));
											
											if (s + f >= t) {
												sd.setButtons(new int[] {R.string.word_confirm});
											}
										}
										
										if (cb != null) {
											cb.onFinish();
										}
									}
								});
							}
						}
					});
				}
				
				dialog.dismiss();
				
				if (cb != null) {
					cb.onConfirm();
				}
			}
		});
		sd.show();
	}
	
	private static class FilePair {
		public File from;
		public File to;
	}
	private static void loadFilePair(File file, File toDir, List<FilePair> ret) {
		try {
			FilePair fp = new FilePair();
			fp.from = file;
			fp.to = new File(toDir, file.getName());
			ret.add(fp);
			
			if (file.isDirectory()) {
				for (File child : file.listFiles()) {
					loadFilePair(child, fp.to, ret);
				}
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}
	}
	
	public static void copy(Context context, final List<Leaf> list,
			final String parent, final ProgressCallback cb) {
		
		final SimpleDialog sd = new SimpleDialog(context);
		sd.setMessage(AppUtil.getString(R.string.msg_copy_file_progress,
				0, list.size(), 0, 0));
		sd.setButtons(new int[] {R.string.word_cancel});
		sd.setClickListener(new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index) {
				dialog.dismiss();
			}
		});
		sd.setCanceledOnTouchOutside(false);
		sd.show();

		AppUtil.runOnNewThread(new Runnable() {
			public void run() {
				final List<FilePair> ret = new ArrayList<FilePair>();
				for (Leaf leaf : list) {
					loadFilePair(leaf.getFile(), new File(parent), ret);
				}
				
				final AtomicInteger success = new AtomicInteger(0);
				final AtomicInteger failed = new AtomicInteger(0);
				
				for (FilePair fp : ret) {
					if (fp.to.exists()) {
						success.addAndGet(1);
					} else if (fp.from.isDirectory()) {
						if (FileUtil.createDirect(fp.to) == null) {
							success.addAndGet(1);
						} else {
							failed.addAndGet(1);
						}
					} else {
						if (FileUtil.createFile(fp.to) == null && FileUtil.write(fp.from, fp.to)) {
							success.addAndGet(1);
						} else {
							failed.addAndGet(1);
						}
					}
						
					AppUtil.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (sd.isShowing()) {
								int s = success.get();
								int f = failed.get();
								int t = ret.size();
								
								sd.setMessage(AppUtil.getString(
									R.string.msg_copy_file_progress,
									s + f, t, s, f));
								
								if (s + f >= t) {
									sd.setButtons(new int[] {R.string.word_confirm});
								}
							}
							
							if (cb != null) {
								cb.onFinish();
							}
						}
					});
				}
			}
		});
	}
}
