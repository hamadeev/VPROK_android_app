package com.NGSE.vprok;

import com.NGSE.vprok.Data.BarCode;
import com.NGSE.vprok.Task.AbstractAsyncRequestGet;
import com.NGSE.vprok.Task.AbstractAsyncRequestGet.TYPE_GET_OPERATION;
import com.NGSE.vprok.Task.AbstractAsyncRequestPost;
import com.NGSE.vprok.Task.AbstractAsyncRequestPost.TYPE_POST_OPERATION;
import com.NGSE.vprok.Utils.CodeRequestManager;
import com.NGSE.vprok.Utils.Constants;
import com.NGSE.vprok.Utils.JsonParser;
import com.NGSE.vprok.Utils.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ProductActivity extends Activity
{
	private BarCode mBarCode;;
	public static final String BARCODE_STRING = "barcode";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.product_activity);
		Intent intent = getIntent();
		Bundle bundle = null;
		if (intent != null)
		{
			bundle = intent.getExtras();
			if (bundle != null && bundle.containsKey(BARCODE_STRING))
			{
				String apiString = Utility.getPref(getApplicationContext(), Constants.API);
				new AsyncBarcodeRequest(this).execute(getApplicationContext(), TYPE_GET_OPERATION.GET_PRODUCT, apiString + "info" + CodeRequestManager.codeProductRequest(bundle.getString(BARCODE_STRING)));
			}
		}
		((Button) findViewById(R.id.nextProductButton)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		((Button) findViewById(R.id.startPhotoButton)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(ProductActivity.this, ProductPhotoActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(ProductActivity.BARCODE_STRING, mBarCode.barcode);
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			}
		});
	}

	private void updateUI()
	{
		((TextView) findViewById(R.id.productNameTextView)).setText(getString(R.string.product_name) + " " + mBarCode.name);
		((TextView) findViewById(R.id.productCodeTextView)).setText(getString(R.string.product_code) + " " + mBarCode.barcode);
		((TextView) findViewById(R.id.productPhotoTextView)).setText(getString(R.string.product_photo_count) + " " + mBarCode.imageCount);
		if (mBarCode.imageCount > 0)
		{
			((Button) findViewById(R.id.nextProductButton)).setVisibility(View.VISIBLE);
		}
	}

	class AsyncBarcodeRequest extends AbstractAsyncRequestGet
	{
		public AsyncBarcodeRequest(Activity activity)
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
					new AlertDialog.Builder(ProductActivity.this).setMessage("Ошибка загрузки!").setPositiveButton("Ок", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialogInterface, int i)
						{
							dialogInterface.dismiss();
							finish();
						}
					}).show();
					Toast.makeText(ProductActivity.this, "Error", Toast.LENGTH_LONG).show();
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
				try
				{
					new AlertDialog.Builder(ProductActivity.this).setMessage("Ошибка загрузки! " + error != null ? error : "").setPositiveButton("Ок", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialogInterface, int i)
						{
							dialogInterface.dismiss();
							finish();
						}
					}).show();
					if (error != null && error.length() > 0)
						Toast.makeText(ProductActivity.this, error, Toast.LENGTH_LONG).show();
				}
				catch (Exception e)
				{

				}
				return;
			}
			mBarCode = JsonParser.parseJsonBarCode(result);
			updateUI();
		}
	}
}
