package cmu.xprize.comp_debug;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

import static cmu.xprize.comp_debug.CD_CONST.STATE_CURRENT;
import static cmu.xprize.comp_debug.CD_CONST.STATE_EASIER;
import static cmu.xprize.comp_debug.CD_CONST.STATE_ERROR;
import static cmu.xprize.comp_debug.CD_CONST.STATE_HARDER;
import static cmu.xprize.comp_debug.CD_CONST.STATE_NEXT;
import static cmu.xprize.comp_debug.CD_CONST.STATE_NORMAL;
import static cmu.xprize.comp_debug.CD_CONST.STATE_NULL;

public class CDebugButton extends ImageButton {

    private int     gridPosition;
    private String  buttonState = STATE_NULL;

    public CDebugButton(Context context) {
        super(context);

        init();
    }

    public CDebugButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CDebugButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {

        setImageResource(R.drawable.debugbutton);
    }

    private void resetState() {

        buttonState = STATE_CURRENT;
    }

    public void setState(String newState) {

        buttonState = newState;
        refreshDrawableState();
    }

    public void setGridPosition(int _gridPosition) {
        gridPosition = _gridPosition;
    }
    public int getGridPosition() {
        return gridPosition;
    }


    /**
     * @param extraSpace
     * @return
     */
    @Override
    public int[] onCreateDrawableState(int extraSpace) {

        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if(buttonState != null) {
            switch (buttonState) {

                case STATE_NORMAL:
                    mergeDrawableStates(drawableState, CD_CONST.SKILLS_NORMAL);
                    break;

                case STATE_CURRENT:
                    mergeDrawableStates(drawableState, CD_CONST.SKILLS_CURRENT);
                    break;

                case STATE_NEXT:
                    mergeDrawableStates(drawableState, CD_CONST.SKILLS_NEXT);
                    break;

                case STATE_HARDER:
                    mergeDrawableStates(drawableState, CD_CONST.SKILLS_HARDER);
                    break;

                case STATE_EASIER:
                    mergeDrawableStates(drawableState, CD_CONST.SKILLS_EASIER);
                    break;

                case STATE_ERROR:
                    mergeDrawableStates(drawableState, CD_CONST.SKILLS_ERROR);
                    break;

                default:
                    mergeDrawableStates(drawableState, CD_CONST.SKILLS_NULL);
                    break;
            }
        }

        return drawableState;
    }

}
