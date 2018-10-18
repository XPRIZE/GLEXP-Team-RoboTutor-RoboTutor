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

package cmu.xprize.comp_writing;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.ltkplus.CGlyph;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.CLinkedScrollView;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;


/**
 */
@RemoteViews.RemoteView
public class CGlyphController extends PercentRelativeLayout implements View.OnTouchListener, IGlyphController {

    private Context                 mContext;

    private IWritingComponent       mWritingComponent;
    private CGlyphController        mThis;
    private CLinkedScrollView       mScrollView;

    private FrameLayout             mDrawnContainer;
    private CGlyphInputContainer    mGlyphInput;
    private CGlyphReplayContainer   mGlyphReplay;

    private ImageButton             mInsLftSpaceBut;
    private ImageButton             mInsRgtSpaceBut;
    private ImageButton             mDeleteSpaceBut;
    private ImageButton             mEraseGlyphBut;

    private ImageButton             mGlyphReplayBut;
    private ImageButton             mGlyphMorphBut;
    private ImageButton             mGlyphFlashBut;
    private Button                  mGlyphSaveBut;

    private boolean                 _allowEraseCorrect = false;
    private int[]                   _screenCoord       = new int[2];
    private boolean                 _isLast;
    private int                     _attempt           = 0;

    protected final Handler         mainHandler = new Handler(Looper.getMainLooper());
    protected HashMap               queueMap    = new HashMap();
    protected boolean               _qDisabled  = false;

    private int wordIndex = 0;

    private LocalBroadcastManager   bManager;

    PercentLayoutHelper.PercentLayoutInfo info;

    final private String TAG = "DrawnInputComp";


    public CGlyphController(Context context) {
        super(context);
        init(context);
    }

    public CGlyphController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public CGlyphController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    protected void init(Context context) {

       mThis    = this;
       mContext = context;

       setClipChildren(false);

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(getContext());
    }

    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();

        mDrawnContainer  = (FrameLayout)findViewById(R.id.drawn_container);
        mGlyphInput      = (CGlyphInputContainer)findViewById(R.id.drawn_box);
        mGlyphReplay     = (CGlyphReplayContainer)findViewById(R.id.replay_box);
        mInsLftSpaceBut  = (ImageButton)findViewById(R.id.insert_space_left);
        mInsRgtSpaceBut  = (ImageButton)findViewById(R.id.insert_space_right);
        mDeleteSpaceBut  = (ImageButton)findViewById(R.id.delete_space);
        mEraseGlyphBut   = (ImageButton)findViewById(R.id.delete_glyph);

        mInsLftSpaceBut.setVisibility(INVISIBLE);
        mInsRgtSpaceBut.setVisibility(INVISIBLE);
        mDeleteSpaceBut.setVisibility(INVISIBLE);
        mEraseGlyphBut.setVisibility(INVISIBLE);

        mGlyphReplayBut = (ImageButton)findViewById(R.id.glyph_playback);
        mGlyphMorphBut = (ImageButton)findViewById(R.id.glyph_align);
        mGlyphFlashBut  = (ImageButton)findViewById(R.id.glyph_flash);
        mGlyphSaveBut   = (Button) findViewById(R.id.Ssave);

        mGlyphReplayBut.setVisibility(INVISIBLE);
        mGlyphMorphBut.setVisibility(INVISIBLE);
        mGlyphFlashBut.setVisibility(INVISIBLE);
        mGlyphSaveBut.setVisibility(INVISIBLE);

        enableButtons(true);

        // Update the control aspect ratio based on the prototype font used in the drawn control
        //
        LayoutParams params = (LayoutParams) mDrawnContainer.getLayoutParams();

        info = ((PercentLayoutHelper.PercentLayoutParams) params).getPercentLayoutInfo();

        info.aspectRatio = mGlyphInput.getFontAspect();     // = 0.84f;

        mGlyphInput.setReplayComp(mGlyphReplay);
    }


    public void enableButtons(boolean listen) {

        if(listen) {

            mInsLftSpaceBut.setOnClickListener(new insLSpaceClickListener());
            mInsRgtSpaceBut.setOnClickListener(new insRSpaceClickListener());
            mDeleteSpaceBut.setOnClickListener(new delSpaceClickListener());
            mEraseGlyphBut.setOnClickListener(new eraseGlyphClickListener());

            mGlyphReplayBut.setOnClickListener(new glyphReplayListener());
            mGlyphMorphBut.setOnClickListener(new glyphMorphListener());
            mGlyphFlashBut.setOnClickListener(new glyphFlashListener());

            mGlyphSaveBut.setOnClickListener(new glyphSaveListener());
        }
        else {

            mInsLftSpaceBut.setOnClickListener(null);
            mInsRgtSpaceBut.setOnClickListener(null);
            mDeleteSpaceBut.setOnClickListener(null);
            mEraseGlyphBut.setOnClickListener(null);

            mGlyphReplayBut.setOnClickListener(null);
            mGlyphMorphBut.setOnClickListener(null);
            mGlyphFlashBut.setOnClickListener(null);

            mGlyphSaveBut.setOnClickListener(null);
        }
    }

    public void setIsLast(boolean isLast) {
        mGlyphInput.setIsLast(isLast);
    }


    public int getGlyphIndex(){
        int index = ((LinearLayout)this.getParent()).indexOfChild(this);
        return index;
    }

    public class insLSpaceClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.v(QGRAPH_MSG, "event.click: " + " CGlyphController:insLSpaceClickListener");

            mWritingComponent.addItemAt(mThis, 1);
        }
    }

    public class insRSpaceClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.v(QGRAPH_MSG, "event.click: " + " CGlyphController:insRSpaceClickListener");

            mWritingComponent.addItemAt(mThis, 0);
        }
    }


    public class delSpaceClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.v(QGRAPH_MSG, "event.click: " + " CGlyphController:deleteItem");
//            mDeleteSpaceBut.set
            mWritingComponent.deleteItem(mThis);
        }
    }


    public class eraseGlyphClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.v(QGRAPH_MSG, "event.click: " + " CGlyphController:eraseGlyph");

            eraseGlyph();
            resetState();

            mWritingComponent.applyBehavior(WR_CONST.ON_ERASE);
        }
    }

    public void setIsPlaying(boolean playing){
        mGlyphInput.setIsPlaying(playing);
    }

    /**
     * This erases the glyph drawn by the user
     */
    public void eraseGlyph() {

        mGlyphInput.erase();
    }

    public void eraseReplayGlyph(){
        mGlyphInput.clearReplay();
    }


    public void resetState() {

        // TODO: this is a test of one feedback modality - in Immediate feeback mode
        // TODO: Here, clearing the bad entry allows them complete access again.
        //
        mWritingComponent.inhibitInput(mThis, false);
        mWritingComponent.resetResponse(mThis);
    }


    public void showSampleChar(Boolean show) {
        mGlyphInput.showSampleChar(show);
    }

    public class glyphReplayListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.v(QGRAPH_MSG, "event.click: " + " CGlyphController:replayGlyph(WR_CONST.REPLAY_DEFAULT");

            mGlyphInput.replayGlyph(WR_CONST.REPLAY_DEFAULT);
        }
    }

    public class glyphMorphListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.v(QGRAPH_MSG, "event.click: " + " CGlyphController:animateOverlay");

            mGlyphInput.animateOverlay();
        }
    }

    public class glyphFlashListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.v(QGRAPH_MSG, "event.click: " + " CGlyphController:flashOverlay");

            mGlyphInput.flashOverlay();
        }
    }

    public void setExpectedChar(String protoChar) {
        mGlyphInput.setExpectedChar(protoChar);
    }

    public void setIsStimulus(String protoChar) {

        mGlyphInput.setExpectedChar(protoChar);
        mGlyphInput.setIsStimulus();

        // For write.missingLtr: To make Stimulus text narrower.
        info.aspectRatio = mGlyphInput.getFontAspect();
    }

    public boolean checkIsStimulus() {
        return mGlyphInput.checkIsStimulus();
    }

    public String getExpectedChar() {
        return mGlyphInput.getExpectedChar();
    }

    public boolean checkAnswer(String resp, boolean isAnswerCaseSensitive) {
        return mGlyphInput.checkAnswer(resp, isAnswerCaseSensitive);
    }

    public void hideUserGlyph() {
        mGlyphInput.hideUserGlyph();
    }


    public void setProtoGlyph(CGlyph protoGlyph) {
        mGlyphInput.setProtoGlyph(protoGlyph);
    }

    //amogh added
    public boolean setPreviousGlyph(){ return mGlyphInput.setPreviousGlyph();}

    public void setStimuliGlyph(CGlyph stimuliGlyph) {
        mGlyphInput.setStimuliGlyph(stimuliGlyph);
    }
    public void setWordIndex(int i){wordIndex = i;}
    public int getWordIndex(){return wordIndex;}
    //amogh added ends

    public int getAttempt() {
        return _attempt;
    }

    public int incAttempt() {
        _attempt++;
        return _attempt;
    }

    public void setRecognisedChar(String recChar){
        mGlyphInput.setRecognisedChar(recChar);
    }

    public String getRecognisedChar(){
        return mGlyphInput.getRecognisedChar();
    }

    public class glyphSaveListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.v(QGRAPH_MSG, "event.click: " + " CGlyphController:saveGlyphAsPrototype");

            mGlyphInput.saveGlyphAsPrototype();
        }
    }


    /**
     * @param drawBase
     */
    public void showBaseLine(boolean drawBase) {

        mGlyphInput.setDrawBaseline(drawBase);
    }


    public void showEraseButton(boolean show) {

        // Ensure it's off - then make a decision on visibility based on state
        //
        mEraseGlyphBut.setVisibility(show? VISIBLE:INVISIBLE);
    }

    public void showInsLftButton(boolean show){
        mInsLftSpaceBut.setVisibility(show? VISIBLE:INVISIBLE);
    }
    public void showInsRgtButton(boolean show){
        mInsRgtSpaceBut.setVisibility(show? VISIBLE:INVISIBLE);
    }
    public void showDeleteSpaceButton(boolean show){
        mDeleteSpaceBut.setVisibility(show? VISIBLE:INVISIBLE);
    }

    public Rect getBoxRect(){
        Rect drawnBox = mGlyphInput.getViewBnds();
        return drawnBox;
    }

    @Override
    public boolean hasError() {
        return mGlyphInput.hasGlyph() && !mGlyphInput.isCorrect();
    }

    @Override
    public boolean hasGlyph() {
        return mGlyphInput.hasGlyph();
    }

    @Override
    public CGlyph getGlyph() {
        return mGlyphInput.getGlyph();
    }

    @Override
    public void pointAtEraseButton() {

        broadcastLocation(TCONST.POINT_AND_TAP, mEraseGlyphBut);
    }

    public void pointAtGlyph() {

        broadcastLocation(TCONST.POINTAT, mGlyphInput);
    }

    public void setProtoTypeDirty(boolean isDirty) {
        mGlyphSaveBut.setVisibility(isDirty? VISIBLE:INVISIBLE);
    }


    public void setWritingController(IWritingComponent writingController) {

        mWritingComponent = writingController;

        mGlyphInput.setWritingController(writingController);
        mGlyphInput.setInputManager(this);

        mGlyphReplay.setWritingController(writingController);
    }

    public void setResponseView(LinearLayout responseView){
        mGlyphInput.setResponseView(responseView);
    }

    public boolean toggleSampleChar() {
        return mGlyphInput.toggleSampleChar();
    }
    public boolean toggleProtoGlyph() {
        return mGlyphInput.toggleProtoGlyph();
    }
    public boolean toggleDebugBounds() {
        return mGlyphInput.toggleDebugBounds();
    }
    public boolean toggleStimuliGlyph() {
        return mGlyphInput.toggleStimuliGlyph();
    }
    public void selectFont(String fontID) {
        mGlyphInput.selectFont(fontID);
    }


    public void setItemGlyph(int index, int glyph) {
    }

    public void setLinkedScroll(CLinkedScrollView linkedScroll) {

        mScrollView = linkedScroll;

        mGlyphInput.setLinkedScroll(mScrollView);
    }

    public ImageButton getInsertBut() {
        return mInsLftSpaceBut;
    }

    public void setupLayout(float offset) {
        mInsLftSpaceBut.setTranslationX(offset);
        mInsRgtSpaceBut.setTranslationX(-offset);
    }


    public boolean firePendingRecognition() {
        return mGlyphInput.firePendingRecognition();
    }

    public void inhibitInput(boolean inhibit) {
        mGlyphInput.inhibitInput(inhibit);
    }

    public void updateAndDisplayCorrectStatus(boolean correct) {
        mGlyphInput.updateAndDisplayCorrectStatus(correct);
    }

    public void updateCorrectStatus(boolean correct){
        mGlyphInput.updateCorrectStatus(correct);
    }

    public void displayCorrectStatus() {
        mGlyphInput.displayCorrectStatus();
    } //amogh added

    public boolean getGlyphStarted() {
        return mGlyphInput.getGlyphStarted();
    }

    public boolean isCorrect() { return mGlyphInput.isCorrect();  }


    private void broadcastLocation(String Action, View target) {

        target.getLocationOnScreen(_screenCoord);

        PointF centerPt = new PointF(_screenCoord[0] + (target.getWidth() / 2), _screenCoord[1] + (target.getHeight() / 2));
        Intent msg = new Intent(Action);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{centerPt.x, (float) centerPt.y});

        bManager.sendBroadcast(msg);
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



    //************************************************************************
    //************************************************************************
    // Component Message Queue  -- Start


    public class Queue implements Runnable {

        protected String _command    = "";
        protected String _target     = "";
        protected String _item       = "";

        public Queue(String command) {
            _command = command;
        }

        public Queue(String command, String target) {
            _command = command;
            _target  = target;
        }

        public Queue(String command, String target, String item) {
            _command = command;
            _target  = target;
            _item    = item;
        }

        @Override
        public void run() {

            try {
                queueMap.remove(this);

                switch(_command) {

                    case WR_CONST.SHOW_SAMPLE:

                        mGlyphInput.showSampleChar(true); // shows the traceable outline
                        break;

                    case WR_CONST.HIDE_SAMPLE:

                        mGlyphInput.showSampleChar(false); // hides the traceable outline
                        break;

                    case WR_CONST.ERASE_GLYPH:

                        eraseGlyph(); // erase the user-drawn glyph
                        break;

                    case TCONST.HIGHLIGHT:

                        mGlyphInput.setBoxColor(WR_CONST.HLCOLOR);
                        mGlyphInput.invalidate();

                        post(TCONST.SHOW_NORMAL, WR_CONST.HIGHLIGHT_TIME);
                        break;

                    case TCONST.SHOW_NORMAL:

                        mGlyphInput.setBoxColor(WR_CONST.BOX_COLOR);
                        mGlyphInput.invalidate();

//                        mWritingComponent.applyBehavior(WR_CONST.ACTION_COMPLETE);
                        break;

                    case WR_CONST.RIPPLE_DEMO:

                        mGlyphReplay.setPointAtStroke(true);
                        mGlyphInput.replayGlyph(WR_CONST.REPLAY_PROTOGLYPH);

                        break;

                    case WR_CONST.RIPPLE_REPLAY:

                        mGlyphReplay.setPointAtStroke(false);
                        mGlyphInput.replayGlyph(WR_CONST.REPLAY_USERGLYPH);
                        break;

                    case WR_CONST.RIPPLE_PROTO:

                        mGlyphReplay.setPointAtStroke(false);
                        mGlyphInput.replayGlyph(WR_CONST.REPLAY_PROTOGLYPH);
                        break;

                    case WR_CONST.ANIMATE_OVERLAY:

                        mGlyphInput.showSampleChar(true);
                        mGlyphInput.animateOverlay();
                        break;

                    case WR_CONST.ANIMATE_ALIGN:

                        mGlyphInput.showSampleChar(false);
                        mGlyphInput.animateOverlay();
                        break;

                    case WR_CONST.DEMO_PROTOGLYPH:

                        mGlyphReplay.setPointAtStroke(true);
                        mGlyphInput.replayGlyph(WR_CONST.REPLAY_PROTOGLYPH);
                        break;

                    case WR_CONST.ANIMATE_PROTOGLYPH:

                        mGlyphReplay.setPointAtStroke(false);
                        mGlyphInput.replayGlyph(WR_CONST.REPLAY_PROTOGLYPH);
                        break;
                }
            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "RUN Error:", e, false);
            }
        }
    }


    /**
     *  Disable the input queues permenantly in prep for destruction
     *  walks the queue chain to diaable scene queue
     *
     */
    private void terminateQueue() {

        // disable the input queue permenantly in prep for destruction
        //
        _qDisabled = true;
        flushQueue();
    }


    /**
     * Remove any pending scenegraph commands.
     *
     */
    private void flushQueue() {

        Iterator<?> tObjects = queueMap.entrySet().iterator();

        while(tObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            mainHandler.removeCallbacks((CWritingComponent.Queue)(entry.getValue()));
        }
    }


    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(Queue qCommand) {
        enQueue(qCommand, 0);
    }
    private void enQueue(Queue qCommand, long delay) {

        if(!_qDisabled) {
            queueMap.put(qCommand, qCommand);

            if(delay > 0) {
                mainHandler.postDelayed(qCommand, delay);
            }
            else {
                mainHandler.post(qCommand);
            }
        }
    }

    /**
     * Post a command to the queue
     *
     * @param command
     */
    public void post(String command) {
        post(command, 0);
    }
    public void post(String command, long delay) {

        enQueue(new CGlyphController.Queue(command), delay);
    }


    /**
     * Post a command and target to this queue
     *
     * @param command
     */
    public void post(String command, String target) {
        post(command, target, 0);
    }
    public void post(String command, String target, long delay) { enQueue(new CGlyphController.Queue(command, target), delay); }


    /**
     * Post a command , target and item to this queue
     *
     * @param command
     */
    public void post(String command, String target, String item) {
        post(command, target, item, 0);
    }
    public void post(String command, String target, String item, long delay) { enQueue(new CGlyphController.Queue(command, target, item), delay); }




    // Component Message Queue  -- End
    //************************************************************************
    //************************************************************************


}