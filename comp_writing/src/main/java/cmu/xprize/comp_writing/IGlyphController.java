package cmu.xprize.comp_writing;


import cmu.xprize.util.CLinkedScrollView;

public interface IGlyphController {

    public void setProtoTypeDirty(boolean isDirty);

    public boolean firePendingRecognition();
    public void inhibitInput(boolean newState);

    public void setLinkedScroll(CLinkedScrollView linkedScroll);
    public void setItemGlyph(int index, int glyph);
    public void setGlyphStatus(boolean validGlyph, boolean hasGlyph);

    public boolean hasError();
    public boolean hasGlyph();

    public void pointAtEraseButton();
}
