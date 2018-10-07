package cmu.xprize.comp_writing;

import android.util.Log;

import org.json.JSONObject;

import cmu.xprize.util.CClassMap;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

public class CWr_Data implements ILoadableObject{

    //json loadable
    public boolean          isStory = false;
    public String           storyName = "";
    public String           stimulus        = "";
    public String[]         audioStimulus   = null;
    public String           answer          = "";



    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);

        if(answer.equals("")) {
            answer = stimulus;
        }
    }

}

