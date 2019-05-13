package cmu.xprize.comp_debug;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.CTutorData_Metadata;
import cmu.xprize.util.TCONST;

import static cmu.xprize.comp_debug.CD_CONST.STATE_CURRENT;
import static cmu.xprize.comp_debug.CD_CONST.STATE_EASIER;
import static cmu.xprize.comp_debug.CD_CONST.STATE_ERROR;
import static cmu.xprize.comp_debug.CD_CONST.STATE_HARDER;
import static cmu.xprize.comp_debug.CD_CONST.STATE_NEXT;
import static cmu.xprize.comp_debug.CD_CONST.STATE_NORMAL;
import static cmu.xprize.comp_debug.CD_CONST.STATE_NULL;
import static cmu.xprize.util.TCONST.Thumb.NOTHING;

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


                TCONST.Thumb thumbnail = CTutorData_Metadata.getThumbImage(tutorData); // NEW_THUMBS don't change this. I don't know what tutor_CONST even does.
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

                if (thumbnail == null) {
                    thumbnail = NOTHING;
                }
                switch (thumbnail) {
                    case AKIRA:
                        tutor_CONST = CD_CONST.TUTOR_AKIRA;
                        break;

                    case BPOP_LTR:
                        tutor_CONST = CD_CONST.TUTOR_BPOP_LTR;
                        break;

                    case BPOP_NUM:
                        tutor_CONST = CD_CONST.TUTOR_BPOP_NUM;
                        break;

                    case GL:
                        tutor_CONST = CD_CONST.TUTOR_COMPARE;
                        break;

                    case MN:
                        tutor_CONST = CD_CONST.TUTOR_MISSINGNO;
                        break;

                    case CX_1:
                        tutor_CONST = CD_CONST.TUTOR_COUNTINGX_1;
                        break;

                    case CX_10:
                        tutor_CONST = CD_CONST.TUTOR_COUNTINGX_10;
                        break;

                    case CX_100:
                        tutor_CONST = CD_CONST.TUTOR_COUNTINGX_100;
                        break;

                    case MATH:
                        tutor_CONST = CD_CONST.TUTOR_MATH;
                        break;

                    case NUMSCALE:
                        tutor_CONST = CD_CONST.TUTOR_NUMBERSCALE;
                        break;

                    case STORY_1:
                        tutor_CONST = CD_CONST.TUTOR_STORY_1;
                        break;

                    case STORY_2:
                        tutor_CONST = CD_CONST.TUTOR_STORY_2;
                        break;

                    case STORY_3:
                        tutor_CONST = CD_CONST.TUTOR_STORY_3;
                        break;

                    case STORY_4:
                        tutor_CONST = CD_CONST.TUTOR_STORY_4;
                        break;

                    case STORY_5:
                        tutor_CONST = CD_CONST.TUTOR_STORY_5;
                        break;

                    case STORY_NONSTORY:
                        tutor_CONST = CD_CONST.TUTOR_STORY_NONSTORY;
                        break;

                    case SONG:
                        tutor_CONST = CD_CONST.TUTOR_SONG;
                        break;

                    case WRITE:
                        tutor_CONST = CD_CONST.TUTOR_WRITE;
                        break;

                    case NUMCOMPARE:
                        tutor_CONST = CD_CONST.TUTOR_NUMCOMPARE;
                        break;

                    case PICMATCH:
                        tutor_CONST = CD_CONST.TUTOR_PICMATCH;
                        break;

                    case PLACEVALUE:
                        tutor_CONST = CD_CONST.TUTOR_PLACEVALUE;
                        break;

                    case BIGMATH:
                        tutor_CONST = CD_CONST.TUTOR_BIGMATH;
                        break;

                    case SPELLING:
                        tutor_CONST = CD_CONST.TUTOR_SPELLING;
                        break;

                    case NOTHING:
                    default:
                        tutor_CONST = CD_CONST.SKILLS_NORMAL;
                        break;

                }

                mergeDrawableStates(drawableState, tutor_CONST);
            } else {
                mergeDrawableStates(drawableState, CD_CONST.SKILLS_NORMAL);
            }

            boolean isCurrent = false;
            switch (buttonState) {
                case STATE_NORMAL:
                case STATE_NEXT:
                case STATE_HARDER:
                case STATE_EASIER:
                    this.setBackground(getResources().getDrawable(R.drawable.outline_normal));
                    this.setAlpha(0.3f); // adjust opacity to make selected tutors stand out
                    break;

                    // BOJACK 2 remove these
                case STATE_CURRENT:
                    isCurrent = true;
                    this.setBackground(getResources().getDrawable(R.drawable.outline_current));
                    this.setAlpha(1f); // adjust opacity to make selected tutors stand out
                    //mergeDrawableStates(drawableState, CD_CONST.SKILLS_CURRENT);
                    break;


                case STATE_ERROR:
                    //mergeDrawableStates(drawableState, CD_CONST.SKILLS_ERROR);
                    break;

                default:
//                    mergeDrawableStates(drawableState, CD_CONST.SKILLS_NULL);
                    this.setAlpha(0f);
                    break;
            }

            // map to special tutors for showcasing at XPRIZE Impact Summit 
            boolean SHOWCASE = false;
            if(tutorData != null && SHOWCASE) {
                switch (tutorData.tutor_id) {

                    // bubble pop matching sounds to numbers
                    case "bpop.num:1..4.by.1.asc.x2s.Ax.mc.9":

                        // math one-digit
                    case "countingx:1_10__it_1":
                    case "num.scale:17..27.off1.5__it_2":

                        // write missing letter (ha)...
                    case "write.missingLtr:lc.begin.ha.1":

                        // story
                    case "story.echo::story_1":
                        this.setBackground(getResources().getDrawable(R.drawable.outline_xprize));
                        this.setAlpha(0.5f); // adjust opacity to make selected tutors stand out


                        // story (with good audio)

                }
            }

            if (isCurrent) {
                this.setBackground(getResources().getDrawable(R.drawable.outline_current));
                this.setAlpha(1f); // adjust opacity to make selected tutors stand out

            }

        }

        return drawableState;
    }

    public void setTutorData(CAt_Data tutorData) {
        this.tutorData = tutorData;
    }
}
