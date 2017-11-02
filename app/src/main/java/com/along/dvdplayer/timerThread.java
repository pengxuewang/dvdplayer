package com.along.dvdplayer;

import android.os.Handler;
import android.os.Message;

/**
 * Created by Administrator on 2017/1/6 0006.
 */
public class timerThread extends Thread {

    private Handler handler = null;

    public timerThread(Handler handler){
        this.handler = handler;
    }
    @Override
    public synchronized void run() {

        boolean run_flag = true;
        try {
            Thread.sleep(8000);   // 无操作8s自动全屏
        } catch (InterruptedException e) {
            run_flag = false;
        }
        if (run_flag == true) {
            Message msg = new Message();
            msg.what = 0;
            handler.sendMessage(msg);
        }
        super.run();
    }
}
