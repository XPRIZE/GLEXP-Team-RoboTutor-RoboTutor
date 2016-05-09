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
    digit19 = "moja" | "mbili" | "tatu" | "nne" | "tano" | "sita" | "saba" | "nane" | "tisa"
    tens    = "kumi" | "ishirini" | "thelathini" | "arobaini" | "hamsini" | "sitini" | "sabini" | "themanini" | "tisini"
    mia     = "mia"
    power   = "elfu" | "milioni" | "bilioni" | "trilioni" | "kwadrilioni"
    na      = "na"

    hundreds = (mia, digit19)

    Number10    = tens, [na digit19]}
    Number100   = hundreds, [na, Number10] | [na digit19]

    powerNgroup1   = power{n}, digit19
    powerNgroup10  = power{n}, tens, [na, powerNgroup1]
    powerNgroup100 = power{n}, hundreds, [na, powerNgroup10] | [na, powerNgroup1]

    powerNgroup = powerNgroup100 | powerNgroup10 | powerNgroup1

    Number1000s = powerNgroup{n},[na, powerNgroup(n-1)],...,[na, powerNgroup(0)],[na, Number100] | [na, Number10] | [na Number1]}

    Number = Number1000s | Number100 | Number10 | digit19 | digit0


    If these aren't the rules then it becomes very complicated to construct a number

    if
    11000 is elfu kumi na moja
             elfu kumi na elfu moja
    and
    11001 is elfu kumi na moja na moja
             elfu kumi na elfu moja na moja

    what is
    10001

    elfu kumi na mia sifuri na moja ??
    elfu kumi na moja


    11,000
    elfu kumi na elfu moja


    Word sequence: elfu mia mbili na thelathini na tano na mia sita na themanini na saba
    Tranformtion: [200000, 200030, 200035, 235600, 235680, 235687

    Word sequence: elfu mia mbili na thelathini na tano
    Tranformtion: [200000, 200030, 200035

    Word sequence: elfu nane na mia tisa na arobaini na nane
    Transformation: [8000, 8900, 8940, 8948]

    if(muliplier > 9 then power-1 must be included potentially as sifuri to disambiguate the na)

    129000
    elfu mia moja na elfu ishirini na elfu tisa

    elfu mia moja na ishirini na tisa

    You need the assurance that if the value belongs in a different power it will be prefixed
    with that power even if it's multiplier is 0

    100029
    elfu mia moja na mia sifuri na ishirini na tisa

    23000
    elfu ishirini na tatu
    20003
    elfu ishirini na mia sifuri na tatu

    235056
    elfu mia mbili na thelathini na tano na hamsini na sita
    elfu mia mbili na elfu thelathini na elfu tano na hamsini na sita
    elfu mia mbili na thelathini na tano na mia sifuri na hamsini na sita

    235000
    elfu mia mbili na thelathini na tano na mia sifuri

    200035
    elfu mia mbili na mia sifuri na thelathini na tano

    1,000,006
    milioni moja na mia sifuri na sita

    1,002,006
    milioni moja na elfu mbili na sita

    1,010,006
    milioni moja na elfu kuma na mia sifuri na sita

    11,000,006
    milioni kumi na moja na elfu sifuri na mia sifuri na sita
    milioni kumi na moja na sita

    10,000,006
    milioni kumi na sita  - ambiguous  16,000,000, or 10,000,006
    milioni kumi na mia sifuri na sita  10,000,006
    milioni kumi na sita na mia sifuri  16,000,000 na mia sifuri could be assumed

    10,000,020
    milioni kumi na ishirini

    11,020,006
    milioni kuma na moja na elfu ishirini na mia sifuri na sita

    elfu mia mbili na thelathini na tano na mia sifuri na hamsini na sita

    power
    100
    1000
    1000,000
    1000,000,000
    .
    .
    .


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

    private final static HashMap<String, Integer> Swahili_digit00 = new HashMap<>();

    static {
        Swahili_digit00.put("sifuri", 0);
        Swahili_digit00.put("SIFURI", 0);
    }

    private final static HashMap<String, Integer> Swahili_digit19 = new HashMap<>();

    static {
        Swahili_digit19.put("moja", 1);
        Swahili_digit19.put("mbili", 2);
        Swahili_digit19.put("tatu", 3);
        Swahili_digit19.put("nne", 4);
        Swahili_digit19.put("tano", 5);
        Swahili_digit19.put("sita", 6);
        Swahili_digit19.put("saba", 7);
        Swahili_digit19.put("nane", 8);
        Swahili_digit19.put("tisa", 9);

        Swahili_digit19.put("MOJA", 1);
        Swahili_digit19.put("MBILI", 2);
        Swahili_digit19.put("TATU", 3);
        Swahili_digit19.put("NNE", 4);
        Swahili_digit19.put("TANO", 5);
        Swahili_digit19.put("SITA", 6);
        Swahili_digit19.put("SABA", 7);
        Swahili_digit19.put("NANE", 8);
        Swahili_digit19.put("TISA", 9);
    }

    private final static HashMap<String, Integer> Swahili_tens = new HashMap<>();

    static {
        Swahili_tens.put("kumi", 10);
        Swahili_tens.put("ishirini", 20);
        Swahili_tens.put("thelathini", 30);
        Swahili_tens.put("arobaini", 40);
        Swahili_tens.put("hamsini", 50);
        Swahili_tens.put("sitini", 60);
        Swahili_tens.put("sabini", 70);
        Swahili_tens.put("themanini", 80);
        Swahili_tens.put("tisini", 90);

        Swahili_tens.put("KUMI", 10);
        Swahili_tens.put("ISHIRINI", 20);
        Swahili_tens.put("THELATHINI", 30);
        Swahili_tens.put("AROBAINI", 40);
        Swahili_tens.put("HAMSINI", 50);
        Swahili_tens.put("SITINI", 60);
        Swahili_tens.put("SABINI", 70);
        Swahili_tens.put("THEMANINI", 80);
        Swahili_tens.put("TISINI", 90);
    }

    private final static HashMap<String, Integer> Swahili_mia = new HashMap<>();

    static {
        Swahili_mia.put("mia", 100);
        Swahili_mia.put("MIA", 100);
    }

    private final static HashMap<String, Long> Swahili_power = new HashMap<>();

    static {
        Swahili_power.put("elfu", 1000L);
        Swahili_power.put("milioni", 1000000L);
        Swahili_power.put("bilioni", 1000000L);
        Swahili_power.put("trilioni", 1000000L);
        Swahili_power.put("kwadrilioni", 1000000L);

        Swahili_power.put("ELFU", 1000L);
        Swahili_power.put("MILIONI", 1000000L);
        Swahili_power.put("BILIONI", 1000000000L);
        Swahili_power.put("TRILIONI", 1000000000000L);
        Swahili_power.put("KWADRILIONI", 1000000000000000L);
    }

    private final static HashMap<String, String> Swahili_na = new HashMap<>();

    static {
        Swahili_na.put("na", "");
        Swahili_na.put("NA", "");
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


    static public int getValueType(String word, String langFtr) {

        int state = TCONST.W2N_ERR;

        switch(langFtr) {
            case TCONST.LANG_SW:

                if(Swahili_digit00.containsKey(word))     state = TCONST.W2N_ZERO;
                else if(Swahili_digit19.containsKey(word))state = TCONST.W2N_DIGIT;
                else if(Swahili_tens.containsKey(word))   state = TCONST.W2N_TENS;
                else if(Swahili_mia.containsKey(word))    state = TCONST.W2N_HUNDREDS;
                else if(Swahili_power.containsKey(word))  state = TCONST.W2N_POWER;
                else if(Swahili_na.containsKey(word))     state = TCONST.W2N_CONJ;
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


    static public long transformSW(ArrayList<String> words)  {

        int nextState;
        int state  = TCONST.STARTSTATE;
        int index  = 0;
        int multi  = TCONST.UNSET;

        long value     = 0;

        long power     = 0;
        long prevPower = 0;

        long multiplier = 0;
        long prevMultiplier  = Long.MAX_VALUE;

        int errorCode = TCONST.NO_ERROR;
        int warnCode  = TCONST.NO_WARNING;

        words.add(TCONST.NUM_EOD);

        try {

            do {

                nextState = getValueType(words.get(index), TCONST.LANG_SW);

                switch (state) {

                    // Number = Number1000s | Number100 | Number10 | digit19 | digit0

                    case TCONST.STARTSTATE:

                        switch (nextState) {
                            case TCONST.W2N_ZERO:
                            case TCONST.W2N_DIGIT:
                            case TCONST.W2N_TENS:
                            case TCONST.W2N_HUNDREDS:
                            case TCONST.W2N_POWER:
                                state = nextState;
                                break;

                            case TCONST.W2N_CONJ:
                                errorCode = TCONST.ERRW2N_LEADING_CONJ;
                                state = TCONST.W2N_EOD;
                                break;

                            case TCONST.W2N_EOD:
                                errorCode = TCONST.ERRW2N_NO_DATA;
                                state = TCONST.W2N_EOD;
                                break;
                        }
                        break;


                    // Number = digit0

                    case TCONST.W2N_ZERO:

                        value    = 0;

                        switch (nextState) {
                            case TCONST.W2N_ZERO:
                            case TCONST.W2N_DIGIT:
                            case TCONST.W2N_TENS:
                            case TCONST.W2N_HUNDREDS:
                            case TCONST.W2N_POWER:
                            case TCONST.W2N_CONJ:

                                errorCode = TCONST.ERRW2N_NONTERM_ZERO;
                                state = TCONST.W2N_EOD;
                                break;
                        }
                        break;

                    // Number = X | digit19

                    case TCONST.W2N_DIGIT:

                        value    += Swahili_digit19.get(words.get(index++));
                        prevPower = Long.MAX_VALUE;  // Cause an error if this is followed by a power

                        switch (nextState) {
                            case TCONST.W2N_ZERO:

                                errorCode = TCONST.ERRW2N_NONTERM_ZERO;
                                state = TCONST.W2N_EOD;
                                break;

                            case TCONST.W2N_DIGIT:
                            case TCONST.W2N_TENS:
                            case TCONST.W2N_HUNDREDS:
                            case TCONST.W2N_POWER:

                                errorCode = TCONST.ERRW2N_MISSING_CONJ;
                                state = TCONST.W2N_EOD;
                                break;

                            case TCONST.W2N_CONJ:
                                break;

                            case TCONST.W2N_EOD:
                                warnCode = TCONST.W2N_HYPOTHESIS;
                                state = TCONST.W2N_EOD;
                                break;
                        }
                        break;

                    // Number = X | Number10 |

                    case TCONST.W2N_TENS:

                        value   += Swahili_tens.get(words.get(index++));
                        prevPower = Long.MAX_VALUE;  // Cause an error if this is followed by a power

                        switch (nextState) {
                            case TCONST.W2N_ZERO:

                                errorCode = TCONST.ERRW2N_NONTERM_ZERO;
                                state = TCONST.W2N_EOD;
                                break;

                            case TCONST.W2N_DIGIT:
                            case TCONST.W2N_TENS:
                            case TCONST.W2N_HUNDREDS:
                            case TCONST.W2N_POWER:
                                errorCode = TCONST.ERRW2N_MISSING_CONJ;
                                state = TCONST.W2N_EOD;
                                break;

                            case TCONST.W2N_CONJ:
                                break;

                            case TCONST.W2N_EOD:
                                warnCode = TCONST.W2N_HYPOTHESIS;
                                state = TCONST.W2N_EOD;
                                break;
                        }
                        break;

                    // Number = X | Number100 |
                    // Number100   = hundreds, [na, Number10] | [na digit19]

                    case TCONST.W2N_HUNDREDS:

                        index++;
                        nextState = getValueType(words.get(index), TCONST.LANG_SW);
                        prevPower  = Long.MAX_VALUE;  // Cause an error if this is followed by a power

                        switch(nextState) {
                            case TCONST.W2N_ZERO:

                                errorCode = TCONST.ERRW2N_ZERO_HUNDRED_MULTI;
                                state = TCONST.W2N_EOD;
                                break;

                            case TCONST.W2N_DIGIT:

                                value += Swahili_digit19.get(words.get(index++)) * 100;
                                break;

                            case TCONST.W2N_TENS:
                            case TCONST.W2N_HUNDREDS:
                            case TCONST.W2N_POWER:

                                errorCode = TCONST.ERRW2N_MISSING_HUNDRED_MULTI;
                                state = TCONST.W2N_EOD;
                                break;

                            case TCONST.W2N_CONJ:

                                errorCode = TCONST.ERRW2N_HUNDRED_ADDED_CONJ;
                                state = TCONST.W2N_EOD;
                                break;

                            case TCONST.W2N_EOD:
                                warnCode = TCONST.W2N_DANGLING_HUNDRED_WARN;
                                state = TCONST.W2N_EOD;
                                break;
                        }
                        break;

                    // Number = Number1000s |

                    case TCONST.W2N_POWER:

                        power = Swahili_power.get(words.get(index++));

                        // It's an error if the power is increasing it may repeat for different mulipliers 1 , 10 , 100
                        // e.g.  thousand -> million
                        //
                        if(power > prevPower) {

                            errorCode = TCONST.ERRW2N_INCREASING_POWER;
                            state = TCONST.W2N_EOD;
                        }
                        else {

                            // If this is a new power then reset the multiplier test
                            // the multiplier must be monotonically decreasing within a power
                            //
                            if(power < prevPower) {
                                prevMultiplier = Long.MAX_VALUE;
                            }

                            prevPower = power;

                            switch (nextState) {
                                case TCONST.W2N_ZERO:

                                    errorCode = TCONST.ERRW2N_NONTERM_ZERO;
                                    state = TCONST.W2N_EOD;
                                    break;

                                case TCONST.W2N_DIGIT:

                                    // It's an error if the multiplier is increasing or repeated
                                    // e.g.  10 -> 100
                                    // elfu * 10 -> elfu * 100
                                    //
                                    multiplier  = Swahili_digit19.get(words.get(index++));
                                    nextState = TCONST.W2N_POWER_MULTIPLIER;
                                    break;

                                case TCONST.W2N_TENS:

                                    multiplier  = Swahili_tens.get(words.get(index++));
                                    nextState = TCONST.W2N_POWER_MULTIPLIER;
                                    break;

                                case TCONST.W2N_HUNDREDS:

                                    index++;
                                    nextState = getValueType(words.get(index), TCONST.LANG_SW);

                                    switch(nextState) {

                                        case TCONST.W2N_ZERO:

                                            errorCode = TCONST.ERRW2N_NONTERM_ZERO;
                                            state = TCONST.W2N_EOD;
                                            break;

                                        case TCONST.W2N_DIGIT:
                                            multiplier  = Swahili_digit19.get(words.get(index++)) * 100;
                                            nextState = TCONST.W2N_POWER_MULTIPLIER;
                                            break;

                                        case TCONST.W2N_TENS:
                                        case TCONST.W2N_HUNDREDS:
                                        case TCONST.W2N_POWER:

                                            errorCode = TCONST.ERRW2N_MISSING_HUNDRED_MULTI;
                                            state = TCONST.W2N_EOD;
                                            break;

                                        case TCONST.W2N_CONJ:

                                            errorCode = TCONST.ERRW2N_HUNDRED_ADDED_CONJ;
                                            state = TCONST.W2N_EOD;
                                            break;

                                        case TCONST.W2N_EOD:
                                            warnCode = TCONST.W2N_DANGLING_HUNDRED_WARN;
                                            state = TCONST.W2N_EOD;
                                            break;
                                    }
                                    break;

                                case TCONST.W2N_POWER:
                                    errorCode = TCONST.ERRW2N_REPEATED_POWER;
                                    state = TCONST.W2N_EOD;
                                    break;

                                case TCONST.W2N_CONJ:
                                    errorCode = TCONST.ERRW2N_POWER_CONJ;
                                    state = TCONST.W2N_EOD;
                                    break;

                                case TCONST.W2N_EOD:
                                    warnCode = TCONST.W2N_DANGLING_POWER_WARN;
                                    state = TCONST.W2N_EOD;
                                    break;
                            }
                        }
                        break;

                    case TCONST.W2N_POWER_MULTIPLIER:

                        if(multiplier >= prevMultiplier) {

                            errorCode = TCONST.ERRW2N_INCREASING_MULTIPLIER;
                            state = TCONST.W2N_EOD;
                        }
                        else {
                            prevMultiplier = multiplier;
                        }

                        value += power * multiplier;

                        switch (nextState) {
                            case TCONST.W2N_ZERO:
                                errorCode = TCONST.ERRW2N_NONTERM_ZERO;
                                state = TCONST.W2N_EOD;
                                break;

                            case TCONST.W2N_DIGIT:
                            case TCONST.W2N_TENS:
                            case TCONST.W2N_HUNDREDS:
                            case TCONST.W2N_POWER:
                                errorCode = TCONST.ERRW2N_MISSING_CONJ;
                                state = TCONST.W2N_EOD;
                                break;

                            case TCONST.W2N_CONJ:
                                index++;
                                break;

                            case TCONST.W2N_EOD:
                                warnCode = TCONST.W2N_HYPOTHESIS;
                                state = TCONST.W2N_EOD;
                                break;
                        }
                        break;


                    case TCONST.W2N_CONJ:

                        switch (nextState) {
                            case TCONST.W2N_ZERO:
                                errorCode = TCONST.ERRW2N_NONTERM_ZERO;
                                state = TCONST.W2N_EOD;
                                break;

                            case TCONST.W2N_DIGIT:
                            case TCONST.W2N_TENS:
                            case TCONST.W2N_HUNDREDS:
                            case TCONST.W2N_POWER:
                                break;

                            case TCONST.W2N_CONJ:
                                errorCode = TCONST.ERRW2N_REPEAT_CONJ;
                                state = TCONST.W2N_EOD;
                                break;

                            case TCONST.W2N_EOD:
                                warnCode = TCONST.W2N_DANGLING_CONJ_WARN;
                                state = TCONST.W2N_EOD;
                                break;
                        }
                        break;

                    // We should never loop on this
                    case TCONST.W2N_EOD:

                        errorCode = TCONST.ERRW2N_INTERNAL;
                        state = TCONST.W2N_EOD;
                        break;
                }

            } while (state != TCONST.W2N_EOD);
        }
        catch(Exception e) {
            Log.e("W2N", "Error: " + e );
        }


        return value;
    }

    private int transformEN(ArrayList<String> words)  {

        int nextState;
        int state  = TCONST.STARTSTATE;
        int index  = 0;
        int multi  = TCONST.UNSET;
        int result = TCONST.UNSET;

        words.add(TCONST.NUM_EOD);

//        try {

//            nextState = getValueType(words.get(index), langFtr);
//
//            do {
//
//                state     = nextState;
//                nextState = getValueType(words.get(index), langFtr);
//
//                switch (state) {
//                    //
//                    case TCONST.STARTSTATE:
//
//                        switch (langFtr) {
//
//                            case TCONST.LANG_EN:
//                                switch (state) {
//
//                                }
//                                break;
//
//                            case TCONST.LANG_SW:
//                                switch (state) {
//                                    case TCONST.W2N_ZERO:
//                                        value = 0;
//
//                                        if(nextState != TCONST.W2N_EOD) {
//
//                                        }
//                                        break;
//
//                                    case TCONST.W2N_DIGIT:
//
//
//
//                                    case TCONST.W2N_POWER:
//                                        break;
//
//                                    case TCONST.W2N_CONJ:
//                                        break;
//
//                                    case TCONST.W2N_EOD:
//                                        break;
//                                }
//                                break;
//                        }
//                        break;
//
//                    case TCONST.W2N_ZERO:
//
//
//                        break;
//
//                    case TCONST.W2N_DIGIT:
//
//                        state = getValueType(words.get(index), langFtr);
//
//                        switch (langFtr) {
//                            case TCONST.LANG_EN:
//                                switch (state) {
//
//                                }
//                                break;
//
//                            case TCONST.LANG_SW:
//
//                                break;
//                        }
//                        break;
//
//                    case TCONST.W2N_TENS:
//                        break;
//
//                    case TCONST.W2N_POWER:
//                        break;
//
//                    case TCONST.W2N_CONJ:
//                        break;
//
//                    case TCONST.W2N_EOD:
//                        break;
//
//
//
//
//                }
//            } while (state != TCONST.W2N_EOD);
//        }
//        catch(Exception e) {
//        }


        return result;
    }

}
