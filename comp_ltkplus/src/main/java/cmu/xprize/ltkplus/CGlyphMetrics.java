//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
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

import cmu.xprize.util.CPixelArray;
import cmu.xprize.util.TCONST;

public class CGlyphMetrics {

    private float PosVarV;
    private float SizVarV;
    private float PosVarH;
    private float SizVarH;
    private float aspectPr;
    private float aspectGl;
    private float aspectVar;

    private float visualMatch;

    private Bitmap                _bitmap;
    private CPixelArray           _pixels;
    private int                   _charValue = 0;

    private Paint                 mPaint;
    private Paint                 mPaintDBG;
    private boolean               DBG        = false;

    private static final String   TAG = "CGlyphMetrics";



    public CGlyphMetrics() {

        // Create a paint object to deine the line parameters
        mPaint = new Paint();

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(GCONST.STROKE_WEIGHT);
        mPaint.setAntiAlias(true);
    }


    public void calcMetrics(CGlyph currentGlyph, RectF glyphBnds, Rect protoBnds, Rect expectBnds, float STROKE_WEIGHT) {

        PosVarV = Math.abs(protoBnds.top - glyphBnds.top) / expectBnds.height();
        PosVarH = Math.abs(protoBnds.left - glyphBnds.left) / expectBnds.width();

        SizVarV = Math.abs(protoBnds.height() - glyphBnds.height()) / expectBnds.height();
        SizVarH = Math.abs(protoBnds.width() - glyphBnds.width()) / expectBnds.width();

        aspectPr = ((float) protoBnds.width() / (float) protoBnds.height());
        aspectGl = (glyphBnds.width() / glyphBnds.height());

        aspectVar = Math.abs(aspectPr - aspectGl);
    }


    private String formatFloat(float value) {

        return String.format("%.3f", value);
    }


    public String getVerticalDeviation() {
        return formatFloat(PosVarV);
    }

    public String getHorizontalDeviation() {
        return formatFloat(PosVarH);
    }

    public String getHeightDeviation() {
        return formatFloat(SizVarV);
    }

    public String getWidthDeviation() {
        return formatFloat(SizVarH);
    }

    public String getAspectDeviation() {
        return formatFloat(aspectVar);
    }

    public String getVisualMetric() {
        return formatFloat(visualMatch);
    }

    public void showDebugBounds(boolean showHide) { DBG = showHide; }


    public float getDeltaY() {
        return PosVarV;
    }
    public float getDeltaX() {
        return PosVarH;
    }
    public float getDeltaH() {
        return SizVarV;
    }
    public float getDeltaW() {
        return SizVarH;
    }
    public float getDeltaA() {
        return aspectVar;
    }
    public float getVisualDelta() {
        return visualMatch;
    }


    public Bitmap generateVisualComparison(Rect fontBounds, String charToCompare, CGlyph glyphToCompare, Paint mPaint, boolean isVolatile) {

        generateVisualMetric(fontBounds, charToCompare, charToCompare,  glyphToCompare,  mPaint,  isVolatile);

        // If we are going to use the bitmap after then we apply the pixels back to the bitmap and
        // return the result to be drawn externally
        //
        if(!isVolatile){

            _pixels.applyBitmap();
        }

        return _bitmap;
    }



    public float generateVisualMetric(Rect fontBounds, String charToCompare, String expectedChar, CGlyph glyphToCompare, Paint mPaint, boolean isVolatile) {

        Rect charBnds = new Rect();
        visualMatch   = 0;

        // Ensure that there is a testable region - otherwise Bitmap.createBitmap will fail
        //
        if(fontBounds.width() > 0 && fontBounds.height() > 0) {

            _bitmap = Bitmap.createBitmap(fontBounds.width(), fontBounds.height(), Bitmap.Config.ARGB_8888);

            Canvas drawSurface = new Canvas(_bitmap);

            //     mPaint.getTextBounds(expectedChar, 0, 1, charBnds);
            mPaint.getTextBounds(charToCompare, 0, 1, charBnds);

            Rect insetBnds = new Rect(0, 0, charBnds.width(), charBnds.height());
            int inset = (int) (GCONST.STROKE_WEIGHT / 2);

            insetBnds.inset(inset, inset);

            glyphToCompare.rebuildGlyph(TCONST.VIEW_NORMAL, insetBnds);

            // Draw the charToCompare using the desired font to the Bitmap - then capture the pixels
            // and count the numberof pixels occupied by the char
            //
            mPaint.setColor(TCONST.FONTCOLOR1);
            drawSurface.drawText(charToCompare, -charBnds.left, -charBnds.top, mPaint);

            _pixels = new CPixelArray(_bitmap);
            _charValue = 0;

            for (int i1 = 0; i1 < _pixels.getWidth(); i1++) {
                for (int i2 = 0; i2 < _pixels.getHeight(); i2++) {

                    if (_pixels.getPixel(i1, i2) == TCONST.FONTCOLOR1) {
                        _charValue++;
                    }
                }
            }

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
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(GCONST.STROKE_WEIGHT);

            // Redraw the current glyph path to the bitmap surface
            //
            glyphToCompare.drawGylyph(drawSurface, mPaint, fontBounds);

            _pixels.getPixels();
            int errValue = 0;
            int excessValue = 0;

            for (int i1 = 0; i1 < _pixels.getWidth(); i1++) {
                for (int i2 = 0; i2 < _pixels.getHeight(); i2++) {

                    if (_pixels.getPixel(i1, i2) == TCONST.FONTCOLOR1) {

                        _pixels.setPixel(i1, i2, TCONST.ERRORCOLOR1);
                        errValue++;
                    } else if (_pixels.getPixel(i1, i2) == TCONST.GLYPHCOLOR1) {
                        //excessValue++;
                    }
                }
            }

            // Calc the visual match metric -
            //
            // 0 = complete overlay - glyph may still have undesirable form but covers the char at least
            // 1 = no match
            //
            visualMatch = (float) (_charValue - errValue - (excessValue / 2)) / (float) _charValue;
            Log.d(TAG, "Char Error: " + visualMatch);

            if (isVolatile)
                _bitmap.recycle();
        }

        return visualMatch;
    }

}
