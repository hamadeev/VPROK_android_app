package com.NGSE.vprok;

import com.NGSE.vprok.Utils.Constants;
import com.NGSE.vprok.Utils.Utility;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends Activity
{

	EditText loginEditText;
	EditText passwordEditText;
	EditText apiEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_activity);

		((Button) findViewById(R.id.saveButton)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Utility.savePref(getApplicationContext(), Constants.LOGIN, loginEditText.getText().toString());
				Utility.savePref(getApplicationContext(), Constants.PASSWORD, passwordEditText.getText().toString());
				String apiString = apiEditText.getText().toString();
				if (apiString.length() > 0 && !apiString.substring(apiString.length() - 1).equalsIgnoreCase("/"))
				{
					apiString += "/";
				}
				Utility.savePref(getApplicationContext(), Constants.API, apiString);
				finish();
			}
		});
		loginEditText = (EditText) findViewById(R.id.loginEditText);
		passwordEditText = (EditText) findViewById(R.id.passwordEditText);
		apiEditText = (EditText) findViewById(R.id.apiEditText);

		String temp = Utility.getPref(getApplicationContext(), Constants.LOGIN);
		if (temp != null)
			loginEditText.setText(temp);
		temp = Utility.getPref(getApplicationContext(), Constants.PASSWORD);
		if (temp != null)
			passwordEditText.setText(temp);
		temp = Utility.getPref(getApplicationContext(), Constants.API);
		if (temp != null)
			apiEditText.setText(temp);

	}
}
