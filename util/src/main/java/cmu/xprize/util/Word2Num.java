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

import java.util.*;

public class Word2Num {

    private static List<Integer> NumberList;

    private final static HashMap<String, Integer> SwahiliInTenMap = new HashMap<>();

    static {
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

        SwahiliInTenMap.put("SIFURI", 0);
        SwahiliInTenMap.put("MOJA", 1);
        SwahiliInTenMap.put("MBILI", 2);
        SwahiliInTenMap.put("TATU", 3);
        SwahiliInTenMap.put("NNE", 4);
        SwahiliInTenMap.put("TANO", 5);
        SwahiliInTenMap.put("SITA", 6);
        SwahiliInTenMap.put("SABA", 7);
        SwahiliInTenMap.put("NANE", 8);
        SwahiliInTenMap.put("TISA", 9);
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

    private final static HashMap<String, Integer> EnglishNumeralMap = new HashMap<>();

    static {
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

        EnglishNumeralMap.put("ZERO", 0);

        EnglishNumeralMap.put("ONE", 1);
        EnglishNumeralMap.put("TWO", 2);
        EnglishNumeralMap.put("THREE", 3);
        EnglishNumeralMap.put("FOUR", 4);
        EnglishNumeralMap.put("FIVE", 5);
        EnglishNumeralMap.put("SIX", 6);
        EnglishNumeralMap.put("SEVEN", 7);
        EnglishNumeralMap.put("EIGHT", 8);
        EnglishNumeralMap.put("NINE", 9);

        EnglishNumeralMap.put("TEN", 10);
        EnglishNumeralMap.put("ELEVEN", 11);
        EnglishNumeralMap.put("TWELVE", 12);
        EnglishNumeralMap.put("THIRTEEN", 13);
        EnglishNumeralMap.put("FOURTEEN", 14);
        EnglishNumeralMap.put("FIFTEEN", 15);
        EnglishNumeralMap.put("SIXTEEN", 16);
        EnglishNumeralMap.put("SEVENTEEN", 17);
        EnglishNumeralMap.put("EIGHTEEN", 18);
        EnglishNumeralMap.put("NINETEEN", 19);

        EnglishNumeralMap.put("TWENTY", 20);
        EnglishNumeralMap.put("THIRTY", 30);
        EnglishNumeralMap.put("FORTY", 40);
        EnglishNumeralMap.put("FIFTY", 50);
        EnglishNumeralMap.put("SIXTY", 60);
        EnglishNumeralMap.put("SEVENTY", 70);
        EnglishNumeralMap.put("EIGHTY", 80);
        EnglishNumeralMap.put("NINETY", 90);
    }



    public static List transform(String word, String language) {

        String[] words = word.split(" ");
        if (words.length == 0) {
            return null;
        }

        int rsl = transform(words, language);

        return NumberList;
    }

    public static int transform(String[] words, String language) {

        NumberList = new ArrayList<Integer>();

        if (language.equalsIgnoreCase("sw")) {
            return SwahiliWord2Num(words);
        } else {
            return EnglishWord2Num(words);
        }
    }

    public static List getNumberList() {
        return NumberList;
    }

    private static int EnglishWord2Num(String[] words) {

        int rsl = 0;
        int thousandIdx = words.length;
        int thousandstr;

        for (int i = 0; i < words.length; i++) {
            if (words[i].equalsIgnoreCase("thousand")) {
                thousandIdx = i;
                break;
            }
        }

        String[] tmp = new String[thousandIdx];
        for (int i = 0; i < thousandIdx; i++) {
            tmp[i] = words[i];
        }
        int getans = EnglishWord2SmallNum(0, tmp);
        
        if (getans >= 0){
            rsl += getans;
        }
        else {
            return -1;
        }

        if (thousandIdx == words.length) {
            return rsl;
        } 
        else {
            thousandstr = rsl * 1000;
            if(rsl != 0){
                NumberList.add(thousandstr);
            }
            tmp = new String[words.length - thousandIdx - 1];
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = words[thousandIdx + 1 + i];
            }
            getans = EnglishWord2SmallNum(thousandstr, tmp);
            if (getans > 0) {
                int ans = thousandstr + getans;
                return ans;
            }
            else {
                return -1;
            }
        }

    }

    private static int EnglishWord2SmallNum(int thousandstr, String[] words) {
        
        int rsl = 0;
        String notFound;
        for (int i = 0; i < words.length; i++) {
            if (!words[i].equalsIgnoreCase("hundred")) {
                if (EnglishNumeralMap.containsKey(words[i])) {
                    int val = EnglishNumeralMap.get(words[i]);
                    rsl += val;
                    NumberList.add(thousandstr + rsl);
                    
                } else {
                    return -100000;
                }
            } else {
                rsl *= 100;
                NumberList.add(thousandstr + rsl);
            }
        }
        return rsl;
    }

    private static int SwahiliWord2Num(String[] words) {
        
        int rsl = 0;
        int idx = 0;

        if (words[idx].equalsIgnoreCase("elfu")) {
            rsl += SwahiliInTenMap.get(words[idx + 1]) * 1000;
            NumberList.add(rsl);
            idx += 2;

            if (idx >= words.length) {
                return rsl;
            } else {
                if (!words[idx].equalsIgnoreCase("na")) {
                    return rsl;
                } else {
                    idx += 1;
                }
            }
        }


        if (words[idx].equalsIgnoreCase("mia") && idx < words.length) {
            rsl += SwahiliInTenMap.get(words[idx + 1]) * 100;
            NumberList.add(rsl);
            idx += 2;

            if (idx >= words.length) {
                return rsl;
            } else {
                if (!words[idx].equalsIgnoreCase("na")) {
                    return rsl;
                } else {
                    idx += 1;
                }
            }
        }

        if (SwahiliTensMap.containsKey(words[idx])) {
            rsl += SwahiliTensMap.get(words[idx]);
            NumberList.add(rsl);
            idx += 1;
            if (idx >= words.length) {
                return rsl;
            } else {
                if (!words[idx].equalsIgnoreCase("na")) {
                    return rsl;
                } else {
                    idx += 1;
                }
            }
        }

        rsl += SwahiliInTenMap.get(words[idx]);
        NumberList.add(rsl);
        return rsl;

    }

}
