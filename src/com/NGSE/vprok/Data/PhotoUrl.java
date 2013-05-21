package com.NGSE.vprok.Data;

public class PhotoUrl
{

	private String full;
	private String resize;
	private String thumb;

	public PhotoUrl()
	{

	}

	public PhotoUrl(String fullTemp, String thumbTemp, String resizeTemp)
	{
		full = fullTemp.trim();
		thumb = thumbTemp.trim();
		resize = resizeTemp.trim();
	}

	public String getFull()
	{
		return full;
	}

	public void setFull(String fullTemp)
	{
		full = fullTemp.trim();
	}

	public String getResize()
	{
		return resize;
	}

	public void setResize(String resizeTemp)
	{
		resize = resizeTemp.trim();
	}

	public String getThumb()
	{
		return thumb;
	}

	public void setThumb(String thumbTemp)
	{
		thumb = thumbTemp.trim();
	}
}
