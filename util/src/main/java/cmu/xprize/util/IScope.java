package cmu.xprize.util;


public interface IScope {

    public IScriptable mapSymbol(String symbolName) throws Exception;
    public String parseTemplate(String source);
    public void put(String key, IScriptable obj);
}
