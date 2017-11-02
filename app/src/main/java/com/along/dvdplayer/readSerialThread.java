package com.along.dvdplayer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by Administrator on 2017/3/31 0031.
 */
public class readSerialThread extends Thread{


    private byte[] bytes = new byte[256];//读取到的总字节数
    private byte[] bytesdate = new byte[20];//存放编译时间
    private byte[] bytesversion = new byte[12];//存放编译版本

    private SerialPort serialPort;
    private Context myContext;
    private AudioManager mAudioManager;

    public readSerialThread(SerialPort serialPort,Context mContext,AudioManager mAudioManager){
        this.serialPort = serialPort;
        this.myContext = mContext;
        this.mAudioManager = mAudioManager;
    }

    public void sendBroadcastToSystem(){
        if (discInfo.getDvdlinein().equals("false")) {
            Intent in = new Intent("com.along.dvdplayer.status");
            in.putExtra("dvdstatus", "start");
            myContext.sendBroadcast(in);
            discInfo.setDvdlinein("true");
            Log.e(Main.TAG, "+++++++++++++向系统发送广播！！++++++++++++++++");
        }
    }

    @Override
    public void run() {

        Log.e(Main.TAG, "running read uartdata thread!");
        while (true) {
                bytes = serialPort.readData();
               // System.out.println(Arrays.toString(bytes));
                for (int index = 0; index < 250; index++) { //if 256 will Array crosses！！！
                    if (bytes[index] + 256 == 0xaa) {
                        if (bytes[index + 1] == 0x55) {
                            if (bytes[index + 2] + 256 == 0xc5) {//编译时间  20个字节
                                if (bytes[index + 3] == 0x14) {
                                    for (int m = 0; m < 20; m++) {
                                        bytesdate[m] = bytes[index + 4 + m];
                                    }
                                    Log.e(Main.TAG, "编译时间！！");
                                    discInfo.setCompDate(new String(bytesdate));
                                }
                            } else if (bytes[index + 2] + 256 == 0xc6) {//伺服版本 12个字节
                                if (bytes[index + 3] == 0x0c) {
                                    for (int n = 0; n < 12; n++) {
                                        bytesversion[n] = bytes[index + 4 + n];
                                    }
                                    Log.e(Main.TAG, "伺服版本！！");
                                    discInfo.setServoVer(new String(bytesversion));
                                }
                            } else if (bytes[index + 2] + 256 == 0xd4) {//碟片类型 2个字节
                                Log.e(Main.TAG, "发送碟片类型次数");
                                if (bytes[index + 3] == 0x02) {
                                    if (bytes[index + 4] == 0x02) {//MP3 和MP4
                                        if (bytes[index + 5] == 0x02) { //MP3
                                            Log.e(Main.TAG, "读出碟片类型为MP3");
                                            //discInfo.setShowUI("show");
                                            discInfo.setDiscType("MP3");
                                            sendBroadcastToSystem();
                                        } else if (bytes[index + 5] == 0x1b) {//MP4
                                            Log.e(Main.TAG, "读出碟片类型为MP4");
                                            //discInfo.setShowUI("show");
                                            discInfo.setDiscType("MP4");
                                            sendBroadcastToSystem();
                                        }

                                    } else if (bytes[index + 4] == 0x05) {  // DVD
                                        Log.e(Main.TAG, "读出碟片类型为DVD");
                                        //discInfo.setShowUI("show");
                                        discInfo.setDiscType("DVD");
                                        sendBroadcastToSystem();
                                    } else if (bytes[index + 4] == 0x03) {  // VCD
                                        Log.e(Main.TAG, "读出碟片类型为VCD");
                                        //discInfo.setShowUI("show");
                                        discInfo.setDiscType("VCD");
                                        sendBroadcastToSystem();
                                    } else if (bytes[index + 4] == 0x01) { // CD
                                        Log.e(Main.TAG, "读出碟片类型为CD");
                                       // discInfo.setShowUI("show");
                                        discInfo.setDiscType("CD");
                                        sendBroadcastToSystem();
                                    }
                                }
                            }else if (bytes[index + 2] + 256 == 0xd1){// play status
                                if (bytes[index + 3] == 0x02){
                                    if (bytes[index + 4] == 0x02){
                                        if (bytes[index + 5] == 0x00){
                                            //discInfo.setPlayStatus("play");
                                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC,false);
                                        }
                                    }else if (bytes[index + 4] == 0x03 || bytes[index + 4] == 0x04
                                            || bytes[index + 4] == 0x05 || bytes[index + 4] == 0x0d ){
                                        //discInfo.setPlayStatus("other");
                                        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC,true);
                                    }
                                }
                            }else if (bytes[index + 2] + 256 == 0xd2){//play time
                                //只有在状态发生改变的时候才发送播放状态 所以第一次需要通过播放时间来释放mute
                                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC,false);
                            }else if (bytes[index + 2] + 256 == 0xd9){// play volume
                                if (bytes[index + 3] == 0x02){
                                    if (bytes[index + 4] == 0x00){
                                        if (bytes[index + 5] == 0x00){ // volume  0  mute
                                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC,true);
                                        }
                                    }else if (bytes[index + 4] == 0x01){
                                        if (bytes[index + 5] == 0x01){ // volume  1  unmute
                                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC,false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
           // try {
           //     Thread.sleep(6000);// Every three seconds readSerial!!! 3s good
           // } catch (InterruptedException e) {
            //    e.printStackTrace();
           // }
        }
    }
}
