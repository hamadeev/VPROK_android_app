package com.NGSE.vprok.Task;

import com.NGSE.vprok.Data.PhotoUrl;
import com.NGSE.vprok.Utils.BitmapSizeConverter;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

public class AsyncTaskBitmapSizeConverter extends ProgressedAsyncTask<Void, Object, Void>
{
	Context context;
	PhotoUrl photoUrl;
	Runnable action;

	public AsyncTaskBitmapSizeConverter(Context context, PhotoUrl photoUrl, Runnable action)
	{
		super((Activity) context);
		this.context = context;
		this.photoUrl = photoUrl;
		this.action = action;
	}

	@Override
	protected void onPreExecute()
	{
		showProgressing();
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		resizePicture(0);
		resizePicture(1);
		return null;
	}

	private void resizePicture(int type) // 0 - thumb, 1 - resize
	{
		DisplayMetrics displaymetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int screenWidth = displaymetrics.widthPixels;
		if (type == 1)
		{
			screenWidth /= 3;
			screenWidth *= 2;
		}
		else
		{
			screenWidth = 800;
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		// Returns null, sizes are in the options variable
		BitmapFactory.decodeFile(photoUrl.getFull(), options);
		int width = options.outWidth;
		int height = options.outHeight;
		double rate;
		if (width > height)
		{
			rate = (double) height / (double) width;
			width = screenWidth;
			height = (int) (screenWidth * rate);
		}
		else
		{
			rate = (double) width / (double) height;
			height = screenWidth;
			width = (int) (screenWidth * rate);
		}
		// If you want, the MIME type will also be decoded (if possible)
		// String type = options.outMimeType;

		if (type == 0)
		{
			BitmapSizeConverter.createThumbnailToFile(photoUrl.getFull(), photoUrl.getResize(), width, height);
		}
		else
		{
			BitmapSizeConverter.createThumbnailToFile(photoUrl.getResize(), photoUrl.getThumb(), width, height);
		}
	}

	@Override
	protected void onPostExecute(Void result)
	{
		hideProgressing();
		if (action != null)
		{
			action.run();
		}

		super.onPostExecute(result);
	}

	public void progress(int progress)
	{
		this.publishProgress(progress);
	}

}
