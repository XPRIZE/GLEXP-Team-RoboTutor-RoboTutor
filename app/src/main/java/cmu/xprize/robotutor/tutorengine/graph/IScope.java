package cmu.xprize.robotutor.tutorengine.graph;

import java.util.ArrayList;

import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;

public interface IScope {
    public IScriptable mapSymbol(String symbolName) throws Exception;
}
