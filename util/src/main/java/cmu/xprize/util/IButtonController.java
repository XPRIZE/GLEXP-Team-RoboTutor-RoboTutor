package cmu.xprize.util;

public interface IButtonController {

    public void doDebugLaunchAction(String debugTutor);
    public void doButtonBehavior(String buttonid);
    public void doAskButtonAction(String actionid);

    public void doLaunch(String intent, String intentData, String dataSource);
}
