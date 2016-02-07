package cmu.xprize.robotutor.tutorengine;

import org.json.JSONObject;

import java.util.ArrayList;

import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;

public interface ILoadableObject {
    public void loadJSON(JSONObject jsonObj, TScope scope);
}
