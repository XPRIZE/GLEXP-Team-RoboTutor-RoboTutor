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

package cmu.xprize.comp_questions;

import cmu.xprize.util.ILoadableObject;
import edu.cmu.xprize.listener.ListenerBase;

public interface ICQn_ViewManager extends ILoadableObject {

    public void initStory(IVManListener owner, String assetPath, String assetLocation);

    public void onDestroy();

    void startStory();

    //UHQ
    void setPictureMatch();

    void setClozePage();

    void seekToPage(int pageIndex);

    void nextPage();

    void prevPage();

    void seekToParagraph(int paraIndex);

    void nextPara();

    void prevPara();

    void seekToLine(int lineIndex);

    void echoLine();

    void parrotLine();

    void nextLine();

    void prevLine();

    void seekToWord(int wordIndex);

    void nextWord();

    void prevWord();

    void setHighLight(String highlight, boolean update);

    //UHQ
    void decideToPlayGenericQuestion();

    void genericQuestions();

    void displayGenericQuestion();

    void setRandomGenericQuestion();

    void setClozeQuestion();

    void displayClozeQuestion();

    void displayPictureMatching();

    void hasClozeDistractor();

    void hasQuestion();

    public boolean endOfData();

    public void onUpdate(String[] heardWords);

    public void onUpdate(ListenerBase.HeardWord[] heardWords, boolean finalResult);

    public void onUpdate(String[] heardWords, boolean finalResult);

    public void continueListening();

    public void setSpeakButton(String command);

    public void setPageFlipButton(String command);

    public void resetImageButtons();

    public void showImageButtons();

    public void hideImageButtons();

    public void enableImageButtons();

    public void disableImageButtons();

    public void resetClozeButtons();

    public void showClozeButtons();

    public void hideClozeButtons();

    public void enableClozeButtons();

    public void disableClozeButtons();

    public void showClozeWordInBlank();

    public void hideClozeWordInBlank();

    public void publishClozeWord();

    public void highlightClozeWord();

    public void undoHighlightClozeWord();

    public void playClozeSentence();

    public void execCommand(String _command, Object _target);

//    public int getmCurrPara();
//
//    public int getmCurrLine();
//
//    public int getmParaCount();
//
//    public int getmLineCount();
//    public int getSegmentNdx();
//    public int getNumSegments();
//    public int getUtteranceNdx();
//    public int getNumUtterance();
//    public boolean getEndOfSentence();
//
//    public CASB_Narration[] getRawNarration();
//    public int getUtterancePrev();
//    public int getSegmentPrev();

     boolean isClozeMode();
     boolean isGenMode();
     boolean isPicMode();
}
