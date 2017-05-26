package cmu.xprize.asm_component;

import android.content.Context;
import android.graphics.Color;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

import cmu.xprize.comp_writing.CGlyphController;
import cmu.xprize.comp_writing.CStimulusController;
import cmu.xprize.comp_writing.IGlyphController;
import cmu.xprize.comp_writing.IWritingComponent;
import cmu.xprize.comp_writing.WR_CONST;
import cmu.xprize.ltkplus.CGlyphMetrics;
import cmu.xprize.ltkplus.CRecResult;
import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.ltkplus.IGlyphSink;
import cmu.xprize.util.CEvent;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventDispatcher;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.TCONST;

/**
 *
 */

public class CAsm_Popup extends PopupWindow implements IWritingComponent, IEventDispatcher {

    private Context          mContext;
    private CGlyphController fw;
    public boolean           isActive;
    private IGlyphSink       _recognizer;

    public List<IEventListener> mListeners = new ArrayList<IEventListener>();

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
        fw = (CGlyphController) LayoutInflater.from(context)
                .inflate(cmu.xprize.comp_writing.R.layout.full_input_comp, null, false);

        // Update the last child flag
        //
        fw.setIsLast(true);
        fw.setWritingController(this);
        fw.showBaseLine(false);

        layout.addView(fw);
        layout.requestLayout();
    }


    public void setExpectedDigit(String expectedDigit) {

        fw.setExpectedChar(expectedDigit);
    }


    public void enable(boolean _enable,ArrayList<IEventListener> listeners) {


        if(_enable) {
            fw.eraseGlyph();

            for(IEventListener listener : listeners) {
                mListeners.add(listener);
            }
        }
        else {
            mListeners = new ArrayList<IEventListener>();
        }

    }

    public void reset() {
        isActive = false;
        enable(false, null);
        dismiss();
    }




    //***********************************************************
    // Event Listener/Dispatcher - Start


    @Override
    public void addEventListener(String linkedView) {

    }

    @Override
    public void dispatchEvent(IEvent event) {

        for (IEventListener listener : mListeners) {
            listener.onEvent(event);
        }
    }

    // Event Listener/Dispatcher - End
    //***********************************************************



    //***********************************************************
    // IWritingComponent - Start

    @Override
    public void onCreate() {

    }

    @Override
    public void deleteItem(View child) {

    }

    @Override
    public void addItemAt(View child, int inc) {

    }

    @Override
    public void autoScroll(IGlyphController glyph) {

    }

    @Override
    public void stimulusClicked(CStimulusController controller) {

    }

    @Override
    public boolean scanForPendingRecognition(IGlyphController source) {
        return false;
    }

    @Override
    public void inhibitInput(IGlyphController source, boolean inhibit) {

    }

    @Override
    public boolean applyBehavior(String event) {
        return false;
    }

    @Override
    public void updateGlyphStats(CRecResult[] ltkPlusResult, CRecResult[] ltkresult, CGlyphMetrics metricsA, CGlyphMetrics metricsB) {

    }

    @Override
    public boolean updateStatus(IGlyphController child, CRecResult[] _ltkPlusCandidates) {

        CRecResult          candidate      = _ltkPlusCandidates[0];

//        publishValue(WR_CONST.CANDIDATE_VAR, candidate.getRecChar().toLowerCase());
//        publishValue(WR_CONST.EXPECTED_VAR, mActiveController.getExpectedChar().toLowerCase());

        // Let anyone interested know there is a new recognition set available
        // Do synchronous update
        //
        dispatchEvent( new CEvent(TCONST.FW_RESPONSE, TCONST.FW_VALUE, candidate.getRecChar()));

        return false;
    }

    @Override
    public void resetResponse(IGlyphController child) {

    }

    // IWritingComponent - End
    //***********************************************************

}
