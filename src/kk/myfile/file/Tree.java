package kk.myfile.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.text.InputType;

import kk.myfile.R;
import kk.myfile.activity.App;
import kk.myfile.leaf.Apk;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.InputDialog;
import kk.myfile.ui.SimpleDialog;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;
import kk.myfile.util.Setting;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

public class Tree {
	public static final String HIDDEN_FILE = ".nomedia";

	public static enum ProgressType {
		Confirm, Cancel, Progress, Finish, Error,
	}

	public static interface IProgressCallback {
		public void onProgress(ProgressType type, Object... data);
	}

	public static List<Leaf> getDirect(String path, final AtomicBoolean finish) {
		final Direct direct = new Direct(path);
		final List<Leaf> list = new ArrayList<Leaf>();

		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				direct.loadChildren(list, !Setting.getShowHidden(), false);

				finish.set(true);
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

	@SuppressWarnings("deprecation")
	public static List<Leaf> loadType(List<Leaf> direct, Class<?> cls) {
		List<Leaf> ret = new ArrayList<Leaf>();
		
		if (Apk.class.equals(cls)) {
			List<PackageInfo> pis = AppUtil.getContext().getPackageManager().getInstalledPackages(0);
			
			for (PackageInfo pi : pis) {
				for (int index = 1; index <= 2; index++) {
					try {
						String path = String.format(Setting.LOCALE, "/data/app/%s-%d.apk",
							pi.packageName, index);
						File file = new File(path);
						if (file.exists()) {
							Leaf leaf = new Apk(path);
							ret.add(leaf);
						}
					} catch (Exception e) {
					}
				}
			}
		}
		
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

	public static void createDirect(Context context, final String parent, String name,
		final IProgressCallback cb) {
		final InputDialog id = new InputDialog(context);
		id.setMessage(R.string.msg_input_direct_name);

		if (name == null) {
			for (int i = 1; i < 1000; i++) {
				String tmp = AppUtil.getString(R.string.def_direct_name, i);
				String err = FileUtil.checkNewName(parent, tmp);
				if (err == null) {
					name = tmp;
					break;
				}
			}
		}
		id.setInput(name);
		id.setSelection(name.length());

		id.setClickListener(new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index, ClickType type) {
				if (index == 1) {
					String input = id.getInput();
					String err = FileUtil.checkNewName(parent, input);
					if (err != null) {
						App.showToast(err);
						return;
					}

					File file = new File(parent, input);
					err = FileUtil.createDirect(file);
					if (err == null) {
						err = AppUtil.getString(R.string.err_create_direct_success);
					}

					App.showToast(err);

					if (cb != null) {
						cb.onProgress(ProgressType.Finish, file);
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

	public static void createFile(Context context, final String parent, String name,
		final IProgressCallback cb) {
		final InputDialog id = new InputDialog(context);
		id.setMessage(R.string.msg_input_file_name);

		if (name == null) {
			for (int i = 1;; i++) {
				String tmp = AppUtil.getString(R.string.def_file_name, i);
				String err = FileUtil.checkNewName(parent, tmp);
				if (err == null) {
					name = tmp;
					break;
				}
			}
		}
		id.setInput(name);
		id.setSelection(name.length());

		id.setClickListener(new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index, ClickType type) {
				if (index == 1) {
					String input = id.getInput();
					String err = FileUtil.checkNewName(parent, input);
					if (err != null) {
						App.showToast(err);
						return;
					}

					File file = new File(parent, input);
					err = FileUtil.createFile(file);
					if (err == null) {
						err = AppUtil.getString(R.string.err_create_file_success);
					}

					App.showToast(err);

					if (cb != null) {
						cb.onProgress(ProgressType.Finish, file);
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
				public void onClick(Dialog dialog, int index, ClickType type) {
					if (index == 1) {
						String input = id.getInput();
						String err = FileUtil.checkNewName(parent, input);
						if (err != null) {
							App.showToast(err);
							return;
						}

						File to = new File(parent, input);
						err = FileUtil.rename(file, to);
						if (err == null) {
							err = AppUtil.getString(R.string.err_rename_file_success);
						}

						App.showToast(err);

						if (cb != null) {
							cb.onProgress(ProgressType.Finish, to);
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
			public void onClick(Dialog dialog, int index, ClickType type) {
				if (index == 1) {
					final SimpleDialog sd = new SimpleDialog(context);
					sd.setMessage(AppUtil.getString(R.string.msg_delete_file_progress, 0, list
						.size(), 0, 0));
					sd.setButtons(R.string.word_cancel);
					sd.setClickListener(new IDialogClickListener() {
						@Override
						public void onClick(Dialog dialog, int index, ClickType type) {
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

	public static final int SELECT_MASK = 0xf;
	public static final int SELECT_SKIP = 0x1;
	public static final int SELECT_COVER = 0x2;
	public static final int SELECT_RENAME = 0x3;

	public static final int ALL_MASK = 0xf0;
	public static final int ALL_NO = 0x00;
	public static final int ALL_YES = 0x10;

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
			if ((ext & ALL_MASK) == ALL_NO) {
				exist.set(0);
			}

			try {
				if (fp.to.exists()) {
					switch (ext & SELECT_MASK) {
					case SELECT_SKIP:
						suc.addAndGet(1);
						fp.delete = false;
						break;

					case SELECT_COVER:
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

					case SELECT_RENAME:
						String path = fp.to.getPath();
						int pi = path.lastIndexOf('.');
						String prefix = pi == -1 ? path : path.substring(0, pi);
						String subfix = pi == -1 ? "" : path.substring(pi, path.length());

						for (int j = 1;; j++) {
							fp.to = new File(String.format("%s_%d%s", prefix, j, subfix));
							if (fp.to.exists() == false) {
								break;
							}
						}

						i--;
						continue;

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
								ec.setButtons(R.string.word_skip_or_all,
									R.string.word_cover_or_all, R.string.word_rename_or_all);
								ec.setClickListener(new IDialogClickListener() {
									@Override
									public void onClick(Dialog dialog, int index, ClickType type) {
										switch (index) {
										case 0:
											exist.set(SELECT_SKIP);
											break;

										case 1:
											exist.set(SELECT_COVER);
											break;

										case 2:
											exist.set(SELECT_RENAME);
											break;

										default:
											exist.set(0);
											break;
										}

										if (type == ClickType.LongClick) {
											exist.set(exist.get() | ALL_YES);
										}

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
			public void onClick(Dialog dialog, int index, ClickType type) {
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

	public static void monitorZip(final boolean cmps, final ProgressMonitor pm,
		final AtomicBoolean cancel, final IProgressCallback cb) {
		
		final List<Runnable> mark = new ArrayList<Runnable>();

		Runnable mk = AppUtil.runOnUiThread(new Runnable() {
			public void run() {
				if (pm.getState() == ProgressMonitor.STATE_BUSY) {
					if (cb != null) {
						String path = pm.getFileName();
						cb.onProgress(ProgressType.Progress, path, pm.getWorkCompleted(),
							pm.getTotalWork());
					}
					
					if (cancel != null && cancel.get()) {
						pm.cancelAllTasks();
					}
				} else {
					AppUtil.removeUiThread(mark.get(0));

					if (pm.getResult() == ProgressMonitor.RESULT_SUCCESS) {
						if (cb != null) {
							cb.onProgress(ProgressType.Finish);
						}
					} else {
						if (cb != null) {
							int err = cmps ? R.string.err_compress_failed
								: R.string.err_extract_failed;
							cb.onProgress(ProgressType.Error, err);
						}
					}
				}
			}
		}, 100, 100);

		mark.add(mk);
	}

	public static void compress(final Context context, final String dir, final List<Leaf> list,
		final IProgressCallback cb) {
		
		final InputDialog id = new InputDialog(context);
		id.setMessage(R.string.msg_input_file_name);
		id.setInput(AppUtil.getString(R.string.def_zip_name));
		id.setClickListener(new IDialogClickListener() {
			@Override
			public void onClick(Dialog dialog, int index, ClickType type) {
				if (index != 1) {
					dialog.dismiss();
					return;
				}

				String input = id.getInput();
				String err = FileUtil.checkNewName(dir, input);
				if (err != null) {
					App.showToast(err);
					return;
				}

				dialog.dismiss();

				final File target = new File(dir, input);

				final InputDialog id = new InputDialog(context);
				id.setMessage(R.string.msg_input_password);
				id.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				id.setClickListener(new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index, ClickType type) {
						dialog.dismiss();
						if (index != 1) {
							return;
						}
						
						String password = id.getInput();
						
						final AtomicBoolean cancel = new AtomicBoolean(false);
						
						final SimpleDialog progress = new SimpleDialog(context);
						progress.setCanceledOnTouchOutside(false);
						progress.setButtons(R.string.word_cancel);
						progress.setClickListener(new IDialogClickListener() {
							@Override
							public void onClick(Dialog dialog, int index, ClickType type) {
								dialog.dismiss();
								cancel.set(true);
							}
						});
						progress.show();
						
						final IProgressCallback callback = new IProgressCallback() {
							@Override
							public void onProgress(final ProgressType type, final Object... data) {
								AppUtil.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (type == ProgressType.Progress) {
											progress.setMessage(data[0] + "");
										} else if(type == ProgressType.Cancel) {
											
										} else if(type == ProgressType.Finish) {
											progress.setMessage(R.string.err_extract_success);
											progress.setButtons(R.string.word_confirm);
										} else if(type == ProgressType.Error) {
											progress.setMessage(data[0] + "");
										}
										
										if (cb != null) {
											cb.onProgress(type, data);
										}
									}
								});
							}
						};

						try {
							final ZipParameters zp = new ZipParameters();
							zp.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
							zp.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

							if (password != null && password.length() > 0) {
								zp.setEncryptFiles(true);
								zp.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
								zp.setPassword(password);
							}

							final ZipFile zf = new ZipFile(target);

							AppUtil.runOnNewThread(new Runnable() {
								public void run() {
									for (Leaf leaf : list) {
										if (cancel.get()) {
											return;
										}
										
										try {
											if (leaf instanceof Direct) {
												zf.addFolder(leaf.getPath(), zp);
											} else {
												zf.addFile(leaf.getFile(), zp);
											}
										} catch (Exception e) {
											Logger.print(null, e);
											callback.onProgress(ProgressType.Error, e.toString());
										}
									}
								}
							});

							monitorZip(true, zf.getProgressMonitor(), cancel, callback);
						} catch (Exception e) {
							Logger.print(null, e);
							callback.onProgress(ProgressType.Error, e.toString());
						}
					}
				});
				id.show();
			}
		});
		id.show();
	}

	public static void extract(final Context context, final String file, final String dir,
		final IProgressCallback cb) {
		
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				final ArchiveHelper ah = new ArchiveHelper();
				boolean success = ah.setFile(file);
				if (success == false) {
					AppUtil.runOnUiThread(new Runnable() {
						public void run() {
							App.showToast(R.string.err_not_support);
						}
					});
					return;
				}
				
				AppUtil.runOnUiThread(new Runnable() {
					public void run() {
						final AtomicBoolean cancel = new AtomicBoolean(false);
						
						final SimpleDialog progress = new SimpleDialog(context);
						progress.setCanceledOnTouchOutside(false);
						progress.setButtons(R.string.word_cancel);
						progress.setClickListener(new IDialogClickListener() {
							@Override
							public void onClick(Dialog dialog, int index, ClickType type) {
								dialog.dismiss();
								cancel.set(true);
							}
						});
						progress.show();
						
						final IProgressCallback callback = new IProgressCallback() {
							@Override
							public void onProgress(final ProgressType type, final Object... data) {
								AppUtil.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (type == ProgressType.Progress) {
											progress.setMessage(String.format("%d/%d\n\n%s",
												data[1], data[2], data[0]));
										} else if(type == ProgressType.Cancel) {
											
										} else if(type == ProgressType.Finish) {
											progress.setMessage(R.string.err_extract_success);
											progress.setButtons(R.string.word_confirm);
										} else if(type == ProgressType.Error) {
											progress.setMessage(data[0] + "");
										}
										
										if (cb != null) {
											cb.onProgress(type, data);
										}
									}
								});
							}
						};
						
						if (ah.isEncrypted()) {
							final InputDialog id = new InputDialog(context);
							id.setMessage(R.string.msg_input_password);
							id.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
							id.setClickListener(new IDialogClickListener() {
								@Override
								public void onClick(Dialog dialog, int index, ClickType type) {
									dialog.dismiss();
									if (index != 1) {
										return;
									}
									
									final String password = id.getInput();
									AppUtil.runOnNewThread(new Runnable() {
										@Override
										public void run() {
											ah.setPassword(password);
											ah.extractFile(dir, cancel, callback);
										}
									});
								}
							});
							id.show();
						} else {
							AppUtil.runOnNewThread(new Runnable() {
								@Override
								public void run() {
									ah.extractFile(dir, cancel, callback);
								}
							});
						}
					}
				});
			}
		});
	}
}
