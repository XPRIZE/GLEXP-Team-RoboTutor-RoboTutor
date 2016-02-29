package cmu.xprize.mn_component;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.json.JSONObject;

import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

public class CMn_Component extends LinearLayout{

    private Context      mContext;
    private LinearLayout mAlleyContainer;
    private int          mAlleyCount = 0;
    private float        mAlleyRadius;

    static final String TAG = "CMn_Component";

    public CMn_Component(Context context) {
        super(context);
        init(context, null);
    }

    public CMn_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CMn_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {

        mContext = context;

        if(attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CMn_Component,
                    0, 0);

            try {
                mAlleyCount  = a.getInteger(R.styleable.CMn_Component_numAlley, 2);
                mAlleyRadius = a.getFloat(R.styleable.CMn_Component_radAlley, 0.25f);
            } finally {
                a.recycle();
            }
        }

        for(int i1 = 0 ; i1 < mAlleyCount ; i1++ ) {
            addAlley(0);
        }
    }


    private void addAlley(int margin) {

        // Defining the layout parameters of the TextView
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        lp.weight     = 1;
        lp.leftMargin = margin;

        // Setting the parameters on the TextView
        CMn_Alley alley = new CMn_Alley(mContext);
        alley.setLayoutParams(lp);

        addView(alley);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
    }



    //**  Data Source Management


    /**
     *
     * @param dataSource
     */
    public void setDataSource(JSONObject dataSource) {

        try {
                String jsonData = JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + TCONST.TASSETS + "/" + dataSource);
        }
        catch (Exception e) {
            Log.e(TAG, "Invalid Data Source : ");
            System.exit(1);
        }

    }
}
