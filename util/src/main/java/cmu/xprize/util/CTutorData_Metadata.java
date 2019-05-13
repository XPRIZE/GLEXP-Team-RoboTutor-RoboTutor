package cmu.xprize.util;

import android.content.Context;
import android.text.style.LocaleSpan;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import cmu.xprize.common.R;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 5/18/18.
 */

public class CTutorData_Metadata {

    /**
     * NEW_THUMBS why can't this return a string instead?
     * @param tutor
     * @return
     */
    public static TCONST.Thumb getThumbImage(CAt_Data tutor) {


        Log.d("CHUNT", "tutor_desc = " + tutor.tutor_desc);
        // tutortype is first token... e.g. "story.hear" --> "story"
        String[] tutorDesc = tutor.tutor_desc.split("\\.");
        if (tutorDesc.length == 0) {
            return TCONST.Thumb.NOTHING;
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

                String storyPrefix = tutor.tutor_id.split(":")[2];
                // story or song
                if(storyPrefix.toLowerCase().contains("song") || storyPrefix.toLowerCase().contains("tune")) {
                    return TCONST.Thumb.SONG;
                }
                else if(storyPrefix.startsWith("story")){
                    String storyNum = storyPrefix.split("_")[1];
                    int storyNumInt = Integer.parseInt(storyNum);

                    switch (storyNumInt % 5) {
                        case 0:
                            return TCONST.Thumb.STORY_5;

                        case 1:
                            return TCONST.Thumb.STORY_1;

                        case 2:
                            return TCONST.Thumb.STORY_2;

                        case 3:
                            return TCONST.Thumb.STORY_3;

                        case 4:
                            return TCONST.Thumb.STORY_4;
                    }

                } else {
                    return TCONST.Thumb.STORY_NONSTORY;
                }


            case "write":
                // only one... for now
                return TCONST.Thumb.WRITE;


            // Added Tutors Code Drop 2
            case "picmatch":
                return TCONST.Thumb.PICMATCH;

            case "placevalue":
            case "place":
                return TCONST.Thumb.PLACEVALUE;

            case "numcompare":
                return TCONST.Thumb.NUMCOMPARE;

            case "spelling":
                return TCONST.Thumb.SPELLING;

            case "bigmath":
                return TCONST.Thumb.BIGMATH;



            default:
                return TCONST.Thumb.NOTHING;

        }

    }

    /**
     * Return filename to a thumb image.
     *
     * @param tutor
     * @return
     */
    public static String getThumbName(CAt_Data tutor) {
        // NEW_THUMBS (if returns null... then use the "getThumbImage" method")

        // check story
        if (tutor.tutor_desc.startsWith("story")) {

            StringBuilder builder = new StringBuilder("thumb_");
            String suffix = null;

            switch(tutor.tutor_desc) {

                case "story.hear":
                    suffix = "_hear";
                    break;

                case "story.echo":
                case "story.read":
                case "story.hide":
                case "story.reveal":
                case "story.parrot":
                    suffix = "_read";
                    break;

                case "story.gen.hide":
                case "story.clo.hear":
                case "story.pic.hear":
                case "story.gen.hear":
                    suffix = "_comp";
                    break;
            }

            String storyName = tutor.tutor_id.split("::")[1];
            builder.append(storyName)
                    .append(suffix)
                    .append(".png");

            return builder.toString();
        }


        switch(tutor.tutor_desc) {
            case "numcompare":
                return "thumb_number_discrimination.png";

            case "picmatch":
                return "thumb_picture_matching.png";

            case "place.value":
            case "placevalue":
                return "thumb_place_value.png";

            case "spelling":
                return "thumb_spelling_tutor.png";

            case "write.sen.corr.ltr":
            case "write.sen.corr.wrd":
            case "write.sen.corr.sen":
            case "write.sen.copy.ltr":
            case "write.sen.copy.wrd":
            case "write.sen.copy.sen":
                return "thumb_sentence_writing.png";

            case "bigmath":
                return "thumb_bigmath_1d.png";

        }

        return null;
    }

    /**
     * Given a tutor, return a list of Strings that describes the tutor.
     * @param tutor
     * @return
     */
    public static ArrayList<String> parseNameIntoLabels(CAt_Data tutor) {

        // OPEN_SOURCE FIXME
        // write.ltr.uc.trc:A..D_asc √√√
        // bpop.ltr.lc:A..D.asc.show.mc --> "null A to Z"
        // bpop.wrd:m2M.noShow.rise --> nothing
        // bpop.wrd:dolch_preprimer.noShow.mc --> nothing
        //
        // write.wrd:phon.m2M √√√

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

        try {


            switch (tutorType) {
                case "akira":
                    result.add("<b>Akira</b>");

                    result = processAkiraTutorId(tutor, result);
                    break;

                case "bpop":
                    result = processBubblePopTutorId(tutor, result);

                    break;

                case "countingx":
                    result.add("<b>Tap to Count</b>");
                    String[] countingSplit = tutor.tutor_id.split("[:_]");
                    result.add(String.format("Tap to count from %s to %s.", countingSplit[1], countingSplit[2]));
                    break;


                case "math":
                    result.add("<b>Math</b>");
                    String[] splitMe = tutor.tutor_id.split(":");
                    String[] secondPart = splitMe[1].split("\\.");

                    String[] thirdPart = secondPart[3].split("-");

                    StringBuilder descriptor = new StringBuilder();
                    descriptor.append(thirdPart[0].equals("SUB") ? "<b>Subtract</b> " : "<b>Add</b> ");
                    descriptor.append(String.format("values between <b>%s</b> and <b>%s</b>", secondPart[0], secondPart[2]));
                    result.add(descriptor.toString());
                    break;

                case "bigmath":
                    result.add("<b>Math</b>");
                    break;

                case "place":
                case "place.value":
                    result.add("<b>Place Value</b>");
                    break;

                case "numcompare":
                    result.add("<b>Number Comparison</b>");
                    break;

                case "picmatch":
                    result.add("<b>Picture Match</b>");
                    break;

                case "spelling":
                    result.add("<b>Spelling</b>");
                    break;

                case "numberscale":
                    result.add("<b>Number Scale</b>");
                    String numscaleSuffix = tutor.tutor_id.split(":")[1];
                    String[] numscaleDetails = numscaleSuffix.split("\\.");
                    String numscaleOffset = numscaleDetails[3].substring("off".length());
                    result.add(String.format("Explore numbers <b>%s</b> to <b>%s</b>, counting by <b>%s</b>.", numscaleDetails[0], numscaleDetails[2], numscaleOffset));
                    break;


                case "story":
                    result.add("<b>Story</b>");
                    result = processStoryTutorId(tutor, result);
                    break;


                case "write":
                    result.add("<b>Write</b>");
                    result = processWriteTutorId(tutor, result);
                    break;


                default:
                    break;
            }
        }catch (Exception e) {
            // result.add("<b>Error generating name</b>");
            // commenting out (for now) so that it doesn't show the error
        }

        // give it some empty lines
        for (int i = result.size(); i < 4; i++) {
            result.add(""); // add blanks til we get to the end
        }
        result.add("<i>" + tutor.tutor_id + "</i>"); // add ID for each one

        return result;
    }


    private static ArrayList<String> processStoryTutorId(CAt_Data tutor, ArrayList<String> result) {

        // story.hear::1_1 --> "Error generating name"
        // story.[hear|echo|read]::\d_\d --> "Error generating name"

        String[] splitStory = tutor.tutor_id.split(":");
        String storySuffix = splitStory[2];

        if (storySuffix.split("[0-9]")[0].equals("")) { // if begins with number
            // it's a number story
            String[]  storyNumberSuffix = storySuffix.split("[._]");

            //System.out.println(Arrays.toString(splitStory));
            String[] storyMode = splitStory[0].split("\\.");
            String storyModeCap = storyMode[1].substring(0, 1).toUpperCase() + storyMode[1].substring(1);
            //System.out.println(Arrays.toString(storyMode));

            result.add(String.format(Locale.US, "%s numbers from <b>%d</b> to <b>%d</b>",
                    storyModeCap, Integer.parseInt(storyNumberSuffix[0]), Integer.parseInt(storyNumberSuffix[2])));

        } else {

            String[] storySuffixSplit = storySuffix.split("[_\\.\\-]");
            String syl1, syl2, letter, wordType;
            StringBuilder descriptionBuilder = new StringBuilder();

            boolean isLitStory = true;

            String storyType = tutor.tutor_desc.split("\\.")[1];
            descriptionBuilder.append(storyType.substring(0,1).toUpperCase() + storyType.substring(1) + " "); // capitalize first letter

            //System.out.println(Arrays.toString(storySuffixSplit));
            switch(storySuffixSplit[0]) {

                case "ltr":
                    letter = storySuffixSplit[1]; // "ltr-A.rand"
                    descriptionBuilder.append(String.format("the letter %s", letter));
                    break;

                case "vow":
                    descriptionBuilder.append(String.format("all vowels"));
                    break;

                case "all":
                    descriptionBuilder.append(String.format("all letters"));
                    break;

                case "syl":
                    syl1 = storySuffixSplit[1];
                    syl2 = storySuffixSplit[3];
                    descriptionBuilder.append(String.format("the syllables %s through %s", syl1, syl2));
                    break;

                case "begin":
                    syl1 = storySuffixSplit[2];
                    descriptionBuilder.append(String.format("words beginning with %s", syl1));
                    break;

                case "end":
                    syl1 = storySuffixSplit[2];
                    descriptionBuilder.append(String.format("words ending with %s", syl1));
                    break;

                case "comm":
                    String wordCategory = storySuffixSplit[3];
                    if (wordCategory.equals("body")) wordCategory = "the " + wordCategory;
                    descriptionBuilder.append(String.format("common words about <b>%s</b>", wordCategory));
                    break;

                case "HF":
                    descriptionBuilder.append(String.format(Locale.US, "common %d-letter words", Integer.parseInt(storySuffixSplit[3])));
                    break;

                case "nonwrd":
                    descriptionBuilder.append(String.format(Locale.US, "%d-letter nonsense words", Integer.parseInt(storySuffixSplit[2])));
                    break;

                default:

                    isLitStory = false;
                    // it's a real story...
                    String storyName = storySuffix.split("__")[0]; // this removes the iteration at the end
                    result.add(storyName.replace("_", " ")); // "Kusoma_Song" --> "Kusoma Song"
                    break;
            }

            if(isLitStory) {
                //System.out.println(descriptionBuilder.toString());
                result.add(descriptionBuilder.toString());
            }


        }

        return result;
    }

    private static ArrayList<String> processWriteTutorId(CAt_Data tutor, ArrayList<String> result) {
        // write.wrd.dic:phon.r2R √√√
        // write.wrd.trc:phon.m2M √√√
        // write.wrd:dolch_preprimer √√√
        // write.wrd:dolch_1st_grade √√√

        // write.ltr.uc.trc:A..D_asc √√√

        String splitDesc[] = tutor.tutor_desc.split("\\.");
        String modeOfEntry = "Write";
        // check for trace
        if(splitDesc.length > 2) {
            switch(splitDesc[2]) {

                case "trc":
                    modeOfEntry = "Trace";
                    break;

                case "dic":
                default:
                    // do nothing
                    break;
            }
        }

        String splitSuffix[] = tutor.tutor_id.split(":")[1].split("[\\-\\.]");
        String str;
        switch(splitDesc[1]) {

            // letters
            // write.ltr.uc.trc:vow.asc.A..Z.1
            // write.ltr.lc:all.asc.A..Z.11
            case "ltr":
                String caps;
                switch(splitDesc[2]) {
                    case "uc":
                        caps = "uppercase";
                        break;
                    case "lc":
                        caps = "lowercase";
                        break;
                    default:
                        caps = null;
                }

                result.add(String.format("%s %s letters", modeOfEntry, caps));

                String vowels = null, order;
                switch(splitSuffix[0]) {
                    case "vow":
                        vowels = "Vowels";
                        break;

                    case "all":
                        vowels = "All Letters";
                        break;

                    default:
                        break;

                }

                // THIS is a mess... fix
                if (vowels == null) {
                    // write.ltr.uc:A..D_asc
                    order = splitSuffix[2].substring(2);
                    if (order.equals("asc")) order = "ascending";

                    result.add(String.format("Letters %s through %s, %s", splitSuffix[0], splitSuffix[2].substring(0, 1), order));
                } else {
                    switch(splitSuffix[1]) {
                        case "asc":
                            order = "Ascending";
                            break;

                        case "rand":
                        default:
                            order = "Random";

                    }
                    result.add(String.format("%s, %s", vowels, order));
                }

                break;

            // words
            // write.wrd.trc:syl.2ch..2ch..1
            // write.wrd:syl.2ch..2ch..2
            // write.wrd:lc.wrd.cat-food.1
            // write.wrd:lc.wrd.cat-body.4
            // write.wrd:lc.wrd.len-4.1
            // write.wrd:lc.wrd.len-10
            // write.wrd:lc.nonwrd.len-4.lev1
            case "wrd":

                String wordToWrite = null;

                //System.out.println(Arrays.toString(splitSuffix));
                if(splitSuffix[0].equals("syl")) {
                    String syl = splitSuffix[1];
                    wordToWrite = String.format("the syllable %s", syl);
                } else if (splitSuffix[0].equals("phon")) {
                    String[] g2p = splitSuffix[1].split("2");
                    wordToWrite = " words with g2p: <i>" + g2p[0] + "</i> to <i>" + g2p[1] + "</i>";

                } else if (splitSuffix[1].startsWith("dolch")) {
                    wordToWrite = splitSuffix[1].substring(6) + " grade Dolch words";
                } else if (splitSuffix[1].equals("wrd")) {

                    if(splitSuffix[2].equals("cat")) {
                        wordToWrite = String.format("common words about %s", splitSuffix[3]);
                    } else if (splitSuffix[2].equals("len")) {
                        wordToWrite = String.format(Locale.US, "common %d-letter words", Integer.parseInt(splitSuffix[3]));
                    }

                } else if (splitSuffix[1].equals("nonwrd")) {

                    wordToWrite = String.format(Locale.US, "%d-letter nonsense words", Integer.parseInt(splitSuffix[3]));
                }

                result.add(String.format("%s %s", modeOfEntry, wordToWrite));
                break;

            // missing letter
            // write.missingLtr:lc.begin.ha.1
            // write.missingLtr:lc.end.ko.9
            // "write.missingLtr:0.1.5.fin.s"
            case "missingLtr":

                modeOfEntry = "Complete";
                String wordToComplete = null;;
                System.out.println(Arrays.toString(splitSuffix));
                if(splitSuffix[1].equals("begin")) {
                    wordToComplete = String.format(" the word beginning with %s", splitSuffix[2]);
                } else if (splitSuffix[1].equals("end")) {
                    wordToComplete = String.format(" the word ending with %s", splitSuffix[2]);
                } else {
                    wordToComplete = String.format(" the word containing %s", splitSuffix[3]);
                }
                result.add(String.format("%s %s", modeOfEntry, wordToComplete));
                break;

                // numbers
            case "num":

                String numberToWrite = "the number";
                result.add(String.format("%s %s", modeOfEntry, numberToWrite));
                break;

               // arith
            case "arith":

                String answerToWrite = "the equation";
                result.add(String.format("%s %s", modeOfEntry, answerToWrite));
                break;


            case "dolch":

                str = "Write " + splitSuffix[2] + " grade Dolch words";
                result.add(str);
                break;


            case "phon":





            default:
        }


        // MATH
        // write.num:WR-100s.8
        // write.num:WR-3D.9
        // write.num:WR-2D.6
        // write.num.trc:WR-1..4.1"
        // write.num.trc:WR-1..10.2
        // write.num.dic:WR-1..20.13
        // write.num.dic:WR-2D.15

        // write.arith:ADD-2D-H.3 -- write.arith.add
        // write.arith:SUB-2D-H.4 -- write.arith.sub
        // write.arith:ADD-3D-H



        return result;
    }


    private static ArrayList<String> processAkiraTutorId(CAt_Data tutor, ArrayList<String> result) {

        //
        // LIT
        // "akira:vow.ltr.lc:I..I.vow.10.rand.say.8"
        // "akira:all.ltr.uc:CH..CH.all.10.rand.say.17"
        // akira:syl.lc.say.nye..nye.noShow..19
        // akira:syl.lc.say.ku..ku.noShow..9
        // akira:begin.wrd.wa.show.8
        // akira:end.wrd.wa.show.15
        // akira:comm.wrd.body.noShow.4
        // akira:HF.wrd.7.noShow.4
        // akira:HF.wrd.4.noShow.1
        // akira:nonwrd.8.noShow.3
        // akira:comm.wrd.people.noShow.3

        // MATH
        // "akira:0..9.by.1.asc
        // "akira:0..9.by.1.des"
        // akira:10..100.by.10.asc
        // akira:100..1000.by.100.asc
        // "akira:100..1000.by.within.asc

        String suffix = tutor.tutor_id.substring("akira:".length());
        String[] splits = suffix.split("[:\\.\\-_]");


        StringBuilder identifyString = new StringBuilder("Identify ");
        String str = null;
        //System.out.println(Arrays.toString(splits));
        switch(tutor.skill) {
            case "numbers":

                String num1 = splits[0];
                String num2 = splits[2];
                str = String.format(Locale.US, "numbers %d through %d", Integer.parseInt(num1), Integer.parseInt(num2));
                break;


            case "letters":

                switch(splits[0]) {
                    case "vow":
                        if(splits[3].equals(splits[5])) {
                            str = String.format("the vowel %s", splits[3]);
                        } else {
                            str = String.format("all vowels");
                        }
                        break;

                    case "all":
                        if (splits[3].equals(splits[5])) {
                            str = String.format("the letter %s", splits[3]);
                        } else {
                            str = String.format("letters from A to Z");
                        }

                        break;

                    case "syl":
                        str = String.format("the syllable %s", splits[3]);
                        break;


                    case "begin":
                        str = String.format("words beginning with %s", splits[2]);
                        break;

                    case "end":
                        str = String.format("words ending with %s", splits[2]);
                        break;

                    case "comm":
                        String wordCategory = splits[2];
                        if (wordCategory.equals("body")) wordCategory = "the " + wordCategory;
                        str = String.format("common words about <b>%s</b>", wordCategory);
                        break;

                    case "HF":
                        str = String.format(Locale.US, "common %d-letter words", Integer.parseInt(splits[2]));
                        break;


                    case "nonwrd":
                        str = String.format(Locale.US, "%d-letter nonsense words", Integer.parseInt(splits[1]));
                        break;

                    // OPEN_SOURCE new English options...
                    // akira:wrd.a2AE √√√
                    // akira:wrd.th2TH √√√
                    // akira:wrd.dolch_preprimer √√√
                    // akira:wrd.dolch_2nd_grade √√√
                    case "wrd":
                        if (splits[1].startsWith("dolch")) {
                            str = splits[2] + " grade Dolch words";
                        } else if (splits[1].contains("2")) {
                            String[] g2p = splits[1].split("2");
                            str = " g2p mapping: <i>" + g2p[0] + "</i> to <i>" + g2p[1] + "</i>";
                        }

                        break;

                    // akira:ltr.lc_A..D_rand
                    // akira:ltr.lc_E..G_rand --> "Identify null"
                    case "ltr":
                        //str = String.format()
                        switch(splits[1]) {
                            case "lc":
                                str = "lowercase";
                                break;
                            case "uc":
                                str = "uppercase";
                        }
                        str += " letters " + splits[2] + " to " + splits[4];
                        break;
                }
                break;
        }

        identifyString.append(str);
        result.add(identifyString.toString());

        return result;
    }

    /**
     * Specifically for processing Bubble Pop.
     * @param tutor
     * @param result
     * @return
     */
    private static ArrayList<String> processBubblePopTutorId(CAt_Data tutor, ArrayList<String> result) {

        // new cases for English Version:

        // OPEN_SOURCE Bpop metadata errors
        // letters

        // change to bpop.ltr.lc:A..D.[all|vow].[asc|rand].... [show|noShow] should be in index 7

        // phon words
        // bpop.wrd:u2AH.show.mc --> Fail on first row
        // bpop.wrd:oo2UH.show.mc
        // change to bpop.wrd:phon.oo2UH.show.mc

        // common words
        // bpop.wrd:dolch_preprimer.show.mc
        // bpop.wrd:dolch_preprimer.noShow.mc
        // bpop.wrd:dolch_1st_grade.show.rise
        result.add("<b>Bubble Pop</b>");


        String suffix = tutor.tutor_id.split(":")[1];
        String[] suffixSplit = suffix.split("[_//.//-]");


        // global
        String showWord;
        // lit vars
        String itemType, caps, wrdType, ltrOrder;
        boolean lc = false;
        // math vars
        String startRange, endRange, numOrder, addSub, digits, av, orientation, mc, translate;
        switch(tutor.tutor_desc) {

            case "bpop.ltr.lc":
                lc = true;
            case "bpop.ltr.uc":
                if (!lc) {
                    caps = "uppercase";
                } else {
                    caps = "lowercase";
                }

                String ltrName;
                if (suffixSplit[0].equals(suffixSplit[2])) {
                    ltrName = suffixSplit[0];
                    if (lc) ltrName = ltrName.toLowerCase();
                } else {
                    ltrName = "letters"; // all letters
                }
                result.add(String.format("Identify the %s %s", caps, ltrName));

                ltrOrder = suffixSplit[4];
                String ltrOrderText;
                switch(ltrOrder) {
                    case "asc":
                        ltrOrderText = "Ascending";
                        break;

                    case "rand":
                        ltrOrderText = "Random";
                        break;

                    default:
                        ltrOrderText = null;
                }

                String ltrType = suffixSplit[3];
                String ltrTypeText;
                switch (ltrType) {
                    case "vow":
                        ltrTypeText = "vowels";
                        break;

                    case "all":
                    default:
                        ltrTypeText = "A to Z";
                        break;
                }

                if (ltrOrderText != null) {
                    result.add(String.format("%s %s", ltrOrderText, ltrTypeText));
                    showWord = suffixSplit[7]; // wrong index?
                } else {
                    // bpop.ltr.uc:A..D.asc.noShow.rise --> "null A to Z"
                    // bpop.ltr.lc:A..D.asc.show.rise --> "null A to Z"
                    result.add(String.format("Letters %s to %s", suffixSplit[0], suffixSplit[2]));

                    showWord = suffixSplit[4];
                }
                

                switch(showWord) {
                    case "show":
                        result.add("Audio plus visual stimuli");
                        break;

                    case "noShow":
                        result.add("Audio stimulus only");
                    default:
                        break;
                }



                break;

            case "bpop.wrd":

                // "bpop.wrd:2ch..2ch.syl.rand.all.stat.show.47__it_1",
                // "bpop.wrd:cha..cha.syl.rand.sim.stat.show.52"
                // "bpop.wrd:wrd.4.lc.rand.stat.show.1"
                // "bpop.wrd:wrd.4.lc.rand.stat.noShow.2"
                // "bpop.wrd:nonwrd.4.lc.rand.stat.show.1"
                // "bpop.wrd:nonwrd.4.lc.rand.stat.noShow.1"
                // "bpop.wrd:begin.wrd.ha.noShow.2"
                // "bpop.wrd:begin.wrd.ki.noShow.2"
                // "bpop.wrd:wrd.food.lc.rand.stat.show.1"
                // "bpop.wrd:wrd.animals.lc.rand.stat.noShow.4"
                // "bpop.wrd:wrd.people.lc.rand.stat.noShow.6"

                showWord = null;

                boolean syl = suffixSplit[3].equals("syl"); // OPEN_SOURCE e2EH.show.rise not enough
                // check if syllable
                if(syl) {
                    if (suffixSplit[0].substring(1).equals("ch")) {
                        String numCharsInSyllable = suffixSplit[0].substring(0, 1); // 3ch --> 3
                        result.add(String.format("Identify syllables with %s characters", numCharsInSyllable));
                    } else {
                        String syllable = suffixSplit[0];
                        result.add(String.format("Practice identifying the syllable <b>%s</b>", syllable));
                    }
                    showWord = suffixSplit[7];

                } else {
                    switch(suffixSplit[0]) {
                        case "wrd":
                            try {
                                // can be either a number, or a category
                                int numChars = Integer.parseInt(suffixSplit[1]);
                                result.add(String.format("Identify <b>words with %s letters</b>", numChars));
                            } catch (NumberFormatException e) {
                                result.add(String.format("Identify <b>words about %s</b>", suffixSplit[1]));
                            }
                            showWord = suffixSplit[5];
                            break;

                        case "begin":
                        case "end":
                            String suffixOrPrefix = suffixSplit[2];
                            result.add(String.format("Identify <b>words that %s with %s</b>", suffixSplit[0], suffixOrPrefix));
                            showWord = suffixSplit[3];
                            break;

                        case "nonwrd":
                            result.add(String.format("Identify <b>nonsense words with %s letters</b>", suffixSplit[1]));
                            showWord = suffixSplit[5];
                            break;

                        case "phon":

                            result.add(String.format("Identify words that map <b>grapheme %s</b>", "GGG"));
                            result.add(String.format("to <b>phoneme %s</b>", "PPP"));
                            break;
                    }

                    //System.out.println(Arrays.toString(suffixSplit));

                }


                switch(showWord) {
                    case "show":
                        result.add("Audio plus visual stimuli");
                        break;

                    case "noShow":
                        result.add("Audio stimulus only");
                    default:
                        break;
                }


                break;

            case "bpop.num":

                // bpop.num:1..4.by.1.asc.q2q.AV.mc.1

                startRange = suffixSplit[0];
                endRange = suffixSplit[2];
                String offset = suffixSplit[4];
                numOrder = suffixSplit[5];
                translate = suffixSplit[6];
                av = suffixSplit[7];
                mc = suffixSplit[8];

                String translateText = null;
                switch (translate) {
                    case "q2q":
                        translateText = "quantities to quantities";
                        break;

                    case "s2s":
                        translateText = "numerals to numerals";
                        break;

                    case "q2s":
                        translateText = "quantities to numerals";
                        break;

                    case "s2q":
                        translateText = "numerals to quantities";
                        break;

                    case "x2s":
                        translateText = "audio to numerals";
                        break;
                }
                result.add(String.format("Identify numbers: %s", translateText));

                result.add(String.format("Range: <b>%s</b> to <b>%s</b>", startRange, endRange));

                String mcText;
                switch(mc) {

                    case "rise":
                        mcText = "Rising";
                        break;

                    case "mc":
                    default:
                        mcText = "Static";
                        break;
                }
                result.add(String.format("%s bubbles", mcText));

                break;

            case "bpop.addsub":

                // bpop.addsub:0..10.
                startRange = suffixSplit[0];
                // empty string in [1]
                endRange = suffixSplit[2];
                numOrder = suffixSplit[3]; // incr, decr, rand
                String orderTxt;

                addSub = suffixSplit[4].equals("SUB") ? "subtraction" : "addition";
                digits = suffixSplit[5]; // [123]D or number

                orientation = suffixSplit[6].equals("V") ? "Vertical" : "Horizontal";

                result.add(String.format("%s %s", orientation, addSub));

                boolean incr = false;
                switch(numOrder) {

                    case "incr":
                        incr = true;
                    case "decr":
                        orderTxt = numOrder + "ementing";
                        String start = incr ? startRange : endRange;
                        String end = incr ? endRange : startRange;
                        result.add(String.format("Numbers %s from <b>%s</b> to <b>%s</b>, by <b>%s</b>",
                                orderTxt, start, end, digits));
                        break;

                    case "rand":
                        orderTxt = "random";
                        digits = digits.substring(0, digits.indexOf("D"));
                        result.add(String.format("Random %s-digit numbers between <b>%s</b> and <b>%s</b>",
                                digits, startRange, endRange));
                        break;
                }


                break;

            case "bpop.gl":

                // bpop.gl:num.10..99.GL_DD_OFFw10_L
                translate = suffixSplit[0];
                String representationText = null;
                switch(translate) {
                    case "num":
                        representationText = "number";
                        break;

                    case "dot":
                        representationText = "quantity";
                        break;
                }

                startRange = suffixSplit[1];
                // empty string in [1]
                endRange = suffixSplit[3];

                String GL = suffixSplit[7];
                String GLtext = null;
                switch(GL) {
                    case "M":
                        GLtext = "greater";
                        break;

                    case "L":
                        GLtext = "lesser";
                        break;
                }

                // System.out.println(Arrays.toString(suffixSplit));
                result.add(String.format("Which %s is %s?", representationText, GLtext));
                result.add(String.format("Numbers between %s and %s", startRange, endRange));

                break;
            case "bpop.mn":

                // bpop.mn:10..99.MN-DD-UP-OFF1-BL1
                startRange = suffixSplit[0];
                // empty string in [1]
                endRange = suffixSplit[2];

                // System.out.println(Arrays.toString(suffixSplit));
                result.add("Select the missing number");
                result.add(String.format("Numbers between %s and %s", startRange, endRange));

                break;
        }

        return result;
    }
}
