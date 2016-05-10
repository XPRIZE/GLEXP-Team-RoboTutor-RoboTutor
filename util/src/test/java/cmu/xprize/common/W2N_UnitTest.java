package cmu.xprize.common;


import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import cmu.xprize.util.TCONST;
import cmu.xprize.util.Word2NumFSM;


public class W2N_UnitTest {

    static private ArrayList<ArrayList<String>> w2ntest = new ArrayList<ArrayList<String>>();

    static {

        w2ntest.add(new ArrayList<>(Arrays.asList("sifuri")));
        w2ntest.add(new ArrayList<>(Arrays.asList("moja")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mbili")));
        w2ntest.add(new ArrayList<>(Arrays.asList("tatu")));
        w2ntest.add(new ArrayList<>(Arrays.asList("nne")));
        w2ntest.add(new ArrayList<>(Arrays.asList("tano")));
        w2ntest.add(new ArrayList<>(Arrays.asList("sita")));
        w2ntest.add(new ArrayList<>(Arrays.asList("saba")));
        w2ntest.add(new ArrayList<>(Arrays.asList("nane")));
        w2ntest.add(new ArrayList<>(Arrays.asList("tisa")));

        w2ntest.add(new ArrayList<>(Arrays.asList("kumi")));
        w2ntest.add(new ArrayList<>(Arrays.asList("ishirini")));
        w2ntest.add(new ArrayList<>(Arrays.asList("thelathini")));
        w2ntest.add(new ArrayList<>(Arrays.asList("arobaini")));
        w2ntest.add(new ArrayList<>(Arrays.asList("hamsini")));
        w2ntest.add(new ArrayList<>(Arrays.asList("sitini")));
        w2ntest.add(new ArrayList<>(Arrays.asList("sabini")));
        w2ntest.add(new ArrayList<>(Arrays.asList("themanini")));
        w2ntest.add(new ArrayList<>(Arrays.asList("tisini")));

        w2ntest.add(new ArrayList<>(Arrays.asList("mia","moja")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mia","mbili")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mia","tatu")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mia","nne")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mia","tano")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mia","sita")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mia","saba")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mia","nane")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mia","tisa")));

        w2ntest.add(new ArrayList<>(Arrays.asList("tisini","na","moja")));
        w2ntest.add(new ArrayList<>(Arrays.asList("tisini","na","mbili")));
        w2ntest.add(new ArrayList<>(Arrays.asList("tisini","na","tatu")));
        w2ntest.add(new ArrayList<>(Arrays.asList("tisini","na","nne")));
        w2ntest.add(new ArrayList<>(Arrays.asList("tisini","na","tano")));
        w2ntest.add(new ArrayList<>(Arrays.asList("tisini","na","sita")));
        w2ntest.add(new ArrayList<>(Arrays.asList("tisini","na","saba")));
        w2ntest.add(new ArrayList<>(Arrays.asList("tisini","na","nane")));
        w2ntest.add(new ArrayList<>(Arrays.asList("tisini","na","tisa")));

        w2ntest.add(new ArrayList<>(Arrays.asList("elfu","mia","tano")));
        w2ntest.add(new ArrayList<>(Arrays.asList("elfu","mia","tano","na","elfu","ishirini","na","elfu","tisa","na","mia","tatu","na","sabini","na","nne")));
        w2ntest.add(new ArrayList<>(Arrays.asList("elfu","ishirini","na","mia","tatu","na","nne")));

        w2ntest.add(new ArrayList<>(Arrays.asList("mbili","sifuri")));
        w2ntest.add(new ArrayList<>(Arrays.asList("sifuri","sifuri")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mbili")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mia","moja")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mia","elfu")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mia","moja","na","mbili")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mia")));
        w2ntest.add(new ArrayList<>(Arrays.asList("elfu")));
        w2ntest.add(new ArrayList<>(Arrays.asList("elfu","tatu")));
        w2ntest.add(new ArrayList<>(Arrays.asList("elfu","ishirini")));
        w2ntest.add(new ArrayList<>(Arrays.asList("elfu","mia","tano")));
        w2ntest.add(new ArrayList<>(Arrays.asList("error")));
        w2ntest.add(new ArrayList<>(Arrays.asList("mia")));
    }

    public  W2N_UnitTest() {

        for (ArrayList<String> elem : w2ntest) {
            long result = Word2NumFSM.transformSW(elem);

            Log.i("W2NTEST", "result: " + result + "\tWarn: " + TCONST.W2N_WARNMSG[Word2NumFSM.warnCode] + "  Error: " + TCONST.W2N_ERRORMSG[Word2NumFSM.errorCode]);
        }
    }


}
