package cmu.xprize.util;

import org.json.JSONObject;


public interface ILoadableObject {
    public void loadJSON(JSONObject jsonObj, IScope scope);
}
