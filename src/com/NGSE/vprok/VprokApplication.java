package com.NGSE.vprok;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "", mailTo = "dashkevich.dmitry@gmail.com", mode = ReportingInteractionMode.TOAST, resToastText = R.string.error_crash)
public class VprokApplication extends Application
{

	@Override
	public void onCreate()
	{
		super.onCreate();

		ACRA.init(this);
		ErrorReporter.getInstance().checkReportsOnApplicationStart();
	}
}
