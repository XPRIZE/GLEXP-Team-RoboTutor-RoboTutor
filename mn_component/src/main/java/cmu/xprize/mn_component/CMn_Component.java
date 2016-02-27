package cmu.xprize.mn_component;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class CMn_Component extends android.support.percent.PercentRelativeLayout{

    private LinearLayout mAlleyContainer;

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
        inflate(context, R.layout.mn_layout, this);

        // Get the primary container for tutor scenes
        mAlleyContainer = (LinearLayout)findViewById(R.id.Salleys);

//        LayoutInflater inflater;
//        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        CMn_Alley alley = (CMn_Alley)inflater.inflate(R.layout.mn_comp_vert, null );

        mAlleyContainer.addView(new CMn_Alley(context));
        mAlleyContainer.addView(new CMn_Alley(context));
        mAlleyContainer.addView(new CMn_Alley(context));


    }



}
