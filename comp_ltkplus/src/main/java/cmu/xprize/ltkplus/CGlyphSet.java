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

package cmu.xprize.ltkplus;

import android.content.Context;

public class CGlyphSet {

    private Context   mContext;

    private String    _alphabet;
    private CGlyph[]   _glyphSet;


    public CGlyphSet(Context context) {
    }


    public CGlyphSet(Context context, String alphabet) {

        mContext = context;
        load(alphabet);
    }


    public void load(String alphabet) {

        _alphabet = alphabet;

        _glyphSet = new CGlyph[alphabet.length()];

        for(int i1 = 0 ; i1 < alphabet.length() ; i1++) {

            _glyphSet[i1] = new CGlyph(mContext, 0, null, 0);

            if(!_glyphSet[i1].loadGlyphFactory(alphabet.substring(i1,i1+1), null)) {
                _glyphSet[i1] = null;
            }
        }
    }


    public CGlyph cloneGlyph(String glyph) {

        CGlyph cloned   = null;
        int glyphIndex = _alphabet.indexOf(glyph);

        if(_glyphSet[glyphIndex] != null) {
            cloned = _glyphSet[glyphIndex].clone();
        }

        return cloned;
    }


}
