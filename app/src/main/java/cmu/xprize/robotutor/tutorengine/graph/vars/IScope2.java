package cmu.xprize.robotutor.tutorengine.graph.vars;


import cmu.xprize.util.IScope;

public interface IScope2 extends IScope {
    public IScriptable2 mapSymbol(String symbolName) throws Exception;
    public void put(String key, IScriptable2 obj);
}
