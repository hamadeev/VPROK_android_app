package com.NGSE.photosave;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;

public class MainActivity extends Activity
{
	Intent intent_service;

	private static final String TAG = "MainActivity";
	public final static String APP_KEY = "hg4p26yuu1vl13y";
	public final static String APP_SECRET = "ra246z7wt4x13st";
	public final static AccessType ACCESS_TYPE = AccessType.DROPBOX;
	public final static String ACCOUNT_PREFS_NAME = "prefs";
	public final static String ACCESS_KEY_NAME = "ACCESS_KEY";
	public final static String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	DropboxAPI<AndroidAuthSession> mApi;
	private boolean mLoggedIn;
	private Button mSubmit;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);

		setContentView(R.layout.activity_main);

		checkAppKeySetup();

		mSubmit = (Button) findViewById(R.id.auth_button);

		mSubmit.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (mLoggedIn)
				{
					logOut();
				}
				else
				{
					mApi.getSession().startAuthentication(MainActivity.this);
				}
			}
		});

		((Button) findViewById(R.id.photo)).setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(MainActivity.this, PictureTakeActivity.class);
				startActivity(i);
			}
		});
		setLoggedIn(mApi.getSession().isLinked());

		intent_service = new Intent(this, ServiceManager.class);
		startService(intent_service);
		Log.e("TEST", "onCreate!");

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		AndroidAuthSession session = mApi.getSession();
		if (session.authenticationSuccessful())
		{
			try
			{
				session.finishAuthentication();

				TokenPair tokens = session.getAccessTokenPair();
				storeKeys(tokens.key, tokens.secret);
				setLoggedIn(true);
			}
			catch (IllegalStateException e)
			{
				showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
				Log.i(TAG, "Error authenticating", e);
			}
		}
	}

	private void logOut()
	{
		mApi.getSession().unlink();
		clearKeys();
		setLoggedIn(false);
	}

	private void setLoggedIn(boolean loggedIn)
	{
		mLoggedIn = loggedIn;
		if (loggedIn)
		{
			mSubmit.setText("Unlink from Dropbox");
		}
		else
		{
			mSubmit.setText("Link with Dropbox");
		}
	}

	private void checkAppKeySetup()
	{
		if (APP_KEY.startsWith("CHANGE") || APP_SECRET.startsWith("CHANGE"))
		{
			showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
			finish();
			return;
		}
		Intent testIntent = new Intent(Intent.ACTION_VIEW);
		String scheme = "db-" + APP_KEY;
		String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
		testIntent.setData(Uri.parse(uri));
		PackageManager pm = getPackageManager();
		if (0 == pm.queryIntentActivities(testIntent, 0).size())
		{
			showToast("URL scheme in your app's " + "manifest is not set up correctly. You should have a " + "com.dropbox.client2.android.AuthActivity with the " + "scheme: " + scheme);
			finish();
		}
	}

	private void showToast(String msg)
	{
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}

	private String[] getKeys()
	{
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
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

	private void storeKeys(String key, String secret)
	{
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.putString(ACCESS_KEY_NAME, key);
		edit.putString(ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	private void clearKeys()
	{
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	private AndroidAuthSession buildSession()
	{
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		String[] stored = getKeys();
		if (stored != null)
		{
			AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
		}
		else
		{
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		}

		return session;
	}
}
