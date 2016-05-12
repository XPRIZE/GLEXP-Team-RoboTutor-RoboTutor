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

package cmu.xprize.util;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



/**
 * Global helper to cache spec files from the tutor assets folder
 */
public class JSON_Helper {

    static public AssetManager           _assetManager;
    static public String                 _cacheSource;
    static public String                 _externFiles;


    static private final String TAG = "JSON_HELPER";


    public JSON_Helper(AssetManager am, String cs, String ef) {
        set_assetManager(am);
        set_cacheSource(cs);
        set_externFiles(ef);
    }

    static public void set_assetManager(AssetManager am) { _assetManager = am; }
    static public void set_cacheSource(String cs) { _cacheSource = cs; }
    static public void set_externFiles(String ef) { _externFiles = ef; }

    static public AssetManager assetManager() {
        return _assetManager;
    }

    static public String cacheData(String fileName) {

        InputStream in;

        StringBuilder buffer = new StringBuilder();
        byte[] databuffer    = new byte[1024];
        int    count;

        String state = Environment.getExternalStorageState();

        try {

            if ((Environment.MEDIA_MOUNTED.equals(state)) ||
                    (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))) {

                // Cache the JSON Spec -
                // We can load from Android Assets or from an external file based on the
                // CacheSource setting
                //
                if(_cacheSource.equals(TCONST.ASSETS)) {

                    in = _assetManager.open(fileName);

                } else {
                    String filePath = _externFiles + "/" + fileName;

                    in = new FileInputStream(filePath);
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line = null;

                // Filter Comments out of the json source
                //
                while ((line = br.readLine()) != null) {
                    line = line.replaceFirst("//.*$","");
                    buffer.append(line);
                }
                in.close();

                Log.d(TAG, "NOTICE: SceneDescr - Loaded.");

            } else {
                Log.d(TAG, "ERROR: SceneDescr - Assets Unavailable.");
            }

        } catch (FileNotFoundException e) {
            Log.d(TAG, "ERROR: " + e);

        } catch (IOException e) {
            Log.d(TAG, "ERROR: " + e);
        }

        return buffer.toString();
    }


    /**
     * This is called from within an object to parse its own structure from a JSON spec
     * jsonObj is the JSON spec
     *
     * It iterates over the fields in the root object and initializes fields that are found in the
     * JSON spec - ignoring missing fields.  Child Objects are created on demand and recursively
     * initialized from the JSON child/sub-objects.
     *
     * Obj is the object itself - so it already exists and its type is known at call time. So this
     * is not used to generate the root object from the spec.
     *
     * @param jsonObj
     * @param self
     * @return
     */
    static public void parseSelf(JSONObject jsonObj, Object self, HashMap<String, Class> classMap, IScope scope) {

        // determine the class of the object that wants to be expanded
        Class tClass = self.getClass();

        // get a list of all its fields - we only use public fields here as the use case for this
        // function is constrained to specific object types -
        Field[] fields = tClass.getFields();

        System.out.printf("fields:%d\n", fields.length);

        // Iterate over all the fields in the object class to see which field have initializer
        // values in the JSON Image
        for (Field field : fields) {

            Class<?> fieldClass = field.getType();
            Class<?> fieldTyoe  = field.getClass();
            String   className  = fieldClass.toString();
            String   fieldName  = field.getName();
            Object   field_obj  = null;

            JSONObject nJsonObj = null;
            JSONObject nJsonMap;
            JSONObject cObj;
            JSONArray  nArr;

            try {
                // we don't care about the value only whether if there is a field there
                //TODO: fill out JSONExceptions error messages
                if(jsonObj.has(fieldName)) {

                    // Most of our fields are Strings so handle them as efficiently as possible
                    if(fieldClass.equals(String.class)) {

                        try {
                            field.set(self, jsonObj.getString(fieldName));

                        } catch (JSONException e) {
                            //e.printStackTrace();
                            Log.e(TAG, "field conversion: " + e);
                            System.exit(1);
                        }
                    }

                    // Most of our fields are Strings so handle them as efficiently as possible
                    else if(fieldClass.equals(Boolean.class)) {

                        try {
                            field.set(self, jsonObj.getBoolean(fieldName));

                        } catch (JSONException e) {
                            //e.printStackTrace();
                            Log.e(TAG, "field conversion: " + e);
                            System.exit(1);
                        }
                    }

                    // Most of our fields are Strings so handle them as efficiently as possible
                    else if(className.equals("float")) {

                        try {
                            field.setFloat(self, Float.parseFloat(jsonObj.getString(fieldName)));

                        } catch (JSONException e) {
                            //e.printStackTrace();
                            Log.e(TAG, "field conversion: " + e);
                            System.exit(1);
                        }
                    }

                    // Most of our fields are Strings so handle them as efficiently as possible
                    else if(className.equals("boolean")) {

                        try {
                            field.set(self, jsonObj.getBoolean(fieldName));

                        } catch (JSONException e) {
                            //e.printStackTrace();
                            Log.e(TAG, "field conversion: " + e);
                            System.exit(1);
                        }
                    }

                    // Most of our fields are Strings so handle them as efficiently as possible
                    //else if(fieldClass.equals(Long.class)) {
                    else if(className.equals("long")) {

                        try {
                            field.set(self, jsonObj.getLong(fieldName));

                        } catch (JSONException e) {
                            //e.printStackTrace();
                            Log.e(TAG, "field conversion: " + e);
                            System.exit(1);
                        }
                    }

                    // Most of our fields are Strings so handle them as efficiently as possible
                    //else if(fieldClass.equals(Integer.class)) {
                    else if(className.equals("int")) {
                        try {
                            field.set(self, jsonObj.getInt(fieldName));

                        } catch (JSONException e) {
                            //e.printStackTrace();
                            Log.e(TAG, "field conversion: " + e);
                            System.exit(1);
                        }
                    }

                    // Our hash maps are assumed to always be of the form <String, ?>
                    // where ? is resolved through the "type" field of the Map element
                    else  if (fieldClass.equals(HashMap.class)) {

                        nJsonObj = jsonObj.getJSONObject(fieldName);

                        HashMap<String, Object> field_Map = new HashMap<String, Object>();

                        field.set(self,field_Map);

                        Iterator<?> keys = nJsonObj.keys();

                        while(keys.hasNext() ) {

                            String key  = (String)keys.next();
                            Object eObj = null;

//                            if(key.equals("CANCEL_HESITATION_TIMER")) {
//                                Log.i(TAG, "THERE");
//                            }

                            // Throw away comment fields
                            if(!key.equals("COMMENT")) {
                                Log.d(TAG, "Inflating Object: " + key);
                                JSONObject elem = nJsonObj.getJSONObject(key);

                                // This is a convenience construct -
                                // If there is a maptype then the node has it's instance data
                                // partially or completely defined in a HashMap object that is in
                                // a separate JSON construct to allow reuse in multiple nodes
                                // or simply to make the JSON easier to read.
                                //
                                // For each "maptype" there is a named json Map construct
                                // (e.g. "maptype":"moduleMap") "moduleMap" is assumed to exist
                                // on the parent JSONobject.  For each node with a maptype there
                                // is an associated "mapname" which defines the element to use
                                // in the object creation.  Note that the mapType is assumed to be
                                // either a "graph_node" or subclass thereof.
                                //
                                if(elem.has("maptype")) {

                                    // Note that if there has a "maptype" then it's actual type
                                    // is found in the "type" field of the mapped object.
                                    // i.e. parentjsonobj.maptype.mapname.type
                                    //
                                    nJsonMap = jsonObj.getJSONObject(elem.getString("maptype"));
                                    JSONObject mapElem = nJsonMap.getJSONObject(elem.getString("mapname"));

                                    Class<?> elemClass = classMap.get(mapElem.getString("type"));

                                    System.out.printf("class type:%s\n", elemClass.getName());
                                    eObj = elemClass.newInstance();

                                    // First load the shared instance info in the map
                                    ((ILoadableObject) eObj).loadJSON(mapElem, scope);
                                }
                                else {
                                    try {
                                        Class<?> elemClass = classMap.get(elem.getString("type"));

                                        System.out.printf("class type:%s\n", elemClass.getName());
                                        eObj = elemClass.newInstance();
                                    }
                                    catch(Exception e) {
                                        Log.e(TAG, "Check Syntax on Element: " + key);
                                        Log.e(TAG, "Probable missing 'type': " + e);
                                        System.exit(1);
                                    }
                                }

                                // Load the base instance data whether or not it has a mapped
                                // link.  There may or may not be any data here. But note that
                                // it will override any info in the map instance data.

                                ((ILoadableObject) eObj).loadJSON(elem, scope);

                                // Initialize graph mode types
                                //
                                if(eObj instanceof IScriptable) {

                                    // Associate the node with its Map name
                                    // This overrides any names assigned in the subtype spec
                                    ((IScriptable) eObj).setName(key);

                                    // Add the new object to the scope - if it is a scoped object
                                    // it may just be a data source etc.

                                    if (scope != null) {
                                        scope.put(key, (IScriptable) eObj);
                                        Log.i(TAG, "Adding to scope: " + key);
                                    }
                                }
                                else {
                                    field_Map.put(key, eObj);
                                }
                            }
                        }
                    }

                    else {
                        // Treat arrays uniquely - arrays can be of a single class or a mixture
                        // of subclasses of the array component type.  Mixtures must be identified
                        // by "type" fields within the array element json declaration.
                        //
                        if (fieldClass.isArray()) {

                            // Get the array on the 1st dimension for the field (attribute)
                            nArr = jsonObj.getJSONArray(fieldName);

                            Class<?> elemClass = fieldClass.getComponentType();

                            Object field_Array = Array.newInstance(elemClass, nArr.length());

                            field.set(self, parseArray(jsonObj, self, classMap, scope, nArr, elemClass, field_Array));


//                            nArr = jsonObj.getJSONArray(fieldName);
//
//                            Class<?> elemClass = fieldClass.getComponentType();
//
//                            Object field_Array = Array.newInstance(elemClass, nArr.length());
//
//                            for (int i = 0; i < nArr.length(); i++) {
//                                try {
//                                    Object eObj;
//
//                                    if (elemClass.equals(String.class)) {
//                                        eObj = nArr.getString(i);
//                                    }
//                                    else if (elemClass.equals(int.class)) {
//                                        eObj = nArr.getInt(i);
//                                    }
//                                    else {
//                                        nJsonObj = nArr.getJSONObject(i);
//
//                                        // If the element has a type field then assume it is a subtype
//                                        // of the array component type and instantiate it by type
//                                        if (nJsonObj.has("type")) {
//                                            Class<?> subClass = classMap.get(nJsonObj.getString("type"));
//
//                                            System.out.printf("class type:%s\n", subClass.getName());
//                                            eObj = subClass.newInstance();
//                                        }
//
//                                        // Otherwise use the array component type by default.
//                                        else {
//                                            System.out.printf("class type:%s\n", elemClass.getName());
//                                            eObj = elemClass.newInstance();
//                                        }
//
//                                        ((ILoadableObject) eObj).loadJSON(nJsonObj, scope);
//                                    }
//
//                                    Array.set(field_Array, i, eObj);
//
//                                } catch (NullPointerException e) {
//                                    e.printStackTrace();
//                                    Log.e(TAG, "Null Object in :" + nJsonObj);
//                                }
//                            }
//
//                            field.set(self, field_Array);
                        }

                        // otherwise assume it is a discrete object of ILoadable type
                        else {
                            try {
                                System.out.printf("class type:%s\n", fieldName);
                                field_obj = fieldClass.newInstance();

                                nJsonObj = jsonObj.getJSONObject(fieldName);

                                ((ILoadableObject)field_obj).loadJSON(nJsonObj, scope);

                                field.set(self, field_obj);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                // Just ignore items where there is no JSON data
                e.printStackTrace();
                Log.e(TAG, "ERROR: parseSelf:" + e);
                System.exit(1);

            } catch (InstantiationException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR: parseSelf:" + e);
                System.exit(1);

            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR: parseSelf:" + e);
                System.exit(1);

            }
        }
    }


    /**
     *
     *   Support parsing multidimesnsional arrays.
     *
     * @param jsonObj
     * @param self
     * @param classMap
     * @param scope
     * @param nArr
     * @param elemClass
     * @param field_Array
     * @return
     * @throws JSONException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    static Object parseArray(JSONObject jsonObj, Object self, HashMap<String, Class> classMap, IScope scope, JSONArray  nArr, Class<?> elemClass, Object field_Array ) throws JSONException, IllegalAccessException, InstantiationException {

        JSONObject nJsonObj = null;
        JSONArray  subArr;

        try {
            for (int i = 0; i < nArr.length(); i++) {
                try {
                    Object eObj;

                    if (elemClass.isArray()) {
                        subArr = nArr.getJSONArray(i);

                        Class<?> subElemClass = elemClass.getComponentType();
                        Object subField_Array = Array.newInstance(subElemClass, subArr.length());

                        eObj = parseArray(jsonObj, self, classMap, scope, subArr, subElemClass, subField_Array);
                    } else if (elemClass.equals(String.class)) {
                        eObj = nArr.getString(i);
                    } else if (elemClass.equals(int.class)) {
                        eObj = nArr.getInt(i);
                    } else {
                        nJsonObj = nArr.getJSONObject(i);

                        // If the element has a type field then assume it is a subtype
                        // of the array component type and instantiate it by type
                        if (nJsonObj.has("type")) {
                            Class<?> subClass = classMap.get(nJsonObj.getString("type"));

                            System.out.printf("class type:%s\n", subClass.getName());
                            eObj = subClass.newInstance();
                        }

                        // Otherwise use the array component type by default.
                        else {
                            System.out.printf("class type:%s\n", elemClass.getName());
                            eObj = elemClass.newInstance();
                        }

                        ((ILoadableObject) eObj).loadJSON(nJsonObj, scope);
                    }

                    Array.set(field_Array, i, eObj);

                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Null Object in :" + nJsonObj);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Json Array Format Error: " + e);
            System.exit(1);
        }

        return field_Array;
    }

}