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



//*********************************************************************************************
//*********************************************************************************************
//*********************************************************************************************
// NOTE: You don't want an underscore in a JNI package name !!!!!!!
// This will just cause you grief
// see: http://stackoverflow.com/questions/16069209/invoking-jni-functions-in-android-package-name-containing-underscore
// see: https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/design.html#resolving_native_method_names
//*********************************************************************************************
//*********************************************************************************************
//*********************************************************************************************

package cmu.xprize.ltkplus;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class CLipiTKJNIInterface {

    private String _lipiDirectory;
    private String _project;

    static
    {
        try {
            System.loadLibrary("lipitk");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    //  Initializes the interface with a directory to look for projects in
    //  	the name of the project to use for recognition, and the name
    //  	of the ShapeRecognizer to use.
    public CLipiTKJNIInterface(String lipiDirectory, String project) {
        _lipiDirectory = lipiDirectory;
        _project = project;
    }


    // TODO: Fix this - It's NUTS
    public String getSymbolName(int id,String project_config_dir)
    {
        String line;
        int temp;
        String [] splited_line= null;
        try
        {
            File map_file = new File(project_config_dir+"unicodeMapfile.ini");
            BufferedReader readIni = new BufferedReader(new FileReader(map_file));
            readIni.readLine();
            readIni.readLine();
            readIni.readLine();
            readIni.readLine();
            while((line=readIni.readLine())!=null)
            {
                splited_line = line.split(" ");
//                Log.d("JNI_LOG","split 0="+splited_line[0]);
//                Log.d("JNI_LOG","split 1="+splited_line[1]);
                splited_line[0] = splited_line[0].substring(0, splited_line[0].length()-1); //trim out = sign
                if(splited_line[0].equals((new Integer(id)).toString()))
                {
                    splited_line[1] = splited_line[1].substring(2);
                    temp = Integer.parseInt(splited_line[1], 16);
                    return String.valueOf((char)temp);
                }
            }
        }
        catch(Exception ex)
        {
            Log.d("JNI_LOG","Exception in getSymbolName Function"+ex.toString());
            return "-1";
        }
        return "0";
    }

    public void initialize() {
        try
        {
            initializeNative(_lipiDirectory, _project);
        }
        catch(Exception ex) {

        }
    }

    public CRecResult[] recognize(CStroke[] strokes) {
        CRecResult[] results = recognizeNative(strokes, strokes.length);

        for (CRecResult result : results)
            Log.d("jni", "ShapeID = " + result.Id + " Confidence = " + result.Confidence);

        return results;
    }

    // Initializes the LipiTKEngine in native code
    private native void initializeNative(String lipiDirectory, String project);

    // Returns a list of results when recognizing the given list of strokes
    private native CRecResult[] recognizeNative(CStroke[] strokes, int numJStrokes);

    public String getLipiDirectory() {
        return _lipiDirectory;
    }

}
