package cmu.xprize.asm_component;

import android.content.Context;
import android.support.percent.PercentLayoutHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import java.util.ArrayList;
import java.util.List;
import cmu.xprize.comp_writebox.CGlyphController_Simple;
import cmu.xprize.comp_writebox.IGlyphController_Simple;
import cmu.xprize.comp_writebox.IWritingComponent_Simple;
import cmu.xprize.comp_writebox.ICharRecListener_Simple;
import cmu.xprize.ltkplus.CRecResult;
import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.ltkplus.IGlyphSink;

/**
 *
 */

public class CAsm_Popup extends PopupWindow implements IWritingComponent_Simple {

    private Context          mContext;
    private CGlyphController_Simple fw;
    public boolean           isActive;
    private IGlyphSink       _recognizer;

    public List<ICharRecListener_Simple> mListeners = new ArrayList<ICharRecListener_Simple>();

    PercentLayoutHelper.PercentLayoutInfo info;

    public CAsm_Popup(Context context) {
        super(context);
        init(context, null);
    }

    public CAsm_Popup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAsm_Popup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        mContext    = context;
        _recognizer = CRecognizerPlus.getInstance();

        _recognizer.setClassBoost(GCONST.FORCE_DIGIT);

        LinearLayout layout = new LinearLayout(context);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        this.setContentView(layout);
        layout.setLayoutParams(lp);
        setBackgroundDrawable(null);

        // create a new view
        fw = (CGlyphController_Simple) LayoutInflater.from(context)
                .inflate(cmu.xprize.comp_writebox.R.layout.simple_input_comp, null, false); // MATHFIX_WRITE √√√ this should be R.layout.simple_input_comp

        // Update the last child flag
        //
        fw.setIsLast(true);
        fw.setWritingController(this);
        fw.showBaseLine(false);

        layout.addView(fw); // MATHFIX_WRITE MATHFIX_LAYOUT where FingerWriter is added to view
        layout.requestLayout();
    }


    public void setExpectedDigit(String expectedDigit) {

        fw.setExpectedChar(expectedDigit);
    }


    public void enable(boolean _enable,ArrayList<ICharRecListener_Simple> listeners) {


        if(_enable) {
            fw.eraseGlyph();

            for(ICharRecListener_Simple listener : listeners) {
                mListeners.add(listener);
            }
        }
        else {
            mListeners = new ArrayList<ICharRecListener_Simple>();
        }

    }

    public void reset() {
        isActive = false;
        enable(false, null);
        dismiss();
    }
    @Override
    public boolean updateStatus(IGlyphController_Simple child, CRecResult[] _ltkPlusCandidates) {

        CRecResult          candidate      = _ltkPlusCandidates[0];

//        publishValue(WR_CONST.CANDIDATE_VAR, candidate.getRecChar().toLowerCase());
//        publishValue(WR_CONST.EXPECTED_VAR, mActiveController.getExpectedChar().toLowerCase());

        // Let anyone interested know there is a new recognition set available
        // Do synchronous update
        //
        for (ICharRecListener_Simple listener : mListeners) {
            listener.charRecCallback(candidate.getRecChar());
        }

        return false;
    }

    @Override
    public boolean updateStatus(String _ltkPlusResult) {
        return false;
    }

}
