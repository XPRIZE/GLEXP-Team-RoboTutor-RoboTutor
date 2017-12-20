package cmu.xprize.comp_counting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import cmu.xprize.util.TCONST;

/**
 * Created by kevindeland on 10/27/17.
 */

@SuppressLint("AppCompatCustomView")
public class CCount_Increment extends ImageView {

    private Context mContext;

    private String imageName;
    private int units;

    private String TAG = "CCount_Increment";


    /** All of these constructors are needed in order to use this as a View **/
    public CCount_Increment(Context context) {
        super(context);
        init(context, null);
    }

    public CCount_Increment(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CCount_Increment(Context context, AttributeSet attrs, int defStyleAttr) {
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
            imgPath = "inc_1";
        } else if (units == 10) {
            imgPath = "inc_10";
        } else if (units == 100) {
            imgPath = "inc_100";
        } else {
            Log.e(TAG, "units not found");
            imgPath = "increment_one";
        }

        int imageResource = mContext.getResources().getIdentifier(imgPath, "drawable", mContext.getPackageName());
        Drawable image = ContextCompat.getDrawable(mContext, imageResource);
        setImageDrawable(image);
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);

        Log.d(TCONST.COUNTING_DEBUG_LOG, "Increment Tapped!");

        if (action == MotionEvent.ACTION_DOWN) {

            // TODO how to use Mechanics to make it increment???
        }

        return false;
    }*/

}
