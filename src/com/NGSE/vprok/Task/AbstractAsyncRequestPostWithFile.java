package com.NGSE.vprok.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import com.NGSE.vprok.LoginActivity;
import com.NGSE.vprok.Data.CustomCookies;
import com.NGSE.vprok.Utils.Constants;
import com.NGSE.vprok.Utils.JsonParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

public class AbstractAsyncRequestPostWithFile extends ProgressedAsyncTask<Object, Void, String>
{
	protected Context context;
	protected String uploadUrl;
	protected ArrayList<UploadItem> items;
	protected boolean isPost;
	protected boolean status;
	protected Runnable action;

	public AbstractAsyncRequestPostWithFile(Context context)
	{
		super((Activity) context);
		this.context = context;
	}

	@Override
	protected void onPreExecute()
	{
		showProgressing();
		super.onPreExecute();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String doInBackground(Object... arg0)
	{
		context = (Context) arg0[0];
		uploadUrl = (String) arg0[1];
		items = (ArrayList<UploadItem>) arg0[2];
		isPost = (Boolean) arg0[3];
		action = (Runnable) arg0[4];
		return uploadFileData(uploadUrl, items, isPost);
	}

	@Override
	protected void onPostExecute(String result)
	{
		hideProgressing();
		Log.e("TEST", "result " + result);

		String error = JsonParser.parseJsonError(result);
		if (error == null)
		{
			if (action != null)
			{
				action.run();
			}
		}
		else
		{
			new AlertDialog.Builder(context).setMessage("Ошибка отправки!").setPositiveButton("Ок", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialogInterface, int i)
				{
					dialogInterface.dismiss();
				}
			}).show();
			if (error != null && error.length() > 0)
				Toast.makeText(context, error, Toast.LENGTH_LONG).show();
		}
		super.onPostExecute(result);
	}

	private String uploadFileData(String uploadUrl, ArrayList<UploadItem> items, boolean isPost)
	{
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, Constants.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParameters, Constants.CONNECTION_TIMEOUT);

		httpParameters.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

		DefaultHttpClient mHttpClient = new DefaultHttpClient(httpParameters);
		HttpPost httprequest = new HttpPost(uploadUrl);
		httprequest.addHeader("Charset", "UTF-8");

		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		try
		{
			for (int i = 0; i < items.size(); ++i)
				if (!items.get(i).isFile)
					multipartEntity.addPart(items.get(i).name, new StringBody(items.get(i).value, Charset.forName("UTF-8")));
				else
					multipartEntity.addPart(items.get(i).name, new FileBody(new File(items.get(i).value)));
			// set login/pass
			// httprequest.addHeader("Authorization","Basic " +
			// Base64.encodeToString("admin:zpkjq".getBytes(),Base64.NO_WRAP));

			httprequest.setEntity(multipartEntity);
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
				mHttpClient.setCookieStore(cookieStore);
			}
			return (String) mHttpClient.execute(httprequest, new ResponseHandler<Object>()
			{
				@Override
				public Object handleResponse(HttpResponse response) throws ClientProtocolException, IOException
				{
					int statusCode = response.getStatusLine().getStatusCode();

					if (statusCode < 400)
					{
						status = true;
						return generateString((InputStream) response.getEntity().getContent(), response.getEntity().getContentLength());
						// if (statusCode != 204)
						// return Utility.generateString(response
						// .getEntity().getContent());
					}
					else
						status = false;
					return null;
				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
			status = false;
			return null;
		}
	}

	protected String generateString(InputStream stream, long size) throws IOException
	{
		BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		String cur;
		// long total = 0;
		while ((cur = buffer.readLine()) != null)
		{
			if (this.isCancelled())
				return null;
			sb.append(cur + "\n");
			// total += cur.length() + 1;
			// this.publishProgress((int)((float)(total*100/size)));
		}
		return sb.toString();
	}

	public static class UploadItem
	{
		String name;
		String value;
		boolean isFile;

		public UploadItem(String name, String value, boolean isFile)
		{
			this.name = name;
			this.value = value;
			this.isFile = isFile;
		}
	}

}
