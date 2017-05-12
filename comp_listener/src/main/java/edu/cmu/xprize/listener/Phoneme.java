package edu.cmu.xprize.listener;

/* 
Target Language text to phoneme Package in Java, 
derived from the C source code written by John A. Wasser <speech@John-Wasser.com>, 
available at http://ww.John-Wasser.com/TextToSpeech/

Translated to Java by Olivier Sarrat, olivier_sarrat@hotmail.com

Modifications to support multiple languages: Copyright(c) 2016  Kevin Willows 
*/


/*
**      Target Language to Phoneme translation.
**
**      tarLang.rules are made up of four parts:
**      
**              The left context.
**              The text to match.
**              The right context.
**              The phonemes to substitute for the matched text.
**
**      Procedure:
**
**              Seperate each block of letters (apostrophes included) 
**              and add a space on each side.  For each unmatched 
**              letter in the word, look through the tarLang.rules where the 
**              text to match starts with the letter in the word.  If 
**              the text to match is found and the right and left 
**              context patterns also match, output the phonemes for 
**              that rule and skip to the next unmatched letter.
**
**
**      Special Context Symbols:
**
**              #       One or more vowels
**              :       Zero or more consonants
**              ^       One consonant.
**              .       One of B, D, V, G, J, L, M, N, R, W or Z (voiced 
**                      consonants)
**              %       One of ER, E, ES, ED, ING, ELY (a suffix)
**                      (Right context only)
**              +       One of E, I or Y (a "front" vowel)
*/


import java.util.HashMap;
import java.util.Locale;

import cmu.xprize.comp_logging.CErrorManager;


public class Phoneme {

    // This is used to map "language features" to the associated dictionary filenames
    // these are located in the assets/sync/models/lm
    // Note: on Android these are case sensitive filenames

    static private HashMap<String, Class> langMap = new HashMap<String, Class>();

    static {
        langMap.put("LANG_EN", RulesEnglish.class);
        langMap.put("LANG_SW", RulesSwahili.class);
    }

    static private IPhonemeRules tarLang;
    static private String  TAG = "Phoneme";


    public static void setTargetLanguage(String targetlang) {

        try {
            Class<?> phonemeClass = langMap.get(targetlang);

            tarLang = (IPhonemeRules)phonemeClass.newInstance();

            //System.out.printf("Phoneme class type:%s\n", phonemeClass.getName());

        }
        catch (Exception e) {
            CErrorManager.logEvent(TAG, "Langauge configuration error: ", e, false);
        }
    }

    public static boolean isupper(char chr) {
        return (!(chr < 'A' || chr > 'Z'));
    }

    public static boolean islower(char chr) {
        return (!(chr < 'a' || chr > 'z'));
    }

    public static boolean isalpha(char chr) {
        return (isupper(chr) || islower(chr));
    }

    public static boolean isvowel(char chr) {
        return (chr == 'A' || chr == 'E' || chr == 'I' ||
                chr == 'O' || chr == 'U');
    }

    public static boolean isconsonant(char chr) {
        return (isupper(chr) && !isvowel(chr));
    }

    private static String xlate_word(String word) {
        int index;      /* Current position in word */
        int type;       /* First letter of match part */
        int wordLength = word.length();

        String phoneme = "";
        int indexRule;

        index = 1;      /* Skip the initial blank */
        do {
            if (isupper(word.charAt(index)))
                type = word.charAt(index) - 'A' + 1;
            else
                type = 0;

            indexRule = find_rule(word, index, tarLang.getRules()[type]);

            if (indexRule == -1)
                index++;
            else {
                phoneme = phoneme + tarLang.getRules()[type][indexRule][tarLang.OUT_PART];
                index += tarLang.getRules()[type][indexRule][tarLang.MATCH_PART].length();
            }
        }
        while (index < wordLength);
        return phoneme;
    }

    private static int find_rule(String word, int index, String chosenRules[][]) {
        String rule[];
        int indexRule = 0;
        String left, match, right, output;
        int indexMatch = 0;
        int remainder;
        int wordLength = word.length();

        for (; ; )        /* Search for the rule */ {
            rule = chosenRules[indexRule];
            indexRule++;
            match = rule[tarLang.MATCH_PART];

            if (match.equals( "!%@$#")) /* bad symbol! */ {
                //System.err.println("Error: Can't find rule for: "+word.charAt(index)+" in "+word);
                return -1; /* Skip it! */
            }
            for (remainder = index, indexMatch = 0; (indexMatch != match.length()) && (remainder != wordLength); indexMatch++, remainder++) {
                if (match.charAt(indexMatch) != word.charAt(remainder))
                    break;
            }

            if (indexMatch != match.length())     /* found missmatch */
                continue;

            left = rule[tarLang.LEFT_PART];
            right = rule[tarLang.RIGHT_PART];

            if (!leftmatch(left, word, index - 1))
                continue;
            if (!rightmatch(right, word, remainder))
                continue;

            return --indexRule;
        }
    }


    private static boolean leftmatch(String pattern, /* pattern to match in text */
                                     String context, /*text to be matched */
                                     int indexText)/* index of last char of text to be matched */ {
        int pat;
        String text;
        int count;

        if (pattern == "")   /* null string matches any context */ {
            return true;
        }

        /* point to last character in pattern string */
        count = pattern.length();
        pat = count - 1;

        for (; count > 0; pat--, count--) {
                /* First check for simple text or space */
            if (isalpha(pattern.charAt(pat)) || pattern.charAt(pat) == '\'' || pattern.charAt(pat) == ' ')
                if (pattern.charAt(pat) != context.charAt(indexText))
                    return false;
                else {
                    indexText--;
                    continue;
                }

            Character carpat =Character.valueOf(pattern.charAt(pat));
            if (0 == carpat.compareTo('#')) {
                        /* One or more vowels */
                if (!isvowel(context.charAt(indexText)))
                    return false;

                indexText--;

                while (isvowel(context.charAt(indexText)))
                    indexText--;
            } else if (0 == carpat.compareTo(Character.valueOf(':'))) {
                       /* Zero or more consonants */
                while (isconsonant(context.charAt(indexText)))
                    indexText--;
            } else if (0 == carpat.compareTo(Character.valueOf('^'))) {
                        /* One consonant */
                if (!isconsonant(context.charAt(indexText)))
                    return false;
                indexText--;
            } else if (0 == carpat.compareTo(Character.valueOf('.'))) {
                       /* B, D, V, G, J, L, M, N, R, W, Z */
                if (context.charAt(indexText) != 'B' && context.charAt(indexText) != 'D' && context.charAt(indexText) != 'V'
                        && context.charAt(indexText) != 'G' && context.charAt(indexText) != 'J' && context.charAt(indexText) != 'L'
                        && context.charAt(indexText) != 'M' && context.charAt(indexText) != 'N' && context.charAt(indexText) != 'R'
                        && context.charAt(indexText) != 'W' && context.charAt(indexText) != 'Z')
                    return false;
                indexText--;
            } else if (0 == carpat.compareTo(Character.valueOf('+'))) {
                       /* E, I or Y (front vowel) */
                if (context.charAt(indexText) != 'E' && context.charAt(indexText) != 'I' && context.charAt(indexText) != 'Y')
                    return false;
                indexText--;
            } else {
                System.err.println("Bad char in left rule: '" + pattern.charAt(pat) + "'");
                return false;
            }
        }

        return true;
    }


    private static boolean rightmatch(String pattern, /* pattern to match in text */
                                      String context, /*text to be matched */
                                      int indexText)/* index of last char of text to be matched */ {
        if (pattern == "")   /* null string matches any context */
            return true;

        int pat = 0;
        for (pat = 0; pat != pattern.length(); pat++) {
                /* First check for simple text or space */
            if (isalpha(pattern.charAt(pat)) || pattern.charAt(pat) == '\'' || pattern.charAt(pat) == ' ')
                if (pattern.charAt(pat) != context.charAt(indexText))
                    return false;
                else {
                    indexText++;
                    continue;
                }
            Character carpat = Character.valueOf(pattern.charAt(pat));
            if (0 == carpat.compareTo(Character.valueOf('#'))) {
                       /* One or more vowels */
                if (!isvowel(context.charAt(indexText)))
                    return false;

                indexText++;

                while (isvowel(context.charAt(indexText)))
                    indexText++;
            } else if (0 == carpat.compareTo(Character.valueOf(':'))) {
                       /* Zero or more consonants */
                while (isconsonant(context.charAt(indexText)))
                    indexText++;
            } else if (0 == carpat.compareTo(Character.valueOf('^'))) {
                       /* One consonant */
                if (!isconsonant(context.charAt(indexText)))
                    return false;
                indexText++;
            } else if (0 == carpat.compareTo(Character.valueOf('.'))) {
                        /* B, D, V, G, J, L, M, N, R, W, Z */
                if (context.charAt(indexText) != 'B' && context.charAt(indexText) != 'D' && context.charAt(indexText) != 'V'
                        && context.charAt(indexText) != 'G' && context.charAt(indexText) != 'J' && context.charAt(indexText) != 'L'
                        && context.charAt(indexText) != 'M' && context.charAt(indexText) != 'N' && context.charAt(indexText) != 'R'
                        && context.charAt(indexText) != 'W' && context.charAt(indexText) != 'Z')
                    return false;
                indexText++;
            } else if (0 == carpat.compareTo(Character.valueOf('+'))) {
                       /* E, I or Y (front vowel) */
                if (context.charAt(indexText) != 'E' && context.charAt(indexText) != 'I' && context.charAt(indexText) != 'Y')
                    return false;
                indexText++;
            } else if (0 == carpat.compareTo(Character.valueOf('%'))) {
                       /* ER, E, ES, ED, ING, ELY (a suffix) */
                if (context.charAt(indexText) == 'E') {
                    indexText++;
                    if (context.charAt(indexText) == 'L') {
                        indexText++;
                        if (context.charAt(indexText) == 'Y') {
                            indexText++;
                            break;
                        } else {
                            indexText--; /* Don't gobble L */
                            break;
                        }
                    } else if (context.charAt(indexText) == 'R' || context.charAt(indexText) == 'S'
                            || context.charAt(indexText) == 'D')
                        indexText++;
                    break;
                } else if (context.charAt(indexText) == 'I') {
                    indexText++;
                    if (context.charAt(indexText) == 'N') {
                        indexText++;
                        if (context.charAt(indexText) == 'G') {
                            indexText++;
                            break;
                        }
                    }
                    return false;
                } else
                    return false;
            } else {
                System.err.println("Bad char in right rule:'" + pattern.charAt(pat) + "'");
                return false;
            }
        }

        return true;
    }

    public static String toPhoneme(String text) {

        return (xlate_word(" " + text.toUpperCase(Locale.US) + " "));
    }
}


