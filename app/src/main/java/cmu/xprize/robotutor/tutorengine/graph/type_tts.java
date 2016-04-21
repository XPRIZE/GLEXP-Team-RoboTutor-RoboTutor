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

package cmu.xprize.robotutor.tutorengine.graph;

import android.util.Log;

import org.json.JSONObject;

import java.io.FileNotFoundException;

import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TTSsynthesizer;

public class type_tts extends type_action {

    private TTSsynthesizer mSynthesizer;


    // json loadable fields
    public String        command;
    public String        content;
    public String        lang;


    public type_tts() {
        mSynthesizer = CTutor.TTS;
    }

    /**
     * TODO: Look at disposing of Media Players once scene is finished - optimization
     */
    @Override
    public void preEnter()
    {
    }


    @Override
    public String applyNode() {

        String status = TCONST.DONE;

        // play on creation if command indicates
        if(command.equals(TCONST.SAY)) {

            say(content);
        }

        return status;
    }


    /**
     *
     */
    public void say(String prompt) {

        //mListener.setPauseListener(true);
        mSynthesizer.speak(prompt);

        while (mSynthesizer.isSpeaking()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //mListener.setPauseListener(false);
    }



}
