package com.yk.rxjava.util;


import android.os.Handler;

public class CustomThread extends Thread {

    private Handler mHandler;

    public CustomThread(Handler handler){
        this.mHandler=handler;
    }

    @Override
    public synchronized void start() {
        super.start();
        mHandler.sendEmptyMessage(0);
    }


}
