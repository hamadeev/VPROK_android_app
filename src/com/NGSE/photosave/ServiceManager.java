package com.NGSE.photosave;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.widget.Toast;

public class ServiceManager extends Service
{

	private Timer mTimer;
	private UpdateReceiverTimerTask mUpdateReceiverTimerTask;
	private ComponentName mR;
	private AudioManager mAudioManager;
	private static Camera mCamera;

	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	public static void writeLog(String tag, String msg)
	{
		File logFile = new File(Environment.getExternalStorageDirectory().getPath() + "/PhotoSave.txt");
		if (!logFile.exists())
		{
			try
			{
				logFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();
			String timeString = "";

			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
			try
			{
				timeString = formatter.format(date).toString();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				timeString = "----------";
			}
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
			buf.append(timeString);
			buf.append("\t");
			buf.append(msg);
			buf.newLine();
			buf.newLine();
			buf.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy()
	{
		if (mUpdateReceiverTimerTask != null)
		{
			mUpdateReceiverTimerTask.unregister();
		}
		if (mTimer != null)
		{
			mTimer.cancel();
		}
		Log.e("TEST", "onDestroy!");
		writeLog("TEST", "onDestroy!");
		Toast.makeText(this, "onDestroy!", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.e("TEST", "ServiceManager onStartCommand");
		writeLog("TEST", "ServiceManager onStartCommand");

		if (mTimer != null)
		{
			mTimer.cancel();
		}
		mTimer = new Timer();
		mUpdateReceiverTimerTask = new UpdateReceiverTimerTask(getApplicationContext());
		mTimer.schedule(mUpdateReceiverTimerTask, 30000, 30000);

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	private class UpdateReceiverTimerTask extends TimerTask
	{
		private Context mContext;

		public UpdateReceiverTimerTask(Context context)
		{
			mContext = context;
			mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			mR = new ComponentName(context, MediaButtonIntentReceiver.class);
			mAudioManager.registerMediaButtonEventReceiver(mR);
		}

		@Override
		public void run()
		{
			if (mAudioManager != null && mR != null)
			{
				mAudioManager.unregisterMediaButtonEventReceiver(mR);
			}
			else
			{
				if (mAudioManager == null)
					mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				if (mR == null)
					mR = new ComponentName(mContext, MediaButtonIntentReceiver.class);
			}
			mAudioManager.registerMediaButtonEventReceiver(mR);
		}

		public void unregister()
		{
			if (mAudioManager != null && mR != null)
			{
				mAudioManager.unregisterMediaButtonEventReceiver(mR);
			}
		}
	}

	public static class MediaButtonIntentReceiver extends BroadcastReceiver implements Camera.PictureCallback
	{
		private Context mContext;
		private PowerManager mPowermanager;
		private WakeLock mWakeLock;

		public MediaButtonIntentReceiver()
		{
			super();
		}

		@Override
		public void onReceive(Context context, Intent intent)
		{
			mContext = context;
			String intentAction = intent.getAction();
			if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction))
			{
				Log.e("TEST", "!!!!");
				return;
			}
			KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if (event == null)
			{
				Log.e("TEST", "Null!");
				return;
			}
			int action = event.getAction();
			if (action == KeyEvent.ACTION_DOWN)
			{
				Log.e("TEST", "BUTTON PRESSED! " + event.getKeyCode());
				writeLog("TEST", "BUTTON PRESSED! " + event.getKeyCode());

				mPowermanager = ((PowerManager) context.getSystemService(Context.POWER_SERVICE));
				mWakeLock = mPowermanager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
				mWakeLock.acquire();

				try
				{
					Camera.CameraInfo info = new Camera.CameraInfo();

					for (int i = 0; i < Camera.getNumberOfCameras(); i++)
					{
						Camera.getCameraInfo(i, info);

						if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
						{
							mCamera = Camera.open(i);
						}
					}
				}
				catch (Exception e)
				{
					return;
				}
				SurfaceView sv = new SurfaceView(context);
				try
				{
					mCamera.setPreviewDisplay(sv.getHolder());
					mCamera.startPreview();

					Log.e("TEST", "111");
					writeLog("TEST", "111");
					try
					{
						mCamera.takePicture(null, null, this);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						if (mCamera != null)
						{
							mCamera.stopPreview();
							mCamera.release();
						}
						if (mWakeLock.isHeld())
						{
							mWakeLock.release();
						}
						// Intent i = new Intent(context, PictureTakeActivity.class);
						// i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						// context.startActivity(i);
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
					if (mCamera != null)
					{
						mCamera.stopPreview();
						mCamera.release();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					if (mCamera != null)
					{
						mCamera.stopPreview();
						mCamera.release();
					}
				}
			}
		}

		@Override
		public void onPictureTaken(byte[] data, Camera camera)
		{
			FileOutputStream outStream = null;
			try
			{
				Calendar c = Calendar.getInstance();
				String photoDir = Environment.getExternalStorageDirectory().getPath() + "/PhotoSave/";
				String fileStr = photoDir + "Image" + c.getTimeInMillis() + ".jpg";
				Log.e("TEST", fileStr);
				writeLog("TEST", fileStr);
				File photo = new File(photoDir);
				photo.mkdir();
				outStream = new FileOutputStream(fileStr);
				outStream.write(data);
				Log.e("TEST", "DATA: " + data);
				writeLog("TEST", "DATA: " + data);
				outStream.flush();
				outStream.close();

				mCamera.stopPreview();
				mCamera.release();

				mCamera = null;
				DropboxAPI<AndroidAuthSession> mApi;

				AndroidAuthSession session = buildSession();
				mApi = new DropboxAPI<AndroidAuthSession>(session);

				Log.e("TEST", "isLinked: " + mApi.getSession().isLinked());
				writeLog("TEST", "isLinked: " + mApi.getSession().isLinked());
				if (mApi.getSession().isLinked())
				{
					photo = new File(fileStr);
					new UploadPicture(mContext, mApi, "/PhotoSave/", photo, mWakeLock).execute();
				}

				if (mWakeLock.isHeld())
				{
					mWakeLock.release();
				}
			}
			catch (FileNotFoundException e)
			{
				Log.d("TEST", "CAMERA1: " + e.getMessage());
				e.printStackTrace();
			}
			catch (IOException e)
			{
				Log.d("TEST", "CAMERA2: " + e.getMessage());
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		private AndroidAuthSession buildSession()
		{
			AppKeyPair appKeyPair = new AppKeyPair(MainActivity.APP_KEY, MainActivity.APP_SECRET);
			AndroidAuthSession session;

			String[] stored = getKeys();
			if (stored != null)
			{
				AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
				session = new AndroidAuthSession(appKeyPair, MainActivity.ACCESS_TYPE, accessToken);
			}
			else
			{
				session = new AndroidAuthSession(appKeyPair, MainActivity.ACCESS_TYPE);
			}

			return session;
		}

		private String[] getKeys()
		{
			SharedPreferences prefs = mContext.getSharedPreferences(MainActivity.ACCOUNT_PREFS_NAME, 0);
			String key = prefs.getString(MainActivity.ACCESS_KEY_NAME, null);
			String secret = prefs.getString(MainActivity.ACCESS_SECRET_NAME, null);
			if (key != null && secret != null)
			{
				String[] ret = new String[2];
				ret[0] = key;
				ret[1] = secret;
				return ret;
			}
			else
			{
				return null;
			}
		}
	}
}
