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

 package edu.cmu.xprize.listener;

// Listener Package constants


public class LCONST {

    // Recognizer configuration parameters

    /**
     * weight to assign to our language models. Higher weights bias the recognizer towards the model-expected word
     */
    public static int LMWEIGHT = 9;

    // Listener-specific parameters

    /**
     * Lag behind the last hypothesis word if not followed by silence since it may be unreliable
     */
    public static boolean LAST_WORD_LAG = false;
    /**
     * align heard words left to right with sentence words
     */
    public static boolean ALIGN_L2R = false;

    // parameters passed through to pocketsphinx

    /**
     * pocketsphinx threshold signal-to-noise ratio for voice activity detection, default 2.0
     */
    public static float VAD_THRESHOLD = 2.0f;

    /**
     * pocketsphinx filler probability: higher than default
     */
    public static float FILLPROB = 1e-3f;

}
