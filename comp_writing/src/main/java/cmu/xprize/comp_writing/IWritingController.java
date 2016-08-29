package cmu.xprize.comp_writing;

import android.content.Context;
import android.view.View;

import cmu.xprize.ltkplus.CGlyphMetrics;
import cmu.xprize.ltkplus.CRecResult;

public interface IWritingController {

    public void onCreate(Context context);

    public void deleteItem(View child);
    public void addItemAt(View child, int inc);

    public void updateGlyphStats(CRecResult[] ltkPlusResult, CRecResult[] ltkresult, CGlyphMetrics metricsA, CGlyphMetrics metricsB);
    public void updateGlyph(IDrawnInputController child, String glyph);
}
