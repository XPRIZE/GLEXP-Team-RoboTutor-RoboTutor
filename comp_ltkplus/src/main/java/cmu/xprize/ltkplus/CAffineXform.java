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

import android.graphics.PointF;

public class CAffineXform {

    private float _scaleX;
    private float _scaleY;
    private float _offsetX;
    private float _offsetY;
    private float _timeScale;

    private int   _origX;
    private int   _origY;

    public CAffineXform() {
        init(0,0,0,0,0);
    }

    public CAffineXform(float sx, float sy, float x, float y, float t) {
        init(sx, sy, x, y, t);
    }

    public void init(float sx, float sy, float x, float y, float t) {

        _scaleX = sx;
        _scaleY = sy;
        _offsetX = x;
        _offsetY = y;
        _timeScale = t;
    }

    public boolean isChanged(float sx, float sy, float x, float y, float t) {

        boolean result = false;

        if(_scaleX    != sx) result = true;
        if(_scaleY    != sy) result = true;
        if(_offsetX   != x)  result = true;
        if(_offsetY   != y)  result = true;
        if(_timeScale != t)  result = true;

        return result;
    }


    public int  getOrigX() {return _origX;}
    public void setOrigX(int orig) { _origX = orig; }

    public int  getOrigY() {return _origY;}
    public void setOrigY(int orig) { _origY = orig; }

    public float getScaleX() {return _scaleX;}

    public float getScaleY() {return _scaleY;}

    public float getOffsetX() {return _offsetX;}

    public float getOffsetY() {return _offsetY;}

    public PointF getPoint() {return new PointF(_origX, _origY);}

    public void setOffsetX(float offset) { _offsetX = offset;}

    public void setOffsetY(float offset ) { _offsetY = offset;}

    public float getTimeScale() {return _timeScale;}


}
