package com.NGSE.photosave;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

public class PictureTakeActivity extends Activity
{
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera mCamera = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;
	private PowerManager mPowermanager;
	private WakeLock mWakeLock;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// KeyguardManager mKeyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
		// KeyguardLock mKeyguardLock = mKeyguardManager.newKeyguardLock("tag");
		// mKeyguardLock.disableKeyguard();
		// mKeyguardManager.exitKeyguardSecurely(null);

		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Log.i("TEST", "Wake up the phone and disable keyguard");
		mPowermanager = ((PowerManager) this.getSystemService(Context.POWER_SERVICE));
		mWakeLock = mPowermanager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
		mWakeLock.acquire();

		setContentView(R.layout.picture_take_activity);

		preview = (SurfaceView) findViewById(R.id.preview);

		previewHolder = preview.getHolder();

		previewHolder.addCallback(surfaceCallback);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.e("TEST", "onResume");
		getCamera();
		Log.e("TEST", "camera " + mCamera);
		startPreview();
	}

	private void getCamera()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
		{
			Camera.CameraInfo info = new Camera.CameraInfo();

			for (int i = 0; i < Camera.getNumberOfCameras(); i++)
			{
				Camera.getCameraInfo(i, info);

				if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
				{
					try
					{
						mCamera = Camera.open(i);
					}
					catch (Exception e)
					{
						mCamera = null;
						e.printStackTrace();
					}
				}
			}
		}

		if (mCamera == null)
		{
			mCamera = Camera.open();
		}
	}

	@Override
	public void onPause()
	{
		Log.e("TEST", "onPause");
		if (inPreview)
		{
			mCamera.stopPreview();
		}

		mCamera.release();
		mCamera = null;
		inPreview = false;

		super.onPause();
	}

	private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters)
	{
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes())
		{
			if (size.width <= width && size.height <= height)
			{
				if (result == null)
				{
					result = size;
				}
				else
				{
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea)
					{
						result = size;
					}
				}
			}
		}

		return (result);
	}

	private Camera.Size getBiggestPictureSize(Camera.Parameters parameters)
	{
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPictureSizes())
		{
			if (result == null)
			{
				result = size;
			}
			else
			{
				int resultArea = result.width * result.height;
				int newArea = size.width * size.height;

				if (newArea > resultArea)
				{
					result = size;
				}
			}
		}

		return (result);
	}

	private void initPreview(int width, int height)
	{
		Log.e("TEST", "initPreview");

		Log.e("TEST", "camera " + mCamera);
		if (mCamera == null)
		{
			getCamera();
		}
		Log.e("TEST", "camera " + mCamera);
		Log.e("TEST", "previewHolder.getSurface() " + previewHolder.getSurface());
		if (mCamera != null && previewHolder.getSurface() != null)
		{
			try
			{
				mCamera.setPreviewDisplay(previewHolder);
			}
			catch (Throwable t)
			{
				Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
				Toast.makeText(PictureTakeActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
			}

			if (!cameraConfigured)
			{
				Camera.Parameters parameters = mCamera.getParameters();
				Camera.Size size = getBestPreviewSize(width + 300, height + 300, parameters);
				Camera.Size pictureSize = getBiggestPictureSize(parameters);

				if (size != null && pictureSize != null)
				{
					parameters.setPreviewSize(size.width, size.height);
					parameters.setPictureSize(pictureSize.width, pictureSize.height);
					parameters.setPictureFormat(ImageFormat.JPEG);
					mCamera.setParameters(parameters);
					cameraConfigured = true;
				}
			}
			Log.e("TEST", "cameraConfigured: " + cameraConfigured);
		}
	}

	private void startPreview()
	{
		if (cameraConfigured && mCamera != null)
		{
			mCamera.startPreview();
			inPreview = true;
		}
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback()
	{
		public void surfaceCreated(SurfaceHolder holder)
		{
			Log.e("TEST", "surfaceCreated");
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
		{
			Log.e("TEST", "surfaceChanged");
			initPreview(width, height);
			startPreview();
			Log.e("TEST", "inPreview: " + inPreview);
			if (inPreview)
			{
				if (mCamera != null)
				{
					mCamera.takePicture(null, null, photoCallback);
					inPreview = false;
				}
				else
				{
					Log.e("TEST", "Camera NULL");
				}
			}
			else
			{
				new Handler().postDelayed(new Runnable()
				{

					@Override
					public void run()
					{
						if (inPreview)
						{
							if (mCamera != null)
							{
								mCamera.takePicture(null, null, photoCallback);
								inPreview = false;
							}
							else
							{
								Log.e("TEST", "Camera NULL");
							}
						}
						else
						{
							if (mWakeLock.isHeld())
							{
								mWakeLock.release();
							}
							finish();
						}
					}
				}, 1000);
			}

		}

		public void surfaceDestroyed(SurfaceHolder holder)
		{
			Log.e("TEST", "surfaceDestroyed");
		}
	};

	Camera.PictureCallback photoCallback = new Camera.PictureCallback()
	{
		public void onPictureTaken(byte[] data, Camera camera)
		{
			new SavePhotoTask(getApplicationContext(), data).execute();
			if (mCamera != null)
			{
				mCamera.stopPreview();
				mCamera.release();
			}
			finish();
		}
	};

	class SavePhotoTask extends AsyncTask<Void, String, String>
	{
		private Context mContext;
		private byte[] mData;

		public SavePhotoTask(Context context, byte[] data)
		{
			mContext = context;
			mData = data;
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... object)
		{
			Log.e("TEST", "SavePhotoTask");
			Calendar c = Calendar.getInstance();
			String photoDir = Environment.getExternalStorageDirectory().getPath() + "/PhotoSave/";
			String fileStr = photoDir + "Image" + c.getTimeInMillis() + ".jpg";
			Log.e("TEST", fileStr);
			File photo = new File(photoDir);
			photo.mkdir();
			FileOutputStream outStream = null;
			try
			{
				outStream = new FileOutputStream(fileStr);
				outStream.write(mData);
				Log.e("TEST", "DATA: " + mData);
				outStream.flush();
				outStream.close();
			}
			catch (java.io.IOException e)
			{
				Log.e("TEST", "Exception in photoCallback", e);
			}

			DropboxAPI<AndroidAuthSession> mApi;

			AndroidAuthSession session = buildSession();
			mApi = new DropboxAPI<AndroidAuthSession>(session);

			if (mApi.getSession().isLinked())
			{
				photo = new File(fileStr);
				new UploadPicture(mContext, mApi, "/PhotoSave/", photo, mWakeLock).execute();
			}

			return (null);
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
