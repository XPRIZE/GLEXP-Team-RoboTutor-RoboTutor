package cmu.xprize.mn_component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cmu.xprize.util.View_Helper;

public class CMn_Alley extends android.support.percent.PercentRelativeLayout{

    private Button      Splus;
    private Button      Sminus;
    private CMn_IconSet SiconSet;
    private CMn_Text    Snumber;


    static final String TAG = "CMn_Alley";


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

        Splus    = (Button) View_Helper.getViewById(R.id.Splus, (ViewGroup)getParent());
        Sminus   = (Button) View_Helper.getViewById(R.id.Sminus, (ViewGroup)getParent());
        SiconSet = (CMn_IconSet) View_Helper.getViewById(R.id.SiconSet, (ViewGroup)getParent());
        Snumber  = (CMn_Text) View_Helper.getViewById(R.id.Snumber, (ViewGroup)getParent());
    }


    public void setData(CMn_Data data, int index, int mnIndex) {

        int visible = (index == mnIndex)? View.VISIBLE: View.INVISIBLE;

        Splus.setVisibility(visible);
        Sminus.setVisibility(visible);

        if(visible == View.INVISIBLE) {
            Snumber.setText(String.valueOf(data.dataset[index]));
        }
        else {
            Snumber.setText("");
        }

        SiconSet.setMaxIcons(data.maxvalue);
    }
}
