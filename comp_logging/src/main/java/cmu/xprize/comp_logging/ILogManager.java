package cmu.xprize.comp_logging;

public interface ILogManager {

    public void startLogging(String logPath);
    public void stopLogging();

    public void postTutorState(String Tag, String Msg);

    public void postEvent_V(String Tag, String Msg);

    public void postEvent_D(String Tag, String Msg);

    public void postEvent_I(String Tag, String Msg);

    public void postEvent_W(String Tag, String Msg);

    public void postEvent_E(String Tag, String Msg);

    public void postEvent_A(String Tag, String Msg);

    public void postDateTimeStamp(String Tag, String Msg);

    public void post(String command);

    public void postError(String Tag, String Msg);
    public void postError(String Tag, String Msg, Exception e);

    public void postPacket(String packet);
}
