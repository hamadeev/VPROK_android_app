package com.NGSE.vprok;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CancelPhotoActivity extends Activity
{
	public static final String PHOTO_COUNT = "photo_count";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cancel_photo_activity);
		Intent intent = getIntent();
		Bundle bundle = null;
		if (intent != null)
		{
			bundle = intent.getExtras();
			if (bundle != null && bundle.containsKey(PHOTO_COUNT))
			{
				((TextView) findViewById(R.id.complitePhotoCountTextView)).setText(String.format(getString(R.string.complite_photo_count_text), bundle.getInt(PHOTO_COUNT)));
			}
		}
		((Button) findViewById(R.id.yesButton)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				returnData(true);
			}
		});
		((Button) findViewById(R.id.noButton)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				returnData(false);
			}
		});
	}

	private void returnData(boolean cancel)
	{
		Intent intent = new Intent();
		intent.putExtra("cancel", cancel);
		setResult(RESULT_OK, intent);
		finish();
	}
}
