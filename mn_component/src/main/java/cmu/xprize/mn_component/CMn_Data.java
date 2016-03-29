package cmu.xprize.mn_component;

import org.json.JSONObject;

import cmu.xprize.util.CClassMap;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

/**
 * Created by Kevin on 2/29/2016.
 */
public class CMn_Data implements ILoadableObject{

    // json loadable
    public String        options;
    public int[]         dataset;
    public int           maxvalue;
    public String        mn_index;


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
    }
}
