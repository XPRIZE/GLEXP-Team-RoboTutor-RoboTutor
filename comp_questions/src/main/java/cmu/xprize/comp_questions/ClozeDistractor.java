package cmu.xprize.comp_questions;

import org.json.JSONException;
import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

public class ClozeDistractor implements ILoadableObject{
    // json loadable
    public String[] ungrammatical;
    public String[] nonsensical;
    public String[] plausible;

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        try{
            JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
        } catch(Exception e){
            this.ungrammatical = new String[]{"a","b","c"};
            this.nonsensical = new String[]{"1","2","3"};
            this.plausible = new String[]{"x","y","z"};
        }

    }
}