package cmu.xprize.mn_component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cmu.xprize.util.View_Helper;

public class CMn_Alley extends android.support.percent.PercentRelativeLayout {

    private IValueListener _owner;
    private boolean        _isMissingNumber;

    private Button      Splus;
    private Button      Sminus;
    private CMn_IconSet SiconSet;
    private CMn_Text    Snumber;

    private int         mMaxValue;
    private int         mCurValue;

    static final String TAG = "CMn_Alley";


    public CMn_Alley(Context context) {
        super(context);
        init(context, null);
    }

    public CMn_Alley(Context context, IValueListener owner) {
        super(context);
        _owner = owner;     // callback target for value updates
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

        Splus    = (Button) View_Helper.getViewById(R.id.Splus, this);
        Sminus   = (Button) View_Helper.getViewById(R.id.Sminus, this);
        SiconSet = (CMn_IconSet) View_Helper.getViewById(R.id.SiconSet, this);
        Snumber  = (CMn_Text) View_Helper.getViewById(R.id.Snumber, this);

        // When plus or minus are pressed we also allow the component to perform any top level
        // behaviors defined in scripts
        //
        Splus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mCurValue < mMaxValue) {
                    updateValue(mCurValue + 1, true);
                    _owner.performClick();
                }
            }
        });

        Sminus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mCurValue > 0) {
                    updateValue(mCurValue - 1, true);
                    _owner.performClick();
                }
            }
        });
    }

    private void updateValue(int newValue, boolean invalidate) {

        mCurValue = newValue;

        Snumber.setText(String.valueOf(newValue));
        SiconSet.setIconCount(newValue, invalidate);

        if(_isMissingNumber)
            _owner.UpdateValue(newValue);
    }


    public void setData(CMn_Data data, int index, int mnIndex) {

        _isMissingNumber = (index == mnIndex)? true:false;

        int visible = (_isMissingNumber)? View.VISIBLE: View.INVISIBLE;

        mMaxValue = data.maxvalue;

        Splus.setVisibility(visible);
        Sminus.setVisibility(visible);
        SiconSet.setMaxIcons(mMaxValue);

        if(visible == View.INVISIBLE) {
            updateValue(data.dataset[index], true);
        }
        else {
            updateValue(0, true);
        }

    }

    public boolean isCorrect(int corValue) {
        return mCurValue == corValue;
    }
}
