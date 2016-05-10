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
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cmu.xprize.util.Num2Word;
import cmu.xprize.util.TCJSGF;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.Word2Num;
import cmu.xprize.util.Word2NumFSM;
import edu.cmu.xprize.listener.ListenerBase;

public class CNl_PLRT implements CNl_Processor {

    private INl_Implementation      _Owner;
    private Context                 _Context;

    private ListenerBase            mListener;

    protected boolean               missingConjTolerant    = false;         // Do you need "AND" between number words
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

    protected long                  mResponseNumber;
    protected String                mResponseText;
    protected List                  mResponseList;
    protected ArrayList<String>     mResponseTextList;

    protected LocalBroadcastManager bManager;

    static public String TAG = "CNl_PLRT";



    public CNl_PLRT(INl_Implementation owner, Context context) {
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
        mStimulusComp   = Arrays.asList(mStimulusText.split(" "));

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
                    conjunction           = TCJSGF.conjMap.get(cachedLanguageFeature);

                    // We use the stimiulus as the base string to listen for and
                    // add all the other numbers as distractors
                    //
                    String[] distractors = (TCONST.numberMap.get(cachedLanguageFeature).split(","));

                    ArrayList<String> combo = new ArrayList<String>();

                    for(String elem : mStimulusComp) {
                        combo.add(elem.toUpperCase());
                    }

                    for(String elem : distractors) {
                        combo.add(elem);
                    }

                    mListener.reInitializeListener(true);
                    mListener.listenFor(combo.toArray(new String[combo.size()]),0);
                    mListener.setPauseListener(false);
                }

                // mListener.listenFor(TCONST.numberMap.get(_Owner.getLanguageFeature()).split(","), 0);
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

        int     expectedWordIndex      = 0;             // index of expected next word in sentence

        boolean Correct   = false;
        boolean Error     = false;
        int     ErrorType = 0;
        int     maxPlace  = 0;
        int     maxHeard  = 0;

        mResponseTextList   = new ArrayList<String>();

        if (heardWords.length >= 1) {

            for(int i = 0; i < mStimulusComp.size() ; i++) {
                mResponseTextList.add("");
            }

            // Reprocess the HeardWords hypothesis to allow incorrect repeated numbers.
            // e.g. If the stimulus is 179 and the user says 171 the second '1' will have
            // an iSentenceWord of 0 instead of 2
            // This is to make the screen updates more responsive to user input. Otherwise when
            // they say 171 in response to a stimulus of 179 there would be no update.
            //
            // If the user makes a repeated word mistake there will be a heardword with an
            // iSentence position that is behind where the previous word was
            //  MIA:0 | MOJA:1 | NA:2 | SABINI:3 | NA:4 | MOJA:1 |
            // So in this case we update the last MOJA iSentenceWord to :5
            //
            if(repeatedWordIntolerant && heardWords.length > 1) {
                for (int i = 1; i < heardWords.length; i++) {
                    if(heardWords[i].iSentenceWord < i) {
                        Log.d("ASR", "Repeated Word <error>");
                        heardWords[i].iSentenceWord = i;
                        break;
                    }
                }
            }

            String logString = "";
            for (int i = 0; i < heardWords.length; i++) {
                logString += heardWords[i].hypWord.toUpperCase() + ":" + heardWords[i].iSentenceWord + " | ";
            }
            Log.d("ASR", "New HypSet: "  + logString);

            // Place the words in order
            // Loop throught as many words as are in the stimulus
            //
            for (int i1 = 0; i1 < mStimulusComp.size(); i1++) {

                // Assign the most recent one to it's associated place value
                // This may result in empty place values and may place some values in the
                // wrong spot.
                //
                for (int i2 = 0; i2 < heardWords.length; i2++) {
                    if(heardWords[i2].iSentenceWord == i1) {
                        mResponseTextList.set(i1, heardWords[i2].hypWord.toUpperCase());

                        // Track the max place where we have placed a word to limit the
                        // conjuntion insertion below
                        if(i1 > maxPlace) maxPlace = i1;
                    }
                }
            }

//            // Prune out false positive conjunctions
//            // e.g for a stimulus of 100 it hears ONE AND...
//            //
//            if(addedConjTolerant) {
//
//                for (int i1 = 0; i1 <= maxPlace; i1++) {
//
//                    // If the response is a conjunction and the stimulus
//                    // isn't then remove the response conjunction (false positive) and
//                    // retest the element if we aren't at the end of the reponse set
//                    //
//                    if(!mStimulusComp.get(i1).equals(conjunction)) {
//                        if(mResponseTextList.get(i1).equals(conjunction)) {
//                            Log.d("ASR", "Conjunction pruned" );
//                            mResponseTextList.remove(i1);
//
//                            if(i1 == maxPlace) break;
//                            i1--;
//                        }
//                    }
//                }
//            }

            Log.d("ASR", "Pre MCT: " + TextUtils.join(" ", mResponseTextList));

            // Insert missing conjunctions
            // If we are tolerant of missing conjunctions then if there we are looking
            // at a conj in the stimulus that is not in the response insert it
            //
            if(missingConjTolerant) {

                for (int i1 = 0; i1 <= maxPlace; i1++) {

                    String respElem = mResponseTextList.get(i1);

                    // If the stimulus place value is a conjunction and the response
                    // isn't then either insert it or replace an empty element
                    //
                    if (mStimulusComp.get(i1).equals(conjunction)) {
                        if(!respElem.equals(conjunction)) {
                            if(respElem.equals("")) {
                                Log.d("ASR", "Conjunction Insertion");
                                mResponseTextList.set(i1, conjunction);
                            }
                            else {
                                mResponseTextList.add(i1, conjunction);
                            }
                        }
                    }
                }
            }

            Log.d("ASR", "Pre Strip: " + TextUtils.join(" ", mResponseTextList));

            // Prune the "" entries so they don't cause errors in Word2Num.
            //
            for(int i1 = 0 ; i1 < mResponseTextList.size() ; i1++) {
                if(mResponseTextList.get(i1).equals("")) {
                    mResponseTextList.remove(i1--);
                }
            }

            try {

                Log.d("ASR", "Parsing: " + TextUtils.join(" ", mResponseTextList));

                mResponseNumber = Word2NumFSM.transform(mResponseTextList, _Owner.getLanguageFeature());

                mResponseString = new Long(mResponseNumber).toString();

                Log.d("ASR", "ResponseNumber: " + mResponseNumber);
                Log.d("ASR", "ResponseString: " + mResponseString);

                // We update the control directly in this case - unlike the base component
                //
                if(mResponseNumber != -1)
                    _Owner.updateText(mResponseString);

                // Let anyone interested know there is a new recognition set available
                Intent msg = new Intent(TCONST.LISTENER_RESPONSE);
                msg.putExtra(TCONST.FW_VALUE, mResponseString);

                bManager.sendBroadcast(msg);


                Log.d("ASR", "Stimulus: " + TextUtils.join(" ", mStimulusComp));
                Log.d("ASR", "Response: " + TextUtils.join(" ", mResponseTextList));

                int size = Math.min(mStimulusComp.size(),mResponseTextList.size());

                for (int i1 = 0; i1 < size; i1++) {

                    String subElem = mResponseTextList.get(i1);

                    // track the next expected digit
                    //
                    expectedWordIndex = i1;

                    // Check if the next element matches
                    //
                    if(!mStimulusComp.get(i1).equals(subElem)) {

                        ErrorType = mStimulusDigitValue.size();
                        Error     = TCONST.TRUE_ERROR;
                        Correct   = false;
                        break;
                    }
                    // Note - don't use the response length here as it may have been made
                    // longer than the stimulus by a conj insertion or repeated word.
                    //
                    else if(i1 >= mStimulusComp.size()-1) {
                        Correct   = true;
                        break;
                    }
                }
                if(Correct) {
                    _Owner.updateOutcomeState(TCONST.FALSE_NOERROR);
                    _Owner.onASREvent(TCONST.RECOGNITION_EVENT);
                }
                else if(Error) {
                    _Owner.updateOutcomeState(TCONST.TRUE_ERROR);

                    // If they've uttered as many words as are in the stimulus and they are
                    // still incorrect emit a recognition event to terminate.
                    //
                    //if(mStimulusComp.size() == mResponseTextList.size())
                        _Owner.onASREvent(TCONST.RECOGNITION_EVENT);
                }

                // Update the expected word in MultiMatch
                //
                mListener.updateNextWordIndex(expectedWordIndex);

            } catch (Exception e) {
                Log.e("ASR", "Number Parser" + e);
                //  System.exit(1);
            }
        }
    }

    //*********************  Speech Recognition Interface - End
    //****************************************************************************
}
