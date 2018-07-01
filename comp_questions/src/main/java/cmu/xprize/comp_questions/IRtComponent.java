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

package cmu.xprize.rt_component;

public interface IRtComponent {

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

    void setHighLight(String highlight);

    public boolean endOfData();

    public void continueListening();

}
