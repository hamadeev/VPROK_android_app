package com.NGSE.vprok;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import com.NGSE.vprok.Adapters.ProductPhotoListViewAdapter;
import com.NGSE.vprok.Adapters.ProductPhotoListViewAdapter.OnLayoutLoaded;
import com.NGSE.vprok.Data.PhotoUrl;
import com.NGSE.vprok.ProductActivity.AsyncBarcodeRequest;
import com.NGSE.vprok.Task.AbstractAsyncRequestPostWithFile;
import com.NGSE.vprok.Task.AbstractAsyncRequestGet.TYPE_GET_OPERATION;
import com.NGSE.vprok.Task.AbstractAsyncRequestPostWithFile.UploadItem;
import com.NGSE.vprok.Task.AsyncTaskBitmapSizeConverter;
import com.NGSE.vprok.Utils.CodeRequestManager;
import com.NGSE.vprok.Utils.Constants;
import com.NGSE.vprok.Utils.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ProductPhotoActivity extends Activity
{
	private String FOLDER_PHOTO_NAME;
	private String PHOTO_NAME = "";
	private ProductPhotoListViewAdapter mAdapter;
	private ListView mListView;
	private ArrayList<ArrayList<PhotoUrl>> mItems;
	private TextView mComplitePhotoTextView;
	public final int PHOTO_REQUEST_CODE = 101;
	public final int CLOSE_ACTIVITY_REQUEST_CODE = 102;
	private String mBarCode;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.product_photo_activity);

		Intent intent = getIntent();
		Bundle bundle = null;
		if (intent != null)
		{
			bundle = intent.getExtras();
			if (bundle != null && bundle.containsKey(ProductActivity.BARCODE_STRING))
			{
				mBarCode = bundle.getString(ProductActivity.BARCODE_STRING);
			}
		}

		mListView = (ListView) findViewById(R.id.productPhotoListView);
		mComplitePhotoTextView = (TextView) findViewById(R.id.complitePhotoTextView);

		mItems = new ArrayList<ArrayList<PhotoUrl>>();
		mAdapter = new ProductPhotoListViewAdapter(ProductPhotoActivity.this, R.layout.product_photo_listview_item, mItems, new OnLayoutLoaded()
		{
			@Override
			public void onLoad(int position)
			{
				// TODO Auto-generated method stub
			}
		});
		mListView.setAdapter(mAdapter);

		((Button) findViewById(R.id.makePhotoButton)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startCameraApplication(isSDCardEnabled());
			}
		});
		((Button) findViewById(R.id.saveOnServerButton)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				sendData();
			}
		});
		((Button) findViewById(R.id.backButton)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mAdapter.getAllCount() != 0)
				{
					Intent intent = new Intent(ProductPhotoActivity.this, CancelPhotoActivity.class);
					Bundle bundle = new Bundle();
					bundle.putInt(CancelPhotoActivity.PHOTO_COUNT, mAdapter.getAllCount());
					intent.putExtras(bundle);
					startActivityForResult(intent, CLOSE_ACTIVITY_REQUEST_CODE);
				}
			}
		});
		clearTempFolder();
		if (isSDCardEnabled())
		{
			FOLDER_PHOTO_NAME = getTempPath() + File.separator;
		}
	}

	private boolean isSDCardEnabled()
	{
		File tempFile = new File(getTempPath());
		tempFile.mkdirs();
		return tempFile.canWrite();
	}

	private String getTempPath()
	{
		StringBuilder path = new StringBuilder();
		path.append(getRootFolder(getApplicationContext()));
		path.append("phototemp");

		return path.toString();
	}

	private String getRootFolder(Context context)
	{
		return getRootFolderWithoutPackage(context) + "com.NGSE.vprok" + File.separator;
	}

	private static String getRootFolderWithoutPackage(Context context)
	{

		String state = Environment.getExternalStorageState();
		boolean isTempStorageAccessibile = Environment.MEDIA_MOUNTED.equals(state);

		if (isTempStorageAccessibile)
		{
			return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/";
		}
		else
		{
			ContextWrapper cw = new ContextWrapper(context);
			File directory = cw.getDir("media", Context.MODE_PRIVATE);
			return directory.getAbsolutePath();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);
		String selectedImage = null;

		if (resultCode != Activity.RESULT_OK)
		{
			return;
		}
		if (requestCode == CLOSE_ACTIVITY_REQUEST_CODE)
		{
			if (intent.getBooleanExtra("cancel", false))
			{
				finish();
			}
		}
		if (requestCode == PHOTO_REQUEST_CODE)
		{
			String full_path = null;
			Uri selectedImageUri = null;
			if (intent == null)
			{
				selectedImageUri = Uri.fromFile(new File(FOLDER_PHOTO_NAME + PHOTO_NAME));
				full_path = FOLDER_PHOTO_NAME + PHOTO_NAME;
			}
			else
			{
				if (intent.getData() != null)
				{
					selectedImageUri = intent.getData();
					if (selectedImageUri.getScheme().equalsIgnoreCase("content"))
					{
						String[] filePathColumn =
						{ MediaStore.Images.Media.DATA };
						Cursor cursor = getApplicationContext().getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
						cursor.moveToFirst();
						int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
						selectedImage = cursor.getString(columnIndex);
						cursor.close();
					}
					else
					{
						if (selectedImageUri.getScheme().equalsIgnoreCase("file"))
						{
							selectedImage = selectedImageUri.getPath();
						}
						else
						{
							selectedImage = selectedImageUri.toString();
						}
					}
					full_path = selectedImage;
				}
				else
				{
					selectedImageUri = Uri.fromFile(new File(FOLDER_PHOTO_NAME + PHOTO_NAME));
					full_path = FOLDER_PHOTO_NAME + PHOTO_NAME;
				}
			}
			if (full_path != null)
			{
				String thumbfilename = "";
				String resizefilename = "";
				String[] filename = full_path.split("/");
				String[] filenamepart = filename[filename.length - 1].split("\\.");
				for (int i = 0; i < filenamepart.length; i++)
				{
					if (i < filenamepart.length - 1)
					{
						thumbfilename += filenamepart[i].toString();
						resizefilename += filenamepart[i].toString();
					}
					else
					{
						thumbfilename += "_thumb." + filenamepart[i].toString();
						resizefilename += "_resize." + filenamepart[i].toString();
					}
				}
				resizefilename = FOLDER_PHOTO_NAME + resizefilename;
				thumbfilename = FOLDER_PHOTO_NAME + thumbfilename;
				Log.e("TEST", full_path);
				Log.e("TEST", resizefilename);
				Log.e("TEST", thumbfilename);
				final PhotoUrl photoUrl = new PhotoUrl(full_path, thumbfilename, resizefilename);
				// copyFile(photoUrl.getFull(), photoUrl.getThumb());
				// copyFile(photoUrl.getFull(), photoUrl.getResize());
				AsyncTaskBitmapSizeConverter asyncTaskBitmapSizeConverter = new AsyncTaskBitmapSizeConverter(ProductPhotoActivity.this, photoUrl, new Runnable()
				{
					@Override
					public void run()
					{
						if (mItems == null)
						{
							mItems = new ArrayList<ArrayList<PhotoUrl>>();
						}
						ArrayList<PhotoUrl> item;
						if (mItems.size() > 0)
						{
							item = mItems.get(mItems.size() - 1);
							if (item.size() < 3)
							{
								item.add(photoUrl);
							}
							else
							{
								item = new ArrayList<PhotoUrl>();
								item.add(photoUrl);
								mItems.add(item);
							}
						}
						else
						{
							item = new ArrayList<PhotoUrl>();
							item.add(photoUrl);
							mItems.add(item);
						}
						mAdapter.notifyDataSetChanged();
						updateUI();
					}
				});
				asyncTaskBitmapSizeConverter.execute();
			}
		}
	}

	private void startCameraApplication(boolean isSdAvailable)
	{
		// waitDialog = ProgressedAsyncTask.showProgress(getActivity());
		Intent makePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (isSdAvailable)
		{
			Calendar calendar = Calendar.getInstance();
			PHOTO_NAME = "image" + calendar.getTimeInMillis() + ".jpg";
			Uri uri = Uri.fromFile(new File(FOLDER_PHOTO_NAME + PHOTO_NAME));
			makePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		}

		startActivityForResult(makePhotoIntent, PHOTO_REQUEST_CODE);
	}

	private void clearTempFolder()
	{
		File tempFolder = new File(getTempPath());
		if (tempFolder.canWrite())
		{
			String[] children = tempFolder.list();
			for (int i = 0; i < children.length; i++)
			{
				new File(tempFolder, children[i]).delete();
			}
		}
	}

	public void copyFile(String source, String destination)
	{
		InputStream inStream = null;
		OutputStream outStream = null;

		try
		{
			File afile = new File(source);
			File bfile = new File(destination);

			inStream = new FileInputStream(afile);
			outStream = new FileOutputStream(bfile);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0)
			{
				outStream.write(buffer, 0, length);
			}
			inStream.close();
			outStream.close();

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void updateUI()
	{
		mComplitePhotoTextView.setText(getString(R.string.complite_photo) + " (" + mAdapter.getAllCount() + ")");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{

		}
		return super.onKeyDown(keyCode, event);
	}

	private void sendData()
	{
		ArrayList<UploadItem> uploadItems = new ArrayList<UploadItem>();
		UploadItem uploadItem;
		ArrayList<PhotoUrl> photoUrls = new ArrayList<PhotoUrl>();
		photoUrls.addAll(mAdapter.getItems());

		for (int i = 0; i < photoUrls.size(); i++)
		{
			uploadItem = new UploadItem("file" + Integer.toString(i), photoUrls.get(i).getFull(), true);
			uploadItems.add(uploadItem);
		}
		uploadItem = new UploadItem("barcode", mBarCode, false);
		uploadItems.add(uploadItem);
		AbstractAsyncRequestPostWithFile asyncupload = new AbstractAsyncRequestPostWithFile(this);
		String apiString = Utility.getPref(getApplicationContext(), Constants.API);
		asyncupload.execute(this, apiString + "photo", uploadItems, true, new Runnable()
		{
			@Override
			public void run()
			{
				Intent intent = new Intent(ProductPhotoActivity.this, SendDataActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}
}
