package cmu.xprize.asm_component;

/**
 *
 */
public interface IDotMechanics {

    void preClickSetup();
    void next();
    void nextDigit();
    void handleClick();
    String getOperation();

}
