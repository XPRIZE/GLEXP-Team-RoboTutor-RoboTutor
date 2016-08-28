package cmu.xprize.comp_writing;


import cmu.xprize.util.CLinkedScrollView;

public interface IDrawnInputController {

    public void setProtoTypeDirty(boolean isDirty);
    public void setLinkedScroll(CLinkedScrollView linkedScroll);
    public void setItemGlyph(int index, int glyph);
    public void setHasGlyph(boolean hasGlyph);

}
