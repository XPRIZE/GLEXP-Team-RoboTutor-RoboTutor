//*********************************************************************************
//
//    Copyright(c) 2016-2017  Kevin Willows All Rights Reserved
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
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.zip.ZipFile;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.tutorengine.util.CAssetObject;
import cmu.xprize.robotutor.tutorengine.util.Zip;
import cmu.xprize.util.TCONST;

import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.ASSET_DOWNVERSION;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.ASSET_FIRSTRELEASE;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.ASSET_UPVERSION;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.ASSET_VERSIONMATCH;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.DEL_MATCH_SET;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.DEL_NO_MATCH;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.DEL_RELEASE_MATCH;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.HAS_NOMATCH;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.ASSET_TO_MATCH;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.INDEX_ASSET;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.HAS_ORPHAN_MATCH;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.HAS_RELEASE_MATCH;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.HAS_UPDATE_MATCH;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.INDEX_INSTALLED;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.INDEX_RELEASE;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.INDEX_UPDATE;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.ORPHAN_MATCH;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.RELEASE_MATCH;
import static cmu.xprize.robotutor.tutorengine.util.CAssetObject.UPDATE_MATCH;


public class CTutorAssetManager {

    private Context      mContext;
    private AssetManager mAssetManager;
    private CAssetObject mAssetObject;

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
            outPath = RoboTutor.APP_PRIVATE_FILES;

            // catch the special case where we are copying a single storyFolder from assets root
            // as we may need to precreate the base storyFolder.

            if(!baseAsset.equals("") && files.length > 0) {

                outPath += "/" + baseAsset;

                File outputFile = new File(outPath);

                // Note that this may fail but the error will be picked up in the following
                // copy process as there will not be a valid target.
                if(!outputFile.exists())
                    outputFile.mkdir();
            }

            copyFolderAsset(baseAsset, files, outPath, true);

            Log.v(TAG, "NOTICE: Assets installed.");

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Log.e(TAG, "ERROR: External Assets READONLY.");

        } else {
            Log.e(TAG, "ERROR: External Assets OTHER.");
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

        File projects = new File(RoboTutor.APP_PRIVATE_FILES + "/" + path);

        return projects.exists();
    }


    private void copyFolderAsset(String inputPath, String []folderContents, String outputPath, Boolean recurse) {

        String[] files = null;

        // Catch the special case where we are copying a single file from assets root (first pass)
        // i.e. skip to file copy if this isn't a storyFolder
        if(folderContents.length > 0) {

            // Paths are relative so don't put '/' path sep on files in base storyFolder
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


    private String[] listFolder(String path) {

        File folder = new File(path);
        int  i1 = 0;

        File[] listOfFiles = folder.listFiles();
        String[] names = new String[listOfFiles.length];

        Log.v(TAG, "Listing storyFolder: " + path);

        for (File fileObj : listOfFiles) {

            names[i1++] = fileObj.getName();

            if (fileObj.isFile()) {
                Log.v(TAG, "File " + fileObj.getName());
            } else if (fileObj.isDirectory()) {
                Log.v(TAG, "Folder " + fileObj.getName());
            }
        }

        return names;
    }




    private void doUpateMatch(File assetFile, ArrayList assetVersion, int matchConstraint) {


        switch (mAssetObject.testConstraint(TCONST.ASSET_UPDATE_VERSION, ASSET_TO_MATCH, matchConstraint)) {

            case ASSET_FIRSTRELEASE:

                switch(matchConstraint) {

                    // There are currently no matching asset found in the storyFolder
                    //
                    case ASSET_TO_MATCH:
                    case RELEASE_MATCH:
                        mAssetObject.deleteAsset(ASSET_TO_MATCH);
                        break;

                    // There is currently a release-match - i.e. a full asset distribution zip
                    //
                    case UPDATE_MATCH:
                        // We have found the matching base release for the existing update
                        //
                        mAssetObject.addMatch(RELEASE_MATCH, assetFile, assetVersion, DEL_MATCH_SET);
                        break;

                    // There is currently an orphan-match - i.e. an update match that doesn't have a
                    // matching or installed full release.
                    //
                    case ORPHAN_MATCH:
                        // We have found the matching base release for the existing update
                        //
                        mAssetObject.addMatch(RELEASE_MATCH, assetFile, assetVersion, DEL_MATCH_SET);
                        mAssetObject.adoptOrphanUpdate();
                        break;
                }
                break;

            case ASSET_VERSIONMATCH:

                mAssetObject.deleteAsset(ASSET_TO_MATCH);
                break;

            case ASSET_DOWNVERSION:

                if(matchConstraint == HAS_UPDATE_MATCH) {

                    if (mAssetObject.getVersionField(ASSET_TO_MATCH, TCONST.ASSET_UPDATE_VERSION) == 0) {

                        mAssetObject.addMatch(RELEASE_MATCH, assetFile, assetVersion, DEL_RELEASE_MATCH);
                    }
                }
                else {
                    mAssetObject.deleteAsset(ASSET_TO_MATCH);
                }
                break;

            case ASSET_UPVERSION:
                // we are deleting a release-match and possibly replacing an update-match
                // so we attempt to delete both downversions
                //
                mAssetObject.addMatch(ORPHAN_MATCH, assetFile, assetVersion, DEL_MATCH_SET);

                switch(matchConstraint) {

                    // There are currently no matching asset found in the storyFolder
                    //
                    case INDEX_ASSET:
                        mAssetObject.addMatch(UPDATE_MATCH, assetFile, assetVersion, DEL_NO_MATCH);
                        break;

                    // There is currently a release-match - i.e. a full asset distribution zip
                    //
                    case RELEASE_MATCH:
                        if(mAssetObject.queryMatch(UPDATE_MATCH)) {
                            doUpateMatch(assetFile, assetVersion, UPDATE_MATCH);
                        }
                        else {
                            mAssetObject.addMatch(UPDATE_MATCH, assetFile, assetVersion, DEL_NO_MATCH);
                        }
                        break;

                    // There is currently a release-match - i.e. a full asset distribution zip
                    //
                    case UPDATE_MATCH:
                        break;

                    // There is currently an orphan-match - i.e. an update match that doesn't have a
                    // matching or installed full release.
                    //
                    case ORPHAN_MATCH:
                        break;
                }
                break;

            default:
                mAssetObject.deleteAsset(ASSET_TO_MATCH);
                break;
        }
    }


    private void doReleaseMatch(File assetFile, ArrayList assetVersion, int matchConstraint) {

        switch (mAssetObject.testConstraint(TCONST.ASSET_RELEASE_VERSION, ASSET_TO_MATCH, matchConstraint)) {

            case ASSET_VERSIONMATCH:
                doUpateMatch(assetFile, assetVersion, matchConstraint);
                break;

            case ASSET_DOWNVERSION:
                mAssetObject.deleteAsset(ASSET_TO_MATCH);
                break;

            case ASSET_UPVERSION:

                if (mAssetObject.getVersionField(ASSET_TO_MATCH, TCONST.ASSET_UPDATE_VERSION) == 0) {

                    mAssetObject.addMatch(RELEASE_MATCH, assetFile, assetVersion, DEL_MATCH_SET);
                }
                else {
                    mAssetObject.addMatch(ORPHAN_MATCH, assetFile, assetVersion, DEL_MATCH_SET);
                }
                break;
        }
    }


    /**
     * Search the DOWNLOAD storyFolder for matching asset files
     *
     * Find the highest matching version - available
     *
     * @param assetName
     * @param installConstraint
     * @return
     */
    private void updateAssetPackage(String assetName, ArrayList<Integer> installConstraint) {

        CAssetObject assetObject  = null;
        boolean      killAsset    = false;

        String[]  folderList   = null;
        File      assetFile    = null;
        ArrayList foundRelease = null;

        // We add a convenience node to the AssetObject with the installed version spec
        // This is used in INDEX_INSTALLED comparisons in HAS_NOMATCH
        //
        mAssetObject = new CAssetObject();
        mAssetObject.addMatch(INDEX_INSTALLED, null, installConstraint, DEL_NO_MATCH);

        folderList = listFolder(RoboTutor.DOWNLOAD_PATH);

        for (String objectname : folderList) {

            // Only process RTAsset_ files
            //
            if (objectname.toLowerCase().startsWith(assetName)) {

                try {

                    String srcPath = RoboTutor.DOWNLOAD_PATH + File.separator + objectname;
                    assetFile     = new File(srcPath);

                    // Extract the Found assets version spec and assign a File object
                    //
                    ArrayList assetVersion = mAssetObject.createConstraintByName(ASSET_TO_MATCH, objectname);
                    mAssetObject.setAssetFile(ASSET_TO_MATCH, assetFile);

                    // i.e. the app can't use an older ot newer asset version than it supports.
                    //
                    switch (mAssetObject.testConstraint(TCONST.ASSET_CODE_VERSION, ASSET_TO_MATCH, INDEX_INSTALLED)) {

                        case ASSET_VERSIONMATCH:

                            switch(mAssetObject.queryMatch()) {

                                // There are currently no matching asset found in the storyFolder
                                //
                                case HAS_NOMATCH:
                                    doReleaseMatch(assetFile, assetVersion, INDEX_INSTALLED);
                                    break;

                                // There is currently a release-match - i.e. a full asset distribution zip
                                //
                                case HAS_RELEASE_MATCH:

                                    doReleaseMatch(assetFile, assetVersion, RELEASE_MATCH);
                                    break;

                                // There are currently release&update-matches -
                                // i.e. a full asset distribution zip and matching update zip
                                //
                                case HAS_UPDATE_MATCH:

                                    doReleaseMatch(assetFile, assetVersion, UPDATE_MATCH);
                                    break;

                                // There is currently an orphan-match - i.e. an update match that doesn't have a
                                // matching or installed full release.
                                //
                                case HAS_ORPHAN_MATCH:

                                    doReleaseMatch(assetFile, assetVersion, ORPHAN_MATCH);
                                    break;
                            }
                            break;

                        // The asset file doesn't match the required version for the current tutorEngine
                        //
                        default:

                            // Delete up and down versions -
                            mAssetObject.deleteAsset(ASSET_TO_MATCH);
                            break;
                    }
                } catch (Exception e) {
                    CErrorManager.logEvent(TAG, "Error loading asset: " + objectname, true);
                }

            }
        }
    }


    /**
     * manage install sequencing -  asset_fullrelease / asset_incremental
     *
     *  See : /doc/Asset Manager Logic.vsdx
     *
     * @param assetName
     * @param assetFolder
     */
    private void updateAssetPackage(String assetName, String assetFolder) {

        SharedPreferences prefs = RoboTutor.ACTIVITY.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int assetFullOrdinal = prefs.getInt(assetName + TCONST.ASSET_RELEASE_VERSION, 0);
        int assetIncrOrdinal = prefs.getInt(assetName + TCONST.ASSET_UPDATE_VERSION, 0);

        // Build a constraint that limits us to looking for compatible ASSET_CODE_VERSION's i.e.
        // the Robotutor_Version_Spec<ASSET_VERSION> == NamedAsset_Version_Spec<ASSET_CODE_VERSION>
        // with full releases that are greater or match what we already have and
        // have a greater or matching incremental release
        //
        ArrayList<Integer> constraint = new ArrayList<>();

        constraint.add(0,(int)RoboTutor.VERSION_SPEC.get(TCONST.ASSET_VERSION));
        constraint.add(1, assetFullOrdinal);
        constraint.add(2, assetIncrOrdinal);

        updateAssetPackage(assetName, constraint);

        // Orphan match is where we find an update that has not had the associated release
        // installed yet.
        //
        if (mAssetObject.queryMatch(HAS_ORPHAN_MATCH)) {

        }
        else {

            if (mAssetObject.queryMatch(HAS_RELEASE_MATCH)) {

                File releaseFile = mAssetObject.getFile(INDEX_RELEASE);

                mAssetObject.unPackAssets(releaseFile.getName(), assetFolder, INDEX_RELEASE, mContext);

                editor.putInt(assetName + TCONST.ASSET_RELEASE_VERSION, mAssetObject.getVersionField(INDEX_RELEASE, TCONST.ASSET_RELEASE_VERSION));
                editor.putInt(assetName + TCONST.ASSET_UPDATE_VERSION , 0);
                editor.apply();
            }

            if (mAssetObject.queryMatch(HAS_UPDATE_MATCH)) {

                File updateFile = mAssetObject.getFile(INDEX_UPDATE);

                mAssetObject.unPackAssets(updateFile.getName(), assetFolder, INDEX_UPDATE, mContext);

                editor.putInt(assetName + TCONST.ASSET_UPDATE_VERSION , mAssetObject.getVersionField(INDEX_UPDATE, TCONST.ASSET_UPDATE_VERSION));
                editor.apply();
            }
        }
    }


    /**
     *  Search for all packages that start with <assetRoot>... and end with .<x...>.<x...>.<x...>.zip
     *
     *
     * @param assetRoot
     * @param assetFolder
     */
    public void updateAssetPackages(String assetRoot, String assetFolder) {

        String                  assetName;
        String[]                folderList = null;
        HashMap<String,String>  dictionary = new HashMap<>();

        SharedPreferences prefs = RoboTutor.ACTIVITY.getPreferences(Context.MODE_PRIVATE);

        // Check the download folder for files that begin with the assetRoot prefix
        // Keep a Map of Unique asset names. i.e. everything except the version/ext 1.2.3.zip
        //
        folderList = listFolder(RoboTutor.DOWNLOAD_PATH);

        for (String objectname : folderList) {

            // Only process RTAsset_ files
            //
            if (objectname.toLowerCase().startsWith(assetRoot.toLowerCase())) {

                int startVer = objectname.indexOf(".");
                assetName = objectname.substring(0,startVer).toLowerCase();

                if(!dictionary.containsKey(assetName)) {
                    dictionary.put(assetName, assetName);
                    Log.v(TAG, "Asset Found: " + assetName);
                }
            }
        }


        // Now look through the unique asset names found for the most recent release/update
        // set available and install them
        //
        Iterator<?> tObjects = dictionary.entrySet().iterator();

        while(tObjects.hasNext() ) {

            Map.Entry entry = (Map.Entry) tObjects.next();

            assetName = (String)entry.getValue();

            Log.v(TAG, "Asset Installing: " + assetName);
            updateAssetPackage(assetName, assetFolder);
        }
    }


    public void extractAsset(String zipName, String targetFolder) throws IOException {

        String zipPath     = RoboTutor.APP_PRIVATE_FILES + "/" + zipName;
        String extractPath = RoboTutor.APP_PRIVATE_FILES + targetFolder;

        File file       = new File(zipPath);
        ZipFile zipFile = new ZipFile(file);

        try {

            Zip _zip = new Zip(zipFile, mContext);

            _zip.extractAll(zipName, extractPath);
            _zip.close();
            file.delete();

        } catch (IOException ie) {
            Log.e(TAG, "ERROR: failed extraction" + zipName + " - reason: " + ie);
        }
    }

}
