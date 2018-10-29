package cmu.xprize.util;

public interface IButtonController {

    public void doDebugLaunchAction(String debugTutor);
    void doDebugTagLaunchAction(String tag);

    public void doButtonBehavior(String buttonid);
    public void doAskButtonAction(String actionid);

    public void doLaunch(String intent, String intentData, String dataSource, String tutorId, String matrix); // WARRIOR_MAN
}
