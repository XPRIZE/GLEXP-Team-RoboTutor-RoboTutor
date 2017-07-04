package cmu.xprize.robotutor.tutorengine;

import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.IEventSource;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;

// This is just a convenience to simplify the syntax in type_action execution

public class CObjectDelegate implements ITutorObject, Button.OnClickListener, IEventSource {

    private View                mOwnerView;

    private String              mInstanceId = null;
    private Context             mContext;

    protected ITutorSceneImpl   mParent;
    protected CTutor            mTutor;
    protected ITutorGraph       mNavigator;
    protected ILogManager       mLogManager;

    private String              mClickBehavior;
    private AnimatorSet         _animatorSets;


    final private String TAG = "ITutorObject";


    // Attach this functionality to the View
    public CObjectDelegate(View owner) {
        mOwnerView = owner;
    }


    @Override
    public void init(Context context, AttributeSet attrs) {

        mContext = context;

//        System.out.println(context.getResources().getResourceEntryName(mOwnerView.getId()));
//        System.out.println(context.getResources().getResourceName(mOwnerView.getId()));
//        System.out.println(context.getResources().getResourcePackageName(mOwnerView.getId()));
//        System.out.println(context.getResources().getResourceTypeName(mOwnerView.getId()));

    }

    @Override
    public void setVisibility(String visible) {

        int visibleFlag;

        switch(visible) {

            case TCONST.VISIBLE:
                visibleFlag = View.VISIBLE;
                break;

            case TCONST.INVISIBLE:
                visibleFlag = View.INVISIBLE;
                break;

            default:
                visibleFlag = View.GONE;
                break;

        }

        mOwnerView.setVisibility(visibleFlag);
    }


    @Override
    public String getEventSourceName() {
        return mContext.getResources().getResourceName(mOwnerView.getId());
    }
    @Override
    public String getEventSourceType() {
        return mContext.getResources().getResourceTypeName(mOwnerView.getId());
    }



    /**
     *  Release listeners and resources
     *
     */
    public void onDestroy() {
        setButtonBehavior("null");
    }


    public void endTutor() {
        mTutor.post(TCONST.ENDTUTOR);
    }

    public CTutor tutor() {
        return mTutor;
    }


    // Tutor Object Methods
    @Override
    public void   setName(String name) { mInstanceId = name; }
    @Override
    public String name() {

        // Some dynamically created objects won't have a name on creation (they aren't loaded from XML)
        // so we get their names on first request.
        //
        try {
            if(mInstanceId == null) {
                mInstanceId = mContext.getResources().getResourceEntryName(mOwnerView.getId());
            }
        }
        catch(Exception e) {
            Log.w(TAG, "Warning: Unnamed Delegate" + e);
        }
        return mInstanceId;
    }

    @Override
    public void setParent(ITutorSceneImpl parent) { mParent = parent; }

    @Override
    public void setTutor(CTutor tutor) { mTutor = tutor; }

    @Override
    public void onCreate() {}

    @Override
    public void setNavigator(ITutorGraph navigator) { mNavigator = navigator; }

    @Override
    public void setLogManager(ILogManager logManager) { mLogManager = logManager; }


    public void setButtonBehavior(String command) {

        if(command.toLowerCase().equals("null")) {
            mClickBehavior = null;
            mOwnerView.setOnClickListener(null);

        }
        else {
            mClickBehavior = command;
            mOwnerView.setOnClickListener(this);
        }
    }


    public void zoomInOut(float scale, long duration ) {

        CAnimatorUtil.zoomInOut(mOwnerView, scale, duration);
    }


    public void wiggle(String direction, float magnitude, long duration, int repetition ) {

        CAnimatorUtil.wiggle(mOwnerView, direction, magnitude, duration, repetition);
    }


    public void setAlpha(Float alpha) {
        mOwnerView.setAlpha(alpha);
    }


    @Override
    public void onClick(View v) {
        IScriptable2 obj = null;

        // TODO: Ultimately we want to instantiate scope symbols for built-in behaviors like NODENEXT
        //
        if(mClickBehavior != null) {

            switch (mClickBehavior) {

                case TCONST.NEXTSCENE:
                    Log.v(QGRAPH_MSG, "event.click: " + " CObjectDelegate: TCONST.NEXTSCENE");

                    mTutor.mTutorGraph.post(this, TCONST.NEXTSCENE);
                    break;

                case TCONST.NEXT_NODE:
                    Log.v(QGRAPH_MSG, "event.click: " + " CObjectDelegate: TCONST.NEXT_NODE");

                    mTutor.mSceneGraph.post(this, TCONST.NEXT_NODE);
                    break;

                case TCONST.STOP:
                    Log.v(QGRAPH_MSG, "event.click: " + " CObjectDelegate: TCONST.STOP");

                    mTutor.mSceneGraph.post(this, TCONST.STOP);
                    break;

                default:
                    Log.v(QGRAPH_MSG, "event.click: " + " CObjectDelegate: applyNode");

                    try {
                        obj = mTutor.getScope().mapSymbol(mClickBehavior);

                        if(obj.testFeatures()) {
                            obj.applyNode();
                        }
                    } catch (Exception e) {
                        // TODO: Manage invalid Button Behavior
                        e.printStackTrace();
                    }
            }
        }
    }

}
