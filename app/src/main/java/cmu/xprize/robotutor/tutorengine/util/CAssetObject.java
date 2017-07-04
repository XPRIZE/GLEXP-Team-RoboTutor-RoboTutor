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

package cmu.xprize.robotutor.tutorengine.util;


import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.zip.ZipFile;

import cmu.xprize.robotutor.RoboTutor;

import static cmu.xprize.robotutor.RoboTutor.logManager;
import static cmu.xprize.util.TCONST.ASSET_UPDATE_VERSION;


public class CAssetObject {

    public static final int INDEX_ASSET     = 0;
    public static final int INDEX_RELEASE   = 1;
    public static final int INDEX_UPDATE    = 2;
    public static final int INDEX_INSTALLED = 3;
    public static final int INDEX_ORPHAN    = 4;

    public static final int ASSET_TO_MATCH  = 0;
    public static final int RELEASE_MATCH   = 1;
    public static final int UPDATE_MATCH    = 2;
    public static final int INSTALLED_MATCH = 3;
    public static final int ORPHAN_MATCH    = 4;


    // Commands ids not indices
    //
    public static final int DEL_NO_MATCH = -1;
    public static final int DEL_ASSET_TO_MATCH = 0;
    public static final int DEL_RELEASE_MATCH  = 1;
    public static final int DEL_UPDATE_MATCH   = 2;
    public static final int DEL_MATCH_SET      = 3;
    public static final int DEL_UPDATE_ORPHAN  = 4;

    // mMatchSet agregate constants
    //
    public static final int HAS_NOMATCH       = 0;
    public static final int HAS_RELEASE_MATCH = 1;
    public static final int HAS_UPDATE_MATCH  = 2;
    public static final int HAS_MATCH_SET     = 3;
    public static final int HAS_ORPHAN_MATCH  = 4;

    public static final int ASSET_UNDEFINED    = -1;
    public static final int ASSET_VERSIONMATCH = 0;
    public static final int ASSET_DOWNVERSION  = 1;
    public static final int ASSET_UPVERSION    = 2;
    public static final int ASSET_FIRSTRELEASE = 3;

    static private final String VERSIONSEP = ".";

    private File[]   mFile;
    private Object[] mConstraint;
    private int      mMatchSet = 0;

    private final String TAG = "CAssetObject";

    public CAssetObject() {

        mFile       = new File[5];
        mConstraint = new Object[5];
    }


    static public ArrayList parseVersionSpec(String assetName) {

        String version = assetName.replace("[^0-9\\.]","");

        String[] versionSpec = version.split("\\.");

        ArrayList<Integer> versionISpec = new ArrayList<>();

        // strip out the version ordinal
        //
        for(String element : versionSpec) {
            versionISpec.add(Integer.parseInt(element));
        }

        return versionISpec;
    }


    public void addMatch(int typeIndex, File assetFile, ArrayList constraint, int matchToDelete) {

        deleteAsset(matchToDelete);

        mFile[typeIndex]       = assetFile;
        mConstraint[typeIndex] = constraint;

        switch(typeIndex) {

            case INDEX_RELEASE:
            case INDEX_UPDATE:
            case INDEX_ORPHAN:
                mMatchSet += typeIndex;
                break;

            case INDEX_ASSET:
            case INDEX_INSTALLED:
                // These don't participate in the mMatchSet flag
                break;
        }
    }


    /**
     * If / When you find a base release for an orphaned update we convert the orphan to a
     * standard update_match which may be installed
     */
    public void adoptOrphanUpdate() {

        mFile[INDEX_UPDATE] = mFile[INDEX_ORPHAN];
        mFile[INDEX_ORPHAN] = null;

        mConstraint[INDEX_UPDATE] = mConstraint[INDEX_ORPHAN];
        mConstraint[INDEX_ORPHAN] = null;

        mMatchSet -= HAS_ORPHAN_MATCH;
        mMatchSet += HAS_UPDATE_MATCH;
    }


    public int queryMatch() {

        int result = HAS_NOMATCH;

        if((mMatchSet & HAS_RELEASE_MATCH) != 0)
           result = HAS_RELEASE_MATCH;
        else if((mMatchSet & HAS_UPDATE_MATCH) != 0)
            result = HAS_UPDATE_MATCH;
        else if((mMatchSet & HAS_ORPHAN_MATCH) != 0)
            result = HAS_ORPHAN_MATCH;

        return mMatchSet;
    }


    public boolean queryMatch(int matchType) {

        return (mMatchSet & matchType) == matchType;
    }


    public String getName(int type) {
        return mFile[type].getName();
    }


    public File getFile(int type) {
        return mFile[type];
    }


    public String getVersionString(int type) {
        return ((ArrayList<Integer>)mConstraint[type]).get(0) + VERSIONSEP +
               ((ArrayList<Integer>)mConstraint[type]).get(1) + VERSIONSEP +
               ((ArrayList<Integer>)mConstraint[type]).get(2);
    }


    public ArrayList createConstraintByName(int type, String assetName) {

        String version;

        version = assetName.replaceAll("[^0-9\\.]","");
        version = version.replaceAll("^(\\.)","");
        version = version.replaceAll("(\\.)$","");

        // Only consider the final 5 characters (3 digits) in the object name
        // i.e. the filename may end in a number which should be ignored
        //
        version = version.substring(version.length() - 5);

        String[] versionSpec = version.split("\\.");

        ArrayList<Integer> versionISpec = new ArrayList<>();

        // strip out the version ordinal
        //
        for(String element : versionSpec) {
            versionISpec.add(Integer.parseInt(element));
        }

        setAssetConstraint(type, versionISpec);

        return versionISpec;
    }


    public void setAssetFile(int type, File assetFile) {

        mFile[type] = assetFile;
    }


    public void setAssetConstraint(int type, ArrayList constraint ) {

        mConstraint[type] = constraint;
    }


    private boolean safeDeleteFile(File assetFile) {

        boolean result = false;

        if(assetFile != null) {
            result = true;

            if(RoboTutor.DELETE_INSTALLED_ASSETS) {
                assetFile.delete();
            }
        }

        return result;
    }

    public void deleteAsset(int typeCmd) {

        switch(typeCmd) {

            case DEL_RELEASE_MATCH:

                if(safeDeleteFile(getFile(INDEX_RELEASE)))
                    mMatchSet -= INDEX_RELEASE;

                mFile[INDEX_RELEASE]       = null;
                mConstraint[INDEX_RELEASE] = null;
                break;

            case DEL_UPDATE_MATCH:

                if(safeDeleteFile(getFile(INDEX_UPDATE)))
                    mMatchSet -= INDEX_UPDATE;

                mFile[INDEX_UPDATE]       = null;
                mConstraint[INDEX_UPDATE] = null;
                break;

            case DEL_MATCH_SET:

                if(safeDeleteFile(getFile(INDEX_RELEASE)))
                    mMatchSet -= INDEX_RELEASE;

                if(safeDeleteFile(getFile(INDEX_UPDATE)))
                    mMatchSet -= INDEX_UPDATE;

                mFile[INDEX_RELEASE]       = null;
                mConstraint[INDEX_RELEASE] = null;
                mFile[INDEX_UPDATE]        = null;
                mConstraint[INDEX_UPDATE]  = null;
                break;

            case DEL_UPDATE_ORPHAN:

                if(safeDeleteFile(getFile(INDEX_ORPHAN)))
                    mMatchSet -= INDEX_ORPHAN;

                mFile[INDEX_ORPHAN]       = null;
                mConstraint[INDEX_ORPHAN] = null;
                break;

            case DEL_NO_MATCH:
                break;
        }
    }


    public int getVersionField(int type, int field) {

        return ((ArrayList<Integer>)mConstraint[type]).get(field);
    }


    private boolean testConstraint(int elementa, ArrayList assetVersion, int elementb, ArrayList appVersion) {

        boolean result = true;

        // Check if the version spec element on the constrain exceeds tha asset
        //
        if((int)appVersion.get(elementb) > (int)assetVersion.get(elementa)) {
            result = false;
        }

        return result;
    }


    public int testConstraint(int versionElement, int assetVersion, int appVersion) {

        int result = ASSET_UNDEFINED;

        int assetLevel      = ((ArrayList<Integer>)mConstraint[assetVersion]).get(versionElement);
        int constraintLevel = ((ArrayList<Integer>)mConstraint[appVersion]).get(versionElement);
        int assetRelease    = Integer.MAX_VALUE;

        // If the target version element is the update then allow it to distunguish the 0 update
        // i.e. first release
        //
        if(versionElement == ASSET_UPDATE_VERSION) {
           assetRelease = ((ArrayList<Integer>) mConstraint[assetVersion]).get(ASSET_UPDATE_VERSION);
        }

        // Check if the version spec element on the constrain exceeds tha asset
        //
        if(assetRelease == 0) {
            result = ASSET_FIRSTRELEASE;
        }
        else if(assetLevel == constraintLevel) {
            result = ASSET_VERSIONMATCH;
        }
        else if(assetLevel < constraintLevel) {
            result = ASSET_DOWNVERSION;
        }
        else {
            result = ASSET_UPVERSION;
        }

        return result;
    }


    public boolean unPackAssets(String assetName, String assetFolder, int type, Context _context) {

        boolean success = false;

        String asset = getName(type);

        logManager.postEvent_V(TAG, "Updating Asset: " + assetName + " -> Version: " + getVersionString(type));

        try {
            ZipFile zipFile = new ZipFile(getFile(type));

            Zip _zip = new Zip(zipFile, _context);

            _zip.extractAll(assetName, assetFolder);
            _zip.close();

            // If we want ot purge installed asset zip files then do it here
            //
            if(RoboTutor.DELETE_INSTALLED_ASSETS) {
                getFile(type).delete();
            }

            success = true;

        } catch (Exception e) {

            logManager.postEvent_V(TAG, "Asset Unpack Failed: " + e);
        }

        return success;
    }

}
