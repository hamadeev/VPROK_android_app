package com.NGSE.vprok.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.NGSE.vprok.Data.CustomCookies;
import com.NGSE.vprok.Utils.Constants;
import com.NGSE.vprok.Utils.Utility;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class AbstractAsyncRequestGet extends AsyncTask<Object, Void, String>
{
	public static enum TYPE_GET_OPERATION
	{
		NONE, GET_PRODUCT
	};

	protected TYPE_GET_OPERATION typeOperation;
	protected TYPE_GET_OPERATION typeOperation2;
	protected Context context;
	protected boolean status;
	public int statusCode;
	protected Dialog dialog;
	protected Activity activity;

	public AbstractAsyncRequestGet(Activity activity)
	{
		super();
		this.activity = activity;
	}

	@Override
	protected void onPreExecute()
	{
		showProgressing();
		super.onPreExecute();
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
						if (dialog != null && dialog.isShowing())
							dialog.dismiss();
					}
					catch (IllegalArgumentException ex)
					{
						ex.printStackTrace();
					}
					dialog = showProgress(activity);
				}
			});
		}
	}

	protected void runOnUiThread(Runnable action)
	{
		activity.runOnUiThread(action);
	}

	protected void hideProgressing()
	{
		try
		{
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
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

	@Override
	protected void onPostExecute(String result)
	{
		super.onPostExecute(result);
		hideProgressing();
	}

	@Override
	protected String doInBackground(Object... arg0)
	{
		context = (Context) arg0[0];
		typeOperation = (TYPE_GET_OPERATION) arg0[1];
		String res = null;
		try
		{
			res = getUrlData((String) arg0[2]);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (res == null)
		{
			try
			{
				res = getUrlData((String) arg0[2]);
			}
			catch (Exception e)
			{
			}
		}
		return res;
	}

	protected String getUrlData(String url) throws IOException, URISyntaxException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
	{
		Log.e("TEST", url);
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, Constants.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParameters, Constants.CONNECTION_TIMEOUT * 2);
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		HttpGet httpget = new HttpGet(url);

		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		final String cookiestr = cookieManager.getCookie("shop.tkvprok.ru");
		Log.e("TEST", "have cookie " + cookiestr);
		if (cookiestr != null)
		{
			String[] separated = cookiestr.split("; ");
			String[] separated1 = separated[0].split("=");

			List<CustomCookies> customCookies = new ArrayList<CustomCookies>();
			customCookies.add(new CustomCookies(separated1[0], separated1[1]));

			CookieStore cookieStore = new BasicCookieStore();
			BasicClientCookie cookie;
			for (int i = 0; i < customCookies.size(); i++)
			{
				cookie = new BasicClientCookie(customCookies.get(i).getName(), customCookies.get(i).getValue());
				cookie.setDomain("shop.tkvprok.ru");
				cookieStore.addCookie(cookie);
			}
			client.setCookieStore(cookieStore);
		}

		HttpResponse res = client.execute(httpget);
		statusCode = res.getStatusLine().getStatusCode();
		if (statusCode == 200 || statusCode == 201)
			status = true;
		else
			status = false;
		return Utility.generateString((InputStream) res.getEntity().getContent());
	}
}
