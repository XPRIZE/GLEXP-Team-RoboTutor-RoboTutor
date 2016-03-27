package cmu.xprize.robotutor.tutorengine.graph.vars;


import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.graph.type_timer;
import cmu.xprize.util.IScope;

public interface IScope2 extends IScope {

    public CTutor       tutor();
    public String       tutorName();
    public IScriptable2 mapSymbol(String symbolName) throws Exception;
    public void         put(String key, IScriptable2 obj);

}
