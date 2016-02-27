package cmu.xprize.mn_component;

import android.content.Context;
import android.util.AttributeSet;

public class CMn_Alley extends android.support.percent.PercentRelativeLayout{

    public CMn_Alley(Context context) {
        super(context);
        init(context, null);
    }

    public CMn_Alley(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CMn_Alley(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        inflate(getContext(), R.layout.mn_comp_vert, this);


    }

}
