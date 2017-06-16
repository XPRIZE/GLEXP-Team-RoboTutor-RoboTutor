/**
 Copyright(c) 2015-2017 Kevin Willows
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package cmu.xprize.banner.persona;

import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


public class PersonaEye {

    private PersonaEyeLid   eyeLid;

    private PointF          _eyeLocation;
    private PointF          _eyeRadius;
    private float           _alignX;
    private float           _alignY;
    private int             _align = ALIGN_CENTER;

    private PointF          _pupilLocation;
    private float           _pupilOffset = 0f;
    private float           _pupilDia;

    private RectF           _eyeBounds;

    private PointF          _intercept;

    private float           _eccen    = 0f;         // NOTE: Eccentricity is defined here as Height / Width
    private float           _height   = 0f;
    private Float           _width    = 0f;

    final private static String   TAG = "PERSONA_EYE";
    final private static int      ALIGN_CENTER = 1;

    /**
     * The default constructor is used to generate
     */
    public PersonaEye() {
        init();
    }

    public PersonaEye(PersonaEye model) {

        init();

        _eyeLocation.x = 0;
        _eyeLocation.y = 0;

        _eyeRadius     = model.getEyeRadius();
        _pupilDia      = model.getPupilDia();

        updateEyeBounds();
    }

    private void init() {

        _intercept     = new PointF();
        _eyeLocation   = new PointF();
        _eyeRadius     = new PointF();
        _pupilLocation = new PointF();

        _eyeBounds     = new RectF();

        eyeLid         = new PersonaEyeLid();
    }


    /**
     * Update the gaze
     *
     * @param canvas
     */
    protected void onDraw(Canvas canvas, PersonaEyeColor eyeColor) {

        Path clip = new Path();

        canvas.drawOval(getEyeBounds(), eyeColor.getEyeColor());        // Draw the eyeball fill

        canvas.save();
        clip.addOval(getEyeBounds(), Path.Direction.CW);                // clip the pupil to the eye region
        canvas.clipPath(clip, Region.Op.INTERSECT);

        canvas.drawCircle(getPupilX(), getPupilY(), getPupilRad(), eyeColor.getPupilColor());
        if(eyeColor.hasPupilStroke())
            canvas.drawCircle(getPupilX(), getPupilY(), getPupilRad(), eyeColor.getPupilStroke());

        eyeLid.onDraw(canvas);                                          // Draw the eyelid - possibly occluding the pupil

        canvas.restore();
        if(eyeColor.hasEyeStroke())
            canvas.drawOval(getEyeBounds(), eyeColor.getEyeStroke());   // Draw the eyeball Stroke

    }


    public void setEyeLocation() {

        _eyeLocation.x = _eyeRadius.x + _alignX + _pupilOffset;
        _eyeLocation.y = _eyeRadius.y + _alignY;

        updateEyeBounds();
        setPupilLocation(_eyeLocation);
    }
    public PointF getEyeLocation() {
        return _eyeLocation;
    }


    public void setPupilLocation(PointF newLocation) {
        setPupilX(newLocation.x);
        setPupilY(newLocation.y);
    }
    public PointF getPupilLocation() {
        return _pupilLocation;
    }
    public void setPupilX(float x) {
        _pupilLocation.x = x;
    }
    public void setPupilY(float y) {
        _pupilLocation.y = y;
    }
    public float getPupilX() {
        return _pupilLocation.x;
    }
    public float getPupilY() {
        return _pupilLocation.y;
    }



    public void setPupilDistance(float offsetX) {

        _pupilOffset = offsetX * _eyeRadius.x;

        setEyeLocation();
    }


    public void alignEye(RectF contBox, RectF compBox) {
        switch(_align) {
            case ALIGN_CENTER:
                _alignX = (contBox.width() - compBox.width()) / 2;
                _alignY = (contBox.height() - compBox.height()) / 2;

                setEyeLocation();
                break;
        }
    }


    public boolean checkPoke(PointF lookAt) {
        float   gx;
        float   gy;
        boolean poked = false;

        gx = (lookAt.x -_eyeLocation.x);
        gy = (lookAt.y -_eyeLocation.y);

        float a = (_eyeBounds.width()  / 2);
        float b = (_eyeBounds.height() / 2);

        if((gx > -a && gx < a) && (gy > -b && gy < b)) {
            poked = true;
            closeEye();
        }

        return poked;
    }


    public void closeEye() {
        eyeLid.setBlink(.99f);
    }

    public void openEye(float blinkLevel) {
        eyeLid.setBlink(blinkLevel);
    }

    public void lookAt(PointF lookAt) {
        float gx;
        float gy;

        //Log.i(TAG,"Looking at Point: X:" + lookAt.x + "  Y:" + lookAt.y);

        gx =  (lookAt.x -_eyeLocation.x);
        gy = -(lookAt.y -_eyeLocation.y);

        //Log.i(TAG,"Looking at Normalized: X:" + gx + "  Y:" + gy);

        calcIntercept(gx, gy, _intercept);

        _intercept.x = (_intercept.x + _eyeLocation.x);
        _intercept.y = (_eyeLocation.y - _intercept.y);

        //Log.i(TAG,"Looking at Intercept: X:" + _intercept.x + "  Y:" + _intercept.y);

        setPupilLocation(_intercept);
    }


    /**
     * http://mathworld.wolfram.com/Ellipse-LineIntersection.html
     *
     * @param gx
     * @param gy
     * @param result
     */
    private void calcIntercept(float gx, float gy, PointF result) {

        // translate the eye center point to the origin

        float x = gx;
        float y = gy;

        float a = (_eyeBounds.width()  / 2) * 0.8f;
        float b = (_eyeBounds.height() / 2) * 0.8f;

        double radical = ((a * b)/ (Math.sqrt((a*a) * (y*y) + (b*b) * (x*x))));

        // translate the eye center point to position

        result.x = (float)(radical * x);
        result.y = (float)(radical * y);
    }

    /**
     * If the design has a specified width percent then calculate size from that
     *
     * @param availWidth
     */
    public void setSizeByWidth(float availWidth) {
        if(_width != 0f) {
            setRadiusX((_width * availWidth) / 2);
            setRadiusY(getRadiusX() * _eccen);

            setEyeLocation();
        }
    }

    /**
     *
     * @param availHeight
     */
    public void setSizeByHeight(float availHeight) {
        if(_height != 0f) {
            setRadiusY((_height * availHeight) / 2);
            setRadiusX(getRadiusY() / _eccen);

            setEyeLocation();
        }
    }

    public float getPupilRad() { return _eyeRadius.x * _pupilDia / 2; }
    public float getPupilDia() { return _pupilDia; }
    public void setPupilDia(float newSize) { _pupilDia = newSize; }

    public int getRadiusX() { return Math.round(_eyeRadius.x + 0.5f); }
    public int getRadiusY() { return Math.round(_eyeRadius.y + 0.5f); }

    public void setRadiusX(float newRadius) { _eyeRadius.x = newRadius; }
    public void setRadiusY(float newRadius) { _eyeRadius.y = newRadius; }

    public PointF getEyeRadius() { return _eyeRadius; }
    public PointF getEyePosition() { return _eyeLocation; }

    public void setEccen(float eccen) {
        this._eccen = eccen;
    }

    public void setHeight(float height) {
        this._height = height;
    }

    public void setWidth(Float width) {
        this._width = width;
    }

    public void updateEyeBounds() {

        _eyeBounds.left   = _eyeLocation.x - _eyeRadius.x;
        _eyeBounds.right  = _eyeLocation.x + _eyeRadius.x;
        _eyeBounds.top    = _eyeLocation.y - _eyeRadius.y;
        _eyeBounds.bottom = _eyeLocation.y + _eyeRadius.y;

        eyeLid.updateBounds(_eyeBounds.left,_eyeBounds.top,_eyeBounds.width(),(_eyeBounds.height() * 0.99f));
    }

    public RectF getEyeBounds() {
        return _eyeBounds;
    }


    /**
     * Animator access to blink state
     *
     * @param blink
     */
    public void setBlink(float blink) {

        //Log.i("Eye","Set Blink: " + blink);
        eyeLid.setBlink(blink);
    }
    public float getBlink() {
        return eyeLid.getBlink();
    }


    /**
     * Load the eye specification from XML spec data
     *
     * @param parser
     */
    public void loadXML(XmlResourceParser parser) throws XmlPullParserException , IOException {

        parser.require(XmlPullParser.START_TAG, null, "eye");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            switch(name) {
                case "radiusx":
                    setRadiusX(new Float(readField(parser, "radiusx")));
                    break;
                case "radiusy":
                    setRadiusY(new Float(readField(parser, "radiusy")));
                    break;
                case "eccen":
                    setEccen(new Float(readField(parser, "eccen")));
                    break;
                case "height":
                    setHeight(new Float(readField(parser, "height")));
                    break;
                case "width":
                    setWidth(new Float(readField(parser, "width")));
                    break;
                case "pupildia":
                    setPupilDia(new Float(readField(parser, "pupildia")));
                    break;
                default:
                    throw new XmlPullParserException("Unexpected Tag: " + name);
            }
        }

        // set the initial eye boundry used to draw the ovel in Persona onDraw
        updateEyeBounds();
    }

    /**
     * Process arbitrary field strings
     *
     * @param parser
     * @param field
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readField(XmlPullParser parser, String field) throws IOException, XmlPullParserException {

        String strData = null;

        parser.require(XmlPullParser.START_TAG, null, field);

        if (parser.next() == XmlPullParser.TEXT) {
            strData = parser.getText();
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, null, field);

        return strData;
    }

}
