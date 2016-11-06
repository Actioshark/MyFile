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

	public static enum ProgressType {
		Confirm, Cancel, Progress, Finish,
	}

	public static interface IProgressCallback {
		public void onProgress(ProgressType type);
	}

	public static List<Leaf> getDirect(String path) {
		final Direct direct = new Direct(path);
		direct.setTag(direct);

		final List<Leaf> list = new ArrayList<Leaf>();

		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				direct.loadChildren(list, !Setting.getShowHidden(), false);

				synchronized (direct) {
					direct.setTag(null);
				}
			}
		});

		return list;
	}

	private static List<Leaf> sTypeDirect = new ArrayList<Leaf>();
	private static Boolean sIsTypeDirectRefreshing = false;
	private static boolean sIsTypeDirectNeedRefresh = false;

	public static List<Leaf> getTypeDirect() {
		return sTypeDirect;
	}

	public static boolean isTypeDirectRefreshing() {
		synchronized (sIsTypeDirectRefreshing) {
			return sIsTypeDirectRefreshing;
		}
	}

	public static void refreshTypeDirect() {
		synchronized (sIsTypeDirectRefreshing) {
			if (sIsTypeDirectRefreshing) {
				sIsTypeDirectNeedRefresh = true;
				return;
			}

			sIsTypeDirectRefreshing = true;
		}

		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				sTypeDirect = new ArrayList<Leaf>();
				new Direct(Setting.DEFAULT_PATH).loadChildren(sTypeDirect,
					!Setting.getShowHidden(), true);

				synchronized (sIsTypeDirectRefreshing) {
					sIsTypeDirectRefreshing = false;

					if (sIsTypeDirectNeedRefresh) {
						sIsTypeDirectNeedRefresh = false;
						refreshTypeDirect();
					}
				}
			}
		});
	}

	public static List<Leaf> loadType(List<Leaf> direct, Class<?> cls) {
		List<Leaf> ret = new ArrayList<Leaf>();
		int size = direct.size();

		for (int i = 0; i < size; i++) {
			Leaf leaf = direct.get(i);
			if (cls.isInstance(leaf)) {
				ret.add(leaf);
			}
		}

		return ret;
	}

	public static List<Leaf> loadBig(List<Leaf> direct, int limit) {
		List<Leaf> ret = new ArrayList<Leaf>();
		int size = direct.size();

		for (int i = 0; i < size; i++) {
			Leaf leaf = direct.get(i);
			if (leaf instanceof Direct) {
				continue;
			}

			long length = leaf.getFile().length();
			leaf.setTag(length);

			int index = -1;
			int s = ret.size();

			for (int j = s - 1; j >= 0; j--) {
				long len = (Long) ret.get(j).getTag();

				if (len >= length) {
					index = j;
					break;
				}
			}

			if (++index < limit) {
				ret.add(index, leaf);

				if (s + 1 > limit) {
					ret.remove(s);
				}
			}
		}

		return ret;
	}

	public static List<Leaf> loadRecent(List<Leaf> direct, int limit) {
		List<Leaf> ret = new ArrayList<Leaf>();
		int size = direct.size();

		for (int i = 0; i < size; i++) {
			Leaf leaf = direct.get(i);
			if (leaf instanceof Direct) {
				continue;
			}

			long time = leaf.getFile().lastModified();
			leaf.setTag(time);

			int index = -1;
			int s = ret.size();

			for (int j = s - 1; j >= 0; j--) {
				long tm = (Long) ret.get(j).getTag();

				if (tm >= time) {
					index = j;
					break;
				}
			}

			if (++index < limit) {
				ret.add(index, leaf);

				if (s + 1 > limit) {
					ret.remove(s);
				}
			}
		}

		return ret;
	}

	public static List<Leaf> search(List<Leaf> list, String key) {
		List<Leaf> ret = new ArrayList<Leaf>();
		int size = list.size();
		key = key.toLowerCase(Setting.LOCALE);

		for (int i = 0; i < size; i++) {
			try {
				Leaf leaf = list.get(i);
				if (leaf.getFile().getName().toLowerCase(Setting.LOCALE).contains(key)) {
					ret.add(leaf);
				}
			} catch (Exception e) {
			}
		}

		return ret;
	}

	public static void createDirect(Context context, final String parent, final IProgressCallback cb) {
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

					if (cb != null) {
						cb.onProgress(ProgressType.Finish);
					}
				} else {
					if (cb != null) {
						cb.onProgress(ProgressType.Cancel);
					}
				}

				dialog.dismiss();
			}
		});
		id.show();
	}

	public static void createFile(Context context, final String parent, final IProgressCallback cb) {
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

					if (cb != null) {
						cb.onProgress(ProgressType.Finish);
					}
				} else {
					if (cb != null) {
						cb.onProgress(ProgressType.Cancel);
					}
				}

				dialog.dismiss();
			}
		});
		id.show();
	}

	public static void rename(Context context, final File file, final IProgressCallback cb) {
		try {
			final InputDialog id = new InputDialog(context);
			id.setMessage(file.isDirectory() ? R.string.msg_input_direct_name
				: R.string.msg_input_file_name);

			final String parent = file.getParent();
			String name = file.getName();
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

						err = FileUtil.rename(file, new File(parent, input));
						if (err == null) {
							err = AppUtil.getString(R.string.err_rename_file_success);
						}

						App.showToast(err);

						if (cb != null) {
							cb.onProgress(ProgressType.Finish);
						}
					} else {
						if (cb != null) {
							cb.onProgress(ProgressType.Cancel);
						}
					}

					dialog.dismiss();
				}
			});
			id.show();
		} catch (Exception e) {
			Logger.print(null, e);
		}
	}

	public static void delete(final Context context, final List<Leaf> list,
		final IProgressCallback cb) {
		SimpleDialog sd = new SimpleDialog(context);
		sd.setMessage(AppUtil.getString(R.string.msg_delete_file_confirm, list.size()));
		sd.setButtons(R.string.word_cancel, R.string.word_confirm);
		sd.setClickListener(new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index) {
				if (index == 1) {
					final SimpleDialog sd = new SimpleDialog(context);
					sd.setMessage(AppUtil.getString(R.string.msg_delete_file_progress, 0, list
						.size(), 0, 0));
					sd.setButtons(R.string.word_cancel);
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
												R.string.msg_delete_file_progress, s + f, t, s, f));

											if (cb != null) {
												cb.onProgress(ProgressType.Progress);
											}

											if (s + f >= t) {
												sd.setButtons(R.string.word_confirm);

												if (cb != null) {
													cb.onProgress(ProgressType.Finish);
												}
											}
										}
									}
								});
							}
						}
					});

					if (cb != null) {
						cb.onProgress(ProgressType.Confirm);
					}
				} else {
					if (cb != null) {
						cb.onProgress(ProgressType.Cancel);
					}
				}

				dialog.dismiss();
			}
		});
		sd.show();
	}

	private static class FilePair {
		public File from;
		public File to;
		public boolean delete = false;
	}

	private static void carry(final List<FilePair> fps, int fpi, final AtomicBoolean stop,
		final AtomicInteger exist, final SimpleDialog pg, final AtomicInteger suc,
		final AtomicInteger fai, final IProgressCallback cb, final boolean delete) {

		final int size = fps.size();
		for (int i = fpi; i < size; i++) {
			if (stop.get()) {
				return;
			}

			final FilePair fp = fps.get(i);

			int ext = exist.get();
			if (ext != 1 && ext != 3) {
				exist.set(-1);
			}

			try {
				if (fp.to.exists()) {
					switch (ext) {
					case 0:
					case 1:
						suc.addAndGet(1);
						fp.delete = false;
						break;

					case 2:
					case 3:
						if (fp.from.isDirectory()) {
							if (fp.to.isDirectory()) {
								suc.addAndGet(1);
								fp.delete = true;
							} else {
								if (FileUtil.delete(fp.to) == null
									&& FileUtil.createDirect(fp.to) == null) {

									suc.addAndGet(1);
									fp.delete = true;
								} else {
									fai.addAndGet(1);
									fp.delete = false;
								}
							}
						} else {
							if (fp.to.isDirectory()) {
								if (FileUtil.delete(fp.to) == null
									&& FileUtil.createFile(fp.to) == null
									&& FileUtil.write(fp.from, fp.to)) {

									suc.addAndGet(1);
									fp.delete = true;
								} else {
									fai.addAndGet(1);
									fp.delete = false;
								}
							} else {
								if (FileUtil.write(fp.from, fp.to)) {
									suc.addAndGet(1);
									fp.delete = true;
								} else {
									fai.addAndGet(1);
									fp.delete = false;
								}
							}
						}
						break;

					default:
						final int idx = i;
						AppUtil.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (stop.get()) {
									return;
								}

								final SimpleDialog ec = new SimpleDialog(pg.getContext());
								ec.setMessage(AppUtil.getString(R.string.msg_file_exist, fp.to
									.getAbsolutePath()));
								ec.setButtons(R.string.word_skip, R.string.word_skip_all,
									R.string.word_cover, R.string.word_cover_all);
								ec.setClickListener(new IDialogClickListener() {
									@Override
									public void onClick(Dialog dialog, int index) {
										exist.set(index);

										AppUtil.runOnNewThread(new Runnable() {
											@Override
											public void run() {
												carry(fps, idx, stop, exist, pg, suc, fai, cb,
													delete);
											}
										});

										ec.dismiss();
									}
								});
								ec.setCanceledOnTouchOutside(false);
								ec.show();
							}
						});
						return;
					}
				} else if (fp.from.isDirectory()) {
					if (FileUtil.createDirect(fp.to) == null) {
						suc.addAndGet(1);
						fp.delete = true;
					} else {
						fai.addAndGet(1);
						fp.delete = false;
					}
				} else {
					if (FileUtil.createFile(fp.to) == null && FileUtil.write(fp.from, fp.to)) {

						suc.addAndGet(1);
						fp.delete = true;
					} else {
						fai.addAndGet(1);
						fp.delete = false;
					}
				}

				AppUtil.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (stop.get()) {
							return;
						}

						int msg = delete ? R.string.msg_cut_file_progress
							: R.string.msg_copy_file_progress;
						int s = suc.get();
						int f = fai.get();

						pg.setMessage(AppUtil.getString(msg, s + f, size, s, f));

						if (cb != null) {
							cb.onProgress(ProgressType.Progress);
						}

						if (s + f >= size) {
							stop.set(true);

							pg.setButtons(R.string.word_confirm);

							if (cb != null) {
								cb.onProgress(ProgressType.Finish);
							}
						}
					}
				});
			} catch (Exception e) {
				Logger.print(null, e);
			}
		}

		if (delete) {
			for (int i = fps.size() - 1; i >= 0; i--) {
				FilePair fp = fps.get(i);

				try {
					if (fp.delete) {
						if (fp.from.isDirectory()) {
							String[] children = fp.from.list();
							if (children == null || children.length < 1) {
								FileUtil.delete(fp.from);
							}
						} else {
							FileUtil.delete(fp.from);
						}
					}
				} catch (Exception e) {
					Logger.print(null, e);
				}
			}
		}
	}

	public static void carry(final Context context, final List<String> list, final String direct,
		final boolean delete, final IProgressCallback cb) {

		final AtomicBoolean stop = new AtomicBoolean(false);

		final SimpleDialog pg = new SimpleDialog(context);
		pg.setMessage(R.string.msg_wait);
		pg.setButtons(R.string.word_cancel);
		pg.setClickListener(new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index) {
				if (stop.get()) {
					if (cb != null) {
						cb.onProgress(ProgressType.Confirm);
					}
				} else {
					stop.set(true);

					if (cb != null) {
						cb.onProgress(ProgressType.Cancel);
					}
				}

				dialog.dismiss();
			}
		});
		pg.setCanceledOnTouchOutside(false);
		pg.show();

		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				try {
					List<FilePair> fps = new ArrayList<FilePair>();
					for (String path : list) {
						if (stop.get()) {
							return;
						}

						FilePair fp = new FilePair();
						fp.from = new File(path);
						fp.to = new File(direct, fp.from.getName());
						fps.add(fp);
					}

					for (int i = 0; i < fps.size(); i++) {
						if (stop.get()) {
							return;
						}

						FilePair fp = fps.get(i);

						if (fp.from.isDirectory()) {
							for (File child : fp.from.listFiles()) {
								FilePair pair = new FilePair();
								pair.from = child;
								pair.to = new File(fp.to, child.getName());
								fps.add(pair);
							}
						}
					}

					carry(fps, 0, stop, new AtomicInteger(-1), pg, new AtomicInteger(0),
						new AtomicInteger(0), cb, delete);
				} catch (Exception e) {
					Logger.print(null, e);
				}
			}
		});
	}
}
