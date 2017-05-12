package edu.cmu.xprize.listener;

public interface IPhonemeRules {

    public String [][][] getRules();

    public final int LEFT_PART = 0;
    public final int MATCH_PART = 1;
    public final int RIGHT_PART = 2;
    public final int OUT_PART = 3;
}
