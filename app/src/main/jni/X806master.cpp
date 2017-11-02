#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <errno.h>
#include <unistd.h>
#include <stdlib.h>
#include <android/log.h>
#include <termios.h>
#include <string.h>

#define TAG "x806AndroidSerialService"
#define ALOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define SERIAL_ERR -1
#define SERIAL_TRUE 0

void TxControlCode(char send[]);
void digitTxControlCode(char digitSend[]);

jint fd;//serial file descript
jint slaveValue ;

jint setX806Serial(JNIEnv *env, jobject thiz, jint nSpeed, jint nBits, jchar nEvent, jint nStop)
{
	 struct termios newtio,oldtio;
        if  ( tcgetattr( fd,&oldtio)  !=  0)
        {
            perror("SetupSerial 1");
            return SERIAL_ERR;
        }
        bzero( &newtio, sizeof( newtio ) );
        newtio.c_cflag  |=  CLOCAL | CREAD;
        newtio.c_cflag &= ~CSIZE;

        switch( nBits )
        {
        case 7:
            newtio.c_cflag |= CS7;
            break;
        case 8:
            newtio.c_cflag |= CS8;
            break;
        }

        switch( nEvent )
        {
        case 'O':                     //奇校验
            newtio.c_cflag |= PARENB;
            newtio.c_cflag |= PARODD;
            newtio.c_iflag |= (INPCK | ISTRIP);
            break;
        case 'E':                     //偶校验
            newtio.c_iflag |= (INPCK | ISTRIP);
            newtio.c_cflag |= PARENB;
            newtio.c_cflag &= ~PARODD;
            break;
        case 'N':                    //无
            newtio.c_cflag &= ~PARENB;
            break;
        }

    switch( nSpeed )
        {
        case 2400:
            cfsetispeed(&newtio, B2400);
            cfsetospeed(&newtio, B2400);
            break;
        case 4800:
            cfsetispeed(&newtio, B4800);
            cfsetospeed(&newtio, B4800);
            break;
        case 9600:
            cfsetispeed(&newtio, B9600);
            cfsetospeed(&newtio, B9600);
            break;
        case 115200:
            cfsetispeed(&newtio, B115200);
            cfsetospeed(&newtio, B115200);
            break;
        case 38400:
            cfsetispeed(&newtio, B38400);
            cfsetospeed(&newtio, B38400);
            break;
        default:
            cfsetispeed(&newtio, B9600);
            cfsetospeed(&newtio, B9600);
            break;
        }
        if( nStop == 1 )
        {
            newtio.c_cflag &=  ~CSTOPB;
        }
        else if ( nStop == 2 )
        {
            newtio.c_cflag |=  CSTOPB;
        }
        newtio.c_cc[VTIME]  = 0;
        newtio.c_cc[VMIN] = 0;
        tcflush(fd,TCIFLUSH);
        if((tcsetattr(fd,TCSANOW,&newtio))!=0)
        {
        	ALOGD("com set error");
            return SERIAL_ERR;
        }
        printf("set done!\n");
        ALOGD("set serial successed！");
        return SERIAL_TRUE;
}

jint openX806Serial(JNIEnv *env, jobject thiz, jint comport)
{
	jlong  vdisable;
        if (comport==0)
        {
        	fd = open( "/dev/ttyS0", O_RDWR|O_NOCTTY|O_NDELAY);
            if (-1 == fd)
            {
            	ALOGD("Can't Open Serial Port ttyS0");
                return SERIAL_ERR;
            }
            else
            {
            	ALOGD("open ttyS0 .....\n");
            }
        }
        else if(comport==1)
        {
        	fd = open( "/dev/ttyS1", O_RDWR|O_NOCTTY|O_NDELAY);
            if (-1 == fd)
            {
            	ALOGD("Can't Open Serial Port ttyS1");
                return SERIAL_ERR;
            }
            else
            {
            	ALOGD("open ttyS1 .....\n");
            }
        }
        else if (comport==2)
        {
            fd = open( "/dev/ttyS2", O_RDWR|O_NOCTTY|O_NDELAY);
            if (-1 == fd)
            {
            	ALOGD("Can't Open Serial Port ttyS2");
                return SERIAL_ERR;
            }
            else
            {
            	ALOGD("open ttyS2 .....\n");
            }
        }
        else if(comport == 3){
        	fd = open( "/dev/ttyS3", O_RDWR|O_NOCTTY|O_NDELAY);
        	if (-1 == fd)
        	{
        	       ALOGD("Can't Open Serial Port ttyS3");
        	       return SERIAL_ERR;
        	}
        	else
        	{
        	       ALOGD("open ttyS3 .....\n");
        	}
        } else {
        	ALOGD("##########SERIAL PORT IS NOT EXIST !#############\n");
        }
        if(fcntl(fd, F_SETFL, 0)<0)
        {
        	ALOGD("fcntl failed!\n");
        }
        else
        {
        	ALOGD("fcntl=%d\n",fcntl(fd, F_SETFL,0));
        }
        if(isatty(STDIN_FILENO)==0)
        {
            printf("standard input is not a terminal device\n");
        }
        else
        {
        	ALOGD("isatty success!\n");
        }
    ALOGD("fd-open=%d\n",fd);
	return SERIAL_TRUE;
}

jint multiexcuteWrite(JNIEnv *env, jobject thiz, jchar ch, jint data)
{
    // 定义发送为字符"Q" 选曲 data为用户所选曲目
    char W[] = {0xaa,0x55,0xc1,0x01,0x31,0xf1,0};//Power 退出   W

    char Last[] = {0xaa,0x55,0xc1,0x01,0x12,0xd2,0};//上一曲       P
    char Back[] = {0xaa,0x55,0xc1,0x01,0x11,0xd1,0};//快退         F
    char PauseOrPlay[] = {0xaa,0x55,0xc1,0x01,0x33,0xf3,0};//暂停  播放   S
    char Advance[] = {0xaa,0x55,0xc1,0x01,0x10,0xd0,0};//快进         X
    char Next[] = {0xaa,0x55,0xc1,0x01,0x13,0xd3,0};//下一曲       N
    char Stop[] = {0xaa,0x55,0xc1,0x01,0x16,0xd6,0};//暂停     T
    char Repeat[] = {0xaa,0x55,0xc1,0x01,0x1d,0xdd,0};//重复   E
    char RepeatAB[] = {0xaa,0x55,0xc1,0x01,0x1c,0xdc,0};//AB重复 B

    char Up[] = {0xaa,0x55,0xc1,0x01,0x0c,0xcc,0};//上        U
    char Down[] = {0xaa,0x55,0xc1,0x01,0x0d,0xcd,0};//下      D
    char Left[] = {0xaa,0x55,0xc1,0x01,0x0e,0xce,0};//左      L
    char Right[] = {0xaa,0x55,0xc1,0x01,0x0f,0xcf,0};//右     H
    char OK[] = {0xaa,0x55,0xc1,0x01,0x0b,0xcb,0};//OK       O

    char Menu[] = {0xaa,0x55,0xc1,0x01,0x2b,0xeb,0};//菜单    M
    char Head[] = {0xaa,0x55,0xc1,0x01,0x2a,0xea,0};//标题    G
    char Subtitle[] = {0xaa,0x55,0xc1,0x01,0x29,0xe9,0};//subtitle A
    char Channel[] = {0xaa,0x55,0xc1,0x01,0x24,0xe4,0};//声道        V
    char Angle[] = {0xaa,0x55,0xc1,0x01,0x2c,0xec,0};//角度        C

    char VolumeUp[] = {0xaa,0x55,0xc1,0x01,0x41,0x81,0};//音量加       I
    char VolumeDown[] = {0xaa,0x55,0xc1,0x01,0x40,0x80,0};//音量减       R

    char Title[] = {0xaa,0x55,0xc1,0x01,0x36,0xf6,0};// title        J
    char Chapter[] = {0xaa,0x55,0xc1,0x01,0x37,0xf7,0};// chapter       K

    char zer[] ={0xaa,0x55,0xc1,0x01,0x00,0xc0,0};//num keyboard
    char one[] ={0xaa,0x55,0xc1,0x01,0x01,0xc1,0};
    char two[] ={0xaa,0x55,0xc1,0x01,0x02,0xc2,0};
    char thr[] ={0xaa,0x55,0xc1,0x01,0x03,0xc3,0};
    char fou[] ={0xaa,0x55,0xc1,0x01,0x04,0xc4,0};
    char fiv[] ={0xaa,0x55,0xc1,0x01,0x05,0xc5,0};
    char six[] ={0xaa,0x55,0xc1,0x01,0x06,0xc6,0};
    char sev[] ={0xaa,0x55,0xc1,0x01,0x07,0xc7,0};
    char eig[] ={0xaa,0x55,0xc1,0x01,0x08,0xc8,0};
    char nin[] ={0xaa,0x55,0xc1,0x01,0x09,0xc9,0};
    char ok[] ={0xaa,0x55,0xc1,0x01,0x0b,0xcb,0};
    char del[] ={0xaa,0x55,0xc1,0x01,0x38,0xf8,0};

    switch (ch) {
            case 'W': // Power 退出
                //TxControlCode(PauseOrPlay);
            break;
            case 'P': // 上一曲
                TxControlCode(Last);
            break;
            case 'F': // 快退
                TxControlCode(Back);
            break;
            case 'S': // 暂停  播放
                TxControlCode(PauseOrPlay);
            break;
            case 'X': // 快进
                TxControlCode(Advance);
            break;
            case 'N': // 下一曲
                TxControlCode(Next);
            break;
            case 'T': // 暂停
                TxControlCode(Stop);
            break;
            case 'E': // 重复
                TxControlCode(Repeat);
            break;
            case 'B': // AB重复
                TxControlCode(RepeatAB);
            break;
            case 'U': // 上
                TxControlCode(Up);
            break;
            case 'D': // 下
                TxControlCode(Down);
            break;
            case 'L': // 左
                TxControlCode(Left);
            break;
            case 'H': // 右
                TxControlCode(Right);
            break;
            case 'O': // OK
                TxControlCode(OK);
            break;
            case 'M': // 菜单
                TxControlCode(Menu);
            break;
            case 'G': // 标题
                TxControlCode(Head);
            break;
            case 'A': // Subtitle
                TxControlCode(Subtitle);
            break;
            case 'V': // 声道
                TxControlCode(Channel);
            break;
            case 'C': // 角度
                TxControlCode(Angle);
            break;
            case 'I':  //音量加
                TxControlCode(VolumeUp);
            break;
            case 'R': // 音量减
                TxControlCode(VolumeDown);
            break;
            case 'J': // title
                TxControlCode(Title);
            break;
            case 'K': // chapter
                TxControlCode(Chapter);
            break;
            case 'Q':
                  switch (data){
                      case 0:  //按键0
                            digitTxControlCode(zer);
                      break;
                      case 1:  //按键1
                            digitTxControlCode(one);
                      break;
                      case 2:  //按键2
                            digitTxControlCode(two);
                      break;
                      case 3:  //按键3
                            digitTxControlCode(thr);
                      break;
                      case 4:  //按键4
                            digitTxControlCode(fou);
                      break;
                      case 5:  //按键5
                            digitTxControlCode(fiv);
                      break;
                      case 6:  //按键6
                            digitTxControlCode(six);
                      break;
                      case 7:  //按键7
                            digitTxControlCode(sev);
                      break;
                      case 8:  //按键8
                            digitTxControlCode(eig);
                      break;
                      case 9:  //按键9
                            digitTxControlCode(nin);
                      break;
                      case 10:  //按键OK
                            digitTxControlCode(ok);
                      break;
                      case 11:  //按键del
                            digitTxControlCode(del);
                      break;
                      default:
                      break;
                  }
            break;
            default:
            break;
    }
	return SERIAL_TRUE;
}
jbyteArray multiexcuteRead(JNIEnv *env, jobject thiz)
{
    int i;
    int BUFFER_SIZE = 256;
    jbyte buf[BUFFER_SIZE];
   	if(read(fd, &buf, BUFFER_SIZE) != BUFFER_SIZE)
   	{
  		//ALOGD("No read %dbyte!!! \n", BUFFER_SIZE);
 	}
   //	ALOGD("receive : %s\n", buf);
   //	for(i=0;i<BUFFER_SIZE;i++){
   //	    ALOGD("receive : %x\n", buf[i]);
   //	}
    //分配ByteArray
    jbyteArray array = env->NewByteArray(BUFFER_SIZE);
    //将传递数据拷贝到JAVA端
    env->SetByteArrayRegion(array, 0, BUFFER_SIZE,buf);
    return array;
}

jint getValue(JNIEnv *env, jobject thiz)
{
	return slaveValue;
}

/*
 * Class:     X804master
 * Method:    getVersionName
 */
jstring getVersionName(JNIEnv *env, jobject args) {
	return env->NewStringUTF("1.2");;
}

JNINativeMethod  myMethods[] = {
		{"getVersionName", "()Ljava/lang/String;", (void *) getVersionName },
		{"openSerial", "(I)I",  (void *)openX806Serial},
		{"setSerial", "(IICI)I", (void *)setX806Serial},
		{"writeData", "(CI)I", (void*)multiexcuteWrite},
		{"readData", "()[B", (void*)multiexcuteRead},
		{"getSlaveValue", "()I", (void *)getValue}
};


jint JNI_OnLoad(JavaVM * vm,void * reserved)
{
	ALOGD("JNI_OnLoad\n");

	JNIEnv   *env;
	jint ret;


	ret = vm->GetEnv((void * * )&env, JNI_VERSION_1_6);//FIXME,the system java version is 1.6 ! Don't mistake it!
	if(ret != JNI_OK)
	{
		ALOGD("vm->GetEnv error\n");
		return -1;
	}

	jclass   mycls = env->FindClass("com/along/dvdplayer/SerialPort");

	if(mycls == NULL)
	{
		ALOGD("Could not find the Activity, Please check the FindClass !\n");
		return -1;
	}

	ret = env->RegisterNatives(mycls, myMethods, sizeof(myMethods)/sizeof(myMethods[0]));
	if(ret < 0)
	{
		ALOGD("env->RegisterNatives error\n");
		return -1;
	}

	return JNI_VERSION_1_6;//FIXME, the system java version is 1.6. Don't mistake it !
}

void TxControlCode(char send[]){
    int i;
    int ret;
    if((ret = write(fd, send, 7)) == 7){
        ALOGD("send data successed!!!\n");
        for(i=0; i<7; i++){
            ALOGD("%x ",send[i]);
        }
    }else{
        ALOGD("send data fail!!!\n");
    }
}
void digitTxControlCode(char digitSend[]){
    int ret;
    if((ret = write(fd, digitSend, 7)) == 7){
        ALOGD("send track data successed!!!\n");
    }else{
        ALOGD("send track data fail!!!\n");
    }
}




