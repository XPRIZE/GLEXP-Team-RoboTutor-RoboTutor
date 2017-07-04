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

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

import static cmu.xprize.util.TCONST.GRAPH_MSG;


/**
 *
 */
public class CStroke {

    // The list of points in this getStroke

    private ArrayList<StrokePoint> _points;

    private RectF   _boundingBox = null;

    // canvas relative coordinates for drawing
    //
    private Path    _canvasPath;
    private PointF  _canvasPoint;
    private float   _X, _Y;

    private float ToleranceFactor = 10;


    // Animation replay variables

    private Point       nPoint;
    private Point       lPoint;
    private int         oldIndex;
    private Path        replayPath;

    final private String TAG = "StrokeClass";



    public CStroke() {
        _points      = new ArrayList<StrokePoint>();
        _canvasPath  = new Path();
        _canvasPoint = new PointF();
    }


    public CStroke(float[] data) {
        _points      = new ArrayList<StrokePoint>();
        _canvasPath  = new Path();
        _canvasPoint = new PointF();

        for(int i1 = 0 ; i1 < data.length ; i1 +=3) {

            addPoint(new PointF(data[i1],data[i1+1]), (long)data[i1 + 2]);
        }
    }


    public CStroke(PointF point, long time) {
        this();
        addPoint(point, time);
    }


    public CStroke(PointF origin) {
        this(origin, System.currentTimeMillis());
    }



    //************************************************************************
    //** JNI access methods
    // LTK uses these methods in JNI code to access the contents of a getStroke
    // in the Native domain
    // These are JNI variables - see ...\ltk\src\main\jniLipiJNI.cpp for how they are accessed


    public int getNumberOfPoints() {
        return _points.size();
    }

    public PointF getPointAt(int index) {
        return _points.get(index).getPoint();
    }


    //** JNI access methods
    //************************************************************************


    public ArrayList<StrokePoint> getPoints() {
        return _points;
    }

    public int getPointsInPath() {
        return _points.size();
    }


    public long getTime() {
        return _points.get(0)._time;
    }



    public Path getPath() {
        return _canvasPath;
    }


    public PointF getPoint() {
        return _canvasPoint;
    }



    // Expands the bounding box to accommodate the given point if necessary
    private void addPointToBoundingBox(PointF point) {

        if (_boundingBox == null) {
            _boundingBox = new RectF(point.x, point.y, point.x, point.y);
            return;
        }

        // Expand the bounding box to include it, if necessary
        _boundingBox.union(point.x, point.y);
    }


    // Adds the given point to this getStroke
    public void addPoint(PointF point, long time) {

        //#Mod #383 Jun 4 2017 - limit arraylist size for JNI
        //
        if(_points.size() < 250) {

            // Add Root node
            //
            if (_points.isEmpty()) {

                _canvasPath.moveTo(point.x, point.y);
                _canvasPoint = new PointF(point.x, point.y);
            } else {
                _canvasPath.quadTo(_X, _Y, (point.x + _X) / 2, (point.y + _Y) / 2);
            }

            _X = point.x;
            _Y = point.y;

            _points.add(new StrokePoint(point, time));

            addPointToBoundingBox(point);
        }
    }


    public boolean isPoint(Rect parentBounds) {

        boolean point = false;

        if((_boundingBox.width()  < parentBounds.width()  / ToleranceFactor) &&
           (_boundingBox.height() < parentBounds.height() / ToleranceFactor)) {

            point = true;
        }
        return point;
    }


    public void reBuildPath(CGlyph srcGlyph, CAffineXform glyphXform) {

        boolean init = false;

        _canvasPath = new Path();

        for(StrokePoint strokepoint : _points) {

            glyphXform.setOrigX((int) ((strokepoint.getPoint().x * glyphXform.getScaleX()) + glyphXform.getOffsetX()));
            glyphXform.setOrigY((int) ((strokepoint.getPoint().y * glyphXform.getScaleY()) + glyphXform.getOffsetY()));

            // Build the new bounding box for the glyph we are recreating -
            //
            srcGlyph.addPointToGlyphBoundingBox(glyphXform.getPoint());

            // Add Root node
            //
            if(!init) {
                _canvasPath.moveTo(glyphXform.getOrigX(), glyphXform.getOrigY());
                _canvasPoint = new PointF(glyphXform.getOrigX(), glyphXform.getOrigY());
                init = true;

            } else {
                _canvasPath.quadTo(_X, _Y, (glyphXform.getOrigX() + _X) / 2, (glyphXform.getOrigY() + _Y) / 2);
            }
            _X = glyphXform.getOrigX();
            _Y = glyphXform.getOrigY();

        }
    }


    public Path initReplayPath(CAffineXform xForm) {

        nPoint = new Point();
        lPoint = new Point(xForm.getOrigX(), xForm.getOrigY());
        replayPath = new Path();

        replayPath.moveTo(lPoint.x, lPoint.y);

        Log.v(GRAPH_MSG, "CStroke:initReplayPath: " + 0 + " : " + xForm.getOrigX() + " : " + xForm.getOrigX());

        oldIndex = 1;

        return replayPath;
    }


    public Path incrReplayPath(CAffineXform xForm, int index) {

        for(int i1 = oldIndex ; i1 <= index ; i1++)
        {
            nPoint.x = (int)((_points.get(i1).getX() * xForm.getScaleX()) + xForm.getOffsetX());
            nPoint.y = (int)((_points.get(i1).getY() * xForm.getScaleY()) + xForm.getOffsetY());

            Log.v(GRAPH_MSG, "CStroke:next: " + i1 + " : " + nPoint.x + " : " + nPoint.y );
            Log.v(GRAPH_MSG, "CStroke:last: " + i1 + " : " + lPoint.x + " : " + lPoint.y );

            replayPath.quadTo(lPoint.x, lPoint.y, (nPoint.x + lPoint.x) / 2, (nPoint.y + lPoint.y) / 2);

            lPoint = nPoint;
        }

        oldIndex = index + 1;

        return replayPath;
    }


    public PointF getReplayPoint(CAffineXform xForm, int index) {
        PointF ipoint = new PointF();

        ipoint.x = (int)((_points.get(index).getX() * xForm.getScaleX()) + xForm.getOffsetX());
        ipoint.y = (int)((_points.get(index).getY() * xForm.getScaleY()) + xForm.getOffsetY());

        return ipoint;
    }


    public Path getReplayPath() {
        return replayPath;
    }


    public void normalizeStroke(float normalX, float normalY, long  cTime ) {

        ArrayList<StrokePoint> normalPoints = new ArrayList<StrokePoint>();
        cTime =  _points.get(0).getTime();

        for (int i1 = 0 ; i1 < _points.size() ; i1++)
        {

            Log.v(GRAPH_MSG, "CStroke:normalizeStroke: " + (long)(_points.get(i1).getX() - normalX) + " : " + (long)(_points.get(i1).getY() - normalY) + " delta : " + (long)(_points.get(i1).getTime() - cTime));

            long pTime =  _points.get(i1).getTime();

            normalPoints.add(new StrokePoint(_points.get(i1).getX() - normalX, _points.get(i1).getY() - normalY, pTime - cTime));

            cTime = pTime;
        }

        _points = normalPoints;
    }


    public class StrokePoint {

        private PointF _point;
        private Long _time;

        StrokePoint(PointF point, long time) {
            _point = point;
            _time = time;
        }

        StrokePoint(float x, float y, long time) {
            _point = new PointF(x, y);
            _time = time;
        }

        public PointF getPoint() {
            return _point;
        }

        public float getX() {
            return _point.x;
        }

        public float getY() {
            return _point.y;
        }

        public long getTime() {
            return _time;
        }
    }

    public Iterator<StrokePoint> iterator() {
        return _points.iterator();
    }
}
