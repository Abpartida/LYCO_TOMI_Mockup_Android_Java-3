package com.lyco.tomi;

import android.app.Application;

import com.lyco.tomi.mock.MockServerController;


public class TomiApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.USE_MOCK_SERVER) {
            MockServerController.initIfNeeded(this);
        }
    }
}
