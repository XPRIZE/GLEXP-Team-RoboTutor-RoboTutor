package cmu.xprize.ak_component;

import org.json.JSONObject;

import cmu.xprize.util.*;

/**
 * Created by jacky on 2016/7/6.
 */

public class CAk_Data implements ILoadableObject{

    public int choice_num = 2;
    public String[] choices = null;
    public String anwser = "";

    public int[] dataset = null;

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        JSON_Helper.parseSelf(jsonObj, this, cmu.xprize.util.CClassMap.classMap, scope);
    }

}
