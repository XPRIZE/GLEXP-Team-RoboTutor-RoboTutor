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

package cmu.xprize.ltk;

import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;


/**
 *
 */
public class Stroke  {

    // The list of points in this stroke

    private ArrayList<StrokePoint> _points;

    private RectF _boundingBox = null;
    private Path  _truePath;
    private float _X, _Y;

    private float ToleranceFactor = 10;


    // Animation replay variables

    private Point       nPoint;
    private Point       lPoint;
    private int         oldIndex;
    private Path        strokePath;

    final private String TAG = "StrokeClass";



    public Stroke() {
        _points   = new ArrayList<StrokePoint>();
        _truePath = new Path();
    }


    public Stroke(PointF point, long time) {
        this();
        addPoint(point, time);
    }


    public Stroke(PointF origin) {
        this(origin, System.currentTimeMillis());
    }


    public ArrayList<StrokePoint> getPoints() {
        return _points;
    }


    public PointF getPoint() {
        return _points.get(0)._point;
    }


    public long getTime() {
        return _points.get(0)._time;
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


    // Adds the given point to this stroke
    public void addPoint(PointF point, long time) {

        // Add Root node
        //
        if(_points.isEmpty()) {

            _truePath.moveTo(point.x, point.y);
        }
        else {
            _truePath.quadTo(_X, _Y, (point.x + _X) / 2, (point.y + _Y) / 2);
        }
        _X = point.x;
        _Y = point.y;

        _points.add(new StrokePoint(point, time));

        addPointToBoundingBox(point);
    }


    public Path getPath() {
        return _truePath;
    }


    public boolean isPoint(Rect parentBounds) {

        boolean point = false;

        if((_boundingBox.width()  < parentBounds.width()  / ToleranceFactor) &&
           (_boundingBox.height() < parentBounds.height() / ToleranceFactor)) {

            point = true;
        }
        return point;
    }


    public void reBuildPath() {

        boolean init = false;

        _truePath = new Path();

        for(StrokePoint strokepoint : _points) {

            PointF point = strokepoint.getPoint();

            // Add Root node
            //
            if(!init) {
                _truePath.moveTo(point.x, point.y);
                init = true;

            } else {
                _truePath.quadTo(_X, _Y, (point.x + _X) / 2, (point.y + _Y) / 2);
            }
            _X = point.x;
            _Y = point.y;
        }
    }


    //************************************************************************
    //** JNI access methods
    // LTK uses these methods in JNI code to access the contents of a stroke
    // in the Native domain

    public int getNumberOfPoints() {
        return _points.size();
    }

    public PointF getPointAt(int index) {
        return _points.get(index).getPoint();
    }

    //** JNI access methods
    //************************************************************************


    public Path startReplaySegments(AffineXform xForm) {

        nPoint = new Point();
        lPoint = new Point(xForm.getOrigX(), xForm.getOrigY());
        strokePath = new Path();

        strokePath.moveTo(lPoint.x, lPoint.y);

        Log.i(TAG, "Next Point: " + 0 + " : " + xForm.getOrigX() + " : " + xForm.getOrigX());

        oldIndex = 1;

        return strokePath;
    }


    public Path addReplaySegments(AffineXform xForm, int index) {

        for(int i1 = oldIndex ; i1 <= index ; i1++)
        {
            nPoint.x = (int)((_points.get(i1).getX() * xForm.getScaleX()) + xForm.getOffsetX());
            nPoint.y = (int)((_points.get(i1).getY() * xForm.getScaleY()) + xForm.getOffsetY());

            Log.i(TAG, "Next Point: " + i1 + " : " + nPoint.x + " : " + nPoint.y );
            Log.i(TAG, "Last Point: " + i1 + " : " + lPoint.x + " : " + lPoint.y );

            strokePath.quadTo(lPoint.x, lPoint.y, (nPoint.x + lPoint.x) / 2, (nPoint.y + lPoint.y) / 2);

            lPoint = nPoint;
        }

        oldIndex = index + 1;

        return strokePath;
    }


    public PointF getReplayPoint(AffineXform xForm, int index) {
        PointF ipoint = new PointF();

        ipoint.x = (int)((_points.get(index).getX() * xForm.getScaleX()) + xForm.getOffsetX());
        ipoint.y = (int)((_points.get(index).getY() * xForm.getScaleY()) + xForm.getOffsetY());

        return ipoint;
    }



    public void normalizeStroke(float normalX, float normalY, long  cTime ) {

        ArrayList<StrokePoint> normalPoints = new ArrayList<StrokePoint>();
        cTime =  _points.get(0).getTime();

        for (int i1 = 0 ; i1 < _points.size() ; i1++)
        {

            Log.i(TAG, "Normal Point: " + (long)(_points.get(i1).getX() - normalX) + " : " + (long)(_points.get(i1).getY() - normalY) + " delta : " + (long)(_points.get(i1).getTime() - cTime));

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
