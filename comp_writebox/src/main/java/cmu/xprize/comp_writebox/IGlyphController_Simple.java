package cmu.xprize.comp_writebox;


public interface IGlyphController_Simple {

    // KEEP
    void setExpectedChar(String sample);

    // MATHFIX_WRITE might not need
    void post(String command);
    void post(String command, long delay);
    void post(String command, String target);
    void post(String command, String target, long delay);
    void post(String command, String target, String item);
    void post(String command, String target, String item, long delay);

}
