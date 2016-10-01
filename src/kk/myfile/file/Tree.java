package kk.myfile.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
					
					err = FileUtil.createFile(new File(parent, input));
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
	
	private static interface ICopyCutCallback {
		public void onFinish(boolean success, AtomicInteger exist);
	}
	
	private static void copy(final Context context, final File from,
			final File dir, final AtomicInteger exist, final ICopyCutCallback callback,
			final AtomicBoolean stop) {
		
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (stop.get()) {
						return;
					}
					
					final File to = new File(dir, from.getName());
					boolean success;
					
					int ext = exist.get();
					if (ext != 1 && ext != 3) {
						exist.set(-1);
					}
					
					if (to.exists()) {
						switch (ext) {
						case 0:
						case 1:
							success = true;
							break;
							
						case 2:
						case 3:
							if (from.isDirectory()) {
								if (to.isDirectory()) {
									success = true;
								} else {
									success = FileUtil.delete(to) == null && FileUtil.createDirect(to) == null;
								}
								
								final File[] children = from.listFiles();
								if (success && children.length > 0) {
									final AtomicInteger idx = new AtomicInteger(-1);
									final AtomicBoolean suc = new AtomicBoolean(true);
									
									final ICopyCutCallback cb = new ICopyCutCallback() {
										@Override
										public void onFinish(boolean success, AtomicInteger exist) {
											int next = idx.addAndGet(1);
											suc.set(suc.get() && success);
											
											if (next < children.length) {
												File child = children[next];
												
												copy(context, child, to, exist, this, stop);
											} else if(callback != null) {
												callback.onFinish(suc.get(), exist);
											}
										}
									};
									
									cb.onFinish(true, exist);
									return;
								}
							} else {
								if (to.isDirectory()) {
									success = FileUtil.delete(to) == null
										&& FileUtil.createFile(to) == null
										&& FileUtil.write(from, to);
								} else {
									success = FileUtil.write(from, to);
								}
							}
							break;

						default:
							AppUtil.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (stop.get()) {
										return;
									}
									
									SimpleDialog existDialog = new SimpleDialog(context);
									existDialog.setMessage(AppUtil.getString(R.string.msg_file_exist,
											to.getAbsolutePath()));
									existDialog.setButtons(new int[] {
										R.string.word_skip, R.string.word_skip_all,
										R.string.word_cover, R.string.word_cover_all,
									});
									existDialog.setClickListener(new IDialogClickListener() {
										@Override
										public void onClick(Dialog dialog, int index) {
											exist.set(index);
											copy(context, from, dir, exist, callback, stop);
											dialog.dismiss();
										}
									});
									existDialog.setCanceledOnTouchOutside(false);
									existDialog.show();
								}
							});
							
							return;
						}
					} else if (from.isDirectory()) {
						success = FileUtil.createDirect(to) == null;
						
						final File[] children = from.listFiles();
						if (success && children.length > 0) {
							final AtomicInteger idx = new AtomicInteger(-1);
							final AtomicBoolean suc = new AtomicBoolean(true);
							
							final ICopyCutCallback cb = new ICopyCutCallback() {
								@Override
								public void onFinish(boolean success, AtomicInteger exist) {
									int next = idx.addAndGet(1);
									suc.set(suc.get() && success);
									
									if (next < children.length) {
										File child = children[next];
										
										copy(context, child, to, exist, this, stop);
									} else if(callback != null) {
										callback.onFinish(suc.get(), exist);
									}
								}
							};
							
							cb.onFinish(true, exist);
							return;
						}
					} else {
						success = FileUtil.createFile(to) == null && FileUtil.write(from, to);
					}
					
					if (callback != null) {
						callback.onFinish(success, exist);
					}
				} catch (Exception e) {
					Logger.print(null, e);
				}
			}
		});
	}
	
	public static void copy(final Context context, final List<Leaf> list,
			final String direct, final ProgressCallback cb) {
		
		final AtomicBoolean stop = new AtomicBoolean(false);
		
		final SimpleDialog pg = new SimpleDialog(context);
		pg.setButtons(new int[] {R.string.word_cancel});
		pg.setClickListener(new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index) {
				stop.set(true);
				dialog.dismiss();
				
				if (cb != null) {
					cb.onFinish();
				}
			}
		});
		pg.setCanceledOnTouchOutside(false);
		pg.show();
		
		final File dir = new File(direct);
		final AtomicInteger idx = new AtomicInteger(-1);
		final AtomicInteger suc = new AtomicInteger(0);
		
		ICopyCutCallback callback = new ICopyCutCallback() {
			@Override
			public void onFinish(boolean success, AtomicInteger exist) {
				final int next = idx.addAndGet(1);
				if (success) {
					suc.addAndGet(1);
				}
				
				if (next < list.size()) {
					copy(context, list.get(next).getFile(), dir, exist, this, stop);
					
					AppUtil.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (stop.get()) {
								return;
							}
							
							pg.setMessage(AppUtil.getString(R.string.msg_copy_file_progress,
								next, list.size(), suc.get(), next - suc.get()));
						}
					});
				} else {
					AppUtil.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (stop.get()) {
								return;
							}
							
							pg.setMessage(AppUtil.getString(R.string.msg_copy_file_progress,
								next, list.size(), suc.get(), next - suc.get()));
							pg.setButtons(new int[] {R.string.word_confirm});
							
							if (cb != null) {
								cb.onFinish();
							}
						}
					});
				}
			}
		};
		
		callback.onFinish(false, new AtomicInteger(-1));
	}
}
