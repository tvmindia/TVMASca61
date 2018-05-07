package com.tech.thrithvam.bakeryapp;

import android.app.Application;
import android.content.Context;
import org.acra.*;
import org.acra.annotation.*;
import org.acra.sender.HttpSender;

@ReportsCrashes(
        formUri =  "http://www.tiquesinn.com/WebServices/WebService.asmx/ErrorDetection",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        customReportContent = { ReportField.ANDROID_VERSION,
                                ReportField.APP_VERSION_CODE,
                                ReportField.AVAILABLE_MEM_SIZE,
                                ReportField.BUILD,
                                ReportField.CRASH_CONFIGURATION,
                                ReportField.LOGCAT,
                                ReportField.PACKAGE_NAME,
                                ReportField.REPORT_ID},
        mode = ReportingInteractionMode.SILENT
)
public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
