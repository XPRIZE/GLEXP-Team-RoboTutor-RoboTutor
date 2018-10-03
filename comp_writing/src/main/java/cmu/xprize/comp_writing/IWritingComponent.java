package cmu.xprize.comp_writing;

import android.view.View;

import cmu.xprize.ltkplus.CGlyphMetrics;
import cmu.xprize.ltkplus.CRecResult;

public interface IWritingComponent {

    public void onCreate();

    public void deleteItem(View child);
    public void addItemAt(View child, int inc);
    public void autoScroll(IGlyphController glyph);

    public void stimulusClicked(int touchIndex);
    public void onErase(int eraseIndex);
    public boolean scanForPendingRecognition(IGlyphController source);
    public void inhibitInput(IGlyphController source, boolean inhibit);

    public boolean applyBehavior(String event);

    public void updateGlyphStats(CRecResult[] ltkPlusResult, CRecResult[] ltkresult, CGlyphMetrics metricsA, CGlyphMetrics metricsB);
    public boolean updateStatus(IGlyphController child, CRecResult[] _ltkPlusCandidates );
    public void resetResponse(IGlyphController child );
}
