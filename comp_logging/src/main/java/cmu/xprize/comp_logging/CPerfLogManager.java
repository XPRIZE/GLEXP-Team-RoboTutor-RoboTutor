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

package cmu.xprize.comp_logging;

import android.util.Log;

public class CPerfLogManager extends CLogManagerBase implements IPerfLogManager {
    private static String TAG = "CLogManager";

    // Singleton
    private static CPerfLogManager ourInstance = new CPerfLogManager();

    public static CPerfLogManager getInstance() {
        return ourInstance;
    }

    private CPerfLogManager() {
        super.TAG = TAG;
    }

    private PerformanceLogItem lastEvent = new PerformanceLogItem();

    @Override
    public void postPerformanceLog(PerformanceLogItem event) {
        lastEvent = event;
        postEvent_I(TLOG_CONST.PERFORMANCE_TAG, event.toString());
    }

    // TODO: Super hacky. Need refactoring.
    @Override
    public void postPerformanceLogWithoutContext(PerformanceLogItem event) {

        if (event.getGameId() == null || event.getGameId().isEmpty()) {
            event.setGameId(lastEvent.getGameId());
        }

        if (event.getTutorName() == null || event.getTutorName().isEmpty()) {
            event.setTutorName(lastEvent.getTutorName());
        }

        if (event.getTutorId() == null || event.getTutorId().isEmpty()) {
            event.setTutorId(lastEvent.getTutorId());
        }

        if (event.getPromotionMode() == null || event.getPromotionMode().isEmpty()) {
            event.setPromotionMode(lastEvent.getPromotionMode());
        }

        if (event.getTaskName() == null || event.getTaskName().isEmpty()) {
            event.setTaskName(lastEvent.getTaskName());
        }

        if (event.getLevelName() == null || event.getLevelName().isEmpty()) {
            event.setLevelName(lastEvent.getLevelName());
        }

        if (event.getProblemName() == null || event.getProblemName().isEmpty()) {
            event.setProblemName(lastEvent.getProblemName());
        }

        if (event.getProblemNumber() == 0) {
            event.setProblemNumber(lastEvent.getProblemNumber());
        }

        if (event.getSubstepNumber() == 0) {
            event.setSubstepNumber(lastEvent.getSubstepNumber());
            event.setSubstepNumber(-1); // 1=ones, 2=tens, 3=hundreds
        }

        if (event.getAttemptNumber() == 0) {
            event.setAttemptNumber(lastEvent.getAttemptNumber());
        }

        postEvent_I(TLOG_CONST.PERFORMANCE_TAG, event.toString());
    }
}

