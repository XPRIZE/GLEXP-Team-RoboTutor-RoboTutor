package cmu.xprize.util;

public interface ILogManager {

    public void post(String command);

    public void postSystemEvent(String Tag, String Msg);
    public void postSystemTimeStamp(String Tag);

    public void postEvent(String Tag, String Msg);
    public void postTimeStamp(String Tag);
    
    public void postError(String Tag, String Msg);
    public void postError(String Tag, String Msg, Exception e);
}
