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

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.Num2Word;
import cmu.xprize.util.TCJSGF;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.Word2NumFSM;
import edu.cmu.xprize.listener.ListenerBase;

public class CNl_PLRT implements CNl_Processor {

    private INl_Implementation      _Owner;
    private Context                 _Context;

    private ListenerBase            mListener;

    protected boolean               missingConjTolerant    = false;         // Do you need "AND" between number words
    protected boolean               addedConjTolerant      = false;         // English tends to false positive conjunctions (AND)
    protected boolean               repeatedWordIntolerant = true;          // Indicate repeated word errors.
    protected String                cachedLanguageFeature;
    protected String                conjunction;

    protected String                mStimulusString;        // String representation - even for numbers e.g. "34"
    protected String                mResponseString;        // String representation - even for numbers e.g. "34"

    protected int                   mStimulusValue;         // The stimulus number - 123
    protected String                mStimulusText;          // The textual version - one hundred twenty three
    protected List<String>          mStimulusTextList;      // A list of the component words ["one", "hundred", "twenty", "three"]

    protected ArrayList<String>     mResponseTextList;      // The processed ASR hypothesis partial or complete - ["one", "hundred", "twenty"] - may contain errors
    protected long                  mResponseNumber;        // The current number as spoken - derived ftom mResponseTextList

    protected ArrayList<Integer>    mStimulusDigitValue;
    protected List<String>          mStimulusDigitString;
    protected ArrayList<String>     mStimulusDigitText;

    protected ArrayList<Integer>    mStimulusPlaceValue;
    protected ArrayList<String>     mStimulusPlaceString;
    protected ArrayList<String>     mStimulusPlaceText;

    protected ArrayList[]           mStimulusPlaceWords;
    protected ArrayList[]           mStimulusDigitWords;


    private boolean DEBUG = false;

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

    @Override
    public boolean isPlaceValueUsed(int place) {
        return mStimulusPlaceWords[place-1] != null;
    }

    @Override
    public int wordsInPlaceValue(int place) {
        return mStimulusPlaceWords[place-1].size();
    }

    @Override
    public int wordsInDigitValue(int place) {
        return mStimulusDigitWords[place-1].size();
    }


    // This provides access to the Stimulus array values as Strings
    //
    @Override
    public String deReference(String _listID, int index) {
        String result = "";

        try {

            switch (_listID) {
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


                case TCONST.PLACE4_WORDS_VAR:
                    result = ((String)mStimulusPlaceWords[3].get(index)).toLowerCase();
                    break;

                case TCONST.PLACE3_WORDS_VAR:
                    result = ((String)mStimulusPlaceWords[2].get(index)).toLowerCase();
                    break;

                case TCONST.PLACE2_WORDS_VAR:
                    result = ((String)mStimulusPlaceWords[1].get(index)).toLowerCase();
                    break;

                case TCONST.PLACE1_WORDS_VAR:
                    result = ((String)mStimulusPlaceWords[0].get(index)).toLowerCase();
                    break;


                case TCONST.DIGIT4_WORDS_VAR:
                    result = ((String)mStimulusDigitWords[3].get(index)).toLowerCase();
                    break;

                case TCONST.DIGIT3_WORDS_VAR:
                    result = ((String)mStimulusDigitWords[2].get(index)).toLowerCase();
                    break;

                case TCONST.DIGIT2_WORDS_VAR:
                    result = ((String)mStimulusDigitWords[1].get(index)).toLowerCase();
                    break;

                case TCONST.DIGIT1_WORDS_VAR:
                    result = ((String)mStimulusDigitWords[0].get(index)).toLowerCase();
                    break;
            }
        }
        catch(Exception e) {
            CErrorManager.logEvent(TAG, "deReference Error: " + _listID + " : Index : " + index + " : ", e, false);
        }

        return result;
    }


    /**
     * Use Num2Word which embodies the grammar rules for the construction of a spoken number uttereance. To generate
     * the words that represent place values when a number is spoken as a whole and when each place value is
     * spoken in isoloatin
     *
     * For each place value we generate an ArrayList of the words mStimulusPlaceWords that constitute that place
     * value when spoken. Note that this may include conjunctions. -
     * e.g. 123   for ten place we would get "twenty" -EN or "na ishirini" - SW
     *
     * We also generate  mStimulusDigitWords which is the words that constitute the number for a place value
     * if spoken in isolation -
     * e.g. 123   for ten place we would get "twenty" -EN or "ishirini" - SW
     *
     * To do this we step through the place values from high to low.
     *
     * e.g.  234 = mia mbeli na thelathini na nne ->  Is broken into:
     *
     *  hundreds - ["mia", "mbeli"]
     *  tens     - ["na", "thelathini"]
     *  ones     - ["na", "nne"]
     *
     *  This makes a possibly language specific assumption that '0' place values are not uttered.
     *  It bases it's use of conjunctions on the language model used in Num2Word
     *
     * @param Number
     * @param text
     * @param placeValue
     * @param parentLen
     */
    private void generateStimPlaceWords(int prevPart, int Number, String text, int placeValue, int parentLen) {

        ArrayList<String> Words  = null;
        ArrayList<String> DWords = null;

        // assume 0 place values don't contribute to the spoken number
        // If zero we don't change the parentLen
        //
        if(text.charAt(placeValue) != '0') {

            // Strip off the part of the number below the place Value we are currently generating
            // e.g. if # - 1234 and place is 2 then we want only 1200
            //
            int power      = (int) Math.pow(10, placeValue);
            int partValue  = (Number / power) * power;

            // Generate the words that contitute that part of the number
            // e.g. if # - 1200 = "one thousand two hundred" - including conjunctions
            //
            String partWords = Num2Word.transform(partValue, _Owner.getLanguage()).toUpperCase();

            // Get the words for the place value by stripping off the string that made the higher place value
            // e.g given 1200 then at place value 2 we remove "one thousand" and are left with  "two hundred"
            // trim the spaces from the leading edge if any
            //
            String placeWords = partWords.substring(parentLen).trim();
            Words             = new ArrayList<String>(Arrays.asList(placeWords.split(" ")));

            Collections.reverse(Words);

            // Update the length of the string that constitutes the parent place value.
            //
            parentLen = partWords.length();


            // Now generate the place value words in isolation
            // remember the partValue for next iteration.
            // i.e. given 2938 -
            //  partPrev= 0     partValue - 2000   digitValue = 2000
            //  partPrev= 2000  partValue - 2900   digitValue =  900
            //  partPrev= 2900  partValue - 2930   digitValue =   30
            //  partPrev= 2930  partValue - 2938   digitValue =    8
            //
            int digitValue = partValue - prevPart;
            prevPart = partValue;

            // Generate the words that constitute that place value in isolation
            // e.g. if # - 1200 = "one thousand two hundred" - without conjunctions
            //
            partWords = Num2Word.transform(digitValue, _Owner.getLanguage()).toUpperCase();
            DWords    = new ArrayList<String>(Arrays.asList(partWords.split(" ")));

            Collections.reverse(DWords);
        }

        try {
            mStimulusPlaceWords[placeValue] =  Words;
            mStimulusDigitWords[placeValue] =  DWords;
            placeValue--;
        }
        catch(Exception e) {
            Log.d(TAG, "Error: " + e);
        }

        if(placeValue >= 0) {
            generateStimPlaceWords(prevPart, Number, text, placeValue, parentLen);
        }
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

        mStimulusString   = stimulusString;
        mStimulusValue    = Integer.parseInt(mStimulusString);
        mStimulusText     = Num2Word.transform(mStimulusValue, _Owner.getLanguage()).toUpperCase();
        mStimulusTextList = Arrays.asList(mStimulusText.split(" "));

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

        // Generate the words for each place value when speaking the whole number
        // Also generate the words for digit of the number - i.e. no conjunctions.
        // Make an arraylist big enough to hold the max numner of plcae values we support
        // init these with null - will be replaced with a string array if the place value is used
        //
        mStimulusPlaceWords = new ArrayList[TCONST.MAX_DIGITS];
        mStimulusDigitWords = new ArrayList[TCONST.MAX_DIGITS];

        for(int i1 = 0 ; i1 < TCONST.MAX_DIGITS ; i1++) {
            mStimulusPlaceWords[i1] = null;
            mStimulusDigitWords[i1] = null;
        }

        // Put the stimulus number text into place value order i.e  2000  -> "0002"
        //
        String placeOrder = new StringBuilder(mStimulusString).reverse().toString();

        generateStimPlaceWords(0, mStimulusValue, placeOrder, mStimulusString.length()-1, 0);

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

        if(DEBUG)
            _Owner.updateDebugText("");

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

                    for(String elem : mStimulusTextList) {
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
            CErrorManager.logEvent(TAG, "Data not initialized - ", e, false);
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

            for(int i = 0; i < mStimulusTextList.size() ; i++) {
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

            // Build a debug log string
            //
            String logString = "";
            for (int i = 0; i < heardWords.length; i++) {
                logString += heardWords[i].hypWord.toUpperCase() + ":" + heardWords[i].iSentenceWord + " | ";
            }
            Log.d("ASR", "New HypSet: "  + logString);


            // Place the words in order
            // Loop through as many words as are in the stimulus
            //
            for (int i1 = 0; i1 < mStimulusTextList.size(); i1++) {

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

            // Prune out false positive conjunctions
            // e.g for a stimulus of 100 it hears ONE AND...
            //
            if(addedConjTolerant) {

                for (int i1 = 0; i1 <= maxPlace; i1++) {

                    // If the response is a conjunction and the stimulus
                    // isn't then remove the response conjunction (false positive) and
                    // retest the element if we aren't at the end of the reponse set
                    //
                    if(!mStimulusTextList.get(i1).equals(conjunction)) {
                        if(mResponseTextList.get(i1).equals(conjunction)) {
                            Log.d("ASR", "Conjunction pruned" );
                            mResponseTextList.remove(i1);

                            if(i1 == maxPlace) break;
                            i1--;
                        }
                    }
                }
            }

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
                    if (mStimulusTextList.get(i1).equals(conjunction)) {
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

                _Owner.publishState(Word2NumFSM.errorCode, Word2NumFSM.warnCode);

                mResponseString = new Long(mResponseNumber).toString();

                Log.d("ASR", "ResponseNumber: " + mResponseNumber);
                Log.d("ASR", "ResponseString: " + mResponseString);

                // We update the control directly in this case - unlike the base component
                //
                if(mResponseNumber != -1) {
                    _Owner.updateNumberString(mResponseString);

                    if(DEBUG)
                        _Owner.updateDebugText(TextUtils.join(" ", mResponseTextList));
                }

                // Let anyone interested know there is a new recognition set available
                Intent msg = new Intent(TCONST.LISTENER_RESPONSE);
                msg.putExtra(TCONST.FW_VALUE, mResponseString);

                bManager.sendBroadcast(msg);


                Log.d("ASR", "Stimulus: " + TextUtils.join(" ", mStimulusTextList));
                Log.d("ASR", "Response: " + TextUtils.join(" ", mResponseTextList));

                int size = Math.min(mStimulusTextList.size(),mResponseTextList.size());

                for (int i1 = 0; i1 < size; i1++) {

                    String subElem = mResponseTextList.get(i1);

                    // track the next expected digit
                    //
                    expectedWordIndex = i1;

                    // Check if the next element matches
                    //
                    if(!mStimulusTextList.get(i1).equals(subElem)) {

                        ErrorType = mStimulusDigitValue.size();
                        Error     = TCONST.TRUE_ERROR;
                        Correct   = false;
                        break;
                    }
                    // Note - don't use the response length here as it may have been made
                    // longer than the stimulus by a conj insertion or repeated word.
                    //
                    else if(i1 >= mStimulusTextList.size()-1) {
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
                    _Owner.onASREvent(TCONST.RECOGNITION_EVENT);
                }

                // Update the expected word in MultiMatch
                //
                mListener.updateNextWordIndex(expectedWordIndex);

            } catch (Exception e) {
                Log.e("ASR", "Number Parser" + e);
            }
        }
    }

    //*********************  Speech Recognition Interface - End
    //****************************************************************************
}
