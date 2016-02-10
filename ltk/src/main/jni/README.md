## Building LipiTk 

This build is designed to work with Android Studio.

Do not run ndk-build directly as it will place the library in the wrong destination folder
Instead run jni-build from a command prompt with the jni folder as the working directory.  
This will simply run ndk-build with the parameters required to redirect the output to 
the jniLibs folder which is where Android Studio expects shared libraries.

e.g. 

    ndk-build NDK_LIBS_OUT="../jniLibs"