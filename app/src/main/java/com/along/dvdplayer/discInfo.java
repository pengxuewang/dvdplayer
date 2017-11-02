package com.along.dvdplayer;

/**
 * Created by Administrator on 2017/3/31 0031.
 */
public class discInfo {
    /*
    *碟片信息
     */
    private static String compDate;  //编译日期
    private static String servoVer;  //伺服版本

    private static String discType;  //碟片类型

    private static String playStatus;  //播放状态   play and other

    private static String showUI;  //show UI  show  or  notshow

    public static String dvdlinein; // 通道切换标识

    static {
        discInfo.compDate = "Unknown";
        discInfo.servoVer = "Unknown";
        discInfo.discType = "Unknown";
        discInfo.playStatus = "other";
        discInfo.showUI = "notshow";
        discInfo.setDvdlinein("false");
    }

    public static String getShowUI() {
        return showUI;
    }

    public static void setShowUI(String showUI) {
        discInfo.showUI = showUI;
    }

    public static String getPlayStatus() {
        return playStatus;
    }

    public static void setPlayStatus(String playStatus) {
        discInfo.playStatus = playStatus;
    }

    public static String getCompDate() {
        return compDate;
    }

    public static void setCompDate(String compDate) {
        discInfo.compDate = compDate;
    }

    public static String getServoVer() {
        return servoVer;
    }

    public static void setServoVer(String servoVer) {
        discInfo.servoVer = servoVer;
    }

    public static String getDiscType() {
        return discType;
    }

    public static void setDiscType(String discType) {
        discInfo.discType = discType;
    }

    public static String getDvdlinein() {
        return dvdlinein;
    }

    public static void setDvdlinein(String dvdlinein) {
        discInfo.dvdlinein = dvdlinein;
    }
}
