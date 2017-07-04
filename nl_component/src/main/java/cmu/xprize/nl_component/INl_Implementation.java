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

public interface INl_Implementation {
    public String getLanguage();
    public String getLanguageFeature();

    public void updateOutcomeState(boolean error);
    public void applyEventNode(String nodeName);

    public void publishState(int error, int warn);

    public void updateNumberString(String newValue);
    public void updateDebugText(String newValue);
    public void onASREvent(int eventType);
}
