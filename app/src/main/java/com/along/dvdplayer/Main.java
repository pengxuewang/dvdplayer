package com.along.dvdplayer;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.along.dvdplayer.R;

import java.util.Arrays;

public class Main extends Activity {

    private ImageButton bt_exit;
    private ImageButton bt_prev;
    private ImageButton bt_fr;
    private ImageButton bt_play;
    private ImageButton bt_ff;

    private ImageButton bt_next;
    private ImageButton bt_stop;
    private ImageButton bt_repeat;
    private ImageButton bt_ab_repeat;
    private ImageButton bt_fangxiang;

    private ImageButton bt_menu;
    private ImageButton bt_title;
    private ImageButton bt_subtitle;
    private ImageButton bt_audio;
    private ImageButton bt_angle;
    private ImageButton bt_selSong;
    private ImageButton bt_info;

    private ImageButton bt_1;
    private ImageButton bt_3;
    private ImageButton bt_7;
    private ImageButton bt_9;
    private ImageButton bt_up;
    private ImageButton bt_down;
    private ImageButton bt_left;
    private ImageButton bt_right;
    private ImageButton bt_ok_two;

    private ImageButton bt_one;
    private ImageButton bt_two;
    private ImageButton bt_thr;
    private ImageButton bt_fou;
    private ImageButton bt_fiv;
    private ImageButton bt_six;
    private ImageButton bt_sev;
    private ImageButton bt_eig;
    private ImageButton bt_nin;
    private ImageButton bt_del;
    private ImageButton bt_zer;
    private ImageButton bt_ok;

    private Context  mContext;

    private TextView tvDiscInfo;

    private Button bt_textview0;
    private Button bt_textview;

    private boolean ret_grid = true;   //Grid
    private boolean ret_bottom = true; //bottom
    private boolean ret_selSong = true; // sellect song
    private boolean ret_disinfo = true; // disc infomation
    private int ret_openSetSerial; // Open set the serial return value

    private LinearLayout nav_bar;  //底部导航栏导航条
    private LinearLayout digitKB;  // 选曲功能数字键盘

    public Handler myHandler = null;
    public Thread timerThread = null;

    public char[] title = new char[]{'T','I','T','L','E',':','0','0','0'};
    public char[] chapter = new char[]{'C','H','A','P','T','E','R',':','0','0','0'};
    public char[] track = new char[]{'T','R','A','C','K',':','0','0','0'};

    private int TtorCh = 0; // 1代表选中title  2 代表选中chapter

    HeadsetPlugReceiver headsetPlugReceiver;

    public static final String TAG = "Along DvdPlayer";

    public AnimationDrawable animationDrawable;
    ImageView animationView;

    private TextureView textureView;
     //摄像头ID，一般0是后视，1是前视
    private String cameraId;
    //定义代表摄像头的成员变量，代表系统摄像头，该类的功能类似早期的Camera类。
    protected CameraDevice cameraDevice;
    //定义CameraCaptureSession成员变量，是一个拍摄绘话的类，用来从摄像头拍摄图像或是重新拍摄图像,这是一个重要的API.
    protected CameraCaptureSession cameraCaptureSessions;
    //当程序调用setRepeatingRequest()方法进行预览时，或调用capture()进行拍照时，都需要传入CaptureRequest参数时
    //captureRequest代表一次捕获请求，用于描述捕获图片的各种参数设置。比如对焦模式，曝光模式...等，程序对照片所做的各种控制，都通过CaptureRequest参数来进行设置
    //CaptureRequest.Builder 负责生成captureRequest对象
    protected CaptureRequest.Builder captureRequestBuilder;
    //预览尺寸
    private Size imageDimension;

    //请求码常量，可以自定义
    private static final int REQUEST_CAMERA_PERMISSION = 300;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    public AudioManager mAudioManager;

    public SerialPort serialport;

    private int currentVolume;
    private int MaxVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        MaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 获取音量焦点
        mAudioManager.requestAudioFocus(null,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,MaxVolume,0);
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC,true);

        //隐藏虚拟按键
        hiddenVirtualkeys();

        //注册耳机插入广播
        registerHeadsetPlugReceiver();

        serialport = SerialPort.getInstance();
        if ((ret_openSetSerial = serialport.openSerial(3)) != 0) {
            Log.e(TAG, "Open Serial Port ttyS3 fiald！！！");
        }
        if ((ret_openSetSerial = serialport.setSerial(38400, 8, 'N', 1)) != 0) {
            Log.e(TAG, "Set Serial Port ttyS3 fiald！！！");
        }

        timerThread = new timerThread(myHandler);
        //启动读取串口数据线程
        new readSerialThread(serialport,mContext,mAudioManager).start();
        // 20S timer
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(16000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                discInfo.setShowUI("show");
//add animation start
                //animationDrawable.stop();
//add animation end
            }
        }).start();

        setContentView(R.layout.main);

        InitUI();


        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0:
                        setGridInVisible();
                        setButtomInviseble();
                        setKeyboardInvisible();
                        setDiscInfoInvisible();
                        cleanThrOrOneNum(3, "track");
                        cleanThrOrOneNum(3, "title");
                        cleanThrOrOneNum(3, "chapter");
                        break;
                }
            }
        };

        textureView = (TextureView) findViewById(R.id.texture);
        //隐藏底部虚拟按键
        //textureView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // assert textureView != null;
        //设置监听
        textureView.setSurfaceTextureListener(textureListener);

    }

      //定义了一个独立的监听类
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            //Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private void openCamera() {
        //实例化摄像头
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        Log.e(TAG, "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        Log.e(TAG, "is camera open");
        try {
         //指定要打开的摄像头
            cameraId = manager.getCameraIdList()[0];
         //获取打开摄像头的属性
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
         //The available stream configurations that this camera device supports; also includes the minimum frame durations and the stall durations for each format/size combination.
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
          //  assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
          //Add permission for camera and let user grant the permission
           //权限检查
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.DVD) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Main.this, new String[]{Manifest.permission.DVD}, REQUEST_CAMERA_PERMISSION);
                return;
            }
           //打开摄像头，第一个参数代表要打开的摄像头，第二个参数用于监测打开摄像头的当前状态，第三个参数表示执行callback的Handler,
           //如果程序希望在当前线程中执行callback，像下面的设置为null即可。
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera 0");
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
         //摄像头打开激发该方法
        public void onOpened(CameraDevice camera) {

            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            //开始预览
            createCameraPreview();
        }
        //摄像头断开连接时的方法
        @Override
        public void onDisconnected(CameraDevice camera) {
            closeCamera();
        }
        //打开摄像头出现错误时激发方法
        @Override
        public void onError(CameraDevice camera, int error) {
            closeCamera();
        }
    };

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            //assert texture != null;
            //设置默认的预览大小
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            //请求预览
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            captureRequestBuilder.addTarget(surface);
            //创建cameraCaptureSession,第一个参数是图片集合，封装了所有图片surface,第二个参数用来监听这处创建过程
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(Main.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
            return;
        }
        //设置模式为自动
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {

            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }
    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        //隐藏虚拟按键
        hiddenVirtualkeys();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            Log.e(TAG, "textureViewAvailable");
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
        super.onResume();
    }
    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        closeCamera();
        Log.e(TAG, "is camera close");
        stopBackgroundThread();
        super.onPause();
    }
//add animation start
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        animationView = (ImageView)findViewById(R.id.mainImageView);
//        animationView.setBackgroundResource(R.drawable.animation);
//        animationDrawable = (AnimationDrawable) animationView.getBackground();
//        animationDrawable.start();
//    }
//add animation end
    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        this.unregisterReceiver(headsetPlugReceiver);
        super.onDestroy();
    }
    // 音量加减 回调函数
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                serialport.writeData('I',99);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                serialport.writeData('R',99);
                break;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                break;
        }
        //return super.onKeyDown(keyCode, event);
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {

        if (discInfo.getShowUI().equals("show")) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.e(TAG, "onTouchEvent");
                fullScreen();
                setDiscInfoInvisible();
                setKeyboardInvisible();
                setGridInVisible();
                cleanThrOrOneNum(3, "track");
                cleanThrOrOneNum(3, "title");
                cleanThrOrOneNum(3, "chapter");
                if (ret_bottom == true) {
                    setButtomVisible();
                } else if (ret_bottom == false) {
                    setButtomInviseble();
                }
                //隐藏 虚拟按键
                hiddenVirtualkeys();
            }
        }
        // TODO Auto-generated method stub
        return super.onTouchEvent(event);
    }

    // 优化时考虑做成单例模式！！！！ instance
    void fullScreen(){
        timerThread.interrupt();
        timerThread = new timerThread(myHandler);
        timerThread.start();
    }

    void InitUI(){

        bt_exit = (ImageButton) findViewById(R.id.bt_exit);
        bt_prev = (ImageButton) findViewById(R.id.bt_prev);
        bt_play = (ImageButton) findViewById(R.id.bt_play);
        bt_fr = (ImageButton) findViewById(R.id.bt_fr);
        bt_ff = (ImageButton) findViewById(R.id.bt_ff);

        bt_next = (ImageButton) findViewById(R.id.bt_next);
        bt_stop = (ImageButton) findViewById(R.id.bt_stop);
        bt_repeat = (ImageButton) findViewById(R.id.bt_repeat);
        bt_ab_repeat = (ImageButton) findViewById(R.id.bt_ab_repeat);
        bt_fangxiang = (ImageButton) findViewById(R.id.bt_fangxiang);

        bt_menu = (ImageButton) findViewById(R.id.bt_menu);
        bt_title = (ImageButton) findViewById(R.id.bt_title);
        bt_subtitle = (ImageButton) findViewById(R.id.bt_subtitle);
        bt_audio = (ImageButton) findViewById(R.id.bt_audio);
        bt_angle = (ImageButton) findViewById(R.id.bt_angle);
        bt_selSong = (ImageButton) findViewById(R.id.bt_selSong);
        bt_info = (ImageButton) findViewById(R.id.bt_info);

        bt_1 = (ImageButton) findViewById(R.id.button9);
        bt_3 = (ImageButton) findViewById(R.id.button7);
        bt_7 = (ImageButton) findViewById(R.id.button3);
        bt_9 = (ImageButton) findViewById(R.id.button);
        bt_down = (ImageButton) findViewById(R.id.button2);
        bt_up = (ImageButton) findViewById(R.id.button8);
        bt_left = (ImageButton) findViewById(R.id.button6);
        bt_right = (ImageButton) findViewById(R.id.button4);
        bt_ok_two = (ImageButton) findViewById(R.id.button5);

        bt_one = (ImageButton) findViewById(R.id.bt_one);
        bt_two = (ImageButton) findViewById(R.id.bt_two);
        bt_thr = (ImageButton) findViewById(R.id.bt_thr);
        bt_fou = (ImageButton) findViewById(R.id.bt_fou);
        bt_fiv = (ImageButton) findViewById(R.id.bt_fiv);
        bt_six = (ImageButton) findViewById(R.id.bt_six);
        bt_sev = (ImageButton) findViewById(R.id.bt_sev);
        bt_eig = (ImageButton) findViewById(R.id.bt_eig);
        bt_nin = (ImageButton) findViewById(R.id.bt_nin);
        bt_del = (ImageButton) findViewById(R.id.bt_del);
        bt_zer = (ImageButton) findViewById(R.id.bt_zer);
        bt_ok = (ImageButton) findViewById(R.id.bt_ok);

        nav_bar = (LinearLayout)findViewById(R.id.nav_bar);
        digitKB = (LinearLayout)findViewById(R.id.digitKB);

        bt_textview0 = (Button) findViewById(R.id.bt_textview0);
        bt_textview = (Button) findViewById(R.id.bt_textview);

        tvDiscInfo = (TextView) findViewById(R.id.tvDiscInfo);

        bt_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serialport.writeData('T',99);
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC,false);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume,0);
                //finish();
                System.exit(0);
                //android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        bt_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('P',99);
            }
        });
        bt_fr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('F',99);
            }
        });
        bt_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('S',99);
            }
        });
        bt_ff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('X',99);
            }
        });

        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('N',99);
            }
        });
        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('T',99);
            }
        });
        bt_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('E',99);
            }
        });
        bt_ab_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('B',99);
            }
        });
        bt_fangxiang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                if (ret_grid == true) {
                    setGridVisible();
                }else if(ret_grid == false){
                   setGridInVisible();
                }
            }
        });

        bt_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('M',99);
            }
        });
        bt_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('G',99);
            }
        });
        bt_subtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('A', 99);
            }
        });
        bt_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('V',99);
            }
        });
        bt_angle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('C',99);
            }
        });
        bt_selSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                // 先判断是否为空 如果为空 会报引用空对象！！！
                if (discInfo.getDiscType() != null) {
                    // 判断碟片类型 disc type
                    if (discInfo.getDiscType().equals("DVD")) {
                        bt_textview0.setText(String.valueOf(title));
                        bt_textview.setText(String.valueOf(chapter));
                        if (ret_selSong == true) {
                            setDiscInfoInvisible();
                            setKeyboardVisible();
                            bt_textview0.setVisibility(View.VISIBLE);
                            bt_textview0.setBackgroundResource(R.drawable.textview0);
                            bt_textview.setBackgroundResource(R.drawable.textview0);
                            TtorCh = 0;

                        } else if (ret_selSong == false) {
                            setKeyboardInvisible();
                            cleanThrOrOneNum(3, "title");
                            cleanThrOrOneNum(3, "chapter");
                        }
                    } else if (discInfo.getDiscType().equals("VCD") || discInfo.getDiscType().equals("CD")
                            || discInfo.getDiscType().equals("MP3") || discInfo.getDiscType().equals("MP4")) {
                        bt_textview.setText(String.valueOf(track));
                        bt_textview0.setVisibility(View.INVISIBLE);
                        if (ret_selSong == true) {
                            setKeyboardVisible();
                            setDiscInfoInvisible();
                            bt_textview.setBackgroundResource(R.drawable.textview);
                        } else if (ret_selSong == false) {
                            setKeyboardInvisible();
                            cleanThrOrOneNum(3, "track");
                        }
                    }
                }
            }
        });
        bt_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                if (ret_disinfo == true) {
                    setDiscInfoVisible();
                    setKeyboardInvisible();
                }else if(ret_disinfo == false){
                    setDiscInfoInvisible();
                }
                tvDiscInfo.setText("                        Disc information"+'\n'+"Compile date:  "+
                        discInfo.getCompDate()+'\n'+"Servo version:  "+discInfo.getServoVer()+'\n'+"Disc type:  "+discInfo.getDiscType());
            }
        });

        bt_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('U',99);
            }
        });
        bt_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('D',99);
            }
        });
        bt_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('H',99);
            }
        });
        bt_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('L',99);
            }
        });
        bt_ok_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                serialport.writeData('O',99);
            }
        });
        bt_textview0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                if( discInfo.getDiscType() != null && discInfo.getDiscType().equals("DVD")){
                    bt_textview0.setBackgroundResource(R.drawable.textview);
                    bt_textview.setBackgroundResource(R.drawable.textview0);
                    serialport.writeData('J',99);
                    cleanThrOrOneNum(3, "chapter");
                }
                TtorCh = 1;
            }
        });
        bt_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                if( discInfo.getDiscType() != null && discInfo.getDiscType().equals("DVD")){
                    bt_textview.setBackgroundResource(R.drawable.textview);
                    bt_textview0.setBackgroundResource(R.drawable.textview0);
                    serialport.writeData('K',99);
                    cleanThrOrOneNum(3, "title");
                }
                TtorCh = 2;
            }
        });

        bt_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                sendDigit('1');
            }
        });
        bt_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                sendDigit('2');
            }
        });
        bt_thr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                sendDigit('3');
            }
        });
        bt_fou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                sendDigit('4');
            }
        });
        bt_fiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                sendDigit('5');
            }
        });
        bt_six.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                sendDigit('6');
            }
        });
        bt_sev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                sendDigit('7');
            }
        });
        bt_eig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                sendDigit('8');
            }
        });
        bt_nin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                sendDigit('9');
            }
        });
        bt_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                cleanThrOrOneNum(3, "track");
                cleanThrOrOneNum(3, "title");
                cleanThrOrOneNum(3, "chapter");
                if (discInfo.getDiscType().equals("DVD")) {
                    if (TtorCh == 1 || TtorCh == 2) {
                        serialport.writeData('Q', 11);
                    }
                }else {
                    serialport.writeData('Q', 11);
                    bt_textview.setText(String.valueOf(track));
                }
            }
        });
        bt_zer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen();
                sendDigit('0');
            }
        });
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fullScreen();
                if (discInfo.getDiscType().equals("DVD")) {
                    if (TtorCh == 1 || TtorCh == 2) {
                        serialport.writeData('Q', 10);
                    }
                }else {
                    serialport.writeData('Q', 10);
                }
                cleanThrOrOneNum(3, "track");
                cleanThrOrOneNum(3, "title");
                cleanThrOrOneNum(3, "chapter");
                setKeyboardInvisible();
            }
        });
    }
    void InitState(){
        new Thread(new Runnable() {
            @Override
            public void run() {


            }
        }).start();
    }
    //  注册广播接收器 监听耳机插入事件
    private void registerHeadsetPlugReceiver(){
        headsetPlugReceiver  = new HeadsetPlugReceiver ();
        IntentFilter  filter = new IntentFilter();
        filter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(headsetPlugReceiver, filter);
    }
    public class HeadsetPlugReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            if(intent.hasExtra("state")){
                if(intent.getIntExtra("state", 0)==0){ // 耳机拔出
                    serialport.writeData('S',99);
                }
                else if(intent.getIntExtra("state", 0)==1){ // 耳机插入
                    serialport.writeData('S',99);
                }
            }
        }
    }
    public void updateChars(char song){

        if (discInfo.getDiscType() != null && discInfo.getDiscType().equals("DVD")){
            if (TtorCh == 1){
                title[6] = title[7];
                title[7] = title[8];
                title[8] = song;
                bt_textview0.setText(String.valueOf(title));

            }else if (TtorCh == 2){
                chapter[8] = chapter[9];
                chapter[9] = chapter[10];
                chapter[10] = song;
                bt_textview.setText(String.valueOf(chapter));
            }
        }else if (discInfo.getDiscType() != null && (discInfo.getDiscType().equals("MP3")||discInfo.getDiscType().equals("MP4")||
                discInfo.getDiscType().equals("CD")||discInfo.getDiscType().equals("VCD"))){
            track[6] = track[7];
            track[7] = track[8];
            track[8] = song;
            bt_textview.setText(String.valueOf(track));
        }
    }
    public void cleanThrOrOneNum(int a, String str){
        if (a == 3) {
            if (str.equals("track")) {
                track[6] = '0';
                track[7] = '0';
                track[8] = '0';
                bt_textview.setText(String.valueOf(track));
            } else if (str.equals("title")) {
                title[6] = '0';
                title[7] = '0';
                title[8] = '0';
                bt_textview0.setText(String.valueOf(title));
            } else if (str.equals("chapter")) {
                chapter[8] = '0';
                chapter[9] = '0';
                chapter[10] = '0';
                bt_textview.setText(String.valueOf(chapter));
            }
        }else if (a == 1) {
            if (str.equals("track")) {
                track[8] = track[7];
                track[7] = track[6];
                track[6] = '0';
                bt_textview.setText(String.valueOf(track));
            } else if (str.equals("title")) {
                title[8] = title[7];
                title[7] = title[6];
                title[6] = '0';
                bt_textview0.setText(String.valueOf(title));
            } else if (str.equals("chapter")) {
                chapter[10] = chapter[9];
                chapter[9] = chapter[8];
                chapter[8] = '0';
                bt_textview.setText(String.valueOf(chapter));
            }
        }
    }
    public void sendDigit(char digit){
        if ((discInfo.getDiscType().equals("DVD") && (TtorCh == 1 && title[7] == '0') || (TtorCh == 2 && chapter[9] == '0'))
                ||(discInfo.getDiscType().equals("VCD") && track[7] == '0')
                ||(discInfo.getDiscType().equals("CD") && track[7] == '0')
                ||(discInfo.getDiscType().equals("MP3") && track[6] == '0')
                ||(discInfo.getDiscType().equals("MP4") && track[6] == '0')) { //DVD 的title选曲
            updateChars(digit);
            String sendDigit=String.valueOf(digit);
            if (discInfo.getDiscType().equals("DVD")) {
                if (TtorCh == 1 || TtorCh == 2) {
                    serialport.writeData('Q', Integer.parseInt(sendDigit));
                }
            } else {
                serialport.writeData('Q', Integer.parseInt(sendDigit));
            }
        }
    }
    public void setKeyboardInvisible(){
        digitKB.setVisibility(View.INVISIBLE);
        ret_selSong = true;
    }
    public void setKeyboardVisible(){
        digitKB.setVisibility(View.VISIBLE);
        ret_selSong = false;
    }
    public void setDiscInfoVisible(){
        tvDiscInfo.setVisibility(View.VISIBLE);
        ret_disinfo = false;
    }
    public void setDiscInfoInvisible(){
        tvDiscInfo.setVisibility(View.INVISIBLE);
        ret_disinfo = true;
    }
    public void setGridVisible(){
        bt_1.setVisibility(View.VISIBLE);
        bt_3.setVisibility(View.VISIBLE);
        bt_7.setVisibility(View.VISIBLE);
        bt_9.setVisibility(View.VISIBLE);
        bt_up.setVisibility(View.VISIBLE);
        bt_down.setVisibility(View.VISIBLE);
        bt_right.setVisibility(View.VISIBLE);
        bt_left.setVisibility(View.VISIBLE);
        bt_ok_two.setVisibility(View.VISIBLE);
        ret_grid =false;
    }
    public void setGridInVisible(){
        bt_1.setVisibility(View.INVISIBLE);
        bt_3.setVisibility(View.INVISIBLE);
        bt_7.setVisibility(View.INVISIBLE);
        bt_9.setVisibility(View.INVISIBLE);
        bt_up.setVisibility(View.INVISIBLE);
        bt_down.setVisibility(View.INVISIBLE);
        bt_right.setVisibility(View.INVISIBLE);
        bt_left.setVisibility(View.INVISIBLE);
        bt_ok_two.setVisibility(View.INVISIBLE);
        ret_grid = true;
    }
    public void setButtomVisible(){
        nav_bar.setVisibility(View.VISIBLE);
        ret_bottom = false;
    }
    public void setButtomInviseble(){
        nav_bar.setVisibility(View.INVISIBLE);
        ret_bottom = true;
    }
    private void hiddenVirtualkeys(){
        // 隐藏虚拟按键 和状态栏
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
}

