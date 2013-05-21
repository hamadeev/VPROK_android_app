package com.NGSE.vprok.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.NGSE.vprok.Data.BarCode;

public class JsonParser
{

	public static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	public static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.US);
	public static SimpleDateFormat timeWithSecFormatter = new SimpleDateFormat("mm:ss", Locale.US);
	public static SimpleDateFormat datetimeFormatter = new SimpleDateFormat("EEE, d MMM, в HH:mm", Locale.getDefault());

	public static String parseJsonError(String json)
	{
		JSONObject jsonObject = null;
		String error = null;
		try
		{
			jsonObject = new JSONObject(json);
			if (jsonObject.has("error"))
			{
				error = jsonObject.getString("error");
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return null;
		}
		return error;
	}

	public static BarCode parseJsonBarCode(String json)
	{
		JSONObject jsonObject = null;
		BarCode result = null;
		try
		{
			jsonObject = new JSONObject(json).getJSONObject("product");
			String barcode = jsonObject.getString("barcode");
			String name = jsonObject.getString("name");
			int images_count = jsonObject.getInt("images_count");
			result = new BarCode(barcode, name, images_count);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return null;
		}
		return result;
	}
}
