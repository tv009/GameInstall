package cn.gameinstall;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.dashi.adwall.utils.ChargeUtils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class GameDownActivity extends Activity {
	/**
	 * 下载标志字符串
	 */
	static final String DL_ID = "downloadId";
	/**
	 * 首次下载来自哪里
	 */
	static final String First_Start_From_Downloader = "First_Start_From_Downloader";
	/**
	 * 游戏通知动作
	 */
	public static final String GAME_NOTIFICATION_ACTION = "com.qqgame.gamenotification";
	public static final String QUIT_ID = "QUIT_ID";
	static final String tag = "downhlddz";
	/**
	 * 下载后的APK 名字
	 */
	String apkName = "hlddz.apk";
	/**
	 * 下载APK存放目录
	 */
	final String downloadPath = "/update_droid186/newApk/";
	public final String strACT = "android.intent.action.PACKAGE_ADDED";
	/**
	 * IO异常
	 */
	static final Exception IOException = null;
	
	/**
	 * 下载包大小
	 */
	public static final int MSG_DWPACKSIZE = 1;
	/**
	 * 进度更新
	 */
	public static final int MSG_PROGRESS = 2;
	/**
	 * 下载失败
	 */
	public static final int MSG_DWFAIL = 3;
	/**
	 * 更新数据
	 */
	public static final int MSG_UPDATE = 4;
	/**
	 * 新应用包安装
	 */
	public static final int MSG_PACKAGE_ADDED = 5;
	/**
	 * 通知栏点击
	 */
	public static final int MSG_NOTIFICATION_CLICKED = 6;
	
	String absoluteDownloadPath;
	Cursor cursor;
	AlertDialog downloadDialog;
	Thread downloadThread;
	boolean downok = true;
	
	long dowsize = 0L;
	long totalsize = 0L;
	
	SharedPreferences prefs;
	int progress = 0;
	DownloadManager.Query query = new DownloadManager.Query();
	DownloadManager manager;
	QuitBroadcastReciver quitBroadcastReciver = new QuitBroadcastReciver();
	Button startBtn;
	State state = State.Resume;
	boolean updateTotalSize = false;

	ChargeUtils cu;
	Handler handler = new MyHandler();

	
	class QuitBroadcastReciver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
		      String action = intent.getAction();
		      Log.i(tag, "onReceive:intent=" + intent);
		      if (action.equals("com.qqgame.gamenotification"))
		      {
		        int i = intent.getIntExtra("QUIT_ID", 0);
		        Log.i(tag, "id=" + i);
		        if (i != 0)
		        {
		          Log.i(tag, "killProcess");
		          android.os.Process.killProcess(android.os.Process.myPid());
		        }
		      }
			
		}
		
	}
	class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case MSG_DWPACKSIZE:
				dowsize = msg.arg1;
				Log.d(tag, "dwpacksize=" + String.valueOf(msg.arg1));
			case MSG_PROGRESS:
				if(downloadDialog != null){
					mProgress.setProgress(msg.arg1);
					mTextView.setText("已下载:"+dowsize+"/"+totalsize);
				}
				if(msg.arg1 == 100){
					downloadComplete();
				}
				break;
			case MSG_DWFAIL:
				stopDownload();
				showRetryDownloadDialog("提示", "重试", "取消", failMessage(msg.arg1));
				break;
			case MSG_NOTIFICATION_CLICKED:
				notifyCationClicked(msg.arg1);
				break;
			case MSG_PACKAGE_ADDED:
				   try{
			         openApp(gamePkgName, "First_Start_From_Downloader");
			         delDownloadApk();
			          return;
			        }
			        catch (Exception e){
			          e.printStackTrace();
			        }
				break;
			case MSG_UPDATE:
				updateProgress();
				break;
			}

		}

	}
	  public void downloadComplete()
	  {
	    Log.i(tag, "downloadComplete");
	    if (!downok)
	    {
	      stopDownload();
	      getDownloadPath();
	      if (!install(absoluteDownloadPath))
	        showRetryDownloadDialog("提示", "重试", "取消", "安装文件不存在，重新下载？");
	    }
	  }
	  
	  public boolean install(String absPath)
	  {
	    Log.v(tag, " install start");
	    File f = new File(absPath);
	    if (!f.exists())
	    {
	      Log.v(tag, " apkfile is not exists");
	      return false;
	    }
	    if (state == State.Pause)
	    {
	      state = State.Install;
	      return true;
	    }
	    Log.v(tag, " install :" + absPath);
	    Intent intent = new Intent("android.intent.action.VIEW");
	    intent.setDataAndType(Uri.parse("file://" + f.toString()), "application/vnd.android.package-archive");
	    startActivity(intent);
	    return true;
	  }

	  void updateProgress(){
	    cursor = manager.query(query);
	    if (cursor == null)
	      Log.i(tag, "cursor == null");
	      while (cursor.moveToNext()){
	        if ((totalsize <= 0L) && (!updateTotalSize)){
	          totalsize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
	          Log.i(tag, "totalsize:" + totalsize);
	          if (totalsize <= 0L){
	        	  updateTotalSize = true;
	          }else{
	        	  Message msg = new Message();
	        	  msg.what = MSG_DWPACKSIZE;
	        	  msg.arg1 = (int)totalsize;
	        	  handler.sendMessage(msg);
	        	  Log.i(tag, "sendMessage:MSG_DWPACKSIZE");
	          }
	        }
	        dowsize = cursor.getLong(cursor.getColumnIndex("bytes_so_far"));
	        if (totalsize > 0L){
	        	progress = (int)(100L * dowsize / totalsize);
	        	progress+=2;
	        }
	            if (downloadStatus(cursor) == 16){
	            int  reasonId = cursor.getInt(cursor.getColumnIndex("reason"));
	            Message msg3 = new Message();
	            msg3.what = MSG_DWFAIL;
	            msg3.arg1 = reasonId;
	            handler.sendMessage(msg3);
	            Log.i(tag, "sendMessage:MSG_DWFAIL");
	          }else{
	        	  Message msg2 = new Message();
	        	  msg2.what = MSG_PROGRESS;
	        	  msg2.arg1 = progress;
	        	  if(msg2.arg1 == 100){
	        		  handler.sendMessageDelayed(msg2, 10*1000);
	        	  }else{
	        		  handler.sendMessageDelayed(msg2, 1000);
	        		  
	        	  }
	          }
	      }
	}
	private int downloadStatus(Cursor cur) {
		int i = cur.getInt(cur.getColumnIndex("status"));
	    switch (i){
	    case 2:
	      return i;
	    case 16:
	      Log.i(tag, "Download STATUS_FAILED");
	      return i;
	    case 4:
	      Log.i(tag, "Download STATUS_PAUSED");
	      return i;
	    case 1:
	      Log.i(tag, "Download STATUS_PENDING");
	      return i;
	    case 8:
	    	Log.i(tag, "Download STATUS_SUCCESSFUL");
	    	return i;
	    default:
	    	Log.i(tag, "Download STATUS_FAILED");
	    }
		return 0;
	}
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(1);
		getWindow().setFlags(1024, 1024);
		Display display = getWindowManager().getDefaultDisplay();
		int height = display.getHeight();
		int width = display.getWidth();
		Log.i(tag, "screenWidth:" + width + " screenHeight" + height);
		AbsoluteLayout absLayout = new AbsoluteLayout(this);
		AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0);
		View bgView = new View(this);
		bgView.setBackgroundResource(R.drawable.splash);
		absLayout.addView(bgView, params);
		cu = new ChargeUtils(this);
		startBtn = new Button(this);
		float f1 = height / 800.0F;
		float f2 = width / 480.0F;
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.start_normal);
		Log.i(tag, " bitmapPic.getWidth():" + bitmap.getWidth() + " bitmapPic.getHeight():" + bitmap.getHeight());
		startBtn.setFocusable(true);
		startBtn.setBackgroundResource(R.drawable.btn_background);
		startBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(tag, "启动 click");
				// 每次收费
				
				// 是否安装 如果安装 直接启动
				if (isAppInstalled(gamePkgName)) {
					try {
						openApp(gamePkgName, "");
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				
				startDownload();

			}
		});
		Log.i(tag, "fx:" + f2 + " fy:" + f1);
		int k = (int) (106.0F * f2);
		int m = (int) (548.0F * f1);
		int n = (int) (266.0F * f2);
		int i1 = (int) (132.0F * f1);
		AbsoluteLayout.LayoutParams param2 = new AbsoluteLayout.LayoutParams(n, i1, k, m);
		Log.i(tag, "x:" + k + " y:" + m + " width:" + n + " height:" + i1);
		absLayout.addView(startBtn, param2);
		setContentView(absLayout);
		manager = ((DownloadManager) getSystemService(DOWNLOAD_SERVICE));
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		registerReceiver(onComplete, new IntentFilter("android.intent.action.DOWNLOAD_COMPLETE"));
		registerReceiver(onNotification, new IntentFilter("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED"));


	}

	void queryDownloadStatus() {
		Log.i(tag, "queryDownloadStatus: downok " + downok);
		if (!downok){
			return;
		}
		long downloadId = prefs.getLong("downloadId", 0L);
		Log.i(tag, "index:" + downloadId);
		query.setFilterById(downloadId);
		final Cursor cursor =  manager.query(query);
		downloadThread = new Thread(new Runnable() {
			public void run() {
				Log.i(tag, "run");
				downok = false;
//				int column_id = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
//				if(column_id != -1){
//					totalsize = cursor.getLong(column_id);
//				}
				while (!downok){
						if (updateTotalSize && (totalsize <= 0L));
						try {
							HttpURLConnection conn = (HttpURLConnection) new URL(httpDownloadUrl).openConnection();
							conn.connect();
							totalsize = conn.getContentLength();
							Log.i(tag, "conn.getContentLength():" + totalsize);
							conn.disconnect();
							if (totalsize > 0L){
								updateTotalSize = false;
								Message msg = new Message();
								msg.what = MSG_UPDATE;
								handler.sendMessage(msg);
							}
							Thread.sleep(1000L);
							} catch (Exception e){
								e.printStackTrace();
							}
						}
					}
				}
			);
		downloadThread.start();
	}
	
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Log.d(tag, "KeyEvent.KEYCODE_BACK");
		    showExitDialog("提示", "确认", "取消", "确定退出欢乐斗地主下载器？");
		    return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(tag, "onDestroy");
	    closeDownload();
	    if (onComplete != null)
	      unregisterReceiver(onComplete);
	    if (onNotification != null)
	      unregisterReceiver(onNotification);
	    super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(tag, "downloader.onPause()");
		cu.onPause();
	    state = State.Pause;
	    Log.i(tag, "state:" + state);
	}

	@Override
	protected void onResume() {
		super.onResume();
		cu.onResume();
	    Log.i(tag, "downloader.onResume()");
	    Log.i(tag, "state:" + state);
	    if (state == State.Install)
	      install(absoluteDownloadPath);
	    state = State.Resume;
	}

	void showDownloadDialog() {
		if (downloadDialog != null)
			return;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("下载进度");
		View view_process = LayoutInflater.from(this).inflate(R.layout.progress, null);
		mProgress = ((ProgressBar) view_process.findViewById(R.id.progress));
		mTextView = ((TextView) view_process.findViewById(R.id.percent));
		mTextView.setText("");
		builder.setView(view_process);
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				stopDownload();
				clearDownload();
				delDownloadApk();
				startBtn.setEnabled(true);
			}
		});
		downloadDialog = builder.create();
		downloadDialog.setCancelable(false);
		downloadDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
				return keyCode == KeyEvent.KEYCODE_SEARCH;
			}
		});
		downloadDialog.show();
	}

	void stopDownload() {
		Log.i(tag, "stopDownload");
		downok = true;
		closeDownload();
	}

	void delDownloadApk() {
		prefs.edit().clear();
		prefs.edit().putLong("downloadId", -1L).commit();
		Log.v(tag, "delDownloadApk");
		getDownloadPath();
		File localFile = new File(absoluteDownloadPath);
		if (!localFile.exists()) {
			Log.v(tag, " apkfile is not exists");
			return;
		}
		Log.v(tag, " apkfile  delete");
		localFile.delete();
	}

	String getDownloadPath() {
		absoluteDownloadPath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + apkName);
		Log.v(tag, "absoluteDownloadPath:" + absoluteDownloadPath);
		return absoluteDownloadPath;
	}

	void closeDownload() {
		if (downloadDialog != null) {
			downloadDialog.hide();
			downloadDialog.dismiss();
			downloadDialog = null;
		}
	}

	void clearDownload() {
		Log.i(tag, "clearDownload");
		prefs.edit().clear();
		prefs.edit().putLong("downloadId", -1L).commit();
		if (lastDownload >= 0L) {
			manager.remove(lastDownload);
		}
	}

	void showExitDialog(String title, String okStr, String noStr, String showMsg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(showMsg);
		builder.setPositiveButton(okStr, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
		builder.setNegativeButton(noStr, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
				startBtn.setEnabled(true);
			}
		});
		builder.create().show();
	}

	void showNoticeDialog(String title, String okStr, String noStr, String showMsg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(showMsg);
		builder.setPositiveButton(okStr, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
				startDownload();
			}
		});
		builder.setNegativeButton(noStr, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
				startBtn.setEnabled(true);
			}
		});
		builder.create().show();
	}

	void showRetryDownloadDialog(String title, String okStr, String noStr, String showMsg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(showMsg);
		builder.setPositiveButton(okStr, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
				download();
			}
		});
		builder.setNegativeButton(noStr, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
				startBtn.setEnabled(true);
			}
		});
		builder.create().show();
	}

	void startDownload() {
		Log.v(tag, "startDownload");
		lastDownload = prefs.getLong("downloadId", 0L);
		if ((!prefs.contains("downloadId")) || (prefs.getLong("downloadId", -1L) < 0L)) {
			download();
			return;
		}
		showDownloadDialog();
		queryDownloadStatus();
	}

	void openApp(String pkgName, String runParam) throws PackageManager.NameNotFoundException, UnsupportedEncodingException {
		Log.i(tag, "openApp :" + pkgName);
		PackageInfo pkgInfo = getPackageManager().getPackageInfo(pkgName, 0);
		Intent intent = new Intent("android.intent.action.MAIN", null);
		intent.addCategory("android.intent.category.LAUNCHER");
		intent.setPackage(pkgInfo.packageName);
		ResolveInfo rInfo = (ResolveInfo) getPackageManager().queryIntentActivities(intent, 0).iterator().next();
		if (rInfo != null) {
			{
				Log.i(tag, "mypackageName :" + rInfo.activityInfo.packageName);
				Log.i(tag, "className :" + rInfo.activityInfo.name);
				Intent intent2 = new Intent();
				ComponentName compontName = new ComponentName(rInfo.activityInfo.packageName, rInfo.activityInfo.name);
				if ("First_Start_From_Downloader".equals(runParam))
					intent2.putExtra("First_Start_From_Downloader", true);
				intent2.setComponent(compontName);
				intent2.setAction("android.intent.action.VIEW");
				startActivity(intent2);
			}
			finish();
		}
	}

	boolean isAppInstalled(String uri) {
		PackageManager pm = getPackageManager();
		boolean installed = false;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			installed = false;
		}
		return installed;
	}

	@SuppressWarnings("deprecation")
	void download() {
	    Log.v(tag, "download");
	    if (Environment.getExternalStorageState().equals("mounted"))
	    {
	    	if(isDelDownloadTask) manager.remove(lastDownload);
	    	delDownloadApk();
	      
	      Log.v(tag, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
	      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
	      DownloadManager.Request request = new DownloadManager.Request(Uri.parse(httpDownloadUrl));
	      request.setTitle("下载");
	      request.setDescription(apkName);
	      Log.i(tag, "setShowRunningNotification");
	      request.setShowRunningNotification(true);
	      request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
	      Log.i(tag, "setVisibleInDownloadsUi");
	      request.setVisibleInDownloadsUi(true);
	      Log.i(tag, "setDestinationInExternalFilesDir");
	      request.setDestinationInExternalFilesDir(this, null, apkName);
	      request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
	      Log.i(tag, "manager.enqueue");
	      lastDownload = manager.enqueue(request);
	      prefs.edit().putLong("downloadId", lastDownload).commit();
	      Log.i(tag, "lastDownload:" + lastDownload);
	      dowsize = 0L;
	      totalsize = 0L;
	      showDownloadDialog();
	      queryDownloadStatus();
	      return;
	    }
	    Log.i(tag, "showRetryDownloadDialog");
	    showRetryDownloadDialog("提示", "重试", "取消", "下载失败，sd卡当前不存在或不可写。");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	String failMessage(int exceptionId) {
		switch (exceptionId) {
		case 1003:
		case 1008:
			return "下载失败，无法重新开始任务";
		case 1007:
			return "下载失败，设备没找到";
		case 1009:
			return "文件已存在";
		case 1001:
			return "下载失败，文件错误";
		case 1004:
			return "下载失败，http数据错误";
		case 1006:
			return "下载失败，空间不足";
		case 1005:
			return "下载失败，重定向太多";
		case 1002:
			return "下载失败，未处理http";
		case 1000:
			return "下载失败，未知错误";
		default:
			return "下载失败，未知错误";
		}
	}

	public enum State {
		Resume, Pause, Install
	}

	final String gamePkgName = "com.qqgame.hlddz";
	final String httpDownloadUrl = "http://adrdir.qq.com/minigamefile/terminal/andriod/hlddz.apk";
	boolean interceptFlag = false;
	boolean isDelDownloadApk = false;
	boolean isDelDownloadTask = false;
	public long lastDownload = -1L;
	Context mContext;
	ProgressBar mProgress;
	TextView mTextView;
	BroadcastReceiver onComplete = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.intent.action.DOWNLOAD_COMPLETE")) {
				long downloadId = intent.getLongExtra("extra_download_id", -1L);
				Log.v(tag, " download complete! id : " + downloadId);
				Log.v(tag, "lastDownload: " + lastDownload);
				if (lastDownload == downloadId) {
					prefs.edit().clear();
					Message msg = new Message();
					msg.what = MSG_PROGRESS;
					msg.arg1 = 100;
					handler.sendMessageDelayed(msg, 1000*60);
					Log.i(tag, "sendMessage:MSG_PROGRESS");
				}
			}
		}
	};
	BroadcastReceiver onNotification = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Log.v(tag, " onNotification ");
			if (intent.getAction().equals("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED")) {
				long downloadId = intent.getLongExtra("extra_download_id", 0L);
				Log.v(tag, " ACTION_NOTIFICATION_CLICKED id : " + downloadId);
				Log.v(tag, " context.getPackageName() : " + context.getPackageName());
				if (mContext.getPackageName().equals(context.getPackageName()))
					notifyCationClicked(downloadId);
			}
		}
	};

	void notifyCationClicked(long downloadId) {
		Log.v(tag, " notifyCationClicked : " + downloadId);
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.setClass(mContext, getClass());
		startActivity(intent);
	}
}
