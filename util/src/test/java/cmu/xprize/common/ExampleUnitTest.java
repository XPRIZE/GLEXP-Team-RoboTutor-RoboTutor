package cmu.xprize.common;

import android.util.Log;

import org.junit.Test;

import cmu.xprize.util.CFileNameHasher;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void checkFileNameHasher() {

        CFileNameHasher hasher = CFileNameHasher.getInstance();

        String[] inputs = {"nge", "n'ge", "n_ge"};

        for (String input:
             inputs) {
            String hash = hasher.generateHash(input);
            System.out.println("HASH_TEST for " + input + ":\t\t" + hash);
        }

    }

}