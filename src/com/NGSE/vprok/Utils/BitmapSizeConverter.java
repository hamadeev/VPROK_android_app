package com.NGSE.vprok.Utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.util.Log;

public class BitmapSizeConverter
{

	static BitmapFactory.Options options;

	public static boolean createCropBitmapToFile(String sourceFilePath, String distFilePath, int startPosX, int startPosY, int reqWidth, int reqHeight)
	{
		final Bitmap bm = createCropBitmap(sourceFilePath, startPosX, startPosY, reqWidth, reqHeight);
		if (bm == null)
			return false;
		final FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(distFilePath);
			bm.compress(CompressFormat.PNG, 100, fos);
			fos.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			bm.recycle();
		}
		return true;
	}

	// returned bitmap need to be Bitmap.recycle() manually
	public static Bitmap createCropBitmap(String filePath, int startPosX, int startPosY, int reqWidth, int reqHeight)
	{
		if (android.os.Build.VERSION.SDK_INT < 10)
			return createUnsafeCropImage(filePath, startPosX, startPosX, reqWidth, reqHeight);
		else
			return createSafeCropImage(filePath, startPosX, startPosY, reqWidth, reqHeight);
	}

	public static boolean createCropThumbnailBitmapToFile(String sourceFilePath, String distFilePath, int startPosX, int startPosY, int reqWidth, int reqHeight, int finalWidth, int finalHeight)
	{
		final Bitmap bm = createCropThumbnailBitmap(sourceFilePath, startPosX, startPosY, reqWidth, reqHeight, finalWidth, finalHeight);
		if (bm == null)
			return false;
		final FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(distFilePath);
			bm.compress(CompressFormat.PNG, 100, fos);
			fos.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			bm.recycle();
		}
		return true;
	}

	// returned bitmap need to be Bitmap.recycle() manually
	public static Bitmap createCropThumbnailBitmap(String filePath, int startPosX, int startPosY, int reqWidth, int reqHeight, int finalWidth, int finalHeight)
	{
		if (android.os.Build.VERSION.SDK_INT < 10)
			return createUnsafeCropThumbnailImage(filePath, startPosX, startPosX, reqWidth, reqHeight, finalWidth, finalHeight);
		else
			return createSafeCropThumbnailImage(filePath, startPosX, startPosY, reqWidth, reqHeight, finalWidth, finalHeight);
	}

	// returned bitmap need to be Bitmap.recycle() manually
	public static Bitmap createThumbnail(String filePath, int reqWidth, int reqHeight)
	{
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inJustDecodeBounds = false;
		final Bitmap bm = BitmapFactory.decodeFile(filePath, options);
		final Bitmap resultBitmap = Bitmap.createScaledBitmap(bm, reqWidth, reqHeight, true);
		if (!bm.equals(resultBitmap))
			bm.recycle();
		return resultBitmap;
	}

	public static boolean createThumbnailToFile(String sourceFilePath, String distFilePath, int reqWidth, int reqHeight)
	{
		Log.e("TEST", "sourceFilePath: " + sourceFilePath);
		Log.e("TEST", "distFilePath: " + distFilePath);
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inJustDecodeBounds = false;
		final Bitmap bm = BitmapFactory.decodeFile(sourceFilePath, options);
		final Bitmap resultBitmap = Bitmap.createScaledBitmap(bm, reqWidth, reqHeight, true);
		if (!bm.equals(resultBitmap))
			bm.recycle();
		final FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(distFilePath);
			resultBitmap.compress(CompressFormat.PNG, 100, fos);
			fos.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			resultBitmap.recycle();
		}
		return true;
	}

	@TargetApi(10)
	private static Bitmap createSafeCropImage(String filePath, int startPosX, int startPosY, int reqWidth, int reqHeight)
	{
		final FileInputStream mapInput;
		BitmapRegionDecoder decoder = null;
		try
		{
			mapInput = new FileInputStream(filePath);
			decoder = BitmapRegionDecoder.newInstance(mapInput, false);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		options.inDither = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inJustDecodeBounds = false;
		return decoder.decodeRegion(new Rect(startPosX, startPosY, startPosX + reqWidth, startPosY + reqHeight), options);
	}

	private static Bitmap createUnsafeCropImage(String filePath, int startPosX, int startPosY, int reqWidth, int reqHeight)
	{
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		options.inDither = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inJustDecodeBounds = false;
		final Bitmap bm = BitmapFactory.decodeFile(filePath, options);
		final Bitmap resultBitmap = Bitmap.createBitmap(bm, startPosX, startPosY, reqWidth, reqHeight);
		bm.recycle();
		return resultBitmap;
	}

	@TargetApi(10)
	private static Bitmap createSafeCropThumbnailImage(String filePath, int startPosX, int startPosY, int reqWidth, int reqHeight, int finalWidth, int finalHeight)
	{
		final BufferedInputStream mapInput;
		BitmapRegionDecoder decoder = null;
		try
		{
			mapInput = new BufferedInputStream(new FileInputStream(filePath));
			decoder = BitmapRegionDecoder.newInstance(mapInput, false);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inJustDecodeBounds = false;
		final Bitmap bm = decoder.decodeRegion(new Rect(startPosX, startPosY, startPosX + reqWidth, startPosY + reqHeight), options);
		final Bitmap resultBitmap = Bitmap.createScaledBitmap(bm, finalWidth, finalHeight, false);
		if (!bm.equals(resultBitmap))
			bm.recycle();
		return resultBitmap;
	}

	private static Bitmap createUnsafeCropThumbnailImage(String filePath, int startPosX, int startPosY, int reqWidth, int reqHeight, int finalWidth, int finalHeight)
	{
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		options.inDither = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inJustDecodeBounds = false;
		final Bitmap bm = BitmapFactory.decodeFile(filePath, options);
		final Bitmap cropBitmap = Bitmap.createBitmap(bm, startPosX, startPosY, reqWidth, reqHeight);
		bm.recycle();
		final Bitmap resultBitmap = Bitmap.createScaledBitmap(cropBitmap, finalWidth, finalHeight, true);
		if (!cropBitmap.equals(resultBitmap))
			cropBitmap.recycle();
		return resultBitmap;
	}

	@SuppressWarnings("unused")
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		return calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);
	}

	private static int calculateInSampleSize(int reqWidth, int reqHeight, int finalWidth, int finalHeight)
	{
		int inSampleSize = 1;

		if (reqHeight > finalHeight || reqWidth > finalWidth)
		{
			if (reqWidth > reqHeight)
			{
				inSampleSize = Math.round((float) reqHeight / (float) finalHeight);
			}
			else
			{
				inSampleSize = Math.round((float) reqWidth / (float) finalWidth);
			}
		}
		return inSampleSize;
	}

	public static boolean createCropeAspectRatio(String sourceFilePath, String distFilePath, String aspectRatio, int finalHeight, int finalWidth)
	{

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(sourceFilePath, options);

		int sourceHeight = options.outHeight;
		int sourceWidth = options.outWidth;

		double kSource = sourceHeight / (float) sourceWidth;
		double kDest = finalHeight / (float) finalWidth;

		int startPosX, startPosY, reqWidth, reqHeight;
		if (kSource < kDest)
		{
			// ratio Height/width of input image less than same ratio of output
			// image
			startPosY = 0;
			reqWidth = (int) (sourceHeight / kDest);
			reqHeight = sourceHeight;
			if (aspectRatio.indexOf("xMin") > -1)
			{
				startPosX = 0;
			}
			else
				if (aspectRatio.indexOf("xMax") > -1)
				{
					startPosX = sourceWidth - reqWidth;
				}
				else
				/* if (aspectRatio.indexOf("xMid") > -1) */{
					startPosX = (int) ((sourceWidth - reqWidth) / 2.0);
				}
		}
		else
		{
			// otherwise
			startPosX = 0;
			reqWidth = sourceWidth;
			reqHeight = (int) (sourceWidth * kDest);
			if (aspectRatio.indexOf("YMin") > -1)
			{
				startPosY = 0;
			}
			else
				if (aspectRatio.indexOf("YMax") > -1)
				{
					startPosY = sourceHeight - reqHeight;
				}
				else
				/* if (aspectRatio.indexOf("YMid") > -1) */{
					startPosY = (int) ((sourceHeight - reqHeight) / 2.0);
				}
		}
		boolean result = createCropThumbnailBitmapToFile(sourceFilePath, distFilePath, startPosX, startPosY, reqWidth, reqHeight, finalWidth, finalHeight);

		options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(distFilePath, options);
		Log.e("TESTING", "resultWidth=" + options.outWidth + ", resultHeight=" + options.outHeight);

		return result;
	}
}
