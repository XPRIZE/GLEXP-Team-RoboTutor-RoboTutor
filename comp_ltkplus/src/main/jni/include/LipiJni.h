/************************************************************************
 * FILE DESCR: Header file for the JNI interface
 *
 * CONTENTS:
 *  main
 *
 * AUTHOR:     Ajay Patial
 *
 * DATE:       August 30, 2012
 * CHANGE HISTORY:
 * Author       Date            Description of change
 * ************************************************************************/
#ifndef LIPI_JNI_H
#define LIPI_JNI_H

#include "LTKInkFileReader.h"
#include "LTKLipiEngineInterface.h"
#include "LipiEngineModule.h"
#include "lipiengine.h"
#include "LTKMacros.h"
#include "LTKInc.h"
#include "LTKTypes.h"
#include "LTKTrace.h"
#include <string>
#include <jni.h>

#define CONFIDENCE_THRESHOLD 0.002f
#define NUMOFCHOICES 5

extern "C" void JNICALL Java_cmu_xprize_ltkplus_CLipiTKJNIInterface_initializeNative(JNIEnv *env, jobject this_object, jstring lipiDirectory, jstring project);

extern "C" jobjectArray JNICALL Java_cmu_xprize_ltkplus_CLipiTKJNIInterface_recognizeNative(JNIEnv *env, jobject this_object, jobjectArray StrokeArray, jint numJStrokes);

#endif
