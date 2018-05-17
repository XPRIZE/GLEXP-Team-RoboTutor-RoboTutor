package cmu.xprize.comp_debug;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

import cmu.xprize.util.CAt_Data;

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
    private CAt_Data tutorData;

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
    }

    public void setGridPosition(int _gridPosition) {
        Log.d("BOJACK", "setting gridPosition=" + _gridPosition);
        Log.wtf("BOJACK", this.hashCode() + " -- setGridPosition -- " + gridPosition + " -> " +_gridPosition);
        gridPosition = _gridPosition;
    }
    public int getGridPosition() {
        Log.d("BOJACK", "getGridPosition=" + gridPosition);
        Log.wtf("BOJACK", this.hashCode() + " -- getGridPosition -- " + gridPosition);
        return gridPosition;
    }


    /**
     * @param extraSpace
     * @return
     */
    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        if(tutorData != null) {
            Log.wtf("BOJACK", this.hashCode() + " -- onCreateDrawableState -- " + gridPosition);
            Log.d("BOJACK", "index=" + gridPosition + "; " + tutorData.tutor_id + "; " + tutorData.tutor_desc);
        }


        Log.d("BOJACK", "CDebugButton.onCreateDrawableState()");

        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if(buttonState != null) {

            if(tutorData != null) {

                Log.d("BOJACK", "tutor_desc = " + tutorData.tutor_desc);
                // tutortype is first token... e.g. "story.hear" --> "story"
                String[] tutorDesc = tutorData.tutor_desc.split("\\.");
                if (tutorDesc.length == 0) {
                    return null;
                }
                Log.d("BOJACK", "tutorDesc = " + tutorDesc + ", " + tutorDesc.length);

                int[] tutor_CONST;


                String tutorType = tutorDesc[0];
                Log.d("BOJACK", "tutorType = " + tutorType);

                switch (tutorType) {
                    case "akira":
                        tutor_CONST = CD_CONST.TUTOR_AKIRA;
                        break;

                    case "bpop":
                        tutor_CONST = CD_CONST.TUTOR_BPOP;
                        break;

                    case "countingx":
                        tutor_CONST = CD_CONST.TUTOR_COUNTINGX;
                        break;

                    case "math":
                        tutor_CONST = CD_CONST.TUTOR_MATH;
                        break;

                    case "numberscale":
                        tutor_CONST = CD_CONST.TUTOR_NUMBERSCALE;
                        break;

                    case "story":
                        tutor_CONST = CD_CONST.TUTOR_STORY;
                        break;

                    case "write":
                        tutor_CONST = CD_CONST.TUTOR_WRITE;
                        break;

                    default:
                        tutor_CONST = CD_CONST.SKILLS_NORMAL;
                        break;

                }

                mergeDrawableStates(drawableState, tutor_CONST);
            } else {
                mergeDrawableStates(drawableState, CD_CONST.SKILLS_NORMAL);
            }

            switch (buttonState) {
                case STATE_NORMAL:
                    this.setBackground(getResources().getDrawable(R.drawable.outline_normal, null));
                    this.setAlpha(0.3f); // adjust opacity to make selected tutors stand out
                    break;

                    // BOJACK 2 remove these
                case STATE_CURRENT:
                    this.setBackground(getResources().getDrawable(R.drawable.outline_current, null));
                    this.setAlpha(1f); // adjust opacity to make selected tutors stand out
                    //mergeDrawableStates(drawableState, CD_CONST.SKILLS_CURRENT);
                    break;

                case STATE_NEXT:
                    this.setBackground(getResources().getDrawable(R.drawable.outline_next, null));
                    this.setAlpha(1f); // adjust opacity to make selected tutors stand out
                    //mergeDrawableStates(drawableState, CD_CONST.SKILLS_NEXT);
                    break;

                case STATE_HARDER:
                    this.setBackground(getResources().getDrawable(R.drawable.outline_harder, null));
                    this.setAlpha(1f); // adjust opacity to make selected tutors stand out
                    //mergeDrawableStates(drawableState, CD_CONST.SKILLS_HARDER);
                    break;

                case STATE_EASIER:
                    this.setBackground(getResources().getDrawable(R.drawable.outline_easier, null));
                    this.setAlpha(1f); // adjust opacity to make selected tutors stand out
                    //mergeDrawableStates(drawableState, CD_CONST.SKILLS_EASIER);
                    break;

                case STATE_ERROR:
                    //mergeDrawableStates(drawableState, CD_CONST.SKILLS_ERROR);
                    break;

                default:
//                    mergeDrawableStates(drawableState, CD_CONST.SKILLS_NULL);
                    this.setAlpha(0f);
                    break;
            }
        }

        return drawableState;
    }

    public void setTutorData(CAt_Data tutorData) {
        this.tutorData = tutorData;
    }
}
