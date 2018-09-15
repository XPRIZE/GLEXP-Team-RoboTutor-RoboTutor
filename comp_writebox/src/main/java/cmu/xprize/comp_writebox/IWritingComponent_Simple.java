package cmu.xprize.comp_writebox;

import cmu.xprize.ltkplus.CRecResult;

public interface IWritingComponent_Simple {

    // MATHFIX_WRITE can we simplify or combine with something else?
    boolean updateStatus(IGlyphController_Simple child, CRecResult[] _ltkPlusCandidates);

    // MATHFIX_WRITE this is the simplified version
    boolean updateStatus(String _ltkPlusResult);
}
