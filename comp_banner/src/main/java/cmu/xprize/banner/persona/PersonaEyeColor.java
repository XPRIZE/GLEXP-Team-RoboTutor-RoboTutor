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

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 *
 */
public class PersonaEyeColor {

    private int   _eyeColor;
    private int   _eyeStrokeColor;
    private float _eyeStrokeWidth;

    private int   _pupilColor;
    private int   _pupilStrokeColor;
    private float _pupilStrokeWidth;

    public PersonaEyeColor() {
    }


    public Paint getEyeColor() {
        Paint eyePaint;

        eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setStyle(Paint.Style.FILL);
        eyePaint.setColor(_eyeColor);

        return eyePaint;
    }

    public boolean hasEyeStroke() { return (_eyeStrokeWidth > 0)? true:false; }

    public Paint getEyeStroke() {
        Paint eyePaint;

        eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setStyle(Paint.Style.STROKE);
        eyePaint.setColor(_eyeStrokeColor);
        eyePaint.setStrokeWidth(_eyeStrokeWidth);

        return eyePaint;
    }

    public Paint getPupilColor() {
        Paint eyePaint;

        eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setStyle(Paint.Style.FILL);
        eyePaint.setColor(_pupilColor);

        return eyePaint;
    }

    public boolean hasPupilStroke() { return (_pupilStrokeWidth > 0)? true:false; }

    public Paint getPupilStroke() {
        Paint eyePaint;

        eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setStyle(Paint.Style.STROKE);
        eyePaint.setColor(_pupilStrokeColor);
        eyePaint.setStrokeWidth(_pupilStrokeWidth);

        return eyePaint;
    }


    public void setEyeColor(Integer eyeColor) {
        this._eyeColor = eyeColor;
    }
    public void setEyeStrokeColor(int eyeStrokeColor) {
        this._eyeStrokeColor = eyeStrokeColor;
    }
    public void setEyeStrokeWidth(Float eyeStrokeWidth) {
        this._eyeStrokeWidth = eyeStrokeWidth;
    }

    public void setPupilColor(int pupilColor) {
        this._pupilColor = pupilColor;
    }
    public void setPupilStrokeColor(int pupilStrokeColor) { this._pupilStrokeColor = pupilStrokeColor; }
    public void setPupilStrokeWidth(Float pupilStrokeWidth) { this._pupilStrokeWidth = pupilStrokeWidth; }


    /**
     * Load the eye specification from XML spec data
     *
     * @param parser
     */
    public void loadXML(XmlResourceParser parser) throws XmlPullParserException , IOException {

        parser.require(XmlPullParser.START_TAG, null, "eyecolor");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            switch(name) {
                case "eyecolor":
                    setEyeColor(Color.parseColor(readField(parser, "eyecolor")));
                    break;
                case "eyestrokecolor":
                    setEyeStrokeColor(Color.parseColor(readField(parser, "eyestrokecolor")));
                    break;
                case "eyestrokewidth":
                    setEyeStrokeWidth(new Float(readField(parser, "eyestrokewidth")));
                    break;

                case "pupilcolor":
                    setPupilColor(Color.parseColor(readField(parser, "pupilcolor")));
                    break;
                case "pupilstrokecolor":
                    setPupilStrokeColor(Color.parseColor(readField(parser, "pupilstrokecolor")));
                    break;
                case "pupilstrokewidth":
                    setPupilStrokeWidth(new Float(readField(parser, "pupilstrokewidth")));
                    break;

                default:
                    throw new XmlPullParserException("Unexpected Tag: " + name);
            }
        }
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
