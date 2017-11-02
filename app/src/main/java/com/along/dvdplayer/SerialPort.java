package com.along.dvdplayer;

/**
 * Created by Administrator on 2016/12/13 0013.
 */
public class SerialPort {

    private static SerialPort instance;

    public static SerialPort getInstance(){
        return instance;
    }
    /**
     * 类加载时 加载动态库
     */
    static {
        // 单例模式
        if (instance == null){
            instance = new SerialPort();
        }
        System.loadLibrary("x806master");
    }

    /**
     *本地方法
     * @param port
     * @return
     */
    public native int openSerial(int port);

    public native int setSerial(int nSpeed, int nBits, char nEvent, int nStop);

    public native int writeData(char ch,int data);

    public native byte[] readData();

    public native int getSlaveValue();

    public native String getVersionName();
}
