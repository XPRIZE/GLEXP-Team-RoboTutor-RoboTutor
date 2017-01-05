package cmu.xprize.ltkplus;

/**
 * This is a JNI accessible Class that receives the result of the LTK recognizer
 *
 * - see ...\ltk\src\main\jniLipiJNI.cpp for how they it is accessed
 */
public class CRecResult {

    // These are JNI variables - see ...\ltk\src\main\jniLipiJNI.cpp for how they are accessed
    //
    public int      Id;
    public float    Confidence;

    // These are non-JNI (i.e. not accessed from JNI)
    //
    private String   _recChar           = " ";
    private float    _visualMatch       = 0;
    private float    _visualError       = 0;
    private boolean  _requestVisual     = false;
    private float    _plusConfidence    = 1.0f;
    private CGlyph   _sampleGlyph       = null;

    private boolean  _isVirtual         = false;    // True if not added (created) by LTK but added externally
    private boolean  _isSample          = false;
    private boolean  _isLTKSample       = false;


    public CRecResult() {
        Id = -1;
        Confidence = 0;
    }

    public CRecResult(String recChar, float confidence, boolean isVirutal) {
        _recChar   = recChar;
        Confidence = confidence;
        _isVirtual = isVirutal;
    }

    public void updateANDConfidence(float PA_B, float PB) {
        _plusConfidence += PA_B * PB;
    }

    public void updateORConfidence(float PAUB) {
        _plusConfidence *= PAUB;
    }

    public void setPlusConfidence(float P) {
        _plusConfidence = P;
    }

    public float getPlusConfidence() {
        return _plusConfidence;
    }

    public void setRecChar(String recChar) {
        _recChar = recChar;
    }
    public String getRecChar() {
        return _recChar;
    }

    public void setVisualConfidence(CGlyphMetrics   metric) {
        _visualMatch = metric.getVisualMatch();
        _visualError = metric.getVisualError();
    }
    public float getVisualConfidence() {
        return _visualMatch;
    }
    public float getVisualErrorConfidence() {
        return _visualError;
    }

    public void setGlyph(CGlyph glyph) { _sampleGlyph = glyph; }
    public CGlyph getGlyph() { return _sampleGlyph; }

    public void setIsExpected(boolean state) {
        _isSample = state;
    }
    public boolean getIsExpected() {return _isSample; }

    public void setIsBestLTK(boolean state) {
        _isLTKSample = state;
    }
    public boolean getIsBestLTK() {return _isLTKSample; }


    public boolean isVirtual() {
        return _isVirtual;
    }

    public boolean getVisualRequest() {
        return _requestVisual;
    }

    public void setVisualRequest(boolean requestProcessing) {
        _requestVisual = requestProcessing;
    }
}
