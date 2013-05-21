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
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
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

public class AbstractAsyncRequestPost extends AsyncTask<Object, Void, String>
{

	public static enum TYPE_POST_OPERATION
	{
		NONE, AUTH
	};

	protected TYPE_POST_OPERATION typeOperation;

	protected Context context;
	protected int statusCode;
	protected boolean status;
	public StringEntity stringEntity;
	protected Dialog dialog;
	protected Activity activity;

	public AbstractAsyncRequestPost(Activity activity)
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
		typeOperation = (TYPE_POST_OPERATION) arg0[1];
		String res = null;
		stringEntity = (StringEntity) arg0[2];
		try
		{
			res = getUrlData((String) arg0[3]);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (res == null)
		{
			try
			{
				res = getUrlData((String) arg0[3]);
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
		httpParameters.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

		HttpConnectionParams.setConnectionTimeout(httpParameters, Constants.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParameters, Constants.CONNECTION_TIMEOUT);

		SchemeRegistry scheme = new SchemeRegistry();
		scheme.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(httpParameters, scheme);

		DefaultHttpClient client = new DefaultHttpClient(conMgr, httpParameters);
		HttpPost httppost = new HttpPost(url);
		httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		httppost.setHeader("Content-Language", "en-US");

		httppost.setEntity(stringEntity);

		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		final String cookiestr = cookieManager.getCookie("vprok.tataronrails.com");
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
				cookie.setDomain("vprok.tataronrails.com");
				cookieStore.addCookie(cookie);
			}
			client.setCookieStore(cookieStore);
		}

		HttpResponse res = client.execute(httppost);
		statusCode = res.getStatusLine().getStatusCode();
		status = statusCode < 400;
		Log.e("TEST", "StatusCode: " + statusCode);

		List<Cookie> cookies = client.getCookieStore().getCookies();
		if (!cookies.isEmpty())
		{
			// CookieSyncManager.createInstance(context);
			// cookieManager = CookieManager.getInstance();
			// cookieManager.removeSessionCookie();

			// sync all the cookies in the httpclient with the webview by
			// generating cookie string
			for (Cookie cookie : cookies)
			{
				Cookie sessionInfo = cookie;

				String cookieString = sessionInfo.getName() + "=" + sessionInfo.getValue() + "; domain=" + sessionInfo.getDomain();
				Log.e("TEST", "get cookie " + cookieString);
				cookieManager.setCookie(sessionInfo.getDomain() + sessionInfo.getPath(), cookieString);
			}
			CookieSyncManager.getInstance().sync();
		}

		return Utility.generateString((InputStream) res.getEntity().getContent());
	}
}
