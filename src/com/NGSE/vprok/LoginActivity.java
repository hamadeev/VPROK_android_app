package com.NGSE.vprok;

import com.NGSE.vprok.Task.AbstractAsyncRequestPost;
import com.NGSE.vprok.Task.AbstractAsyncRequestPost.TYPE_POST_OPERATION;
import com.NGSE.vprok.Utils.CodeRequestManager;
import com.NGSE.vprok.Utils.Constants;
import com.NGSE.vprok.Utils.JsonParser;
import com.NGSE.vprok.Utils.Utility;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class LoginActivity extends Activity
{
	EditText loginEditText;
	EditText passwordEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);

		loginEditText = (EditText) findViewById(R.id.loginEditText);
		passwordEditText = (EditText) findViewById(R.id.passwordEditText);

		((Button) findViewById(R.id.loginButton)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (Utility.getPref(getApplicationContext(), Constants.API) == null || Utility.getPref(getApplicationContext(), Constants.API).length() == 0)
				{
					new AlertDialog.Builder(LoginActivity.this).setMessage("Введите адрес API в настройках соединения.").setPositiveButton("Ок", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialogInterface, int i)
						{
							dialogInterface.dismiss();
						}
					}).show();
					return;
				}
				String login = loginEditText.getText().toString();
				String password = passwordEditText.getText().toString();
				if (checkData(login, password))
				{
					String apiString = Utility.getPref(getApplicationContext(), Constants.API);
					new AsyncLoginPostRequest(LoginActivity.this).execute(getApplicationContext(), TYPE_POST_OPERATION.AUTH, CodeRequestManager.codeAuthRequest(login, password), apiString + "login");
				}
			}
		});

		((Button) findViewById(R.id.connectionSettingButton)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});
	}

	private boolean checkData(String login, String password)
	{
		if (login != null && login.length() > 0 && password != null && password.length() > 0)
		{
			return true;
		}
		return false;
	}

	class AsyncLoginPostRequest extends AbstractAsyncRequestPost
	{
		public AsyncLoginPostRequest(Activity activity)
		{
			super(activity);
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			if (result == null)
			{
				try
				{
					new AlertDialog.Builder(LoginActivity.this).setMessage("Неправильный логин или пароль!").setPositiveButton("Ок", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialogInterface, int i)
						{
							dialogInterface.dismiss();
						}
					}).show();
					Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_LONG).show();
				}
				catch (Exception e)
				{

				}
				return;
			}
			Log.e("TEST", "Result: " + result);
			String error = JsonParser.parseJsonError(result);
			if (error != null || statusCode == 404)
			{
				Utility.savePref(getApplicationContext(), Constants.LOGIN, null);
				Utility.savePref(getApplicationContext(), Constants.PASSWORD, null);
				try
				{
					new AlertDialog.Builder(LoginActivity.this).setMessage("Неправильный логин или пароль!").setPositiveButton("Ок", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialogInterface, int i)
						{
							dialogInterface.dismiss();
						}
					}).show();
					if (error != null && error.length() > 0)
						Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
				}
				catch (Exception e)
				{

				}
				return;
			}
			String login = loginEditText.getText().toString();
			String password = passwordEditText.getText().toString();
			Utility.savePref(getApplicationContext(), Constants.LOGIN, login);
			Utility.savePref(getApplicationContext(), Constants.PASSWORD, password);
			Intent intent = new Intent(LoginActivity.this, DetectProductActivity.class);
			startActivity(intent);
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		String temp = Utility.getPref(getApplicationContext(), Constants.LOGIN);
		if (temp != null)
			loginEditText.setText(temp);
		temp = Utility.getPref(getApplicationContext(), Constants.PASSWORD);
		if (temp != null)
			passwordEditText.setText(temp);
	}

	@Override
	protected void onDestroy()
	{
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		Log.e("TEST", "LoginActivity onDestroy removeAllCookie");
		super.onDestroy();
	}

}
