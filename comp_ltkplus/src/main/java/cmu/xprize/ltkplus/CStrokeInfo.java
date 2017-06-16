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

package cmu.xprize.ltkplus;

import org.json.JSONObject;

import cmu.xprize.util.CClassMap;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

public class CStrokeInfo implements ILoadableObject {

    private CStroke _stroke;
    private long    _time     = 0;
    private long    _duration = 0;

    // json loadable
    public float[] stroke;
    public long    time;
    public long    duration;

    // We need a zero arg constructor for creation during JSON load - Therefore this cannot be an
    // inner class
    //
    public CStrokeInfo() {
    }

    public CStrokeInfo(CStroke newStroke, long time) {
        _stroke = newStroke;
        _time = time;
    }


    public CStroke getStroke() {
        return _stroke;
    }

    public long getTime() {
        return _time;
    }

    public void normalizeTime(long baseTime) {
        _time = _time - baseTime;
    }

    public void setDuration(long duration) {
        _duration = duration;
    }
    public long getDuration() {
        return _duration;
    }



    //************ Serialization



    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);

        _time     = time;
        _duration = duration;
        _stroke   = new CStroke(stroke);
    }
}


