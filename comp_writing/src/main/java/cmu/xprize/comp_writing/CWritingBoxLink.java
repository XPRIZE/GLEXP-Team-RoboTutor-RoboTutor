package cmu.xprize.comp_writing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import cmu.xprize.util.TCONST;

public class CWritingBoxLink extends View {

    private Paint mLinkPaint;
    private LinearLayout mResponseList;
    private LinearLayout mGlyphList;

    private int screenWidth;
    private int glyphControllerWidth;
    private int glyphBoxOffset;
    private int responseHeight;
    private int responseLetterWidth;
    private int drawingBoxOffset;

    //temporary starts
    private int shapeWidth = 500;
    private int shapeHeight = 100;
    private int textXOffset = 0;
    private int textYOffset = 30;
    //temporary ends

    public CWritingBoxLink(Context context) {
        super(context);
        setScreenWidth(context);
    }

    public CWritingBoxLink(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupPaint();
        setScreenWidth(context);
    }

    public CWritingBoxLink(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupPaint();
        setScreenWidth(context);
    }

    public int setScreenWidth(Context context){
        int width = getContext().getResources().getDisplayMetrics().widthPixels;

        screenWidth = width;
        return width;
    }

    private void setupPaint(){
        mLinkPaint = new Paint();
        mLinkPaint.setStyle(Paint.Style.FILL);
        mLinkPaint.setColor(0x44A5C4D4);
        mLinkPaint.setStrokeWidth(5);
        mLinkPaint.setAntiAlias(true);
    }

    public void setResponse(LinearLayout ResponseList){
        mResponseList = ResponseList;
        responseHeight = mResponseList.getHeight();
        responseLetterWidth = mResponseList.getChildAt(0).getWidth();

    }

    public void setGlyphList(LinearLayout GlyphList){
        mGlyphList = GlyphList;
        CGlyphController firstController = (CGlyphController) mGlyphList.getChildAt(0);
        glyphControllerWidth = firstController.getWidth();
        int glyphControllerHeight = firstController.getHeight();
        drawingBoxOffset = (glyphControllerHeight - (firstController.getBoxRect().height()))/2;
//        invalidate();
    }

    public void setVisibility(String visible) {

        int visibleFlag;

        switch (visible) {

            case TCONST.VISIBLE:
                visibleFlag = View.VISIBLE;
                break;

            case TCONST.INVISIBLE:
                visibleFlag = View.INVISIBLE;
                break;

            default:
                visibleFlag = View.GONE;
                break;

        }

        this.setVisibility(visibleFlag);
    }



    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

//        try {
//            int pos[] = new int[2];
//            mGlyphList.getChildAt(2).getLocationInWindow(pos);
//            int endX = pos[0];
////            int endY = mGlyphList.getChildAt(2).getTop();
//            canvas.drawLine(0, 0, endX, 300, mLinkPaint);
//
//        }catch(Exception e){}
        //draw a line for each child in the response.
        try{
            int childCount = mResponseList.getChildCount();
            for (int i = 0; i < childCount; i++){

                int responseLetterCoordinates[] = new int[2];
                mResponseList.getChildAt(i).getLocationInWindow(responseLetterCoordinates);
                int glyphBoxCoordinates[] = new int[2];
                mGlyphList.getChildAt(i).getLocationInWindow(glyphBoxCoordinates);

                int startX = responseLetterCoordinates[0] + responseLetterWidth/2;
                int startY = responseLetterCoordinates[1] + responseHeight;

                int endX = glyphBoxCoordinates[0] + (glyphControllerWidth/2);
                int endY = glyphBoxCoordinates[1] + drawingBoxOffset*3/4;
                boolean hasGlyph = ((CGlyphController) mGlyphList.getChildAt(i)).hasGlyph();
                if(!hasGlyph && endX > 0 && endX < screenWidth) {
                    canvas.drawLine(startX, startY, endX, endY, mLinkPaint);
                    canvas.drawLine(responseLetterCoordinates[0], startY, responseLetterCoordinates[0] + responseLetterWidth*4/5, startY, mLinkPaint);
//                    canvas.drawLine(responseLetterCoordinates[0], startY, responseLetterCoordinates[0] + responseLetterWidth*4/5, startY, mLinkPaint);
                }
            }
        }
        catch(Exception e){}

    }
}
