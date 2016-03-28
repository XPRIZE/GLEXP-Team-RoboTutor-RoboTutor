package cmu.xprize.rt_component;

public interface IVManListener {

    public void publishTargetWord(String word);
    public void publishTargetWordIndex(int index);
    public void publishTargetSentence(String sentence);

}
