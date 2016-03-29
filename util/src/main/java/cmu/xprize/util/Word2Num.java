package cmu.xprize.util;

import java.util.HashMap;

/**
 * Created by haonansun on 15/12/1.
 */
public class Word2Num {

    private static HashMap<String, Integer> SwahiliInTenMap;
    private static HashMap<String, Integer> SwahiliTensMap;
    private static HashMap<String, Integer> EnglishNumeralMap;

    public static Integer Transform(String word, String language) {

        InitializeSwahili();
        InitializeEnglish();

        String[] words = word.split(" ");
        if (words.length == 0) {
            return null;
        }
        return Transform(words, language);
    }

    public static int Transform(String[] words, String language) {
        if (language.equalsIgnoreCase("Swahili")) {
            return SwahiliWord2Num(words);
        } else {
            return EnglishWord2Num(words);
        }
    }

    public static int EnglishWord2Num(String[] words) {

        int rsl = 0;
        int thousandIdx = words.length;

        for (int i = 0; i < words.length; i++) {
            if (words[i] == "Thousand") {
                thousandIdx = i;
            }
        }

        String[] tmp = new String[thousandIdx - 1];
        for (int i = 0; i < thousandIdx; i++) {
            tmp[i] = words[i];
        }
        rsl += EnglishWord2SmallNum(tmp);

        if (thousandIdx == words.length) {
            return rsl;
        } else {
            tmp = new String[words.length - thousandIdx];
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = words[thousandIdx + 1 + i];
            }
            return rsl * 1000 + EnglishWord2Num(tmp);
        }

    }

    /**
     * 0 ~ 999
     *
     * @param words
     * @return
     */
    public static int EnglishWord2SmallNum(String[] words) {
        int rsl = 0;
        for (int i = 0; i < words.length; i++) {
            if (words[i] != "hundred") {
                if (SwahiliTensMap.containsKey(words[i])) {
                    rsl += SwahiliTensMap.get(words[i]);
                } else {
                    rsl += SwahiliTensMap.get(words[i]);
                }
            } else {
                rsl *= 100;
            }
        }
        return rsl;
    }

    public static int SwahiliWord2Num(String[] words) {
        int rsl = 0;
        int idx = 0;

        if (words[idx] == "elfu") {
            rsl += SwahiliInTenMap.get(words[idx + 1]) * 1000;

            idx += 2;

            if (idx >= words.length) {
                return rsl;
            } else {
                if (words[idx] != "na") {
                    return rsl;
                } else {
                    idx += 1;
                }
            }
        }


        if (words[idx] == "mia" && idx < words.length) {
            rsl += SwahiliInTenMap.get(words[idx + 1]) * 100;

            idx += 2;

            if (idx >= words.length) {
                return rsl;
            } else {
                if (words[idx] != "na") {
                    return rsl;
                } else {
                    idx += 1;
                }
            }
        }

        if (SwahiliTensMap.containsKey(words[idx])) {
            rsl += SwahiliTensMap.get(words[idx]);
            idx += 1;
            if (idx >= words.length) {
                return rsl;
            } else {
                if (words[idx] != "na") {
                    return rsl;
                } else {
                    idx += 1;
                }
            }
        }

        rsl += SwahiliInTenMap.get(words[idx]);

        return rsl;

    }

    private static void InitializeSwahili() {

        /*
        Initialization
         */
        SwahiliInTenMap = new HashMap<>();
        SwahiliInTenMap.put("sifuri", 0);
        SwahiliInTenMap.put("moja", 1);
        SwahiliInTenMap.put("mbili", 2);
        SwahiliInTenMap.put("tatu", 3);
        SwahiliInTenMap.put("nne", 4);
        SwahiliInTenMap.put("tano", 5);
        SwahiliInTenMap.put("sita", 6);
        SwahiliInTenMap.put("saba", 7);
        SwahiliInTenMap.put("nane", 8);
        SwahiliInTenMap.put("tisa", 9);

        SwahiliTensMap = new HashMap<>();
        SwahiliTensMap.put("kumi", 10);
        SwahiliTensMap.put("ishirini", 20);
        SwahiliTensMap.put("thelathini", 30);
        SwahiliTensMap.put("arobaini", 40);
        SwahiliTensMap.put("hamsini", 50);
        SwahiliTensMap.put("sitini", 60);
        SwahiliTensMap.put("sabini", 70);
        SwahiliTensMap.put("themanini", 90);
        SwahiliTensMap.put("tisini", 90);

    }

    private static void InitializeEnglish() {
        EnglishNumeralMap = new HashMap<>();

        EnglishNumeralMap.put("zero", 0);

        EnglishNumeralMap.put("one", 1);
        EnglishNumeralMap.put("two", 2);
        EnglishNumeralMap.put("three", 3);
        EnglishNumeralMap.put("four", 4);
        EnglishNumeralMap.put("five", 5);
        EnglishNumeralMap.put("six", 6);
        EnglishNumeralMap.put("seven", 7);
        EnglishNumeralMap.put("eight", 8);
        EnglishNumeralMap.put("nine", 9);

        EnglishNumeralMap.put("ten", 10);
        EnglishNumeralMap.put("eleven", 11);
        EnglishNumeralMap.put("twelve", 12);
        EnglishNumeralMap.put("thirteen", 13);
        EnglishNumeralMap.put("fourteen", 14);
        EnglishNumeralMap.put("fifteen", 15);
        EnglishNumeralMap.put("sixteen", 16);
        EnglishNumeralMap.put("seventeen", 17);
        EnglishNumeralMap.put("eighteen", 18);
        EnglishNumeralMap.put("nineteen", 19);

        EnglishNumeralMap.put("twenty", 20);
        EnglishNumeralMap.put("thirty", 30);
        EnglishNumeralMap.put("forty", 40);
        EnglishNumeralMap.put("fifty", 50);
        EnglishNumeralMap.put("sixty", 60);
        EnglishNumeralMap.put("seventy", 70);
        EnglishNumeralMap.put("eighty", 80);
        EnglishNumeralMap.put("ninety", 90);

    }
}

