package cmu.xprize.asm_component;

/**
 *
 */
public interface IDotMechanics {

    void preClickSetup();
    void handleClick();
    String getOperation();
    void reset();

}
