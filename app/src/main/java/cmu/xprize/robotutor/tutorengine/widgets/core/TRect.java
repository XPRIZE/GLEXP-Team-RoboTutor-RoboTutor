package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.comp_logging.ILogManager;

/**
 * Created by Kevin on 2/13/2016.
 */
public class TRect extends View  implements ITutorObject {

    private CObjectDelegate mSceneObject;

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
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);

        // Create a paint object to deine the line parameters
        mPaint = new Paint();

        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(false);
    }


    @Override
    public void onDestroy() {
        mSceneObject.onDestroy();
    }


    public void setDataSource(String dataSource) {

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


    @Override
    public void setVisibility(String visible) {

        mSceneObject.setVisibility(visible);
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

