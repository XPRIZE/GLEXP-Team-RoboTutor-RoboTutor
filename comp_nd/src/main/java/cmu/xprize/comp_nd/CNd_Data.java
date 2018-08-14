package cmu.xprize.comp_nd;

import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

/**
 * Automatically generated w/ script by Kevin DeLand.
 */

public class CNd_Data implements ILoadableObject{

    // json loadable
    public String level;
    public String task;
    public String layout;
    public int[] dataset;
    public boolean isWorkedExample;


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
    }
}
