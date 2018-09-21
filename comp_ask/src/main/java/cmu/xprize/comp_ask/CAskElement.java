package cmu.xprize.comp_ask;


import org.json.JSONObject;

import java.util.HashMap;

import cmu.xprize.util.CClassMap;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

public class CAskElement implements ILoadableObject {

    // json loadable
    public String                   componentID      = null;   // The target UI element in Layout

    public String                   behavior         = null;   // The button behavior
    public String                   prompt           = null;   // ask prompt mp3
    public String                   help             = null;   // button description mp3

    //************ Serialization



    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
    }


}
