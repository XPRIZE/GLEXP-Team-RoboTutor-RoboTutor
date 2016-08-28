## Building LipiTk 

This build is designed to work with Android Studio.

Do not run ndk-build directly as it will place the library in the wrong destination folder
Instead run jni-build from a command prompt with the jni folder as the working directory.  
This will simply run ndk-build with the parameters required to redirect the output to 
the jniLibs folder which is where Android Studio expects shared libraries.

e.g. 

    ndk-build NDK_LIBS_OUT="../jniLibs"
    
To build successfully you must add:

    sourceSets {
        main {
            jni.srcDirs = []
        }
    }
    
to the build.gradle file for the writingcomp module     
    
To Build you need to update the PATHH variable to point to the jni folder in your dev environment.

If you refactor the ltk source to a different package you need to update the path in the class 
specification. 
 
 lipiJni.cpp 
 lipiJni.h 

e.g. Java_cmu_xprize_ltk_CLipiTKJNIInterface_??
                or
     Java_lipitk_CLipiTKJNIInterface_??


You also need to update the marshalling references to reflect package changes.

env->FindClass("cmu/xprize/ltk/RecResult");

To control debugging log output in Android see   LTKLoggerUtil.cpp : ANDROID_LOG

