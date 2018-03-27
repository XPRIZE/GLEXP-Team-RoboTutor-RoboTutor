package cmu.xprize.comp_logging;

public interface IPerfLogManager extends ILogManager {

    public void postPerformanceLog(PerformanceLogItem event);

    public void postPerformanceLogWithoutContext(PerformanceLogItem event);
}
