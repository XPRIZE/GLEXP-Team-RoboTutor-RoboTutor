package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CTutorObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorLogManager;
import cmu.xprize.robotutor.tutorengine.ITutorNavigator;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;

/**
 * Created by Kevin on 2/13/2016.
 */
public class TRect extends View  implements ITutorObjectImpl {

    private CTutorObjectDelegate mSceneObject;

    private Paint mPaint;
    private Rect  mRegion = new Rect();
    
    public TRect(Context context) {
        super(context);
        init(context, null);
    }

    public TRect(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TRect(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        mSceneObject = new CTutorObjectDelegate(this);
        mSceneObject.init(context, attrs);

        // Create a paint object to deine the line parameters
        mPaint = new Paint();

        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(false);
    }


    /**
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthPixels = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightPixels = View.MeasureSpec.getSize(heightMeasureSpec);

        getDrawingRect(mRegion);

        setMeasuredDimension((int) widthPixels, (int) heightPixels);
    }


    /**
     * Update the gaze
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {

        Path clip = new Path();

        canvas.drawRect(mRegion, mPaint);
    }



    //************************************************************************
    //************************************************************************
    // Tutor methods  Start



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
    public void setNavigator(ITutorNavigator navigator) {
        mSceneObject.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ITutorLogManager logManager) {
        mSceneObject.setLogManager(logManager);
    }

    @Override
    public CTutorObjectDelegate getimpl() {
        return mSceneObject;
    }


}

