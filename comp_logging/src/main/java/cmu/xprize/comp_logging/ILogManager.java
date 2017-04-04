package cmu.xprize.comp_logging;

public interface ILogManager {

    public void startLogging(String logPath);
    public void stopLogging();

    public void postSystemEvent(String Tag, String Msg);
    public void postSystemTimeStamp(String Tag);

    public void postEvent(String Tag, String Msg);
    public void postTimeStamp(String Tag);

    public void post(String command);

    public void postError(String Tag, String Msg);
    public void postError(String Tag, String Msg, Exception e);

    public void postPacket(String packet);
}
