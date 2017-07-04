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

package cmu.xprize.robotutor.tutorengine.graph;

import org.json.JSONObject;

import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CMediaManager;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TTSsynthesizer;

public class type_tts extends type_action {

    private CMediaManager  mMediaManager;
    private TTSsynthesizer mSynthesizer;


    // json loadable fields
    public String        command;
    public String        content    = "";
    public String        language   = "";
    public float         rate       = 0f;


    public type_tts() {
    }


    @Override
    public String applyNode() {

        String status = TCONST.DONE;

        // If the feature test passes then fire the event.
        // Otherwise set flag to indicate event was completed/skipped in this case
        // Issue #58 - Make all actions feature reactive.
        //
        if(testFeatures()) {

            switch (command) {
                case TCONST.SAY:
                    say();
                    break;

                case TCONST.SET_RATE:
                    setRate();
                    break;
            }
        }

        return status;
    }


    private void setRate() {

        mSynthesizer.setSpeechRate(rate);

    }

    private void setLanguage() {

        mSynthesizer.setLanguage(language);

    }

    /**
     *
     */
    private void say() {

        //mListener.setPauseListener(true);

        setRate();
        setLanguage();

        String speechResolved = getScope().parseTemplate(content);

        mSynthesizer.speak(speechResolved);

        while (mSynthesizer.isSpeaking()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //mListener.setPauseListener(false);
    }



    // *** Serialization


    /**
     * Load the object from the factory data
     *
     * @param jsonObj
     * @param scope
     */
    @Override
    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        // Always call super to init _scope - or do it yourself
        //
        super.loadJSON(jsonObj, scope);

        mMediaManager = CMediaController.getManagerInstance(scope.tutorName());
        mSynthesizer  = CMediaController.getTTS();

        // Custom post processing.
        // If rate and language are not initialized use defaults
        //
        if(rate == 0)
            rate = 1.0f;

        // If unset use the current tutor default language
        //
        if(language.equals(""))
            language = mMediaManager.getLanguageFeature(scope.tutor());

        // TODO:  We also want to connect up the ASR

    }
}
