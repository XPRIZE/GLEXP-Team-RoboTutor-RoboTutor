package edu.cmu.xprize.listener;


/*
English text to phoneme Package in Java,
derived from the C source code written by John A. Wasser <speech@John-Wasser.com>,
available at http://ww.John-Wasser.com/TextToSpeech/

Translated to Java by Olivier Sarrat, olivier_sarrat@hotmail.com
*/

/*
**      English to Phoneme rules.
**
**      Derived from:
**
**           AUTOMATIC TRANSLATION OF ENGLISH TEXT TO PHONETICS
**                  BY MEANS OF LETTER-TO-SOUND RULES
**
**                      NRL Report 7948
**
**                    January 21st, 1976
**          Naval Research Laboratory, Washington, D.C.
**
**
**      Published by the National Technical Information Service as
**      document "AD/A021 929".
**
**
**
**      The Phoneme codes:
**
**              IY      bEEt            IH      bIt
**              EY      gAte            EH      gEt
**              AE      fAt             AA      fAther
**              AO      lAWn            OW      lOne
**              UH      fUll            UW      fOOl
**              ER      mURdER          AX      About
**              AH      bUt             AY      hIde
**              AW      hOW             OY      tOY
**
**              p       Pack            b       Back
**              t       Time            d       Dime
**              k       Coat            g       Goat
**              f       Fault           v       Vault
**              TH      eTHer           DH      eiTHer
**              s       Sue             z       Zoo
**              SH      leaSH           ZH      leiSure
**              HH      How             m       suM
**              n       suN             NG      suNG
**              l       Laugh           w       Wear
**              y       Young           r       Rate
**              CH      String          j       Jar
**              WH      WHere
**
**
**      Strings are made up of four parts:
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
**              letter in the word, look through the rules where the
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
**                      (Found in right context only)
**              +       One of E, I or Y (a "front" vowel)
**
*/

import edu.cmu.xprize.listener.IPhonemeRules;

public class RulesSwahili implements IPhonemeRules {

    /* Context definitions */
    static private String Anything = "";    /* No context requirement */
    static private String Nothing = " ";    /* Context is beginning or end of word */

    /* Phoneme definitions */
    static private String Pause = " ";      /* Short silence */
    static private String Silent = "";      /* No phonemes */

/* Phoneme definitions - 41 Strings for phonemes in English
AY, AW, OY, AND WH need two unicode chars to make std IPA representation*/

    // andersw: change to use space-terminated CMU phonemes, not IPA symbols
    static private String IY = "IY ";
    static private String IH = "IH ";
    static private String EY = "EY ";
    static private String EH = "EH ";
    static private String AE = "AE ";

    static private String AA = "AA ";
    static private String AO = "AO ";
    static private String OW = "OW ";
    static private String UH = "UH ";
    static private String UW = "UW ";

    static private String ER = "ER ";
    static private String AX = "AH ";	// andersw: AH used for AX in our "small phoneset" dictionary
    static private String AH = "AH ";
    static private String AY = "AY ";
    static private String AW = "AW ";

    static private String OY = "OY ";
    static private String p  = "P ";
    static private String b  = "B ";
    static private String t  = "T ";
    static private String d  = "D ";

    static private String k  = "K ";
    static private String g  = "G ";
    static private String f  = "F ";
    static private String v  = "V ";
    static private String TH = "TH ";

    static private String DH = "DH ";
    static private String s  = "S ";
    static private String z  = "Z ";
    static private String SH = "SH ";
    static private String ZH = "ZH ";

    static private String HH = "HH ";
    static private String m  = "M ";
    static private String n  = "N ";
    static private String NG = "NG ";
    static private String l  = "L ";

    static private String w  = "W ";
    static private String y  = "Y ";
    static private String r  = "R ";
    static private String CH = "CH ";
    static private String j  = "JH ";

    static private String WH = "W ";	// andersw: W used for WH in our "small phoneset" dictionary


    public RulesSwahili() {
    }

    public String [][][] getRules() {
        return rules;
    }


/* = Punctuation */
/*
**      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
*/

    static String punct_rules[][] =
            {
                    {Anything,      " ",            Anything,       Pause   },
                    {Anything,      "-",            Anything,       Silent  },
                    {".",           "'S",           Anything,       z     	},
                    {"#:.E",        "'S",           Anything,       z     	},
                    {"#",           "'S",           Anything,       z     	},
                    {Anything,      "'",            Anything,       Silent  },
                    {Anything,      ",",            Anything,       Pause   },
                    {Anything,      ".",            Anything,       Pause   },
                    {Anything,      "?",            Anything,       Pause   },
                    {Anything,      "!",            Anything,       Pause   },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

/*
**      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
*/

    static String A_rules[][] =
            {
                    {Anything,      "A",            Anything,       AA    },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

/*
**      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
*/

    static String B_rules[][] =
            {
                    {Anything,      "B",            Anything,       b       },
                    {Anything,      "!%@$#",        Anything,       Silent  },
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String C_rules[][] =
            {
                    {Anything,      "CH",           Anything,       CH      },
                    {Anything,      "C",            Anything,       k       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String D_rules[][] =
            {
                    {Anything,      "DH",           Anything,       DH      },	// 1/13/2016 JM moved DH rule before D rule to make it fire
                    {Anything,      "D",            Anything,       d       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

/*
**      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
*/

    static String E_rules[][] =
            {
                    {Anything,      "E",            Anything,       EH      },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

/*
**      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
*/

    static String F_rules[][] =
            {
                    {Anything,      "F",            Anything,       f       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
// TODO what is the LEFT_PART of the rule w.r.t "GH"?
    static String G_rules[][] =
            {
                    {Anything,      "GH",           Anything,       r  },	// 1/13/2016 JM changed g to r to approximate it better
                    {Anything,      "G",            Anything,       g       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String H_rules[][] =
            {
                    {Anything,      "H",            "#",            HH      },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };


    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String I_rules[][] =
            {
                    {Anything,      "I",            Anything,       IY      },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String J_rules[][] =
            {
                    {Anything,      "J",            Anything,       j       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String K_rules[][] =
            {
                    {Anything,      "K",            Anything,       k       },	// 1/13/2016 JM:  KH will map to K HH, which we hope approximates it
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String L_rules[][] =
            {
                    {Anything,      "L",            Anything,       l       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String M_rules[][] =
            {
                    {Anything,      "M",            Anything,       m       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    // TODO to add " ng' " >-> NG
    static String N_rules[][] =
            {
                    {Anything,      "NG",           Anything,       NG + " " + g     },
                    {Anything,      "N",            Anything,       n       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String O_rules[][] =
            {
                    {Anything,      "O",            Anything,       OW      },  // 1/13/2016 JM changed from AO
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String P_rules[][] =
            {
                    {Anything,      "P",            Anything,       p       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    // Q is never used in Kiswahili
    static String Q_rules[][] =
            {
                    {Anything,      "!%@$#",        Anything,       Silent   }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String R_rules[][] =
            {
                    {Anything,      "R",            Anything,       r       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String S_rules[][] =
            {
                    {Anything,      "SH",           Anything,       SH      },
                    {Anything,      "S",            Anything,       s       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String T_rules[][] =
            {
                    {Anything,      "TH",           Anything,       TH      },
                    {Anything,      "T",            Anything,       t       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String U_rules[][] =
            {
                    {Anything,      "U",            Anything,       UW    },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String V_rules[][] =
            {
                    {Anything,      "V",            Anything,       v       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String W_rules[][] =
            {
                    {Anything,      "W",            Anything,       w       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    // X is never userd in Kiswahili
    static String X_rules[][] =
            {
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String Y_rules[][] =
            {
                    {Anything,      "Y",            Anything,       IH      },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    /*
    **      LEFT_PART       MATCH_PART      RIGHT_PART      OUT_PART
    */
    static String Z_rules[][] =
            {
                    {Anything,      "Z",            Anything,       z       },
                    {Anything,      "!%@$#",        Anything,       Silent  }
            };

    static final String rules[][][] =
            {
                    punct_rules,
                    A_rules, B_rules, C_rules, D_rules, E_rules, F_rules, G_rules,
                    H_rules, I_rules, J_rules, K_rules, L_rules, M_rules, N_rules,
                    O_rules, P_rules, Q_rules, R_rules, S_rules, T_rules, U_rules,
                    V_rules, W_rules, X_rules, Y_rules, Z_rules
            };
}
