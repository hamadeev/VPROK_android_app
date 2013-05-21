package com.NGSE.vprok.Utils;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

public class CodeRequestManager
{
	public static StringEntity codeAuthRequest(String email, String password)
	{
		StringEntity result = null;
		try
		{
			result = new StringEntity("email=" + email + "&password=" + password, HTTP.UTF_8);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static String codeProductRequest(String barcode)
	{
		String result = null;
		result = "?barcode=" + barcode;
		return result;
	}
}
