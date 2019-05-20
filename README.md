[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# **RoboTutor**


Welcome to RoboTutor: this version was uploaded to XPrize 11/20/2018. For subsequent changes, see [https://github.com/RoboTutorLLC/RoboTutor](https://github.com/RoboTutorLLC/RoboTutor).


--- 
## Quick Installation
To quickly install the most recent version of RoboTutor without having to download the full source code, follow these steps:

1. Go to [this Google Drive folder](https://drive.google.com/open?id=1wbqO6CLq8npQTPJc22B8cD2G80ffFBoC).

2. Download the APK to your tablet (do not install yet).

3. Download *config.json* and place it in the *Download* directory of your tablet.

4. Download the ZIP files for the version you would like to try (Swahili, English, or both), and place them in the *Download* directory of your tablet.

5. Install the RoboTutor APK on your tablet, and launch.

6. Upon launch, RoboTutor will unzip the ZIP assets.

---

## **Setup and Configuration:**

[Install Android Studio](http://developer.android.com/sdk/index.html)<br>

[Install GitHub Desktop](https://desktop.github.com/)<br>

RoboTutor uses a large volume of external assets at runtime.  To successfully run RoboTutor you must first install these assets on your target device: [English](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-EnglishAssets). [Swahili](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets). Once you have cloned and run the associated tools to push the data assets to your device you can proceed with building RoboTutor.


## **Building RoboTutor:**

1. Clone RoboTutor to your computer using Git/GitHub

2. **Import** the RoboTutor project into Android Studio.

3. You may need to install different versions of the build tools and android SDKs.

4. There are a number of build variants you can select to generate versions that support static language selections and also vesions that permit dynamic language selection at runtime. In order to generate any flavor that depends on the key signature, you must generate your own keystore (see next steps). Note that the version used in the XPrize code drop 1 submission usees flavor *release_sw*, which depends on a signed APK.


5. If you do not already have one, follow the steps [here](https://stackoverflow.com/questions/3997748/how-can-i-create-a-keystore) to generate a keystore.

6. Add a file named "keystore.properties" to your root project directory, and give it the following contents. The values should be based on the values you used to generate the keystore.
```
storePassword=<your_store_password>
keyPassword=<your_key_password>
keyAlias=<your_key_alias>
storeFile=<path_to_location_of_keystore>
```

7. Use Android Studio or gradlew to generate a signed APK with the flavor *release_sw*. This will generate the file *robotutor.release_sw.1.8.8.1.apk*. This APK should be transferred to the apk in your local SystemBuild directory.


---

## **XPrize Submission:**

The following repositories are part of the Team-RoboTutor entry:
 * XPRIZE/GLEXP-Team-RoboTutor-RoboTutor
 * XPRIZE/GLEXP-Team-RoboTutor-SystemBuild
 * XPRIZE/GLEXP-Team-RoboTutor-RTAsset_Publisher
 * XPRIZE/GLEXP-Team-RoboTutor-CodeDrop1-Assets
 * XPRIZE/GLEXP-Team-RoboTutor-RoboLauncher 
 * XPRIZE/GLEXP-Team-RoboTutor-RoboTransfer 


# Questions?

Do you have questions about the code, content or data? Please reach out to the [Global Learning XPRIZE Community](http://community.xprize.org/learning).
