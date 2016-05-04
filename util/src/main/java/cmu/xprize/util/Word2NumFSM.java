//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

package cmu.xprize.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/*
    Swahili Number System Rules - Spoken language rules

    digit00 = "sifuri"
    digit19 = ""moja" | "mbili" | "tatu" | "nne" | "tano" | "sita" | "saba" | "nane" | "tisa"
    digit29 = " "mbili" | "tatu" | "nne" | "tano" | "sita" | "saba" | "nane" | "tisa"

    tens  = "kumi" | "ishirini" | "thelathini" | "arobaini" | "hamsini" | "sitini" | "sabini" | "themanini" | "tisini"

    HUNDRED = "mia"

    power = "MIA" | "ELFU" | "MILIONI" | "BILIONI" | "TRILIONI" | "KWADRILIONI"

    NA = "na"

    000 = digit00
    00x = digit19
    0x0 = tens
    0xx = tens, NA, digit19

    pow = [power], digit19

    NumberGroup = (00x | 0x0 | 0xx)

    Number = [{power(i),NumberGroup],[power(i-1),NumberGroup],...,[power(0),NumberGroup] | 000 | 00x | 0x0 | 0xx}

    x0x = [power], digit19, NA, digit19
    xx0 = [power], digit19, NA, tens
    xxx = [power], digit19, NA, tens, NA, digit19

    Word sequence: elfu mia mbili na thelathini na tano na mia sita na themanini na saba
    Tranformtion: [200000, 200030, 200035, 235600, 235680, 235687

    Word sequence: elfu mia mbili na thelathini na tano
    Tranformtion: [200000, 200030, 200035

    Word sequence: elfu nane na mia tisa na arobaini na nane
    Transformation: [8000, 8900, 8940, 8948]
*/


/*
    English System Rules - Spoken language rules

    digit0  = "zero"
    digit19 = "one" | "two" | "three" | "four" | "five" | "six" | "seven" | "eight" | "nine"
    tens    = "ten" | "twenty" | "thirty" | "forty" | "fifty" | "sixty" | "seventy" | "eighty" | "ninety"

    HUNDRED = "hundred"

    power = "thousand" | "million" | "billion" | "trillion" | "quadrillion"

    AND = "and"

    000 = digit0
    00x = digit19
    0x0 = tens
    01x = teens
    0xx = tens, digit
    x00 = digit, HUNDRED
    x0x = digit, HUNDRED, [AND], digit
    xx0 = digit, HUNDRED, [AND], tens
    x1x = digit, HUNDRED, [AND], teens
    xxx = digit, HUNDRED, [AND], tens, digit

    NumberGroup = (00x | 0x0 | 01x | 0xx | x00 | x0x | xx0 | x1x | xxx)

    Number = {[NumberGroup,[power(i)]],[NumberGroup,[power(i-1)],...,[NumberGroup,[power(0)]],NumberGroup | 000}

    Word sequnec: two hundred and thirty five thousand six hundred and eighty seven
    Tranformtion: [2, 200, 230, 235, 235000, 235600, 235680, 235687




*/


public class Word2NumFSM {

    private static List<Integer> NumberList;

    private final static HashMap<String, Integer> SwahiliInDigitMap = new HashMap<>();

    static {
        SwahiliInDigitMap.put("sifuri", 0);
        SwahiliInDigitMap.put("moja", 1);
        SwahiliInDigitMap.put("mbili", 2);
        SwahiliInDigitMap.put("tatu", 3);
        SwahiliInDigitMap.put("nne", 4);
        SwahiliInDigitMap.put("tano", 5);
        SwahiliInDigitMap.put("sita", 6);
        SwahiliInDigitMap.put("saba", 7);
        SwahiliInDigitMap.put("nane", 8);
        SwahiliInDigitMap.put("tisa", 9);

        SwahiliInDigitMap.put("SIFURI", 0);
        SwahiliInDigitMap.put("MOJA", 1);
        SwahiliInDigitMap.put("MBILI", 2);
        SwahiliInDigitMap.put("TATU", 3);
        SwahiliInDigitMap.put("NNE", 4);
        SwahiliInDigitMap.put("TANO", 5);
        SwahiliInDigitMap.put("SITA", 6);
        SwahiliInDigitMap.put("SABA", 7);
        SwahiliInDigitMap.put("NANE", 8);
        SwahiliInDigitMap.put("TISA", 9);
    }

    private final static HashMap<String, Integer> SwahiliTensMap = new HashMap<>();

    static {
        SwahiliTensMap.put("kumi", 10);
        SwahiliTensMap.put("ishirini", 20);
        SwahiliTensMap.put("thelathini", 30);
        SwahiliTensMap.put("arobaini", 40);
        SwahiliTensMap.put("hamsini", 50);
        SwahiliTensMap.put("sitini", 60);
        SwahiliTensMap.put("sabini", 70);
        SwahiliTensMap.put("themanini", 80);
        SwahiliTensMap.put("tisini", 90);

        SwahiliTensMap.put("KUMI", 10);
        SwahiliTensMap.put("ISHIRINI", 20);
        SwahiliTensMap.put("THELATHINI", 30);
        SwahiliTensMap.put("AROBAINI", 40);
        SwahiliTensMap.put("HAMSINI", 50);
        SwahiliTensMap.put("SITINI", 60);
        SwahiliTensMap.put("SABINI", 70);
        SwahiliTensMap.put("THEMANINI", 80);
        SwahiliTensMap.put("TISINI", 90);

    }

    private final static HashMap<String, Integer> SwahiliPowers = new HashMap<>();

    static {
        SwahiliPowers.put("kumi", 10);
        SwahiliPowers.put("mia", 100);
        SwahiliPowers.put("elfu", 1000);
        SwahiliPowers.put("laki", 100000);
        SwahiliPowers.put("milioni", 1000000);

        SwahiliPowers.put("KUMI", 10);
        SwahiliPowers.put("MIA", 100);
        SwahiliPowers.put("ELFU", 1000);
        SwahiliPowers.put("LAKI", 100000);
        SwahiliPowers.put("MILIONI", 1000000);
    }

    private final static HashMap<String, Integer> EnglishDigitMap = new HashMap<>();

    static {
        EnglishDigitMap.put("zero", 0);

        EnglishDigitMap.put("one", 1);
        EnglishDigitMap.put("two", 2);
        EnglishDigitMap.put("three", 3);
        EnglishDigitMap.put("four", 4);
        EnglishDigitMap.put("five", 5);
        EnglishDigitMap.put("six", 6);
        EnglishDigitMap.put("seven", 7);
        EnglishDigitMap.put("eight", 8);
        EnglishDigitMap.put("nine", 9);

        EnglishDigitMap.put("ZERO", 0);

        EnglishDigitMap.put("ONE", 1);
        EnglishDigitMap.put("TWO", 2);
        EnglishDigitMap.put("THREE", 3);
        EnglishDigitMap.put("FOUR", 4);
        EnglishDigitMap.put("FIVE", 5);
        EnglishDigitMap.put("SIX", 6);
        EnglishDigitMap.put("SEVEN", 7);
        EnglishDigitMap.put("EIGHT", 8);
        EnglishDigitMap.put("NINE", 9);
    }

    private final static HashMap<String, Integer> EnglishTeensMap = new HashMap<>();

    static {
        EnglishTeensMap.put("ten", 10);
        EnglishTeensMap.put("eleven", 11);
        EnglishTeensMap.put("twelve", 12);
        EnglishTeensMap.put("thirteen", 13);
        EnglishTeensMap.put("fourteen", 14);
        EnglishTeensMap.put("fifteen", 15);
        EnglishTeensMap.put("sixteen", 16);
        EnglishTeensMap.put("seventeen", 17);
        EnglishTeensMap.put("eighteen", 18);
        EnglishTeensMap.put("nineteen", 19);

        EnglishTeensMap.put("TEN", 10);
        EnglishTeensMap.put("ELEVEN", 11);
        EnglishTeensMap.put("TWELVE", 12);
        EnglishTeensMap.put("THIRTEEN", 13);
        EnglishTeensMap.put("FOURTEEN", 14);
        EnglishTeensMap.put("FIFTEEN", 15);
        EnglishTeensMap.put("SIXTEEN", 16);
        EnglishTeensMap.put("SEVENTEEN", 17);
        EnglishTeensMap.put("EIGHTEEN", 18);
        EnglishTeensMap.put("NINETEEN", 19);
    }

    private final static HashMap<String, Integer> EnglishTensMap = new HashMap<>();

    static {
        EnglishTensMap.put("ten", 10);
        EnglishTensMap.put("twenty", 20);
        EnglishTensMap.put("thirty", 30);
        EnglishTensMap.put("forty", 40);
        EnglishTensMap.put("fifty", 50);
        EnglishTensMap.put("sixty", 60);
        EnglishTensMap.put("seventy", 70);
        EnglishTensMap.put("eighty", 80);
        EnglishTensMap.put("ninety", 90);

        EnglishTensMap.put("TEN", 10);
        EnglishTensMap.put("TWENTY", 20);
        EnglishTensMap.put("THIRTY", 30);
        EnglishTensMap.put("FORTY", 40);
        EnglishTensMap.put("FIFTY", 50);
        EnglishTensMap.put("SIXTY", 60);
        EnglishTensMap.put("SEVENTY", 70);
        EnglishTensMap.put("EIGHTY", 80);
        EnglishTensMap.put("NINETY", 90);
    }


    private final static HashMap<String, Integer> EnglishPower = new HashMap<>();

    static {
        EnglishPower.put("hundred", 100);
        EnglishPower.put("thousand",1000);
        EnglishPower.put("million", 1000000);
        EnglishPower.put("billion", 1000000000);

        EnglishPower.put("HUNDRED", 100);
        EnglishPower.put("THOUSAND",1000);
        EnglishPower.put("MILLION", 1000000);
        EnglishPower.put("BILLION", 1000000000);
    }

    private final static HashMap<String, String> conjMap = new HashMap<>();

    static {
        conjMap.put(TCONST.LANG_EN, "AND");
        conjMap.put(TCONST.LANG_SW, "NA");
    }


    public Word2NumFSM() {
    }


    public int getValueType(String word, String langFtr) {

        int state = TCONST.W2N_ERR;

        switch(langFtr) {
            case TCONST.LANG_SW:

                if(SwahiliInDigitMap.containsKey(word))   state = TCONST.W2N_DIGIT;
                else if(SwahiliTensMap.containsKey(word)) state = TCONST.W2N_TENS;
                else if(SwahiliPowers.containsKey(word))  state = TCONST.W2N_POWER;
                else if(word.equals(TCONST.CC_SW_NA))     state = TCONST.W2N_CONJ;
                else if(word.equals(TCONST.NUM_EOD))      state = TCONST.W2N_EOD;
                break;

            case TCONST.LANG_EN:

                if(EnglishDigitMap.containsKey(word))      state = TCONST.W2N_DIGIT;
                else if(EnglishTeensMap.containsKey(word)) state = TCONST.W2N_TENS;
                else if(EnglishTensMap.containsKey(word))  state = TCONST.W2N_POWER;
                else if(EnglishPower.containsKey(word))    state = TCONST.W2N_POWER;
                else if(word.equals(TCONST.CC_EN_AND))     state = TCONST.W2N_CONJ;
                else if(word.equals(TCONST.NUM_EOD))       state = TCONST.W2N_EOD;
                break;
        }

        return state;
    }


    private int transform(ArrayList<String> words, String langFtr)  {

        int state  = TCONST.STARTSTATE;
        int index  = 0;
        int multi  = TCONST.UNSET;
        int result = TCONST.UNSET;

        words.add(TCONST.NUM_EOD);

        try {
            do {
                switch (state) {
                    //
                    case TCONST.STARTSTATE:

                        state = getValueType(words.get(index), langFtr);
                        break;

                    case TCONST.W2N_DIGIT:

                        switch (langFtr) {
                            case TCONST.LANG_EN:
                                switch (state) {

                                }
                                break;

                            case TCONST.LANG_SW:
                                break;
                        }
                        break;

                }
            } while (state != TCONST.W2N_EOD);
        }
        catch(Exception e) {
        }


        return result;
    }

}
