package cmu.xprize.robotutor.tutorengine;

public interface ITutorNavigator {

    public void initTutorContainer(ITutorSceneImpl rootScene);
    public CSceneGraph getAnimator();

    public void questionStart();
    public void questionComplete();
    public void goBackScene();
    public void goNextScene();
    public void goToNamedScene(String name);

    public void goToScene(String tarScene);
    public void gotoNextScene();

    public void onButtonNext();
}
