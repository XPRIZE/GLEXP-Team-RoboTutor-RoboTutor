package cmu.xprize.comp_spelling;

import org.json.JSONObject;

import java.util.List;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

/**
 * Automatically generated w/ script by Kevin DeLand.
 */

public class CSpelling_Data implements ILoadableObject{

    // json loadable
    public String level;
    public String task;
    public String layout;
    public String image;
    public String[] word;
    public String sound;

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
    }
}
