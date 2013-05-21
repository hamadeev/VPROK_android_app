package com.NGSE.vprok.Adapters;

import java.io.File;
import java.util.ArrayList;

import com.NGSE.vprok.R;
import com.NGSE.vprok.Data.PhotoUrl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class ProductPhotoListViewAdapter extends ArrayAdapter<ArrayList<PhotoUrl>>
{
	private LayoutInflater inflater;
	private ArrayList<ArrayList<PhotoUrl>> items;

	final private Context context;
	final private OnLayoutLoaded onLoaded;

	public ProductPhotoListViewAdapter(Context context, int textViewResourceId, ArrayList<ArrayList<PhotoUrl>> items, OnLayoutLoaded onLoaded)
	{
		super(context, textViewResourceId, items);
		this.context = context;
		this.onLoaded = onLoaded;
		this.items = items;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public class ViewHolder
	{
		public ImageView photo1;
		public ImageView photo2;
		public ImageView photo3;
	}

	public ArrayList<PhotoUrl> getItems()
	{
		ArrayList<PhotoUrl> result = new ArrayList<PhotoUrl>();
		for (int i = 0; i < items.size(); i++)
		{
			result.addAll(items.get(i));
		}
		return result;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		View view = convertView;
		ViewHolder holder = null;
		if (view == null)
		{
			view = inflater.inflate(R.layout.product_photo_listview_item, null);
			holder = new ViewHolder();
			holder.photo1 = (ImageView) view.findViewById(R.id.photo1);
			holder.photo2 = (ImageView) view.findViewById(R.id.photo2);
			holder.photo3 = (ImageView) view.findViewById(R.id.photo3);
			view.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) view.getTag();
		}

		DisplayMetrics displaymetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int width = displaymetrics.widthPixels;

		LayoutParams params = (LayoutParams) view.getLayoutParams();
		if (params == null)
		{
			params = new LayoutParams(LayoutParams.MATCH_PARENT, (width + 30) / 3);
		}
		else
		{
			params.height = (width + 30) / 3;
		}

		view.setLayoutParams(params);

		File imgFile;
		Bitmap myBitmap;
		ArrayList<PhotoUrl> item = items.get(position);

		if (item.size() > 0)
		{
			imgFile = new File(item.get(0).getThumb());
			if (imgFile.exists())
			{
				myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				holder.photo1.setImageBitmap(myBitmap);
			}
			holder.photo1.setVisibility(View.VISIBLE);
		}
		else
		{
			holder.photo1.setVisibility(View.INVISIBLE);
		}
		holder.photo1.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onLoaded.onLoad((position) * 3);
			}
		});

		if (item.size() > 1)
		{
			imgFile = new File(item.get(1).getThumb());
			if (imgFile.exists())
			{
				myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				holder.photo2.setImageBitmap(myBitmap);
			}
			holder.photo2.setVisibility(View.VISIBLE);
		}
		else
		{
			holder.photo2.setVisibility(View.INVISIBLE);
		}
		holder.photo2.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onLoaded.onLoad((position) * 3 + 1);
			}
		});

		if (item.size() > 2)
		{
			imgFile = new File(item.get(2).getThumb());
			if (imgFile.exists())
			{
				myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				holder.photo3.setImageBitmap(myBitmap);
			}
			holder.photo3.setVisibility(View.VISIBLE);
		}
		else
		{
			holder.photo3.setVisibility(View.INVISIBLE);
		}
		holder.photo3.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onLoaded.onLoad((position) * 3 + 2);
			}
		});

		return view;
	}

	public int getAllCount()
	{
		int allCount = 0;
		for (int i = 0; i < items.size(); i++)
		{
			allCount += items.get(i).size();
		}
		return allCount;
	}

	public static interface OnLayoutLoaded
	{
		public abstract void onLoad(int position);
	}
}
