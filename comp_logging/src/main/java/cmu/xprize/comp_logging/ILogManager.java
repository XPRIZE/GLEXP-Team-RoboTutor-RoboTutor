package cmu.xprize.comp_logging;

public interface ILogManager {

    /**
     * Transfer logs from one path to another. Separation of hot logs from ready logs prevents
     * RoboTransfer from transferring a log while it is being written.
     *
     * @param hotPath
     * @param readyPath
     */
    public void transferHotLogs(String hotPath, String readyPath);

    public void startLogging(String logPath, String logFileName);
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

    public void postBattery(String Tag, String percent, String chargeType);

    public void postPacket(String packet);
}
