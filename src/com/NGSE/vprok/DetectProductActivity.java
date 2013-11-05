package com.NGSE.vprok;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DetectProductActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detect_product_activity);

		((Button) findViewById(R.id.autoButton)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				getCode();
			}
		});
		((Button) findViewById(R.id.manualButton)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				((EditText) findViewById(R.id.manualProductCodeEditText)).setVisibility(View.VISIBLE);
			}
		});
		final EditText manualProductCodeEditText = (EditText) findViewById(R.id.manualProductCodeEditText);
		manualProductCodeEditText.setOnKeyListener(new OnKeyListener()
		{

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)
				{
					String codeString = manualProductCodeEditText.getText().toString();
					if (codeString.length() > 0)
					{
						Intent intent = new Intent(DetectProductActivity.this, ProductActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString(ProductActivity.BARCODE_STRING, codeString);
						intent.putExtras(bundle);
						startActivity(intent);
					}
				}
				return false;
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if (requestCode == 0)
		{
			if (resultCode == RESULT_OK)
			{
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				// Toast.makeText(getApplicationContext(), format, Toast.LENGTH_SHORT).show();
				//((EditText) findViewById(R.id.manualProductCodeEditText)).setText(contents);
				Intent i = new Intent(DetectProductActivity.this, ProductActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(ProductActivity.BARCODE_STRING, contents);
				i.putExtras(bundle);
				startActivity(i);
			}
			else
				if (resultCode == RESULT_CANCELED)
				{
					// showDialog("Error", getString(R.string.result_failed_why));
				}
		}
	}

	private void getCode()
	{
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		// intent.putExtra("SCAN_MODE", "QR_CODE");
		startActivityForResult(intent, 0);
	}
}
