//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Haonan Sun All Rights Reserved
//    Copyright(c) Abhinav Gupta All Rights Reserved
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

import java.util.Random;
import java.util.Scanner;


public class Num2Word {
    private static String[] EnglishInTwentyWords = {
            "",
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine",
            "ten",
            "eleven",
            "twelve",
            "thirteen",
            "fourteen",
            "fifteen",
            "sixteen",
            "seventeen",
            "eighteen",
            "nineteen",
    };

    private static String[] EnglishTens = {
            "",
            "ten",
            "twenty",
            "thirty",
            "forty",
            "fifty",
            "sixty",
            "seventy",
            "eighty",
            "ninety",
    };

    // TODO: 15/12/1 check the online swahili cheat sheet

    public static String SwahiliZero = "sifuri";
    public static String[] SwahiliInTenWords = {
            "",
            "moja",
            "mbili",
            "tatu",
            "nne",
            "tano",
            "sita",
            "saba",
            "nane",
            "tisa",
            "kumi",
    };

    public static String[] SwahiliTens = {
            "",
            "kumi",
            "ishirini",
            "thelathini",
            "arobaini",
            "hamsini",
            "sitini",
            "sabini",
            "themanini",
            "tisini",
    };

    public static void main(String[] args) {

        Random randomGenerator = new Random();
        int num;

        String language = "sw";
        Scanner in = new Scanner(System.in);
        while (true) {
            num = in.nextInt();
            System.out.println("Number: \t" + num);
            System.out.println(language + ":\t" + transform(num, language));
            System.out.println("--------------------------------------------------");
        }

    }

    /**
     * convert 0~999,999 to English Words
     * convert 0~9,999 to Swahili Words
     *
     * @param num
     * @param language
     * @return
     */
    public static String transform(int num, String language) {
        if (language.equalsIgnoreCase("sw")) {
            return Num2Swahili(num);
        } else { // language.equalsIgnoreCase("English")
            return Num2English(num);
        }
    }

    private static String Num2English(int num) {

        if (num == 0) {
            return "zero";
        } else if (num <= 999) {
            return SmallNum2English(num);
        } else {
            String rsl = "";

            if (num % 1000 != 0) {
                rsl = SmallNum2English(num % 1000);
            }
            num /= 1000;

            return SmallNum2English(num) + " thousand " + rsl;

        }
    }

    private static String SmallNum2English(int num) {
        String rsl;
        if (num % 100 < 20) {
            rsl = EnglishInTwentyWords[num % 100];
            num = num / 100;
        } else {  // num % 100 < 20
            rsl = EnglishInTwentyWords[num % 10];
            num /= 10;

            rsl = EnglishTens[num % 10] + " " + rsl;
            num /= 10;

        }

        if (num == 0) {
            return rsl;
        } else {
            return EnglishInTwentyWords[num] + " hundred " + rsl;
        }
    }

    private static String Num2Swahili(int num) {
        String rsl;
        if (num == 0) return SwahiliZero;


        rsl = SwahiliInTenWords[num % 10];

        num /= 10;
        if (num == 0) return rsl;

        if (rsl.equalsIgnoreCase("")) {
            if (num % 10 != 0) {
                rsl = SwahiliTens[num % 10];
            }
        } else {
            if (num % 10 != 0) {
                rsl = SwahiliTens[num % 10] + " na " + rsl;
            }
        }


        num /= 10;
        if (num == 0) return rsl;

        if (rsl.equalsIgnoreCase("")) {
            if (num % 10 != 0) {
                rsl = "mia " + SwahiliInTenWords[num % 10];
            }
        } else {
            if (num % 10 != 0) {
                rsl = "mia " + SwahiliInTenWords[num % 10] + " na " + rsl;
            }
        }

        num /= 10;
        if (num == 0) return rsl;

        if (rsl.equalsIgnoreCase("")) {
            if (num % 10 != 0) {
                rsl = "elfu " + SwahiliInTenWords[num % 10];
            }
        } else {
            if (num % 10 != 0) {
                rsl = "elfu " + SwahiliInTenWords[num % 10] + " na " + rsl;
            }
        }

        return rsl;

    }
}
