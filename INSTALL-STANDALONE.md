# Building RoboTutor

## Overview

The installation steps mentioned below will allow you to set up RoboTutor as a standalone application without installing the RoboLauncher and RoboTransfer applications.

**Note:** 
1. The installation instructions work for both Windows as well as Mac OS.
2. Terminal in Mac OS is referred to as Command Prompt in Windows OS.
3. Use batch scripts (.bat files) for Windows OS and shell scripts (.sh files) for Mac OS.

## Requirements
1. Ensure you have _adb_ installed on your computer. You can [follow the steps in this helpful guide](https://www.androidpit.com/how-to-install-adb-and-fastboot).
2. Connect your device to the computer via USB.

## 1. Device preparation

##### Enable developer mode
1. Go to **Settings > About tablet**.
2. Tap **Build Number** 7 times until it displays _"You are now a developer"_.

##### Enable USB Debugging
1. Go to **Settings > Developer options**.
2. Tap **Enable USB debugging**.
3. Tap **OK** when prompted with a disclaimer.
4. You should see dialog with you computer's _"RSA key fingerprint"_.
5. Tap **Always allow from this computer**.
6. Tap **OK**.

## 2. Assets preparation

#### Preparing assets for English version
1. Clone the [**EnglishAssets**](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-EnglishAssets) repository:
		
		git clone https://github.com/XPRIZE/GLEXP-Team-RoboTutor-EnglishAssets.git

2. Execute the following scripts to generate zip files:  
    * [ZIP_English_StoriesAudio.bat](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-EnglishAssets/blob/master/ZIP_English_StoriesAudio.bat)  
    * [ZIP_English_TutorAudio.bat](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-EnglishAssets/blob/master/ZIP_English_TutorAudio.bat)  
3. Unzip the _English_StoriesAudio.1.1.0.zip_ and _English_TutorAudio.1.1.0.zip_ zip files.

4. Create a folder named _robotutor_assets_ in the internal storage of your Android device.

5. Copy the assets folder from _English_StoriesAudio.1.1.0_ and _English_TutorAudio.1.1.0_ folders into the _robotutor_assets_ folder.

#### Preparing assets for Swahili version
1. Clone the [**CodeDrop2-Assets**](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets) repository:
		
		git clone https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets.git
    
2. Execute the following scripts to push Swahili assets to your Android device:
    * [PUSH_CodeDrop1_LitAudio_SW.sh](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets/blob/master/PUSH_CodeDrop1_LitAudio_SW.sh)
    * [PUSH_CodeDrop1_NumberStories_SW.sh](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets/blob/master/PUSH_CodeDrop1_NumberStories_SW.sh)
    * [PUSH_CodeDrop1_NumberStories_SW.sh](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets/blob/master/PUSH_CodeDrop1_NumberStories_SW.sh)
    * [PUSH_CodeDrop1_Songs_SW.sh](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets/blob/master/PUSH_CodeDrop1_Songs_SW.sh)
    * [PUSH_CodeDrop2_DemoVideos.sh](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets/blob/master/PUSH_CodeDrop2_DemoVideos.sh)
    * [PUSH_CodeDrop2_Icons.sh](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets/blob/master/PUSH_CodeDrop2_Icons.sh)
    * [PUSH_CodeDrop2_MathStories.sh](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets/blob/master/PUSH_CodeDrop2_MathStories.sh)
    * [PUSH_CodeDrop2_Original_SW.sh](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets/blob/master/PUSH_CodeDrop2_Original_SW.sh)
    * [PUSH_CodeDrop2_PuncStories.sh](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets/blob/master/PUSH_CodeDrop2_PuncStories.sh)
    * [PUSH_CodeDrop2_ReadingStories_SW.sh](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets/blob/master/PUSH_CodeDrop2_ReadingStories_SW.sh)
    * [PUSH_CodeDrop2_Tutors_SW.sh](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets/blob/master/PUSH_CodeDrop2_Tutors_SW.sh)

## 3. Pushing configuration file

#### Pushing configuration file for English version
1. Clone the [**RoboTutor**](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-RoboTutor) repository:
		
		git clone https://github.com/XPRIZE/GLEXP-Team-RoboTutor-RoboTutor.git

2. Execute the [configure_english.sh](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-RoboTutor/blob/master/app/src/sample_config_files/configure_english.sh) script to push the configuration file.

#### Pushing configuration file for Swahili version
1. Clone the [**SystemBuild**](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-SystemBuild) repository:
		
		git clone https://github.com/XPRIZE/GLEXP-Team-RoboTutor-SystemBuild.git

2. Execute the following command from terminal to push the configuration file:

		adb push config_sw/config.json /sdcard/Download
		
(**Note:** The configuration file needs to be stored in _Download_ folder on _sdcard_ since the application searches for it in _Download_ folder.)

## 4. Building and installing the RoboTutor app

1. Ensure that you have installed [**Android Studio**](https://developer.android.com/studio/install.html).

2. Open Android Studio and import the RoboTutor project.  

3. Install different versions of the Build Tools and Android SDKs as prompted by Android Studio.

4. Go to _Run_ and select _Run 'app'_ option. This will install the RoboTutor application into your Android device.  
Alternatively, you can build the APK by selecting _Build_ and then _Make Project_. Copy the APK generated in _[app](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-RoboTutor/tree/master/app)/build/outputs/apk_ folder to your Android device and install it.

5. Launch the RoboTutor app from the app drawer.  
  (**Note:** The English version of application can be used right after the app launches. In case of Swahili version, after launching the app, the user needs to wait for assets extraction to complete.)
