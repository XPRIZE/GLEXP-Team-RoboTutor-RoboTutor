package cmu.xprize.common;


import android.app.ExpandableListActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import cmu.xprize.util.TCONST;
import cmu.xprize.util.Word2NumFSM;


public class W2N_UnitTest {

    static private ArrayList<ArrayList<String>> w2ntestSW = new ArrayList<ArrayList<String>>();

    static {

        w2ntestSW.add(new ArrayList<>(Arrays.asList("sifuri")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("moja")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mbili")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("tatu")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("nne")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("tano")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("sita")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("saba")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("nane")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("tisa")));

        w2ntestSW.add(new ArrayList<>(Arrays.asList("kumi")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("ishirini")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("thelathini")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("arobaini")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("hamsini")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("sitini")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("sabini")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("themanini")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("tisini")));

        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia","moja")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia","mbili")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia","tatu")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia","nne")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia","tano")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia","sita")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia","saba")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia","nane")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia","tisa")));

        w2ntestSW.add(new ArrayList<>(Arrays.asList("tisini","na","moja")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("tisini","na","mbili")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("tisini","na","tatu")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("tisini","na","nne")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("tisini","na","tano")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("tisini","na","sita")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("tisini","na","saba")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("tisini","na","nane")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("tisini","na","tisa")));

        w2ntestSW.add(new ArrayList<>(Arrays.asList("elfu","mia","tano")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("elfu","mia","tano","na","elfu","ishirini","na","elfu","tisa","na","mia","tatu","na","sabini","na","nne")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("elfu","ishirini","na","mia","tatu","na","nne")));

        w2ntestSW.add(new ArrayList<>(Arrays.asList("mbili","sifuri")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("sifuri","sifuri")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mbili")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia","moja")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia","elfu")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia","moja","na","mbili")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("elfu")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("elfu","tatu")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("elfu","ishirini")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("elfu","mia","tano")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("error")));
        w2ntestSW.add(new ArrayList<>(Arrays.asList("mia")));
    }


    static private ArrayList<ArrayList<String>> w2ntestEN = new ArrayList<ArrayList<String>>();

    static {

        w2ntestEN.add(new ArrayList<>(Arrays.asList("zero")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("one")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("two")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("three")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("four")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("five")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("six")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("seven")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("eight")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("nine")));

        w2ntestEN.add(new ArrayList<>(Arrays.asList("ten")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("twenty")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("thirty")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("forty")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("fifty")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("sixty")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("seventy")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("eighty")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("ninety")));

        w2ntestEN.add(new ArrayList<>(Arrays.asList("one","hundred")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("two","hundred")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("three","hundred")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("four","hundred")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("five","hundred")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("six","hundred")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("seven","hundred")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("eight","hundred")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("nine","hundred")));

        w2ntestEN.add(new ArrayList<>(Arrays.asList("ninety","three")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("forty","two")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("one","thousand","two","hundred")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("error")));
        w2ntestEN.add(new ArrayList<>(Arrays.asList("one","thousand","two","hundred","and","thirteen")));
    }


    public  W2N_UnitTest() {

        try {
            for (ArrayList<String> elem : w2ntestSW) {
                long result = Word2NumFSM.transform(elem, "LANG_SW");

                Log.i("W2NTEST", "result: " + result + "\tWarn: " + TCONST.W2N_WARNMSG[Word2NumFSM.warnCode] + "  Error: " + TCONST.W2N_ERRORMSG[Word2NumFSM.errorCode]);
            }


            for (ArrayList<String> elem : w2ntestSW) {
                long result = Word2NumFSM.transform(elem, "LANG_EN");

                Log.i("W2NTEST", "result: " + result + "\tWarn: " + TCONST.W2N_WARNMSG[Word2NumFSM.warnCode] + "  Error: " + TCONST.W2N_ERRORMSG[Word2NumFSM.errorCode]);
            }
        }
        catch(Exception e) {
            Log.e("TAG", "ERROR");
        }
    }


}
