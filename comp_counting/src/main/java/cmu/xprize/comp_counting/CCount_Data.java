package cmu.xprize.comp_counting;

import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

/**
 * Created by kevindeland on 10/23/17.
 */

public class CCount_Data implements ILoadableObject {

    // json loadable
    public String level;
    public String task;
    public String layout;
    public int[] dataset;


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
    }
}
