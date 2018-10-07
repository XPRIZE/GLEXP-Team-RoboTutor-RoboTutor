package cmu.xprize.comp_writing;


import cmu.xprize.ltkplus.CGlyph;
import cmu.xprize.util.CLinkedScrollView;

public interface IGlyphController {

    public void setProtoTypeDirty(boolean isDirty);

    public void updateAndDisplayCorrectStatus(boolean correct);
    public void updateCorrectStatus(boolean correct); //amogh added
    public boolean firePendingRecognition();
    public void inhibitInput(boolean newState);

    public int incAttempt();

    public void setLinkedScroll(CLinkedScrollView linkedScroll);
    public void setItemGlyph(int index, int glyph);
    public void setWordIndex(int i); //amogh added
    public int getWordIndex(); //amogh added
    public int getGlyphIndex();

    public void setExpectedChar(String sample);
    public String getExpectedChar();
    public boolean isCorrect();

    public void showEraseButton(boolean show);

    public boolean hasError();
    public boolean hasGlyph();
    public CGlyph getGlyph();

    public void pointAtEraseButton();
    public void pointAtGlyph();

    public void post(String command);
    public void post(String command, long delay);
    public void post(String command, String target);
    public void post(String command, String target, long delay);
    public void post(String command, String target, String item);
    public void post(String command, String target, String item, long delay);

}
