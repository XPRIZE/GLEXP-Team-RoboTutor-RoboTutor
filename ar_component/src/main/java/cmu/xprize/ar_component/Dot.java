package cmu.xprize.ar_component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Diego on 6/23/2016.
 */
public class Dot extends ImageView {

    private Context context;

    private String imageName;
    int row, col;

    private boolean isClickable = true;
    private boolean isClicked = false;
    private boolean isHollow = false;

    static final private String TAG ="Dot";


    public Dot(Context context) {
        super(context);
        this.context = context;
    }

    public Dot(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
    }

    public Dot(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.context = context;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
        createDrawable(this.imageName);
    }

    public void setParams( boolean isClickable, String imageName, int row, int col) {

        this.isClickable = isClickable;
        setImageName(imageName);
        setOnClickListener(clickListener);
        this.row = row;
        this.col = col;
    }

    private OnClickListener clickListener = new OnClickListener(){
        public void onClick(View v) {

            if (isClickable) {
                isClicked = true;
                isClickable = false;
            }

            View parentDotBag = (View) v.getParent().getParent();
            parentDotBag.performClick();
        }
    };

    protected void onDraw(Canvas canvas) {super.onDraw(canvas);}

    private void createDrawable(String imgPath) {

        int imageResource = context.getResources().getIdentifier(imgPath, "drawable", context.getPackageName());
        Drawable image = ContextCompat.getDrawable(context, imageResource);
        setImageDrawable(image);

    }

    public void setHollow(boolean _isHollow) {

        isHollow = _isHollow;

        String imgPath = imageName;

        if (isHollow) {
            imgPath += "_hollow";
        }

        createDrawable(imgPath);
    }

    public int getRow() {return this.row;}
    public int getCol() {return this.col;}

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
}
