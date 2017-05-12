package cmu.xprize.asm_component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 *
 */
public class CAsm_Dot extends ImageView {

    private Context context;

    private String imageName;
    int row, col;

    private boolean isClickable = true;
    private boolean isClicked = false;
    private boolean isHollow = false;

    static final private String TAG ="Dot";


    public CAsm_Dot(Context context) {
        super(context);
        this.context = context;
    }

    public CAsm_Dot(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
    }

    public CAsm_Dot(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.context = context;
    }

    public void setImageName(String _imageName) {
        this.imageName = _imageName;
        createDrawable(this.imageName);
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
        if (action == MotionEvent.ACTION_DOWN) {
            if (isClickable) {
                isClicked = true;
                isClickable = false;
            }
        }
        return false;
    }

    protected void onDraw(Canvas canvas) {super.onDraw(canvas);}

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

    public int getRow() {return this.row;}
    public int getCol() {return this.col;}

    public boolean getIsHollow() {return this.isHollow;}

    public boolean getIsClicked(){
        if (isClicked) {
            isClicked = false;
            return true;
        }
        else {
            return false;
        }
    }

    public void setIsClickable(boolean _clickable) {

        if (_clickable) {
            isClicked = false; // reset if clickable true
        }

        this.isClickable = _clickable;

    }

    public void setCol(int _col) {
        this.col = _col;
    }

    public void setRow(int _row) {
        this.row = _row;
    }
}
