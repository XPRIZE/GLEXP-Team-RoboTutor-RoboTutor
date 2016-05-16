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

package cmu.xprize.nl_component;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cmu.xprize.util.Num2Word;
import cmu.xprize.util.TCJSGF;
import cmu.xprize.util.TCONST;
import edu.cmu.xprize.listener.ListenerBase;

public class CNl_JSGF implements CNl_Processor {

    private INl_Implementation      _Owner;
    private Context                 _Context;

    private ListenerBase            mListener;

    protected boolean               missingConjTolerant    = true;          // Do you need "AND" between number words
    protected boolean               addedConjTolerant      = true;          // English tends to false positive conjunctions (AND)
    protected boolean               repeatedWordIntolerant = true;          // Indicate repeated word errors.
    protected String                cachedLanguageFeature;
    protected String                conjunction;

    protected String                mStimulusString;        // String representation - even for numbers e.g. "34"
    protected String                mResponseString;        // String representation - even for numbers e.g. "34"

    protected int                   mStimulusValue;
    protected String                mStimulusText;
    protected List<String>          mStimulusComp;

    protected ArrayList<Integer>    mStimulusDigitValue;
    protected List<String>          mStimulusDigitString;
    protected ArrayList<String>     mStimulusDigitText;

    protected ArrayList<Integer>    mStimulusPlaceValue;
    protected ArrayList<String>     mStimulusPlaceString;
    protected ArrayList<String>     mStimulusPlaceText;

    protected int                   mResponseNumber;
    protected String                mResponseText;
    protected List                  mResponseList;
    protected ArrayList<String>     mResponseTextList;

    protected LocalBroadcastManager bManager;

    static public String TAG = "CNl_JSGF";



    public CNl_JSGF(INl_Implementation owner, Context context) {
        _Owner   = owner;
        _Context = context;

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(_Context);
    }

    @Override
    public void setListener(ListenerBase listener) {
        mListener = listener;
    }

    @Override
    public int getLength() {
        return mStimulusString.length();
    }

    @Override
    public String getString() {
        return mStimulusString;
    }

    @Override
    public int getValue() {
        return mStimulusValue;
    }

    @Override
    public String getText() {
        return mStimulusText;
    }


    // This provides access to the Stimulus array values as Strings
    //
    @Override
    public String deReference(String _listName, int index) {
        String result = "";

        try {

            switch (_listName) {
                case TCONST.DIGIT_STRING_VAR:
                    result = mStimulusDigitString.get(index);
                    break;

                case TCONST.PLACE_STRING_VAR:
                    result = mStimulusDigitText.get(index);
                    break;

                case TCONST.DIGIT_TEXT_VAR:
                    result = mStimulusPlaceString.get(index);
                    break;

                case TCONST.PLACE_TEXT_VAR:
                    result = mStimulusPlaceText.get(index);
                    break;
            }
        }
        catch(Exception e) {
            Log.e(TAG, "deReference Error: " + _listName + " : Index : " + index + " : " + e);
            System.exit(1);
        }

        return null;
    }


    /**
     * sStimulusString      - "238"
     * sStimulusValue       - 238
     * sStimulusText        - "TWO HUNDRED THIRTY EIGHT"
     *
     * Note: the lists are in order of increasing place value
     *
     * sStimulusDigitString - ["8", "3", "2"]
     * sStimulusDigitValue  - [8, 3, 2]
     * sStimulusDigitText   - ["EIGHT", "THREE", "TWO"]
     *
     * sStimulusPlaceString - ["8", "30", "200"]
     * sStimulusPlaceValue  - [8, 30, 200]
     * sStimulusPlacetext   - ["eight", "thirty", "two hundred"]
     *
     */
    @Override
    public void preProcessStimulus(String stimulusString) {

        mStimulusString = stimulusString;
        mStimulusValue  = Integer.parseInt(mStimulusString);
        mStimulusText   = Num2Word.transform(mStimulusValue, _Owner.getLanguage()).toUpperCase();
        mStimulusComp   = Arrays.asList(mStimulusText);

        // Note: the list is reordered (reversed) to access in terms of increasing place value
        // 238 = ["8", "3", "2"]
        //
        mStimulusDigitString = Arrays.asList(mStimulusString.split("(?!^)"));
        Collections.reverse(mStimulusDigitString);

        mStimulusDigitValue  = new ArrayList<Integer>();
        mStimulusDigitText   = new ArrayList<String>();

        mStimulusPlaceValue  = new ArrayList<Integer>();
        mStimulusPlaceText   = new ArrayList<String>();
        mStimulusPlaceString = new ArrayList<String>();

        int power = 1;

        for(String elem : mStimulusDigitString) {
            int num = Integer.parseInt(elem);
            mStimulusDigitValue.add(num);
            mStimulusDigitText.add(Num2Word.transform(num, _Owner.getLanguage()).toUpperCase());

            num *= power;
            mStimulusPlaceValue.add(num);
            mStimulusPlaceText.add(Num2Word.transform(num, _Owner.getLanguage()).toUpperCase());
            mStimulusPlaceString.add(Integer.toString(num));

            power *= 10;
        }
    }


    /**
     *
     * @param enable
     */
    @Override
    public void listen(Boolean enable) {

        try {
            if (enable) {

                // Listen for a language specific number set
                //
                if (mListener != null) {

                    // Cache the language feature (e.g. "LANG_EN") and
                    // the language specific conjunction used in numbers e.g. "AND"
                    // as in 100 and 23
                    //
                    cachedLanguageFeature = _Owner.getLanguageFeature();

                    // We use the stimiulus as the base string to listen for and
                    // add all the other numbers as distractors
                    //
                    String[] numberSet = (TCONST.numberMap.get(cachedLanguageFeature).toUpperCase().split(","));

                    mListener.reInitializeListener(true);
                    mListener.listenFor(numberSet, 0);
                    mListener.setPauseListener(false);
                }

                // mListener.listenFor(TCONST.numberMap.get(getLanguageFeature()).split(","), 0);
            }
            else  {
                mListener.setPauseListener(true);
            }
        }
        catch(Exception e) {
            Log.e(TAG, "Data not initialized - " + e);
            System.exit(1);
        }
    }


    //****************************************************************************
    //*********************  Speech Recognition Interface - Start

    @Override
    public void onUpdate(ListenerBase.HeardWord[] heardWords, boolean finalResult) {

        String logString = "";
        for (int i = 0; i < heardWords.length; i++) {
            logString += heardWords[i].hypWord.toUpperCase() + ":" + heardWords[i].iSentenceWord + " | ";
        }
        Log.d("ASR", "New HypSet: "  + logString);

    }

    //*********************  Speech Recognition Interface - End
    //****************************************************************************




}
