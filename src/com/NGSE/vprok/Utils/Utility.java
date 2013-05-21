package com.NGSE.vprok.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Utility
{
	public static String generateString(InputStream stream) throws IOException
	{
		BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		String cur;
		while ((cur = buffer.readLine()) != null)
		{
			sb.append(cur + "\n");
		}
		buffer.close();
		return sb.toString();
	}

	public static String getPref(Context context, String key)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String result = sp.getString(key, null);
		return result;
	}

	public static void savePref(Context context, String key, String value)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}
}
