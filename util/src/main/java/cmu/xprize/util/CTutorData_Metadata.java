package cmu.xprize.util;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import cmu.xprize.common.R;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 5/18/18.
 */

public class CTutorData_Metadata {


    public static TCONST.Thumb getThumbImage(CAt_Data tutor) {


        Log.d("CHUNT", "tutor_desc = " + tutor.tutor_desc);
        // tutortype is first token... e.g. "story.hear" --> "story"
        String[] tutorDesc = tutor.tutor_desc.split("\\.");
        if (tutorDesc.length == 0) {
            return null;
        }
        Log.d("CHUNT", "tutorDesc = " + tutorDesc + ", " + tutorDesc.length);



        String tutorType = tutorDesc[0];
        Log.d("CHUNT", "tutorType = " + tutorType);

        switch (tutorType) {
            case "akira":
                return TCONST.Thumb.AKIRA;

            case "bpop":
                switch (tutor.tutor_desc) {
                    case "bpop.num":
                    case "bpop.addsub":
                        return TCONST.Thumb.BPOP_NUM;

                    case "bpop.gl":
                        return TCONST.Thumb.GL;

                    case "bpop.mn":
                        return TCONST.Thumb.MN;

                    // bpop.ltr
                    default:
                        return TCONST.Thumb.BPOP_LTR;
                }

            case "countingx":

                String startingNumber = tutor.tutor_id.split("[:_]")[1];

                switch (startingNumber) {
                    case "1":
                        return TCONST.Thumb.CX_1;

                    case "10":
                        return TCONST.Thumb.CX_10;

                    case "100":
                        return TCONST.Thumb.CX_100;

                    default:
                        return TCONST.Thumb.CX_1;
                }


            case "math":
                // only one
                return TCONST.Thumb.MATH;

            case "numberscale":
                // only one
                return TCONST.Thumb.NUMSCALE;


            case "story":
                // story or song
                if(tutor.tutor_id.toLowerCase().contains("song")) {
                    return TCONST.Thumb.SONG;
                }
                else {
                    return TCONST.Thumb.STORY;
                }


            case "write":
                // only one... for now
                return TCONST.Thumb.WRITE;


            default:
                return null;

        }

    }

    public static ArrayList<String> parseNameIntoLabels(CAt_Data tutor) {

        Log.d("CHUNT", "tutor_desc = " + tutor.tutor_desc);
        // tutortype is first token... e.g. "story.hear" --> "story"
        String[] tutorDesc = tutor.tutor_desc.split("\\.");
        if (tutorDesc.length == 0) {
            return null;
        }
        Log.d("CHUNT", "tutorDesc = " + tutorDesc + ", " + tutorDesc.length);

        int[] tutor_CONST;


        String tutorType = tutorDesc[0];
        Log.d("CHUNT", "tutorType = " + tutorType);


        ArrayList<String> result = new ArrayList<>();
        result.add("<b>" + tutor.tutor_id + "</b>"); // add ID for each one

        switch (tutorType) {
            case "akira":
                break;

            case "bpop":
                // CHUNT this is the most complicated...
                break;

            case "countingx":
                break;


            case "math":
                String[] splitMe = tutor.tutor_id.split(":");
                String[] secondPart = splitMe[1].split("\\.");

                String[] thirdPart = secondPart[3].split("-");

                StringBuilder descriptor = new StringBuilder();
                descriptor.append( thirdPart[0].equals("SUB") ? "<b>Subtract</b> " : "<b>Add</b> ");
                descriptor.append(String.format("values between <b>%s</b> and <b>%s</b>", secondPart[0], secondPart[2]));
                result.add(descriptor.toString());
                break;

            case "numberscale":
                break;


            case "story":
                break;


            case "write":
                break;


            default:
                break;

        }

        return result;
    }
}
