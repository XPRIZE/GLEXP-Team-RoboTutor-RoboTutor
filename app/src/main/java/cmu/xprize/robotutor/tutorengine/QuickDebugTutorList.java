package cmu.xprize.robotutor.tutorengine;

import java.util.HashMap;
import java.util.Map;

/**
 * RoboTutor
 * QuickDebugTutorList
 * <p>A list of tutors to test with QuickDebug. This serves as a stand-in for a more appropriate format, e.g. a file.</p>
 * Created by kevindeland on 5/9/19.
 */

public class QuickDebugTutorList {

    public static Map<String, QuickDebugTutor> toFixBugMap = new HashMap<>();
    public static Map<String, QuickDebugTutor> resolvedBugMap = new HashMap<>();


    static {
        QuickDebugTutor bug1 = new QuickDebugTutor(
          "write.num",
                "write.num:WR-100s.8",
                "[file]write.num_WR-100s.8.json"
        );
        bug1.setComment("failed to load");
        toFixBugMap.put("write.num:fail.1", bug1);
        bug1.resolve(); // √√√


        QuickDebugTutor bug2 = new QuickDebugTutor(
                "write.num",
                "write.num:WR-3D.9",
                "[file]write.num_WR-3D.9.json"
        );
        bug2.setComment("failed to load");
        toFixBugMap.put("write.num:fail.2", bug2);
        bug1.resolve(); // √√√

        QuickDebugTutor bug3 = new QuickDebugTutor(
                "write.arith",
                "write.arith:ADD-1D-H.1",
                "[file]write.arith_ADD-1D-H.1.json"
        );
        bug3.setComment("Missing plus/minus audio");
        toFixBugMap.put("write.arith:audio", bug3);


    }
    // -------
    // TO FIX as of 5/9/2019
    // -------
    static {
        // BUGS to check...
        // in the future, this could be a cool debugging screen that pops up and leads you through bugs
        // there could also be an in-app bug-reporting system

        // STATUS: review
        QuickDebugTutor bug9 = new QuickDebugTutor(
                "numcompare",
                "numcompare:1d",
                "[file]numcompare_1d.json"
        );
        bug9.setComment("Audio prompts should not say 'First tap on the ones' if there are only ones.");
        bug9.setPriority(QuickDebugTutor.Priority.COULD);
        toFixBugMap.put("numcompare:wordy_prompt", bug9);

        // STATUS: review
        QuickDebugTutor bug10 = new QuickDebugTutor(
                "place.value",
                "place.value:pv-100..499.3D.diff2.26",
                "[file]place.value_pv-100..499.3D.diff2.26.json"
        );
        bug10.setComment("'Good job' audio is overlapping on second screen.");
        bug10.setPriority(QuickDebugTutor.Priority.COULD);
        toFixBugMap.put("place.value:overlapping_audio", bug10);

        QuickDebugTutor bug14 = new QuickDebugTutor(
                "akira",
                "akira:10..100.by.within.des",
                "[file]akira_10..100.by.within.des.json"
        );
        bug14.setComment("Akira prompt number not playing");
        bug14.setLocation("akira/animator_graph.json --> INSTRUCT_3V");
        bug14.setPriority(QuickDebugTutor.Priority.SHOULD);
        toFixBugMap.put("akira:missing_audio", bug14);

    }
    
    static {
        // BACKUP PLAN: remove story.pic from the English matrix.
        QuickDebugTutor bug1 = new QuickDebugTutor(
          "story.pic.hear",
                "story.pic.hear::1_1",
                "[encfolder]1_1"
        );
        bug1.setComment("Shows three ? images instead of pictures");
        bug1.setLocation("OPEN_SOURCE (1) - does not account for pages without images");
        bug1.setPriority(QuickDebugTutor.Priority.MUST);
        toFixBugMap.put("story_1_1:images", bug1);

        QuickDebugTutor bug1_3 = new QuickDebugTutor(
                "story.pic.hear",
                "story.pic.hear::1_3",
                "[encfolder]1_3"
        );
        bug1_3.setComment("Shows three ? images instead of pictures");
        bug1_3.setLocation("OPEN_SOURCE (1) - does not account for pages without images");
        bug1_3.setPriority(QuickDebugTutor.Priority.MUST);
        toFixBugMap.put("story_1_3:images", bug1_3);

        QuickDebugTutor bug2 = new QuickDebugTutor(
                "story.pic.hear",
                "story.pic.hear::1_2",
                "[encfolder]1_2"
        );
        bug2.setComment("After 'When they were moving they came across two guinea fowls.' gets STUCK");
        bug2.setLocation("UNKNOWN - no error output shown. Could be issue with storydata.json");
        bug2.setPriority(QuickDebugTutor.Priority.SHOULD);
        toFixBugMap.put("story_1_2:STUCK", bug2);

        QuickDebugTutor bug3 = new QuickDebugTutor(
                "story.pic.hear",
                "story.pic.hear::2_1",
                "[encfolder]2_1"
        );
        bug3.setComment("Shows two images with question marks.");
        bug3.setPriority(QuickDebugTutor.Priority.SHOULD);
        toFixBugMap.put("story_2_1:images", bug3);

        // TO CHECK
    }

    // NON-TUTOR bugs
    static {

    }

    // --------
    // RESOLVED as of 5/9/2019
    // --------
    static {
        QuickDebugTutor bug4 = new QuickDebugTutor(
                "story.parrot",
                "story.parrot::ee2iy_sound",
                "[encfolder]ee2iy_sound"
        );
        bug4.setComment("should not say 'repeat after me' before reading to kid");
        bug4.setPriority(QuickDebugTutor.Priority.SHOULD);
        toFixBugMap.put("story.parrot:prompt", bug4);
        bug4.resolve();

        QuickDebugTutor bug5 = new QuickDebugTutor(
                "akira",
                "akira:wrd.dolch_2nd_grade",
                "[file]akira_wrd.dolch_2nd_grade.json"
        );
        bug5.setComment("'us' recorded as 'US'. Should prompt 'tap on your answer'");
        bug5.setPriority(QuickDebugTutor.Priority.COULD);
        toFixBugMap.put("akira:audio", bug5);
        bug5.resolve();
    }

    // --------
    // RESOLVED as of 5/8/2019
    // --------
    static {

        QuickDebugTutor bug1 = new QuickDebugTutor("write.wrd.dic",
                "write.wrd.dic:word.dolch_3rd_grade",
                "[file]write.wrd.dic_word.dolch_3rd_grade.json");
        bug1.setComment("Missing the word 'if' and hangs");
        resolvedBugMap.put("write:missing_word", bug1);
        bug1.resolve(); // FIXED

        QuickDebugTutor bug2 = new QuickDebugTutor(
                "numcompare",
                "numcompare:1d",
                "[file]numcompare_1d.json"
        );
        bug2.setComment("Missing audio");
        bug2.setComment("assets/audio/en/cmu/xprize/nd/{{SNumDiscr.digitCompare}}.mp3");
        bug2.setComment("assets/audio/en/cmu/xprize/nd/So the bigger number is.mp3");
        resolvedBugMap.put("nd:missing_prompts", bug2);
        bug2.resolve(); // FIXED

        QuickDebugTutor bug5 = new QuickDebugTutor(
                "place.value",
                "place.value:pv-100..499.3D.diff0.17",
                "[file]place.value_pv-100..499.3D.diff0.17.json"
        );
        bug5.setComment("Missing audio 'tap inside the box'; stimulus box is ugly; second screen missing number audio.");
        bug5.setLocation("CCountingXComponent");
        resolvedBugMap.put("place.value:many_bugs", bug5);
        bug5.resolve(); // FIXED

        QuickDebugTutor bug6 = new QuickDebugTutor(
                "place.value",
                "place.value:pv-100..499.3D.diff2.26",
                "[file]place.value_pv-100..499.3D.diff2.26.json"
        );
        bug6.setComment("Missing audio on second screen.");
        bug6.setPriority(QuickDebugTutor.Priority.MUST);
        resolvedBugMap.put("place.value:missing_audio", bug6);
        bug6.resolve(); // FIXED

        QuickDebugTutor bug11 = new QuickDebugTutor(
                "place.value",
                "place.value:pv-100..499.3D.diff2.26",
                "[file]place.value_pv-100..499.3D.diff2.26.json"
        );
        bug11.setComment("countx audio should just say 'please tap' if place value.");
        bug11.setLocation("countingx/animator_graph.json");
        bug11.setPriority(QuickDebugTutor.Priority.SHOULD);
        resolvedBugMap.put("place.value:wrong_audio", bug11);
        bug11.resolve(); // FIXED

        QuickDebugTutor bug3 = new QuickDebugTutor(
                "bpop.gl",
                "bpop.gl:dot.0..9.GL_SD_OFF1_L.bub2.6",
                "[file]bpop.gl_dot.0..9.GL_SD_OFF1_L.bub2.6.json"
        );
        bug3.setComment("Should say 'tap the bigger number' or 'tap the smaller number");
        bug3.setLocation("bubble_pop/animator_graph.json");
        bug3.setPriority(QuickDebugTutor.Priority.MUST);
        resolvedBugMap.put("bpop.gl:wrong_prompt", bug3);
        bug3.resolve(); // FIXED

        QuickDebugTutor bug4 = new QuickDebugTutor(
                "bpop.mn",
                "bpop.mn:0..9.MN-SD-UP-OFF1-BL1.incr.4",
                "[file]bpop.mn_0..9.MN-SD-UP-OFF1-BL1.incr.4.json"
        );
        bug4.setComment("Should say 'tap the missing number'");
        bug4.setLocation("bubble_pop/animator_graph.json");
        bug4.setPriority(QuickDebugTutor.Priority.MUST);
        resolvedBugMap.put("bpop.mn:wrong_prompt", bug4);
        bug4.resolve(); // FIXED

        QuickDebugTutor bug7 = new QuickDebugTutor(
                "story.parrot",
                "story.parrot::ea2eh_wb_2",
                "[encfolder]ea2eh_wb_2"
        );
        bug7.setComment("Should say 'Read after me'");
        resolvedBugMap.put("story.parrot:missing_prompt", bug7);
        bug7.resolve(); // FIXED

        QuickDebugTutor bug8 = new QuickDebugTutor(
                "story.pic.hear",
                "story.pic.hear::1_13",
                "[encfolder]1_13"
        );
        bug8.setComment("Missing audio prompts e.g. 'Which picture?'");
        resolvedBugMap.put("story.pic:missing_prompt", bug8);
        bug8.resolve(); // FIXED

        QuickDebugTutor bug12 = new QuickDebugTutor(
                "bpop.gl",
                "bpop.gl:num.100..900.GL_DD_OFF100_L.bub2.30.json",
                "[file]bpop.gl_num.100..900.GL_DD_OFF100_L.bub2.30.json");
        bug12.setComment("Audio not playing");
        resolvedBugMap.put("bpop.gl:multidigit_audio", bug12);
        bug12.setPriority(QuickDebugTutor.Priority.MUST);
        bug12.setLocation("bubble_pop/animator_graph.json");
        bug12.resolve(); // FIXED

        QuickDebugTutor bug13 = new QuickDebugTutor(
                "bpop.num",
                "bpop.num:1..4.by.1.asc.q2q.AV.mc.1",
                "[file]bpop.num_1..4.by.1.asc.q2q.AV.mc.1.json"
        );
        bug13.setComment("Number audio not playing");
        resolvedBugMap.put("bpop.num:missing_audio", bug13);
        bug13.setPriority(QuickDebugTutor.Priority.MUST);
        bug13.resolve(); // FIXED
    }
}
