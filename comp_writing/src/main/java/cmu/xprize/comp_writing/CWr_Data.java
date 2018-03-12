package cmu.xprize.comp_writing;

import android.util.Log;

import org.json.JSONObject;

import cmu.xprize.util.CClassMap;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

public class CWr_Data implements ILoadableObject{

    //json loadable
    public String           stimulus        = "";
    public String           audioStimulus   = "";
    public String           answer          = "";


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        Log.d("tadpolr", "loadJSON called");
        Log.d("tadpolr", jsonObj.toString());
        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);

        if(audioStimulus == "") {
            audioStimulus = stimulus;
        }
        if(answer == "") {
            answer = stimulus;
        }
    }

}

//    @Override
//    public void loadJSON(JSONObject jsonObj, IScope scope) {
//        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
//
//        if(choices.length > 3 || choices.length < 1)
//            throw new IllegalArgumentException("Number of choices should be 1~3.");
//
//        choiceNum = choices.length;
//
//        if(answer.equals("LEFT"))
//            answerLane = CAkPlayer.Lane.LEFT;
//        else if(answer.equals("MID"))
//            answerLane = CAkPlayer.Lane.MID;
//        else if(answer.equals("RIGHT"))
//            answerLane = CAkPlayer.Lane.RIGHT;
//        else
//            throw new IllegalArgumentException("Answer should be either \"LEFT\", \"MID\" or \"RIGHT\".");
//
//
//    }

