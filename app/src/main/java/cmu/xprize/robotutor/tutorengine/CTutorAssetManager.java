//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

package cmu.xprize.robotutor.tutorengine;


import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipFile;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.util.Zip;


public class CTutorAssetManager {
    private Context      mContext;
    private AssetManager mAssetManager;

    private final String TAG = "CTutorAssetManager";


    public CTutorAssetManager(Context context) {

        this.mContext = context;
        mAssetManager = context.getAssets();
    }

    public void installAssets(String baseAsset) throws IOException {

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            String[] files = null;
            String   outPath;

            files   = mAssetManager.list(baseAsset);
            outPath = RoboTutor.EXTERNFILES;

            // catch the special case where we are copying a single folder from assets root
            // as we may need to precreate the base folder.

            if(!baseAsset.equals("") && files.length > 0) {

                outPath += "/" + baseAsset;

                File outputFile = new File(outPath);

                // Note that this may fail but the error will be picked up in the following
                // copy process as there will not be a valid target.
                if(!outputFile.exists())
                    outputFile.mkdir();
            }

            copyFolderAsset(baseAsset, files, outPath, true);

            Log.d(TAG, "NOTICE: Assets installed.");

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Log.d(TAG, "ERROR: External Assets READONLY.");

        } else {
            Log.d(TAG, "ERROR: External Assets OTHER.");
        }
    }

    /**
     * Crude check for installation status
     * TODO: Create state file to manage tutor install status etc.
     *
     * @param path
     * @return
     */
    public boolean fileCheck(String path) {

        File projects = new File(RoboTutor.EXTERNFILES + "/" + path);

        return projects.exists();
    }


    private void copyFolderAsset(String inputPath, String []folderContents, String outputPath, Boolean recurse) {

        String[] files = null;

        // Catch the special case where we are copying a single file from assets root (first pass)
        // i.e. skip to file copy if this isn't a folder
        if(folderContents.length > 0) {

            // Paths are relative so don't put '/' path sep on files in base folder
            if (inputPath.length() > 0)
                        inputPath += "/";

            for (String filename : folderContents) {

                boolean success = true;

                String inPath = inputPath + filename;
                String outPath = outputPath + "/" + filename;

                try {
                    files = mAssetManager.list(inPath);

                } catch (IOException e) {

                }

                // Test - if it has files then it is a directory

                if (files.length > 0) {
                    File outputFile = new File(outPath);

                    if (!outputFile.exists())
                        success = outputFile.mkdir();

                    if (success && recurse)
                        copyFolderAsset(inPath, files, outPath, true);
                } else {
                    copyFileAsset(inPath, outPath);
                }
            }
        } else {
            // Catch the special case where we are copying a single file from assets root (first pass)

            String inPath = inputPath;
            String outPath = outputPath + "/" + inputPath;

            copyFileAsset(inPath, outPath);
        }

    }


    private void copyFileAsset(String inputPath, String outputPath) {

        AssetManager assetManager = mContext.getAssets();
        OutputStream out          = null;
        InputStream  in           = null;

        byte[] buffer    = new byte[1024];
        int    read;

        try {
            in  = assetManager.open(inputPath);
            out = new FileOutputStream(outputPath);

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();

        } catch(java.io.FileNotFoundException e) {
            Log.e(TAG, "INFO: Skipping missing file: " + inputPath + " - reason: " + e);

        } catch (IOException e) {
            Log.e(TAG, "ERROR: Failed to copy asset file: " + inputPath + " - reason: " + e);

        } finally {
            in  = null;
            out = null;
        }
    }


    public void extractAsset(String zipName, String targetFolder) throws IOException {
        String zipPath     = RoboTutor.EXTERNFILES + "/" + zipName;
        String extractPath = RoboTutor.EXTERNFILES + targetFolder;

        File file       = new File(zipPath);
        ZipFile zipFile = new ZipFile(file);

        try {

            Zip _zip = new Zip(zipFile);
            _zip.unzip(extractPath);
            _zip.close();
            file.delete();

        } catch (IOException ie) {
            Log.e(TAG, "ERROR: failed extraction" + zipName + " - reason: " + ie);
        }
    }
}
