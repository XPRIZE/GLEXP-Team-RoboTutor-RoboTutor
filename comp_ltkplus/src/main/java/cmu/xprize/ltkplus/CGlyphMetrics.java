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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.CPixelArray;
import cmu.xprize.util.TCONST;

import static cmu.xprize.ltkplus.GCONST.NO_LOG;
import static cmu.xprize.util.TCONST.GRAPH_MSG;
import static cmu.xprize.util.TCONST.LTKPLUS_MSG;

public class CGlyphMetrics {

    static public ILogManager logManager;

    private float CGVarX;
    private float CGVarY;
    private float CGVarW;
    private float CGVarH;

    private boolean CG_isTooLeft;
    private boolean CG_isTooHigh;
    private boolean CG_isTooWide;
    private boolean CG_isTooTall;

    private float PosVarY;
    private float SizVarH;
    private float PosVarX;
    private float SizVarW;
    private float aspectPr;
    private float aspectGl;
    private float aspectVar;

    private float _visualMatch;
    private float _visualError;

    private Bitmap                _bitmap;
    private CPixelArray           _pixels;

    private int                   _charValue   = 0;
    private int                   _missValue   = 0;

    private int                   _glyphExcess = 0;
    private int                   _glyphTotal  = 0;

    private Paint                 mPaint;
    private Paint                 mPaintDBG;
    private boolean               DBG        = false;

    private static final String   TAG = "CGlyphMetrics";



    public CGlyphMetrics() {

        logManager = CLogManager.getInstance();

        // Create a paint object to deine the line parameters
        mPaint = new Paint();

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(GCONST.STROKE_WEIGHT);
        mPaint.setAntiAlias(true);
    }


    public void calcMetrics(String  expectedChar, String  charToCompare, CGlyph currentGlyph, RectF glyphBnds, Rect protoBnds, Rect viewBnds, Rect expectBnds, float STROKE_WEIGHT) {

        // Calc the center of gravity (center) of the glyph / prototype

        float cgx = (glyphBnds.left + glyphBnds.right) / 2;
        float cgy = (glyphBnds.bottom + glyphBnds.top) / 2;

        float cgpx = (protoBnds.left + protoBnds.right) / 2;
        float cgpy = (protoBnds.bottom + protoBnds.top) / 2;

        // Calc the relative variance between the glyph and the proto CG as a percentage
        // of the viewbox

        CG_isTooLeft = cgx < cgpx;
        CG_isTooHigh = cgy < cgpy;
        CG_isTooWide = glyphBnds.width() > protoBnds.width();
        CG_isTooTall = glyphBnds.height() > protoBnds.height();

        CGVarX = Math.abs(cgx - cgpx) / viewBnds.width();
        CGVarY = Math.abs(cgy - cgpy) / viewBnds.height();

        CGVarW = Math.abs(protoBnds.width() - glyphBnds.width()) / viewBnds.width();
        CGVarH = Math.abs(protoBnds.height() - glyphBnds.height()) / viewBnds.height();

        PosVarY = Math.abs(protoBnds.top - glyphBnds.top) / expectBnds.height();
        PosVarX = Math.abs(protoBnds.left - glyphBnds.left) / expectBnds.width();

        SizVarH = Math.abs(protoBnds.height() - glyphBnds.height()) / expectBnds.height();
        SizVarW = Math.abs(protoBnds.width() - glyphBnds.width()) / expectBnds.width();

        aspectPr = ((float) protoBnds.width() / (float) protoBnds.height());
        aspectGl = (glyphBnds.width() / glyphBnds.height());

        aspectVar = Math.abs(aspectPr - aspectGl);

        logManager.postEvent_D(LTKPLUS_MSG, "target:CGlyphMetrics,action:calcmetrics,expected:" + expectedChar + ",comparewith:" + charToCompare + ",var_x:" + PosVarX + ",var_y:" + PosVarY + ",var_w:" + SizVarW + ",var_h:" + SizVarH + ",var_a:" + aspectVar );
    }


    private String formatFloat(float value) {

        return String.format("%.3f", value);
    }


    public String getVerticalDeviation() {
        return formatFloat(PosVarY);
    }

    public String getHorizontalDeviation() {
        return formatFloat(PosVarX);
    }

    public String getHeightDeviation() {
        return formatFloat(SizVarH);
    }

    public String getWidthDeviation() {
        return formatFloat(SizVarW);
    }

    public String getAspectDeviation() {
        return formatFloat(aspectVar);
    }

    public String getVisualMetric() {
        return formatFloat(_visualMatch);
    }

    public String getGlyphErrorMetric() {
        return formatFloat(_visualError);
    }

    public void showDebugBounds(boolean showHide) { DBG = showHide; }

    public boolean getIsLeft() { return CG_isTooLeft; }
    public boolean getIsHigh() { return CG_isTooHigh; }
    public boolean getIsWide() { return CG_isTooWide; }
    public boolean getIsTall() { return CG_isTooTall; }

    public float getDeltaCGY() {
        return CGVarY;
    }
    public float getDeltaCGX() {
        return CGVarX;
    }
    public float getDeltaCGH() {
        return CGVarH;
    }
    public float getDeltaCGW() {
        return CGVarW;
    }

    public float getDeltaY() {
        return PosVarY;
    }
    public float getDeltaX() {
        return PosVarX;
    }
    public float getDeltaH() {
        return SizVarH;
    }
    public float getDeltaW() {
        return SizVarW;
    }
    public float getDeltaA() {
        return aspectVar;
    }

    public float getVisualMatch() {
        return _visualMatch;
    }
    public float getVisualError() {
        return _visualError;
    }



    public Bitmap generateVisualComparison(Rect fontBounds, String charToCompare, CGlyph glyphToCompare, Paint mPaint, float strokeWeight, boolean isVolatile) {

        generateVisualMetric(fontBounds, charToCompare, charToCompare,  glyphToCompare,  mPaint, strokeWeight,  isVolatile);

        // If we are going to use the bitmap after then we apply the pixels back to the bitmap and
        // return the result to be drawn externally
        //
        if(!isVolatile){

            _pixels.applyBitmap();
        }

        return _bitmap;
    }



    public float generateVisualMetric(Rect fontBounds, String charToCompare, String expectedChar, CGlyph glyphToCompare, Paint mPaint, float strokeWeight, boolean isVolatile) {

        Rect charBnds = new Rect();
        _visualMatch = 0;

        long _time = System.currentTimeMillis();

        // Setup the stroke weight -
        // The visual comparator was calibrated with a line weight of 45f so we scale that to whatever
        // font size we use to get approx the same visual coverage on the visual metric.  Otherwise we use
        // the pen size defined by the tutor author.
        //
        if(strokeWeight == GCONST.CALIBRATED_WEIGHT) {

            strokeWeight = fontBounds.height() / GCONST.CALIBRATION_CONST;
        }
        mPaint.setStrokeWidth(strokeWeight);


        // Ensure that there is a testable region - otherwise Bitmap.createBitmap will fail
        //
        if(fontBounds.width() > 0 && fontBounds.height() > 0) {

            _bitmap = Bitmap.createBitmap(fontBounds.width(), fontBounds.height(), Bitmap.Config.ARGB_8888);

            Canvas drawSurface = new Canvas(_bitmap);

            //     mPaint.getTextBounds(expectedChar, 0, 1, charBnds);
            mPaint.getTextBounds(charToCompare, 0, 1, charBnds);

            Rect insetBnds = new Rect(0, 0, charBnds.width(), charBnds.height());
            int inset = (int) (strokeWeight / 2);

            insetBnds.inset(inset, inset);

            glyphToCompare.rebuildGlyph(TCONST.VIEW_NORMAL, insetBnds);

            // Draw the charToCompare using the desired font to the Bitmap - then capture the pixels
            // and count the numberof pixels occupied by the char
            //
            mPaint.setColor(TCONST.FONTCOLOR1);
            drawSurface.drawText(charToCompare, -charBnds.left, -charBnds.top, mPaint);

            _pixels = new CPixelArray(_bitmap);

            _charValue = _pixels.scanForColor(TCONST.FONTCOLOR1);

            // Draw a border around the draw region - debug only
            //
            if (DBG) {
                charBnds.offset(-charBnds.left, -charBnds.top);

                mPaintDBG = new Paint();
                mPaintDBG.setStyle(Paint.Style.STROKE);
                mPaintDBG.setColor(Color.YELLOW);
                drawSurface.drawRect(charBnds, mPaintDBG);
            }

            // Draw the glyphToCompare overtop of the char and count the number of pixels not
            // obscured by the glyph - represents a metric of the correspondence between the two
            //
            mPaint.setColor(TCONST.GLYPHCOLOR1);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(strokeWeight);

            // Redraw the current glyph path to the bitmap surface
            //
            glyphToCompare.drawGylyph(drawSurface, mPaint, fontBounds);

            _pixels.getPixels();

            // Count the total pixels in the glyph image
            // Replace Font pixels that are "missed" and return count
            // Count glyph pixels outside the font iumage
            //
            _glyphTotal  = _pixels.scanNotColor(TCONST.FONTCOLOR1);
            _missValue   = _pixels.scanAndReplace(TCONST.FONTCOLOR1, TCONST.ERRORCOLOR1);
            _glyphExcess = _pixels.scanForColor(TCONST.GLYPHCOLOR1);
//            _glyphExcess = _pixels.scanForColor(mPaint.getColor());

            // Calc the visual match metric -
            //
            // 0 = complete overlay - glyph may still have undesirable form but covers the char at least
            // 1 = no match
            //
            _visualMatch = (float) (_charValue - _missValue) / (float) _charValue;
            _visualError = 1.0f - ((float) _glyphExcess / (float) _glyphTotal);

//            Log.d(TAG, "Glyph Excess: " + _visualError);

            if (isVolatile)
                _bitmap.recycle();
        }

        Log.d("LTKPLUS", "Time in recognizer: " + (System.currentTimeMillis() - _time));

        logManager.postEvent_D(LTKPLUS_MSG, "target:CGlyphMetrics,action:generatevisualmetric,expected:" + expectedChar + ",comparewith:" + charToCompare + ",visualmatch:" + _visualMatch + ",visualerror:" + _visualError);

        return _visualMatch;
    }

}
