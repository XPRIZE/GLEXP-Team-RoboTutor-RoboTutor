package cmu.xprize.comp_ask;


import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

public class CAskElement implements ILoadableObject {

    // json loadable
    public String        datatype         = null;   // "text" OR "image"
    public String        resource         = null;   // resource ID OR text literal
    public String        componentID      = null;   // The target UI element in Layout

    public String        behavior         = null;   // The button behavior
    public String        prompt           = null;   // The mp3 help prompt

    //************ Serialization



    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        JSON_Helper.parseSelf(jsonObj, this, cmu.xprize.util.CClassMap.classMap, scope);
    }


}
