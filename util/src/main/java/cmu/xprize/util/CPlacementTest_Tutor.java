package cmu.xprize.util;

import org.json.JSONObject;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 4/12/18.
 */

public class CPlacementTest_Tutor implements ILoadableObject {


    public int l;

    // json loadable
    public String tutor;
    public String level;
    public String fail;
    public String pass;


    //************ Serialization


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);

        l = Integer.parseInt(level);

    }

}
