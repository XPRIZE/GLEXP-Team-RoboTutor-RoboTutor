package cmu.xprize.comp_counting2;

import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

/**
 * Created by kevindeland on 12/12/17.
 */

public class CCountX_Data implements ILoadableObject{

    // json loadable
    public String level;
    public String task;
    public String layout;
    public int[] dataset;
    public String[] tenPower;//"one","ten","hundred"
    public int difficulty;


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
    }
}
