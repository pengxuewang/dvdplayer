LOCAL_PATH := $(call my-dir)

#static library for x806JNI
#include $(CLEAR_VARS)
#LOCAL_LDLIBS 	:= -llog
#LOCAL_MODULE    := x806JniApi
#LOCAL_SRC_FILES := X806JniApi.cpp
#include $(BUILD_STATIC_LIBRARY)

#dynamic library for x806JNI
include $(CLEAR_VARS)
LOCAL_LDLIBS 	:= -llog
LOCAL_MODULE    := x806master
LOCAL_SRC_FILES := X806master.cpp
include $(BUILD_SHARED_LIBRARY)


