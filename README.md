[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# **RoboTutor**

Welcome to RoboTutor: this version was uploaded to XPrize 11/20/2018. For subsequent changes, see [https://github.com/RoboTutorLLC/RoboTutor](https://github.com/RoboTutorLLC/RoboTutor).


## **Setup and Configuration:**

1. [Install Android Studio](http://developer.android.com/sdk/index.html)<br>

2. [Install GitHub Desktop](https://desktop.github.com/)<br>

(Note: RoboTutor uses a large volume of external assets at runtime.  To successfully run RoboTutor you must first install these assets on your target device: [English](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-EnglishAssets). [Swahili](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets). Once you have cloned and run the associated tools to push the data assets to your device you can proceed with building RoboTutor.)


## **Building RoboTutor:**

1. Clone RoboTutor to your computer using Git/GitHub.

2. Import the RoboTutor project into Android Studio.

3. You may need to install different versions of the build tools and android SDKs.

4. Generate an [upload key and keystore](https://developer.android.com/studio/publish/app-signing#generate-key) and store it in the root directory of the RoboTutor project.

5. Add a file named "keystore.properties" to the root project of RoboTutor directory, and give it the following contents. The values should be based on the values used to generate the keystore in step 4.
```
storePassword=<your_store_password>
keyPassword=<your_key_password>
keyAlias=<your_key_alias>
storeFile=<path_to_location_of_keystore>
```
(Note: For storeFile, the value can be an absolute path to your keystore file.)

6. Sync the project and install relevant build tools as prompted.

7. Go to Build > Build Signed Bundle / APK > APK. Provide your keystore path, keystore password, key alias and key password generated in step 4. Click Next, select “release” under Build Variants and select “V1 (Jar Signature)” under Signature Versions.

8. This will generate the file *robotutor.release.2.7.7.1.apk*. This APK should be transferred to the [apk](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-SystemBuild/tree/master/apk) folder in your local SystemBuild directory.


## **XPrize Submission:**

The following repositories are part of the Team-RoboTutor entry:
 * XPRIZE/GLEXP-Team-RoboTutor-RoboTutor
 * XPRIZE/GLEXP-Team-RoboTutor-SystemBuild
 * XPRIZE/GLEXP-Team-RoboTutor-RTAsset_Publisher
 * XPRIZE/GLEXP-Team-RoboTutor-CodeDrop1-Assets
 * XPRIZE/GLEXP-Team-RoboTutor-RoboLauncher 
 * XPRIZE/GLEXP-Team-RoboTutor-RoboTransfer 

<br>
<br>