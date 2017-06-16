//*********************************************************************************
//
//    Copyright(c) 2016-2017  Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

package cmu.xprize.fw_component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;

public class CStimResp extends LinearLayout implements View.OnClickListener, ITextSink {

    private Context       mContext;
    private StringBuilder mString;
    private String        mDisplayText;

    private boolean       mIsResponse;
    private String[]      mLexemes;
    private String[]      mLinkLex;
    private int[]         mLexEnds;             // records location of the end of lexemes in mDisplayText string
    private boolean       mEmpty = false;

    private String[]      _data;
    private int           _dataIndex = 0;

    private CStimResp     mLinkedView;
    private int           mLinkedViewID;

    private boolean       mComparing      = false;
    private int           mErrorIndex     = Integer.MAX_VALUE;
    private String        mErrorIndicator = TCONST_FOREGROUND;
    private int           mErrorHighlight;
    private int           mNormalColor    = 0xff000000;

    private CTextView     mFocus;
    private int           mFocusId = -1;

    private static final int    CONST_PRIMARYERROR   = 0xFFFF0000;
    private static final int    CONST_SECONDARYERROR = 0x20000000;
    private static final String TCONST_FOREGROUND    = "fgerrorhighlight";
    private static final String TCONST_BACKGROUND    = "bgerrorhighlight";

    public static final String TCONST_SPACE        = "    ";

    final private float aRatio = 1.5f;

    private static final String TAG = "CStimResp";





    public CStimResp(Context context) {
        super(context);
        init(context, null);
    }


    public CStimResp(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    public CStimResp(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        mContext = context;

        if(attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
                mIsResponse   = a.getBoolean(R.styleable.RoboTutor_isResponse, false);
                //mLinkedViewID = a.getResourceId(R.styleable.RoboTutor_linked_view, 0);
            } finally {
                a.recycle();
            }
        }
    }


    public void setText(String newText) {
        mString = new StringBuilder(newText);

        try {
            // Trim leading blanks - the leading blank is added internally as a virtual char
            while (mString.charAt(0) == ' ')
                mString.deleteCharAt(0);

            // Filter multiple spaces within string
            for(int i1 = 0; i1 < mString.length()-1 ; ) {
                if((mString.charAt(i1)  == ' ') &&
                        (mString.charAt(i1+1)== ' ')) {

                    mString.deleteCharAt(i1);
                }
                else i1++;
            }
        }
        catch(StringIndexOutOfBoundsException exception){
            // Ignore case where we delete the entire string
            // charAt(0) will throw
        }

        mDisplayText = mString.toString();
    }


    public void initText() {
        LayoutParams layoutParams;
        int                       height;

        layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        height       = 50; //getHeight();  // TODO: check this
        mErrorIndex  = Integer.MAX_VALUE;       // reset

        if(mDisplayText != null) {

            // We consider any space delimited string an editable item (lexeme - is a xprize project internal name)
            //
            mLexemes = mDisplayText.split(" ");
            mEmpty   = mDisplayText.length() == 0;
            mLexEnds = new int[mLexemes.length*2];

            removeAllViews();

            // We always put a space a the beginning to allow them to add a word if they want.
            //
            addText(-1, TCONST_SPACE, height, layoutParams);

            // We add all the mLexemes with a trailing space to allow them to add a word
            // to the end
            for (int i1 = 0, lexOff = 0 ,lNdx = 0; i1 < mLexemes.length; i1++) {

                // Add strings but ignore the empty "" string
                int index = i1 * 2;

                // TODO: manage - is it a missing word or a typo at mErrorIndex
                //
                if(!mEmpty) {
                    if(mComparing) {
                        // Find primary error
                        if(mErrorIndex == Integer.MAX_VALUE) {
                            if (!mLexemes[i1].equals(mLinkLex[i1])) {
                                mErrorIndex = i1;
                                mErrorHighlight = CONST_PRIMARYERROR;
                            }
                        }
                        // highlight subsequent errors
                        else if(mErrorIndex < Integer.MAX_VALUE)
                            mErrorHighlight = CONST_SECONDARYERROR;
                    }

                    addText(index, mLexemes[i1], height, layoutParams);
                    lexOff += mLexemes[i1].length();
                    mLexEnds[lNdx++] = lexOff++;

                    // TODO: add "    " TCONST
                    addText(index+1, TCONST_SPACE, height, layoutParams);
                    mLexEnds[lNdx++] = lexOff;
                }
            }
        }
    }


    public void addText(int index, String text, int height, LayoutParams layoutParams) {
        CTextView  textView;

        textView = new CTextView(mContext);
        textView.setId(index);
        textView.setText(text);
        textView.setAsSpace(text.equals(TCONST_SPACE));

        if(index >= mErrorIndex) {

            // Note that if the target is a space we have to hightlight the background
            if(mErrorIndicator.equals(TCONST_FOREGROUND) && !text.equals(TCONST_SPACE)) {
                textView.setTextColor(mErrorHighlight);
            }
            else textView.setHighlightColor(mErrorHighlight);
        }
        else
            textView.setTextColor(mNormalColor);

        textView.setTextSize(height);

        addView(textView, -1, layoutParams);

        if(mIsResponse) {
            // Update the focus View - optimization
            // Note that if we delete a word the focus slips to the next view.
            if (mFocusId == index) {
                mFocus = textView;
                mFocus.setFocus(true);
            }
            textView.setOnClickListener(this);
        }
    }

    @Override
    public void addChar(String newChar) {

        int       lexOff;
        int       focusInc = 0;

        if(mFocus != null) {

            // If we are focused on a space then we will be adding a new word
            // so insert a space and move the focus to the new word
            if(mFocus.isSpace()) {
                newChar += " ";
                focusInc   = 1;
            }

            if(mFocusId == -1) lexOff = 0;
                          else lexOff = mLexEnds[mFocusId];

            mFocusId += focusInc;

            mString.insert(lexOff, newChar);
            setText(mString.toString());
            initText();
        }
    }

    @Override
    protected void	onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


    @Override
    public void onClick(View v) {
        int       lexOff;
        int       newFocusId;
        CTextView newFocus;

        newFocus   = (CTextView)v;
        newFocusId = newFocus.getId();

        // Only response type views react to taps
        if(mIsResponse) {

            Log.v(QGRAPH_MSG, "event.click: " + " CStimResp:edit action");

            if(mFocus != newFocus) {
                if(mFocus != null)
                    mFocus.setFocus(false);

                mFocusId = newFocusId;
                mFocus   = newFocus;
                mFocus.setFocus(true);
            }

            // Otherwise this is a subsequent tap on the focus
            // So we process this as a backspace
            else {

                if(!mFocus.isSpace()) {
                    lexOff = mLexEnds[newFocusId];
                    mString.deleteCharAt(lexOff-1);
                    setText(mString.toString());
                    initText();
                }
                else {
                    // TODO: Are spaces immutable
                    // Do nothing on space double tap
                }
            }
        }
    }


    public String getString() {
        return mDisplayText;
    }


    // We work left to right matching string lexemes
    // If there is an error in a lexeme we hightlight the error.
    // The remainder of the string is dehighlighted - indicating an indeterminate state
    //
    public void compareLinked(boolean compare) {

        // This is only a valid call on response view types
        if(mIsResponse) {

            mComparing = compare;
            try {
                if (mComparing) {
                    PercentRelativeLayout parentview = (PercentRelativeLayout)getParent();

                   // mLinkedView = (CStimResp)parentview.findViewById(mLinkedViewID);
                    mLinkLex    = mLinkedView.getString().split(" ");
                }
            } catch (NullPointerException exp) {
            }
        }
    }


    public void setLinkedView(CStimResp respView) {
        mLinkedView = respView;
    }


    //**************** Data Source Management


    public void setDataSource(String[] dataSource) {

        _data      = dataSource;
        _dataIndex = 0;
    }


    public void next() {
        setText(_data[_dataIndex]);
        initText();
        _dataIndex++;


    }

}
