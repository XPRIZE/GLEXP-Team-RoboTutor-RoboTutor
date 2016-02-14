LOCAL_PATH := $(call my-dir)

PATHH := D:\Projects\GitHUB\RoboTutor\ltk\src\main\jni/

include $(CLEAR_VARS)

LOCAL_MODULE    := lipitk
LOCAL_CFLAGS +=   -O0 \
                -I$(PATHH)/include/ \
				-I$(PATHH)/src/util/lib \
				-I$(PATHH)/src/lipiengine \
				-I$(PATHH)/src/reco/shaperec/common \
				-I$(PATHH)/src/reco/shaperec/featureextractor/common \
				-I$(PATHH)/src/reco/shaperec/featureextractor/pointfloat \
				-I$(PATHH)/src/reco/shaperec/nn \
				-I$(PATHH)/src/reco/shaperec/preprocessing	\
				-I$(PATHH)/src/util/logger	\
				-I C:/OpenCV/OpenCV-2.3.1/include \

LOCAL_SRC_FILES := 	./src/util/logger/LTKLogger.cpp	\
					./src/util/logger/logger.cpp	\
					./src/common/LTKCaptureDevice.cpp \
				    ./src/common/LTKChannel.cpp \
					./src/common/LTKException.cpp \
					./src/common/LTKScreenContext.cpp \
					./src/common/LTKTrace.cpp \
					./src/common/LTKTraceFormat.cpp \
					./src/common/LTKTraceGroup.cpp \
					./src/util/lib/LTKCheckSumGenerate.cpp \
					./src/util/lib/LTKConfigFileReader.cpp \
					./src/util/lib/LTKErrors.cpp \
					./src/util/lib/LTKImageWriter.cpp \
					./src/util/lib/LTKInkFileReader.cpp \
					./src/util/lib/LTKInkFileWriter.cpp \
					./src/util/lib/LTKInkUtils.cpp \
					./src/util/lib/LTKLinuxUtil.cpp \
					./src/util/lib/LTKLoggerUtil.cpp \
					./src/util/lib/LTKOSUtilFactory.cpp \
					./src/util/lib/LTKStrEncoding.cpp \
					./src/util/lib/LTKStringUtil.cpp \
					./src/util/lib/LTKVersionCompatibilityCheck.cpp \
					./src/lipiengine/lipiengine.cpp \
					./src/lipiengine/LipiEngineModule.cpp \
					./src/reco/shaperec/common/LTKShapeRecoConfig.cpp \
					./src/reco/shaperec/common/LTKShapeRecognizer.cpp \
					./src/reco/shaperec/common/LTKShapeRecoResult.cpp \
					./src/reco/shaperec/common/LTKShapeRecoUtil.cpp \
					./src/reco/shaperec/common/LTKShapeSample.cpp \
					./src/reco/shaperec/featureextractor/common/LTKShapeFeatureExtractor.cpp \
					./src/reco/shaperec/featureextractor/common/LTKShapeFeatureExtractorFactory.cpp \
					./src/reco/shaperec/featureextractor/pointfloat/PointFloat.cpp \
					./src/reco/shaperec/featureextractor/pointfloat/PointFloatShapeFeature.cpp \
					./src/reco/shaperec/featureextractor/pointfloat/PointFloatShapeFeatureExtractor.cpp \
					./src/reco/shaperec/nn/NN.cpp \
					./src/reco/shaperec/nn/NNShapeRecognizer.cpp \
					./src/reco/shaperec/nn/NNAdapt.cpp \
					./src/reco/shaperec/preprocessing/LTKPreprocessor.cpp \
					./src/reco/shaperec/preprocessing/preprocessing.cpp \
					lipiJni.cpp \

LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
