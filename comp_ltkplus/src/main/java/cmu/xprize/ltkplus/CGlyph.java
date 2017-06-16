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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import cmu.xprize.comp_logging.TLOG_CONST;
import cmu.xprize.util.CClassMap;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.comp_logging.CPreferenceCache;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;


/**
 * A StrokeSet represents a single glyph where each elemental getStroke is a part
 * of the final character.  The glyph is created in absolute getTime and then normalized when
 * the glyph is complete.  It is serialized in a normalized form
 *
 */
public class CGlyph implements ILoadableObject, Cloneable {

    private Context               mContext;
    static public ILogManager     logManager;

    // volatile data
    private CStrokeInfo           _strokeInfo;
    private long                  _startTime;
    private long                  _lastTime;
    private CStroke               _currentStroke;
    private CAffineXform          _reBuildXform;
    private CGlyphMetrics         _metrics         = new CGlyphMetrics();
    private boolean               _isDirty         = false;

    private PointF                _glyphAnimOffset = new PointF();
    private float                 _glyphAnimScaleX = 1.0f;
    private float                 _glyphAnimScaleY = 1.0f;
    private float                 _dotSize         = 0;
    private boolean               _animDirty       = false;
    private String                _drawnState      = TCONST.STROKE_ORIGINAL;

    // persistent glyph data
    private ArrayList<CStrokeInfo> _strokeInfoList;

    private RectF                 _fontBoundingBox   = null;
    private Rect                  _origViewBounds    = null;

    private RectF                 _strokeBoundingBox = null;    // Once the glyph is created this is immutable
    private RectF                 _glyphBoundingBox  = null;    // This is the bound of the currently drawn path
    private PointF                _strokeOrigOffset  = null;

    private float                 _origBaseLine;
    private float                 _fontBaseLine;


    // TODO: Extend JSONHelper so it understands certain built-in composite types - e.g. RectF,
    //
    // Json loadable data

    public String                type;
    public String                tutorid;
    public String                tag;
    public String                version = "0";
    public String                time;
    public long                  duration;
    public String                recognizer;
    public String                constraint;
    public String                stim;
    public String                resp;

    public CStrokeInfo[]         strokes;
    public float[]               gBounds;
    public float[]               fBounds;
    public int[]                 vBounds;
    public float[]               vOffset;
    public float                 gBase;
    public float                 fBase;

    private final static String PUBLIC_DATA_PATH  = Environment.getExternalStorageDirectory() + TCONST.WRITINGTUTOR_FOLDER;



    private float ToleranceFactor = 10;

    //static public ILogManager     logManager;

    private static String      TAG  = "StrokeSet";


    public CGlyph(Context context, float baseline, Rect viewBounds, float dotSize) {

        mContext = context;

        logManager = CLogManager.getInstance();

        _strokeInfoList  = new ArrayList<>();
        _origBaseLine    = baseline;
        _origViewBounds  = viewBounds;
        _dotSize         = dotSize;

        newMetric();
    }


    public void newStroke() {

        _currentStroke = new CStroke();
        _startTime     = System.currentTimeMillis();
        _strokeInfo    = new CStrokeInfo(_currentStroke, _startTime);

        _strokeInfoList.add(_strokeInfo);
    }

    public CGlyph clone() {

        CGlyph newClone = null;

        try {
            newClone = (CGlyph) super.clone();

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return newClone;
    }


    public void endStroke() {

        // #Mod issue #305 - _strokeInfo null when user has finger resting on screen
        // during recycle - see #Mod305 CGlyphInputContainer
        //
        _strokeInfo.setDuration(_lastTime - _startTime);
    }


    public void terminateGlyph() {

        normalizeStroke();
    }

    public void    setDirty(boolean dirty) {
        _isDirty = dirty;
    }
    public boolean getDirty() {
        return _isDirty;
    }

    public CGlyphMetrics newMetric() {
        _metrics = new CGlyphMetrics();
        return _metrics;
    }
    public CGlyphMetrics getMetric() {
        return _metrics;
    }


    public void setDotSize(float dotSize) {
        _dotSize = dotSize;
    }

    public void drawGylyph(Canvas canvas, Paint mPaint, Rect _drawBnds) {

        if(_animDirty) {
            _animDirty = false;
            rebuildGlyph(TCONST.VIEW_ANIMATE, _drawBnds);
        }

        for(int i1 = 0 ; i1 < size() ; i1++) {

            CStroke stroke = getStroke(i1);

//            mPaint.setColor(0xEE8217);
//            mPaint.setAlpha(170);

//            mPaint.setColor(TCONST.GLYPHCOLOR1);

            if(stroke.isPoint(_drawBnds)) {

                PointF center = stroke.getPoint();

                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawCircle(center.x, center.y, _dotSize, mPaint);
            }
            else {
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(stroke.getPath(), mPaint);
            }

//            mPaint.setColor(Color.BLACK);
            mPaint.setAlpha(255);
        }
    }


    public String getDrawnState() {
        return _drawnState;
    }
    public void setDrawnState(String drawnState) {
        _drawnState = drawnState;
    }

    public void setAnimOffsetX(float newX) {
        _glyphAnimOffset.x = newX;
        _animDirty = true;
    }
    public void setAnimOffsetY(float newY) {
        _glyphAnimOffset.y = newY;
        _animDirty = true;
    }
    public void setAnimScaleX(float newX) {
        _glyphAnimScaleX = newX;
        _animDirty = true;
    }
    public void setAnimScaleY(float newY) {
        _glyphAnimScaleY = newY;
        _animDirty = true;
    }

    public float getAnimOffsetX() {
        return _glyphAnimOffset.x;
    }
    public float getAnimOffsetY() {
        return _glyphAnimOffset.y;
    }
    public float getAnimScaleX() {
        return _glyphAnimScaleX;
    }
    public float getAnimScaleY() {
        return _glyphAnimScaleY;
    }

    /**
     * Add the next point to the current getStroke - record when the last segment was added to
     * the getStroke.  This permits calculating inter-getStroke times (to animate hand motions).
     *
     * @param touchPt
     */
    public void addPoint(PointF touchPt) {

        _lastTime  =  System.currentTimeMillis();

        // #MOD issue #305 - _currentStroke can be null just before recognizer called
        // - see #Mod305 CGlyphInputContainer
        //
        _currentStroke.addPoint(touchPt, _lastTime);

        addPointToStrokeBoundingBox(touchPt);
        addPointToGlyphBoundingBox(touchPt);
    }


    public void addPointToStrokeBoundingBox(PointF point) {

        if (_strokeBoundingBox == null) {
            _strokeBoundingBox = new RectF(point.x, point.y, point.x, point.y);
            _fontBoundingBox   = new RectF(point.x, point.y, point.x, point.y);
            return;
        }

        // Expand the bounding box to include it, if necessary
        _strokeBoundingBox.union(point.x, point.y);
    }


    public void addPointToGlyphBoundingBox(PointF point) {

        if (_glyphBoundingBox == null) {
            _glyphBoundingBox = new RectF(point.x, point.y, point.x, point.y);
            return;
        }

        // Expand the bounding box to include it, if necessary
        _glyphBoundingBox.union(point.x, point.y);
    }


    public float getGlyphWidth() {
        return _glyphBoundingBox.width();
    }

    public float getGlyphHeight() {
        return _glyphBoundingBox.height();
    }
    public float getGlyphBaseline() {
        return _origBaseLine;
    }

    public float getOrigOffsetX() {
        return _strokeOrigOffset.x;
    }
    public float getOrigOffsetY() {
        return _strokeOrigOffset.y;
    }


    /**
     * Get the View relative bounding box for the drawn glyph allowing for the stoke weight
     * and whether or not it is a point stroke - e.g. period, colon etc
     *
     * @param weight
     * @return
     */
    public RectF getGlyphViewBounds(Rect viewBnds, float weight) {

        float inset = -weight/2;

        RectF glyphViewBounds = new RectF(_glyphBoundingBox);

        if(isPoint(viewBnds)) {

            glyphViewBounds.inset(-weight, -weight);
        }
        else {

            glyphViewBounds.inset(inset, inset);
        }

        return glyphViewBounds;
    }


    private boolean isPoint(Rect parentBounds) {

        boolean result = false;


        if(_glyphBoundingBox != null &&
           (_glyphBoundingBox.width()  < parentBounds.width()  / ToleranceFactor) &&
           (_glyphBoundingBox.height() < parentBounds.height() / ToleranceFactor)) {

            result = true;
        }
        return result;
    }


    public RectF getFontBoundingBox() {
        return _fontBoundingBox;
    }
    public Rect getOrigBoundingBox() {
        return _origViewBounds;
    }
    public RectF getStrokeBoundingBox() {
        return _strokeBoundingBox;
    }
    public RectF getGlyphBoundingBox() {
        return _glyphBoundingBox;
    }

    public void setFontBoundingBox(RectF bounds) {
        _fontBoundingBox = bounds;
    }
    public void setOrigBoundingBox(Rect bounds) {
        _origViewBounds = bounds;
    }


    public float getGlyphBaselineRatio() { return (_origBaseLine - _glyphBoundingBox.top) / _glyphBoundingBox.height(); }
    public float getFontBaselineRatio() { return (_fontBaseLine - _fontBoundingBox.top) / _fontBoundingBox.height(); }

    public int size() {return _strokeInfoList.size(); }
    public CStroke getStroke(int index) {
        return  _strokeInfoList.get(index).getStroke();
    }


    /**
     * Each Stroke in the glyph is normalized relative to the glyph bounding box upper left
     * corner. The Stroke itself is also time normalized so that each point in a Stroke segment
     * is relative to the previous point (delta) - this simplifies replay animation code
     */
    private void normalizeStroke() {

        float normalX = _glyphBoundingBox.left;
        float normalY = _glyphBoundingBox.top;

        // Record the glyph relative offset in the view -
        //
        _strokeOrigOffset = new PointF(normalX, normalY);

        // First normalize each getStroke relative to the glyph bound box origin.  This allows
        // us to replay relative to the upper left corner of a bounding region.
        //
        for (int i1 = 0 ; i1 < _strokeInfoList.size() ; i1++) {

            long  cTime = _strokeInfoList.get(i1).getTime();

            _strokeInfoList.get(i1).getStroke().normalizeStroke(normalX, normalY, cTime);
        }

        // Normalize the timeline of the StrokeSet itself.  The start of each getStroke is
        // made relative to the start of the entire glyph itself.
        //
        long baseTime = _strokeInfoList.get(0).getTime();

        for (int i1 = 1 ; i1 < _strokeInfoList.size() ; i1++) {

            _strokeInfoList.get(i1).normalizeTime(baseTime);
        }

        // This is Important: record the relative location of the baseline to glyph bounding box
        // itself. This allows us to replay a glyph drawn relative to a given baseline
        // by re-positioning any bounding region vertically relative to a new baseline by the same
        // factor.
        //
        _fontBaseLine  = _origBaseLine - _fontBoundingBox.top;
        _origBaseLine -= _glyphBoundingBox.top;
    }


    public Iterator<CStrokeInfo> iterator() {
        return _strokeInfoList.iterator();
    }


    /**
     * Rebuild the canvas path from the getStroke point data
     */
    public void rebuildGlyph(String options, Rect Container) {

        float scaleX    = 1.0f;
        float scaleY    = 1.0f;
        float timeScale = 1.0f;

        float xOffset = 0;
        float yOffset = 0;

        switch(options) {

            case TCONST.VIEW_ANIMATE:

                // First we have to accommodate the changes in the Stroke scale
                //
                scaleX = (float)Container.width()  / _origViewBounds.width();
                scaleY = (float)Container.height() / _origViewBounds.height();

                scaleX *= (float)_glyphAnimScaleX;
                scaleY *= (float)_glyphAnimScaleY;

                xOffset = _glyphAnimOffset.x;
                yOffset = _glyphAnimOffset.y;
                break;


            // Draw it inside the given container.
            //
            case TCONST.CONTAINER_SCALED:

                Log.d(TAG, "Height: " + Container.height() + "  Width: " + Container.width());

                scaleY = (float)Container.height() / _strokeBoundingBox.height();
                scaleX = (float)Container.width() / _strokeBoundingBox.width();

                xOffset = Container.left;
                yOffset = Container.top;
                break;


            // Position relative to the original view offset and bounds.
            // The Stroke data is invariant
            //
            case TCONST.VIEW_SCALED:

                Log.d(TAG, "Height: " + Container.height() + "  Width: " + Container.width());

                scaleX = (float)Container.width()  / _origViewBounds.width();
                scaleY = (float)Container.height() / _origViewBounds.height();

                xOffset = _strokeOrigOffset.x * scaleX;
                yOffset = _strokeOrigOffset.y * scaleY;
                break;


            // Rebuild normalized relative to the container
            //
            case TCONST.VIEW_NORMAL:

                scaleX = (float)Container.width()  / _strokeBoundingBox.width();
                scaleY = (float)Container.height() / _strokeBoundingBox.height();

                xOffset = Container.left;
                yOffset = Container.top;
                break;
        }

        if(_reBuildXform == null || _reBuildXform.isChanged(scaleX, scaleY, xOffset, yOffset, timeScale)) {

            // Reset the _glyph bounding box for the redraw - addPointToGlyphBoundingBox will re-initialize it
            // within reBuildPath.
            //
            _glyphBoundingBox = null;

            // Use the same transform used in the replay componenet to make them analygous
            //
            _reBuildXform = new CAffineXform(scaleX, scaleY, xOffset, yOffset, timeScale);

            // Rebuild each getStroke relative to a different origin
            //
            for (int i1 = 0; i1 < _strokeInfoList.size(); i1++) {

                _strokeInfoList.get(i1).getStroke().reBuildPath(this, _reBuildXform);
            }

// DEBUG - You can use this point to modify an existing glyph definition file
//
//            _origViewBounds = Container;
//
//            updateGlyphLog();

        }
    }


    public void updateGlyphLog() {

        writeGlyphToLog(recognizer, constraint, stim, resp);
    }


    /**
     *
     * @param recog
     * @param constraint
     * @param stimChar
     * @param respChar
     */
    public void saveGlyphPrototype(String recog, String constraint, String stimChar, String respChar) {

        FileWriter out = null;

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            String[] files = null;
            String outPath;

            // Validate output folder

            outPath = mContext.getExternalFilesDir(null).getPath() + TCONST.GLYPHS_FOLDER;

            File outputFile = new File(outPath);

            if (!outputFile.exists()) {

                if(!outputFile.mkdir()) {

                    Log.d(TAG, "Error creating Output path");
                }
            }

            String nameMap = GCONST.glyphMap.get(stimChar.substring(0,1));

            if(nameMap != null) {

                // Generate a tutor instance-unique id for the log name
                // This won't change until the tutor changes
                //
                outPath += TLOG_CONST.GLYPHLOG + nameMap + TLOG_CONST.JSONLOG;

                // Append Glyph Data to file
                try {
                    out = new FileWriter(outPath, TLOG_CONST.REPLACE);

                    // Throws if there is a JSON serializatin error
                    //
                    out.write(serializeGlyph("no_tutor", "no_tag", recog, constraint, stimChar, respChar));
                    out.close();
                } catch (Exception e) {
                    Log.e(TAG, "Glyph Serialization Error: " + e);
                }
            }
            else {
                Log.e(TAG, "Trying to save unmapped Glyph : " + stimChar);
            }
        }
    }


    /**
     *
     * @param recog
     * @param constraint
     * @param stimChar
     * @param respChar
     */
    public void writeGlyphToLog(String recog, String constraint, String stimChar, String respChar) {

        // Throws if there is a JSON serializatin error
        //
        logManager.postPacket(serializeGlyph(CPreferenceCache.getPrefID(TLOG_CONST.CURRENT_TUTOR), TAG, recog, constraint, stimChar, respChar));
    }


    /**
     *
     * @param tutorID
     * @param tag
     * @param recog
     * @param constraint
     * @param stimChar
     * @param respChar
     * @return
     */
    private String serializeGlyph(String tutorID, String tag, String recog, String constraint, String stimChar, String respChar)  {

        StringWriter outString = new StringWriter();
        JsonWriter   writer    = new JsonWriter(outString);

        writer.setIndent("  ");

        try {
            writer.beginObject();

            writer.name("type").value(TCONST.GLYPH_DATA);
            writer.name("tutorid").value(tutorID);
            writer.name("tag").value(tag);
            writer.name("version").value(GCONST.RECORD_VERSION);

            DateFormat df = DateFormat.getDateTimeInstance();
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            String datetime = df.format(new Date());

            writer.name("time").value(datetime);
            writer.name("duration").value(duration);
            writer.name("recognizer").value(recog);
            writer.name("constraint").value(constraint);
            writer.name("stim").value(stimChar);
            writer.name("resp").value(respChar);

            writeNamedRectF(writer, "gBounds", _strokeBoundingBox);
            writeNamedRectF(writer, "fBounds", _fontBoundingBox);
            writeNamedRect(writer, "vBounds", _origViewBounds);
            writeNamedPointF(writer, "vOffset", _strokeOrigOffset);

            writer.name("gBase").value(_origBaseLine);
            writer.name("fBase").value(_fontBaseLine);

            writer.name("strokes");
            writeStrokeInfo(writer, _strokeInfoList);

            writer.endObject();

            writer.close();
        }
        catch(Exception e) {
            CErrorManager.logEvent(TAG, "Glyph Serialization Failed: ", e, false);
        }

        Log.i(TAG, "GlyphData" + writer.toString());

        // return packet with comma delimiter
        //
        return outString.toString() + ",";
    }

    public void writeStrokeInfo(JsonWriter writer, ArrayList<CStrokeInfo> strokes) throws IOException {

        writer.beginArray();

        for (CStrokeInfo stroke : strokes) {
            writer.beginObject();
            writer.name("time").value(stroke.getTime());
            writer.name("duration").value(stroke.getDuration());
            writer.name("stroke");
            writeStroke(writer, stroke.getStroke());
            writer.endObject();
        }
        writer.endArray();
    }

    public void writeStroke(JsonWriter writer, CStroke stroke) throws IOException {

        writer.beginArray();

        for (CStroke.StrokePoint point : stroke.getPoints()) {
//            writer.beginObject();
//            writer.name("p");

            writePoint(writer, point.getPoint());
            writer.value(point.getTime());

//            writer.name("t").value(point.getTime());
//            writer.endObject();
        }
        writer.endArray();
    }

    public void writePoint(JsonWriter writer, PointF point) throws IOException {

//        writer.beginArray();

        writer.value(point.x);
        writer.value(point.y);

//        writer.endArray();
    }

    public void writeNamedRectF(JsonWriter writer, String name, RectF rect) throws IOException {

        writer.name(name);

        writer.beginArray();

        writer.value(rect.left);
        writer.value(rect.top);
        writer.value(rect.right);
        writer.value(rect.bottom);

        writer.endArray();
    }

    public void writeNamedRect(JsonWriter writer, String name, Rect rect) throws IOException {

        writer.name(name);

        writer.beginArray();

        writer.value(rect.left);
        writer.value(rect.top);
        writer.value(rect.right);
        writer.value(rect.bottom);

        writer.endArray();
    }

    public void writeNamedPointF(JsonWriter writer, String name, PointF point) throws IOException {

        writer.name(name);

        writer.beginArray();

        writer.value(point.x);
        writer.value(point.y);

        writer.endArray();
    }





    //************ Serialization



    /**
     * Load the glyph specification from JSON file data
     *
     */
    public boolean loadGlyphFactory(String glyph, IScope scope) {

        boolean result = false;

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            String inPath;

            // Validate glyph prototype file
            //
            inPath = mContext.getExternalFilesDir(null).getPath() + TCONST.GLYPHS_FOLDER;

            String nameMap = GCONST.glyphMap.get(glyph.substring(0,1));

            if(nameMap != null) {

                inPath += TLOG_CONST.GLYPHLOG + nameMap + TLOG_CONST.JSONLOG;

                File inputFile = new File(inPath);

                if (inputFile.exists()) {

                    try {
                        loadJSON(new JSONObject(JSON_Helper.cacheDataByName(inPath)), scope);
                        result = true;

                    } catch (JSONException e) {

                        CErrorManager.logEvent(TAG, "JSON FORMAT ERROR: " + inPath + " : ", e, false);
                    }
                }
            }
            else {
                Log.e(TAG, "Trying to load unmapped Glyph : " + glyph);
            }
        }

        return result;
    }


    public long calcGlyphDuration() {

        // Calc the glyph getTime
        duration = 0;

        for(CStrokeInfo strokeinfo : _strokeInfoList) {
            duration += strokeinfo.getDuration();
        }
        return duration;
    }


    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);

        if(version.equals(GCONST.RECORD_VERSION)) {

            _strokeInfoList = new ArrayList<CStrokeInfo>(Arrays.asList(strokes));

            _fontBoundingBox = new RectF(fBounds[0], fBounds[1], fBounds[2], fBounds[3]);
            _origViewBounds = new Rect(vBounds[0], vBounds[1], vBounds[2], vBounds[3]);

            _strokeBoundingBox = new RectF(gBounds[0], gBounds[1], gBounds[2], gBounds[3]);
            _glyphBoundingBox = _strokeBoundingBox;

            _strokeOrigOffset = new PointF(vOffset[0], vOffset[1]);
            _origBaseLine = gBase;
            _fontBaseLine = fBase;
        }
        else {
            Log.d(TAG, "Old Data");

            _strokeInfoList = new ArrayList<CStrokeInfo>(Arrays.asList(strokes));

            _fontBoundingBox = new RectF(fBounds[0], fBounds[1], fBounds[2], fBounds[3]);

            _strokeBoundingBox = new RectF(gBounds[0], gBounds[1], gBounds[2], gBounds[3]);
            _glyphBoundingBox = _strokeBoundingBox;

            _origViewBounds = new Rect((int)gBounds[0], (int)gBounds[1], (int)gBounds[2], (int)gBounds[3]);
            _origViewBounds.inset(-20,-20);

            _strokeOrigOffset = new PointF(10,10);
            _origBaseLine = gBase;
            _fontBaseLine = fBase;

        }


        calcGlyphDuration();
    }

}
