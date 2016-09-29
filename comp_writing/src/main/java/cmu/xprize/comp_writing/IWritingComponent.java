package cmu.xprize.comp_writing;

import android.view.View;

import cmu.xprize.ltkplus.CGlyphMetrics;
import cmu.xprize.ltkplus.CRecResult;

public interface IWritingComponent {

    public void onCreate();

    public void deleteItem(View child);
    public void addItemAt(View child, int inc);
    public void autoScroll(IGlyphController glyph);

    public void stimulusClicked(CStimulusController controller);
    public boolean scanForPendingRecognition(IGlyphController source);
    public void inhibitInput(IGlyphController source, boolean inhibit);

    public boolean applyEvent(String event);

    public void updateGlyphStats(CRecResult[] ltkPlusResult, CRecResult[] ltkresult, CGlyphMetrics metricsA, CGlyphMetrics metricsB);
    public void updateResponse(IGlyphController child, String glyph);
}
