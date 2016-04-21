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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cmu.xprize.fw_component.CStimRespBase;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.Num2Word;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TTSsynthesizer;
import cmu.xprize.util.Word2Num;
import edu.cmu.xprize.listener.IAsrEventListener;
import edu.cmu.xprize.listener.ListenerBase;
import edu.cmu.xprize.listener.ListenerPLRT;

public class CNl_Component extends CStimRespBase implements IAsrEventListener {

    protected ListenerBase          mListener;
    protected TTSsynthesizer        mSynthesizer;

    protected String                DATASOURCEPATH;
    protected String                EXTERNPATH;

    private int                     mStimulusNumber;
    private String                  mStimulusText;
    private List                    mStimulusList;

    private int                     mResponseNumber;
    private String                  mResponseText;
    private List                    mResponseList;

    private LocalBroadcastManager bManager;

    static public String TAG = "CNl_Component";



    public CNl_Component(Context context) {
        super(context);
    }

    public CNl_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CNl_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    protected void prepareListener(TTSsynthesizer rootTTS) {

        // Response variants get hooked to the listener
        //
        if(mIsResponse) {
            // Generate a Project Listen type listener
            // Attach the speech recognizer.
            mListener = new ListenerPLRT();
            //mListener = new ListenerJSGF();

            // Configure the mListener language and the callback for ASR Events
            mListener.setLanguage(getLanguageFeature());
            mListener.setEventListener(this);
        }

        // attach TTS
        mSynthesizer = rootTTS;

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());
    }


    /**
     * @Override in Tutor Domain to allow access to CTutor language settting
     */
    public String getLanguage() {
        return "en";
    }

    /**
     * @Override in Tutor Domain to allow access to CTutor language settting
     */
    public String getLanguageFeature() {
        return "LANG_EN";
    }


    // mStimulus contains the string representation of the data source in numeric form e.g. "34"
    // mStimulusList contain the array of positional integers that comprise the number e.g. [3,4]
    // Obtain its mStimulusNumber integer equivalent e.g. "930" = 930
    // Convert it to a mStimulusText string representation e.g. 102 = "one hundred and two"
    //
    @Override
    protected void preProcessStimulus() {

        String[] stimElem =  mStimulus.split("(?!^)");

        mStimulusList = new ArrayList<Integer>();

        for(String elem : stimElem) {
            mStimulusList.add(Integer.parseInt(elem));
        }

        mStimulusNumber = Integer.parseInt(mStimulus);
        mStimulusText   = Num2Word.transform(mStimulusNumber, getLanguage());
    }


    public void listen(Boolean enable) {

        try {
            if (enable) {

                // Listen for a language specific number set
                //
                mListener.listenFor(TCONST.numberMap.get(getLanguageFeature()).split(","), 0);
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
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onUpdate(ListenerBase.HeardWord[] heardWords, boolean finalResult) {

        mResponseText = "";

        for (int i1 = 0; i1 < heardWords.length; i1++) {
            mResponseText += (heardWords[i1].hypWord + " ").toLowerCase();
        }

        try {
            mResponseNumber = Word2Num.transform(mResponseText.split(" "), getLanguage());
            mResponse       = new Integer(mResponseNumber).toString();
            mResponseList   = Word2Num.getNumberList();

            // We update the control directly in this case - unlike the base component
            //
            updateText(mResponse);

            // Let anyone interested know there is a new recognition set available
            Intent msg = new Intent(TCONST.LISTENER_RESPONSE);
            msg.putExtra(TCONST.FW_VALUE, mResponse);

            bManager.sendBroadcast(msg);

        } catch (Exception e) {

        }
    }


    @Override
    public void onASREvent(int eventType) {
    }

    //*********************  Speech Recognition Interface - End
    //****************************************************************************


}
