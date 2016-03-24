package cmu.xprize.util;

/**
 * Created by haonansun on 15/11/30.
 */
public class Num2Word {
    /*
    English numerals
     */
    private static String EnglishZero = "zero";
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

    /*
    RulesSwahili numerals
     */
    private static String SwahiliZero = "sifuri";
    private static String[] SwahiliInTenWords = {
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

    private static String[] SwahiliTens = {
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


    /**
     * convert 0~999,999 to English Words
     * convert 0~9,999 to RulesSwahili Words
     *
     * @param num
     * @param language
     * @return
     */
    public static String transform(int num, String language) {
        if (language.equalsIgnoreCase("LANG_SW")) {
            return Num2Swahili(num);
        } else { // language.equalsIgnoreCase("English")
            return Num2English(num);
        }
    }

    private static String Num2English(int num) {

        if (num == 0) {
            return EnglishZero;
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
