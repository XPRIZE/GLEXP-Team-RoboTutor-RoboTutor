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

package cmu.xprize.comp_counting2;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RemoteViews;

import java.util.HashMap;


/**
 * Code refactored by Kevin DeLand on 7/20/2018
 */
@RemoteViews.RemoteView
public class CGlyphController_Simple extends PercentRelativeLayout implements View.OnTouchListener, IGlyphController_Simple {


    private CGlyphInputContainer_Simple mGlyphInput;


    private int                     _attempt           = 0;

    protected final Handler mainHandler = new Handler(Looper.getMainLooper());
    protected HashMap queueMap    = new HashMap();
    protected boolean               _qDisabled  = false;




    final private String TAG = "DrawnInputComp";


    public CGlyphController_Simple(Context context) {
        super(context);
        init(context);
    }

    public CGlyphController_Simple(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public CGlyphController_Simple(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    protected void init(Context context) {


       setClipChildren(false);

    }

    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();
    }

    public void setIsLast(boolean isLast) {
        mGlyphInput.setIsLast(isLast);
    }



    /**
     * This erases the glyph drawn by the user
     */
    public void eraseGlyph() {

        mGlyphInput.erase();
    }

    public void setExpectedChar(String protoChar) {
        mGlyphInput.setExpectedChar(protoChar);
    }

    public int getAttempt() {
        return _attempt;
    }

    /**
     * @param drawBase
     */
    public void showBaseLine(boolean drawBase) {

        mGlyphInput.setDrawBaseline(drawBase);
    }


    public void setWritingController(IWritingComponent_Simple writingController) {

        mGlyphInput.setWritingController(writingController);
        mGlyphInput.setInputManager(this);
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

    public void setInputContainer(CGlyphInputContainer_Simple inputContainer) {
        this.mGlyphInput = inputContainer;
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

            }
            catch(Exception e) {
                Log.e(TAG, "RUN Error:" + e);
            }
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

        enQueue(new CGlyphController_Simple.Queue(command), delay);
    }


    /**
     * Post a command and target to this queue
     *
     * @param command
     */
    public void post(String command, String target) {
        post(command, target, 0);
    }
    public void post(String command, String target, long delay) { enQueue(new CGlyphController_Simple.Queue(command, target), delay); }


    /**
     * Post a command , target and item to this queue
     *
     * @param command
     */
    public void post(String command, String target, String item) {
        post(command, target, item, 0);
    }
    public void post(String command, String target, String item, long delay) { enQueue(new CGlyphController_Simple.Queue(command, target, item), delay); }




    // Component Message Queue  -- End
    //************************************************************************
    //************************************************************************


}