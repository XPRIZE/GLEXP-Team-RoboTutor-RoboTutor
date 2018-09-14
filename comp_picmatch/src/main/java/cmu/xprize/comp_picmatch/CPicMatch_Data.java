package cmu.xprize.comp_picmatch;

import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

/**
 * Automatically generated w/ script by Kevin DeLand.
 */

public class CPicMatch_Data implements ILoadableObject{

    // ALAN_HILL (1) here is where the data source is defined

    // json loadable
    public String level;
    public String task;
    public String layout;
    public String prompt;
    public String[] images;



    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
    }
}
