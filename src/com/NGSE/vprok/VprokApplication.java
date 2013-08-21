package com.NGSE.vprok;

import android.content.Context;
import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "", mailTo = "dashkevich.dmitry@gmail.com", mode = ReportingInteractionMode.TOAST, resToastText = R.string.error_crash)
public class VprokApplication extends Application
{

    private static Context context;

	@Override
	public void onCreate()
	{
		super.onCreate();

        VprokApplication.context = getApplicationContext();

		ACRA.init(this);
		ErrorReporter.getInstance().checkReportsOnApplicationStart();
	}

    public static Context getAppContext() {
        return VprokApplication.context;
    }
}
