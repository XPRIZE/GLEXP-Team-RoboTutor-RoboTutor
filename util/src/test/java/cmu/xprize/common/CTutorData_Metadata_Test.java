package cmu.xprize.common;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.CFileNameHasher;
import cmu.xprize.util.CTutorData_Metadata;
import cmu.xprize.util.TCONST;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class CTutorData_Metadata_Test {


    private CTutorData_Metadata metadata;

    private JSONObject devData;
    private JSONObject mathTransitions;
    private JSONObject writeTransitions;
    private JSONObject storyTransitions;

    @Before
    public void setup() throws IOException, JSONException {

        metadata = new CTutorData_Metadata();
        parseDevData();
    }

    @Test
    public void testThumbs() throws Exception {

        JSONObject whichTransitions = storyTransitions;

        Iterator<String> it = whichTransitions.keys();
        while(it.hasNext()) {

            String tutorKey = it.next();

            // --- begin tutor Data ---
            CAt_Data data = new CAt_Data();



            data.loadJSON((JSONObject) whichTransitions.get(tutorKey), null);

            //System.out.println(tutorKey);

            TCONST.Thumb thumb = CTutorData_Metadata.getThumbImage(data);

            assertNotNull("Tutor: " + tutorKey + " bad result", thumb);

        }
    }

    @Test
    public void testWriteTransitions() throws Exception {

        JSONObject whichTransitions = writeTransitions;

        Iterator<String> it = whichTransitions.keys();
        while(it.hasNext()) {
            String tutorKey = it.next();

            // --- begin tutor Data ---
            CAt_Data data = new CAt_Data();



            data.loadJSON((JSONObject) whichTransitions.get(tutorKey), null);

            //System.out.println(tutorKey);

            ArrayList displayText = CTutorData_Metadata.parseNameIntoLabels(data);

            assertNotNull("Tutor: " + tutorKey + " bad result", displayText);

            //assertEquals("Tutor: " + tutorKey + " doesn't begin with ID.", displayText.get(0), "<b>" + tutorKey + "</b>");

            //assertTrue("Tutor: " + tutorKey + " has size of " + displayText.size(), displayText.size() > 1);



            if(true) continue;

            // --- begin tokenizing tutorID ---
            String[] tokens = tutorKey.split(":");
            String prefix = tokens[0];
            String[] prefixTokens = prefix.split("\\.");

            String suffix = tokens[1];
            String[] suffixTokens = suffix.split("\\.");

            switch(prefixTokens[0]) {

                case "write":

                    switch (prefixTokens[1]) {
                        case "ltr":

                            System.out.println(data.tutor_id);
                            break;


                        case "wrd":

                            System.out.println(Arrays.toString(suffixTokens));

                            switch (suffixTokens[0]) {
                                case "lc":
                                    System.out.println(String.format("tutor: %s -- (%s, %s) -- %s", data.tutor_id, data.cell_column, data.cell_row, "lowercase"));
                                    break;

                                case "syl":
                                    System.out.println(String.format("tutor: %s -- (%s, %s) -- %s", data.tutor_id, data.cell_column, data.cell_row, "syllables"));
                            }


                            break;
                    }

                    break;


                case "bpop":

                    switch (prefixTokens[1]) {

                        case "ltr":

                            break;

                        case "wrd":

                            String wordCase;

                            //System.out.println(String.format("tutor: %s\nBubblePop Words\nCase=%s", data.tutor_id, wordCase));
                            break;
                    }
                    break;


            }
        }
    }

    @Test
    public void testMathTransitions() throws Exception {

        JSONObject whichTransitions = mathTransitions;

        Iterator<String> it = whichTransitions.keys();
        while(it.hasNext()) {

            String tutorKey = it.next();

            // --- begin tutor Data ---
            CAt_Data data = new CAt_Data();



            data.loadJSON((JSONObject) whichTransitions.get(tutorKey), null);

            //System.out.println(tutorKey);

            ArrayList displayText = CTutorData_Metadata.parseNameIntoLabels(data);

            assertNotNull("Tutor: " + tutorKey + " bad result", displayText);

        }
    }

    private void parseDevData() throws IOException, JSONException {

        InputStream inputStream = null;

        inputStream = new FileInputStream("/Users/kevindeland/RoboTutor/RoboTutor/app/src/main/assets/tutors/activity_selector/assets/data/sw/dev_data.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
        StringBuilder sb = new StringBuilder();

        String line = null;
        while ((line = reader.readLine()) != null)
        {
            sb.append(line + "\n");
        }
        String result = sb.toString();

        devData = new JSONObject(result);

        mathTransitions = (JSONObject) devData.get("mathTransitions");
        writeTransitions = (JSONObject) devData.get("writeTransitions");
        storyTransitions = (JSONObject) devData.get("storyTransitions");
    }


}