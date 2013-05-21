package com.NGSE.photosave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

/**
 * Here we show uploading a file in a background thread, trying to show typical exception handling and flow of control for an app that uploads a file from Dropbox.
 */
public class UploadPicture extends AsyncTask<Void, Long, Boolean>
{

	private DropboxAPI<?> mApi;
	private String mPath;
	private File mFile;

	private UploadRequest mRequest;
	private Context mContext;

	private String mErrorMsg;
	private WakeLock mWakeLock;

	public UploadPicture(Context context, DropboxAPI<?> api, String dropboxPath, File file, WakeLock wakeLock)
	{
		Log.e("TEST", "UploadPicture create");
		ServiceManager.writeLog("TEST", "UploadPicture create");
		mContext = context.getApplicationContext();
		mApi = api;
		mPath = dropboxPath;
		mFile = file;
		mWakeLock = wakeLock;
	}

	@Override
	protected Boolean doInBackground(Void... params)
	{
		Log.e("TEST", "UploadPicture doInBackground");
		ServiceManager.writeLog("TEST", "UploadPicture doInBackground");
		try
		{
			FileInputStream fis = new FileInputStream(mFile);
			String path = mPath + mFile.getName();
			mRequest = mApi.putFileOverwriteRequest(path, fis, mFile.length(), new ProgressListener()
			{
				@Override
				public long progressInterval()
				{
					return 500;
				}

				@Override
				public void onProgress(long bytes, long total)
				{
					publishProgress(bytes);
				}
			});

			if (mRequest != null)
			{
				mRequest.upload();
				return true;
			}

		}
		catch (DropboxUnlinkedException e)
		{
			mErrorMsg = "This app wasn't authenticated properly.";
		}
		catch (DropboxFileSizeException e)
		{
			mErrorMsg = "This file is too big to upload";
		}
		catch (DropboxPartialFileException e)
		{
			mErrorMsg = "Upload canceled";
		}
		catch (DropboxServerException e)
		{
			if (e.error == DropboxServerException._401_UNAUTHORIZED)
			{

			}
			else
				if (e.error == DropboxServerException._403_FORBIDDEN)
				{

				}
				else
					if (e.error == DropboxServerException._404_NOT_FOUND)
					{
						// path not found (or if it was the thumbnail, can't be
						// thumbnailed)
					}
					else
						if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE)
						{
							// user is over quota
						}
						else
						{
							// Something else
						}
			mErrorMsg = e.body.userError;
			if (mErrorMsg == null)
			{
				mErrorMsg = e.body.error;
			}
		}
		catch (DropboxIOException e)
		{
			mErrorMsg = "Network error.  Try again.";
		}
		catch (DropboxParseException e)
		{
			mErrorMsg = "Dropbox error.  Try again.";
		}
		catch (DropboxException e)
		{
			// Unknown error
			mErrorMsg = "Unknown error.  Try again.";
		}
		catch (FileNotFoundException e)
		{
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result)
	{
		if (result)
		{
			showToast("Image successfully uploaded");
		}
		else
		{
			showToast(mErrorMsg);
		}
		if (mWakeLock.isHeld())
		{
			mWakeLock.release();
		}
	}

	private void showToast(String msg)
	{
		Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
		error.show();
	}
}
