package com.NGSE.vprok.Task;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public abstract class ProgressedAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{

	protected Activity activity;

	private Dialog progress;

	public ProgressedAsyncTask()
	{
		super();
	}

	public ProgressedAsyncTask(Activity activity)
	{
		super();
		this.activity = activity;
	}

	public void setActivity(Activity activity)
	{
		this.activity = activity;
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		showProgressing();
	}

	@Override
	protected Result doInBackground(Params... params)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onCancelled()
	{
		// TODO Auto-generated method stub
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(Result result)
	{
		hideProgressing();
	}

	protected void runOnUiThread(Runnable action)
	{
		activity.runOnUiThread(action);
	}

	protected void showProgressing()
	{
		if (activity != null)
		{
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						if (progress != null && progress.isShowing())
							progress.dismiss();
					}
					catch (IllegalArgumentException ex)
					{
						ex.printStackTrace();
					}
					progress = showProgress(activity);
				}
			});
		}
	}

	protected void hideProgressing()
	{
		try
		{
			if (progress != null && progress.isShowing())
				progress.dismiss();
		}
		catch (IllegalArgumentException ex)
		{
			ex.printStackTrace();
		}
	}

	public static Dialog showProgress(Context context)
	{
		try
		{
			ProgressDialog dialog = ProgressDialog.show(context, "", "Loading. Please wait...", true);
			return dialog;
		}
		catch (Exception ex)
		{
		}
		return null;
	}

}
