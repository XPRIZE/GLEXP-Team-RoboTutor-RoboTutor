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

package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.TCONST;


public class TTextView extends TextView implements ITutorObject {

    private   Context         mContext;
    protected CObjectDelegate mSceneObject;

    private   String          mtypeFace;
    private   float           mBorder_width;

    private   Paint           mPaint;
    private   Rect            mRegion = new Rect();

    private   int             _tracking = 0;


    final private String TAG = "TTutorView";


    public TTextView(Context context) {
        super(context);
        init(context, null);
    }

    public TTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);

        mContext = context;

        if(attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    cmu.xprize.mn_component.R.styleable.RoboTutor,
                    0, 0);

            try {
                mtypeFace     = a.getString(cmu.xprize.mn_component.R.styleable.RoboTutor_type_face );
                mBorder_width = a.getFloat(cmu.xprize.mn_component.R.styleable.RoboTutor_border_width, 0f );
            } finally {
                a.recycle();
            }
        }

        if(mtypeFace != null) {

            try {
                // Font path
                String fontPath = TCONST.fontMap.get(mtypeFace.toLowerCase());

                Typeface fontFace = Typeface.createFromAsset(mContext.getAssets(), fontPath);
                setTextSize(1,2);
                setTypeface(fontFace);
            }
            catch(Exception e) {
                // Ignore font not found
            }
        }

        // Create a paint object to deine the line parameters
        mPaint = new Paint();

        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
    }

    @Override
    public void onDestroy() {
        mSceneObject.onDestroy();
    }



    public void setDataSource(String dataSource) {

    }


    public void onDraw(Canvas canvas) {

        if(mBorder_width > 0) {

            getDrawingRect(mRegion);
            canvas.drawRect(mRegion, mPaint);
        }

        super.onDraw(canvas);
    }


    private void applyTracking(CharSequence text) {

        StringBuilder builder = new StringBuilder();

        for(int i1 = 0; i1 < text.length(); i1++) {

            builder.append(text.charAt(i1));

            if(i1+1 < text.length()) {
                builder.append("\u00A0");
            }
        }

        SpannableString trackedText = new SpannableString(builder.toString());

        if(builder.toString().length() > 1) {
            for(int i = 1; i < builder.toString().length(); i+=2) {
                trackedText.setSpan(new ScaleXSpan((_tracking)/10), i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        super.setText(trackedText, BufferType.SPANNABLE);
    }


    public void setText(CharSequence text, BufferType type) {

        if(_tracking != 0) {
            applyTracking(text);
        }
        else {
            super.setText(text, type);
        }
    }

    //************************************************************************
    //************************************************************************
    // Tutor methods  Start


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
    }


    //** Special Object methods for missing method parameter type combinations

    public void setText(String text) {

        if(_tracking != 0) {
            applyTracking(text);
        }
        else {
            super.setText(text);
        }
    }

    // Tutor methods  End
    //************************************************************************
    //************************************************************************




    @Override
    public void setName(String name) {
        mSceneObject.setName(name);
    }

    @Override
    public String name() {
        return mSceneObject.name();
    }

    @Override
    public void setParent(ITutorSceneImpl mParent) {
        mSceneObject.setParent(mParent);
    }

    @Override
    public void setTutor(CTutor tutor) {
        mSceneObject.setTutor(tutor);
    }

    @Override
    public void onCreate() {}

    @Override
    public void setNavigator(ITutorGraph navigator) {
        mSceneObject.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ILogManager logManager) {
        mSceneObject.setLogManager(logManager);
    }
}
