package cmu.xprize.comp_counting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import cmu.xprize.util.TCONST;

/**
 * Created by kevindeland on 10/23/17.
 */

@SuppressLint("AppCompatCustomView")
public class CCount_Dot extends ImageView {

    private Context context;
    String imageName;
    int row, col;

    private boolean isClickable = true;
    private boolean isClicked = false;
    private boolean isHollow = false;


    public CCount_Dot(Context context) {
        super(context);
        this.context = context;
    }

    public CCount_Dot(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CCount_Dot(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void setParams( boolean _isClickable, String _imageName, int _row, int _col) {

        this.isClickable = _isClickable;
        setImageName(_imageName);
        this.row = _row;
        this.col = _col;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);

        Log.d(TCONST.COUNTING_DEBUG_LOG, "Dot Tapped!!!");

        if (action == MotionEvent.ACTION_DOWN) {
            if(isClickable) {
                isClicked = true;
                isClickable = false;
            }
        }
        return false;
    }

    public void setImageName(String _imageName) {
        this.imageName = _imageName;
        createDrawable(this.imageName);
    }

    public void setHollow(boolean _isHollow) {
        isHollow = _isHollow;

        String imgPath = imageName;
        if (isHollow) {
            imgPath += "_hollow";
        }

        createDrawable(imgPath);
    }


    private void createDrawable(String imgPath) {

        int imageResource = context.getResources().getIdentifier(imgPath, "drawable", context.getPackageName());
        Drawable image = ContextCompat.getDrawable(context, imageResource);
        setImageDrawable(image);
    }

    public boolean getIsClickable() {
        return isClickable;
    }

    public void setIsClickable(boolean _isClickable) {
        isClickable = _isClickable;
    }

    public boolean getIsClicked() {
        return isClicked;
    }

    public void setIsClicked(boolean _isClicked) {
        isClicked = _isClicked;
    }


}
