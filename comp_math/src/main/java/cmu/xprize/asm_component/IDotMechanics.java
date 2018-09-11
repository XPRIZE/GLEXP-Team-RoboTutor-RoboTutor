package cmu.xprize.asm_component;

/**
 *
 */
public interface IDotMechanics {

    // set up?
    void preClickSetup();

    // just resets to the default state
    void next();

    // called by C_Component.nextDigit()
    void nextDigit();

    // I *think* this is where the magic of moving the dots happens
    // implemented by Base, Add, Subtract
    void handleClick();


    // looks like only used by Subtract/Borrow... might be deletable
    void correctOverheadText();

    // looks like only used by Subtract/Borrow...
    void highlightOverhead();


    // highlights the result.
    // only in Base
    void highlightResult();


}
