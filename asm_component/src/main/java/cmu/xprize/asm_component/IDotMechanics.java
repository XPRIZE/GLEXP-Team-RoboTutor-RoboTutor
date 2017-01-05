package cmu.xprize.asm_component;

/**
 *
 */
public interface IDotMechanics {

    void preClickSetup();
    void next();
    void nextDigit();
    void handleClick();
    void correctOverheadText();
    void highlightOverheadOrResult(String whichToHighlight);
    int getCurRow();
    String getOperation();

}
