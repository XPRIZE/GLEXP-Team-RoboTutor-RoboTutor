package cmu.xprize.ak_component;

import org.json.JSONObject;

import cmu.xprize.util.*;

/**
 * Created by jacky on 2016/7/6.
 */

public class CAk_Data implements ILoadableObject{


    //json loadable
    public int              choiceNum       = 2;
    public String[]         choices         = null;
    public String           answer          = "";
    public String           originalChoices = "";
    public CAkPlayer.Lane   answerLane      = null;
    public String           aboveString     = "";
    public String           belowString     = "";

    public String           level;
    public String           task;
//    public int[] dataset = null;


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, cmu.xprize.util.CClassMap.classMap, scope);

        if(choices.length > 3 || choices.length < 1)
            throw new IllegalArgumentException("Number of choices should be 1~3.");

        choiceNum = choices.length;

        if(answer.equals("LEFT"))
            answerLane = CAkPlayer.Lane.LEFT;
        else if(answer.equals("MID"))
            answerLane = CAkPlayer.Lane.MID;
        else if(answer.equals("RIGHT"))
            answerLane = CAkPlayer.Lane.RIGHT;
        else
            throw new IllegalArgumentException("Answer should be either \"LEFT\", \"MID\" or \"RIGHT\".");


    }

}
