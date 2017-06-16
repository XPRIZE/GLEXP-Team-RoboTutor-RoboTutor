//*********************************************************************************
//
//    Copyright(c) 2016-2017  Kevin Willows All Rights Reserved
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

import java.util.HashMap;

public class TCJSGF {

    // Loader Constants

    static final public String JSGF_HDR       = "#JSGF V1.0;\n";


    static final public String JSGF_GRAMMAR   = "grammar edu.cmu.xprize.number;\n";

    static final public String JSGF_PUBLIC    = "public <";
    static final public String JSGF_EQUALS    = "> = ";



    //*** JSGF number combos

    static public HashMap<String, String> conjMap = new HashMap<String, String>();

    static {
        conjMap.put("LANG_EN", "AND");
        conjMap.put("LANG_SW", "NA");
    }

    static public HashMap<String, String> digitMap = new HashMap<String, String>();

    static {
        digitMap.put("LANG_EN", "(ZERO ONE TWO THREE FOUR FIVE SIX SEVEN EIGHT NINE)");
        digitMap.put("LANG_SW", "(SIFURI MOJA MBILI TATU NNE TANO SITA SABA NANE TISA)");
    }

    static public HashMap<String, String> teensMap = new HashMap<String, String>();

    static {
        teensMap.put("LANG_EN", "(TEN ELEVEN TWELVE THIRTEEN FORTEEN FIFTEEN SIXTEEN SEVENTEEN EIGHTEEN NINETEEN)");
        teensMap.put("LANG_SW", "(KUMI)");
    }

    static public HashMap<String, String> tensMap = new HashMap<String, String>();

    static {
        tensMap.put("LANG_EN", "(TEN TWENTY THIRTY FORTY FIFTY SIXTY SEVENTY EIGHTY NINETY)");
        tensMap.put("LANG_SW", "(KUMI ISHIRINI THELATHINI AROBAINI HAMSINI SITINI SABINI THEMANINI TISINI)");
    }

    static public HashMap<String, String> powerMap = new HashMap<String, String>();

    static {
        powerMap.put("LANG_EN", "(HUNDRED THOUSAND MILLION)");
        powerMap.put("LANG_SW", "(MIA ELFU LAKI MILIONI)");
    }


}
