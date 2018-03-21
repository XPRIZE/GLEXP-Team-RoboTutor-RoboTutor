package cmu.xprize.comp_numberscale;

import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

/**
 * Automatically generated w/ script by Kevin DeLand.
 */

public class CNumberScale_Data implements ILoadableObject{

    // json loadable
    // insert C_Data fields... ${C_Data_fields}
    public String level;
    public String task;
    public String layout;
    public String offset;
    public String max;
    public String max_taps;
    public String start;
    //public int[] dataset;



    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, cmu.xprize.util.CClassMap.classMap, scope);
    }
}
