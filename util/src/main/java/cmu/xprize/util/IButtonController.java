package cmu.xprize.util;

public interface IButtonController {

    public void doDebugLaunchAction(String debugTutor);
    public void doDebugTagLaunchAction(String tag);

    public void doButtonBehavior(String buttonid);
    public void doTaggedButtonBehavior(String tag);
    public void doAskButtonAction(String actionid);

    public void doLaunch(String intent, String intentData, String dataSource, String tutorId); // WARRIOR_MAN
}
