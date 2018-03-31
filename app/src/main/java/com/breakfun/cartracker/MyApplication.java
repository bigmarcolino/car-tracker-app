package com.breakfun.cartracker;

import android.app.Application;

public class MyApplication extends Application {

    private static MyApplication m_instance;

    @Override
    public void onCreate() {
        super.onCreate();
        m_instance = this;
    }

    public static synchronized MyApplication getInstance() {
        return m_instance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivity_receiver_listener = listener;
    }
}
