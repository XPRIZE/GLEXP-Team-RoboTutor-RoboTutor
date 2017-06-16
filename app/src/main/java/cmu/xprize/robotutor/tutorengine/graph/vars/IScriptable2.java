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

package cmu.xprize.robotutor.tutorengine.graph.vars;

import cmu.xprize.robotutor.tutorengine.graph.vars.TBoolean;
import cmu.xprize.util.IScriptable;

public interface IScriptable2 extends IScriptable {

    public String getType();

    public TBoolean OR(IScriptable2 RHS, boolean lneg, boolean rneg);
    public TBoolean AND(IScriptable2 RHS, boolean lneg, boolean rneg);
    public TBoolean LT(IScriptable2 RHS);
    public TBoolean LTEQ(IScriptable2 RHS);
    public TBoolean GT(IScriptable2 RHS);
    public TBoolean GTEQ(IScriptable2 RHS);
    public TBoolean EQ(IScriptable2 RHS);
    public TBoolean NEQ(IScriptable2 RHS);

    public String resolve(int index);
    public int getIntValue();

}
