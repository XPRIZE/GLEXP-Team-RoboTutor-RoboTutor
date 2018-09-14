package cmu.xprize.comp_questions;

import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;


/**
 * uhq: Class used to parse the mcq.json data for cloze word options
 */
public class ClozeQuestion implements ILoadableObject{
    // json loadable
    public ClozeDistractor distractor;
    public String       punctuation;
    public String       sentence;
    public String       stem;
    public String       target;

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);

    }
}
