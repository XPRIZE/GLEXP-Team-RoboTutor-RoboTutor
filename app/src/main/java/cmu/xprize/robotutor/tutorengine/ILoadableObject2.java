package cmu.xprize.robotutor.tutorengine;

import org.json.JSONObject;

import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.util.ILoadableObject;


public interface ILoadableObject2 extends ILoadableObject {
    public void loadJSON(JSONObject jsonObj, IScope2 scope);
}
