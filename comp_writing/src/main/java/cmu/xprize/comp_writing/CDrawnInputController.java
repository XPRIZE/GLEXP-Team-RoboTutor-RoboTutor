//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
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

package cmu.xprize.comp_writing;

import android.content.Context;
import android.graphics.PointF;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RemoteViews;

import cmu.xprize.ltkplus.CGlyph;
import cmu.xprize.ltkplus.IGlyphSink;
import cmu.xprize.util.CLinkedScrollView;


/**
 */
@RemoteViews.RemoteView
public class CDrawnInputController extends PercentRelativeLayout implements View.OnTouchListener, IDrawnInputController {

    private IWritingController      mWritingController;
    private CDrawnInputController   mThis;
    private CLinkedScrollView       mScrollView;

    private FrameLayout             mDrawnContainer;
    private CGlyphInputContainer    mDrawnInput;
    private CGlyphReplayContainer   mReplayComp;
    private ImageButton             mInsLftSpaceBut;
    private ImageButton             mInsRgtSpaceBut;
    private ImageButton             mDeleteSpaceBut;
    private ImageButton             mEraseGlyphBut;

    private ImageButton             mGlyphReplayBut;
    private ImageButton             mGlyphAlignBut;
    private ImageButton             mGlyphFlashBut;

    private Button mGlyphSaveBut;


    PercentLayoutHelper.PercentLayoutInfo info;

    final private String TAG = "DrawnInputComp";


    public CDrawnInputController(Context context) {
        super(context);
        init(context);
    }

    public CDrawnInputController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public CDrawnInputController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
       mThis = this;
       setOnTouchListener(this);
       setClipChildren(false);
    }

    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();

        mDrawnContainer   = (FrameLayout)findViewById(R.id.drawn_container);
        mDrawnInput       = (CGlyphInputContainer)findViewById(R.id.drawn_box);
        mReplayComp       = (CGlyphReplayContainer)findViewById(R.id.replay_box);
        mInsLftSpaceBut   = (ImageButton)findViewById(R.id.insert_space_left);
        mInsRgtSpaceBut   = (ImageButton)findViewById(R.id.insert_space_right);
        mDeleteSpaceBut   = (ImageButton)findViewById(R.id.delete_space);
        mEraseGlyphBut = (ImageButton)findViewById(R.id.delete_glyph);

        mGlyphReplayBut = (ImageButton)findViewById(R.id.glyph_playback);
        mGlyphAlignBut  = (ImageButton)findViewById(R.id.glyph_align);
        mGlyphFlashBut  = (ImageButton)findViewById(R.id.glyph_flash);
        mGlyphSaveBut   = (Button) findViewById(R.id.Ssave);

        mInsLftSpaceBut.setVisibility(INVISIBLE);
        mInsRgtSpaceBut.setVisibility(INVISIBLE);
        mDeleteSpaceBut.setVisibility(INVISIBLE);
        mEraseGlyphBut.setVisibility(INVISIBLE);
        mGlyphSaveBut.setVisibility(INVISIBLE);

        mInsLftSpaceBut.setOnClickListener(new insLSpaceClickListener());
        mInsRgtSpaceBut.setOnClickListener(new insRSpaceClickListener());
        mDeleteSpaceBut.setOnClickListener(new delSpaceClickListener());
        mEraseGlyphBut.setOnClickListener(new eraseGlyphClickListener());

        mGlyphReplayBut.setOnClickListener(new glyphPlaybackListener());
        mGlyphAlignBut.setOnClickListener(new glyphAlignListener());
        mGlyphFlashBut.setOnClickListener(new glyphFlashListener());

        mGlyphSaveBut.setOnClickListener(new glyphSaveListener());

        // Update the control aspect ratio based on the prototype font used in the drawn control
        //
        LayoutParams params = (LayoutParams) mDrawnContainer.getLayoutParams();

        info = ((PercentLayoutHelper.PercentLayoutParams) params).getPercentLayoutInfo();

        info.aspectRatio = mDrawnInput.getFontAspect();     // = 0.84f;

        mDrawnInput.setReplayComp(mReplayComp);
    }


    public class insLSpaceClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mWritingController.addItemAt(mThis, 1);
        }
    }

    public class insRSpaceClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mWritingController.addItemAt(mThis, 0);
        }
    }


    public class delSpaceClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mWritingController.deleteItem(mThis);
        }
    }


    public class eraseGlyphClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mDrawnInput.clear();

            mWritingController.updateGlyph(mThis, "");
        }
    }

    public class glyphPlaybackListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mDrawnInput.replayGlyph();
        }
    }

    public class glyphAlignListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mDrawnInput.animateOverlay();
        }
    }

    public class glyphFlashListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mDrawnInput.flashOverlay();
        }
    }

    public void setProtoGlyph(String protoChar, CGlyph protoGlyph) {
        mDrawnInput.setProtoGlyph(protoChar, protoGlyph);
    }

    public class glyphSaveListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mDrawnInput.saveGlyphAsPrototype();
        }
    }


    public void rebuildProtoType(String options) {
        //mDrawnInput.rebuildProtoType(options);
    }


    public void setHasGlyph(boolean hasGlyph) {
        mEraseGlyphBut.setVisibility(hasGlyph? VISIBLE:INVISIBLE);
    }


    public void setProtoTypeDirty(boolean isDirty) {
        mGlyphSaveBut.setVisibility(isDirty? VISIBLE:INVISIBLE);
    }


    public void setWritingController(IWritingController writingController) {

        mWritingController = writingController;

        mDrawnInput.setWritingController(writingController);
        mDrawnInput.setInputManager(this);
    }


    public boolean toggleSampleChar() {
        return mDrawnInput.toggleSampleChar();
    }
    public boolean toggleProtoGlyph() {
        return mDrawnInput.toggleProtoGlyph();
    }
    public boolean toggleDebugBounds() {
        return mDrawnInput.toggleDebugBounds();
    }
    public void selectFont(String fontID) {
        mDrawnInput.selectFont(fontID);
    }


    public void setItemGlyph(int index, int glyph) {

    }


    public void setRecognizer(IGlyphSink recognizer) {
        mDrawnInput.setRecognizer(recognizer);
    }


    public void setLinkedScroll(CLinkedScrollView linkedScroll) {

        mScrollView = linkedScroll;

        mDrawnInput.setLinkedScroll(mScrollView);
    }


    public ImageButton getInsertBut() {
        return mInsLftSpaceBut;
    }

    public void setupLayout(float offset) {
        mInsLftSpaceBut.setTranslationX(offset);
        mInsRgtSpaceBut.setTranslationX(-offset);
    }


    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        PointF touchPt;
        final int action = event.getAction();

        touchPt = new PointF(event.getX(), event.getY());

        //Log.i(TAG, "ActionID" + action);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "DIC _ ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, "DIC _ ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "DIC _ ACTION_UP");
                break;
        }
        return true;
    }

    private final PercentLayoutHelper mHelper = new PercentLayoutHelper(this);

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mHelper.adjustChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHelper.handleMeasuredStateTooSmall()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }


}