package cmu.xprize.comp_counting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by kevindeland on 10/27/17.
 */

@SuppressLint("AppCompatCustomView")
public class CCount_Decrement extends ImageView {

    private Context mContext;

    private String imageName;
    private int units;

    private String TAG = "CCount_Decrement";


    /** All of these constructors are needed in order to use this as a View **/
    public CCount_Decrement(Context context) {
        super(context);
        init(context, null);
    }

    public CCount_Decrement(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CCount_Decrement(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        mContext = context;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    private void createDrawable() {

        String imgPath;

        if(units == 1) {
            imgPath = "dec_1";
        } else if (units == 10) {
            imgPath = "dec_10";
        } else if (units == 100) {
            imgPath = "dec_100";
        } else {
            Log.e(TAG, "units not found");
            imgPath = "decrement_one";
        }

        int imageResource = mContext.getResources().getIdentifier(imgPath, "drawable", mContext.getPackageName());
        Drawable image = ContextCompat.getDrawable(mContext, imageResource);
        setImageDrawable(image);
    }
}
