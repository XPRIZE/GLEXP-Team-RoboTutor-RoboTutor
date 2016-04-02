package cmu.xprize.robotutor.tutorengine;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;

import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.util.TCONST;

// This is just a convenience to simplify the syntax in type_action execution

public class CObjectDelegate implements ITutorObject, Button.OnClickListener {

    private View                mOwnerView;

    private String              mTutorId;
    private String              mInstanceId;
    private Context             mContext;

    protected ITutorSceneImpl   mParent;
    protected CTutor            mTutor;
    protected ITutorNavigator   mNavigator;
    protected ITutorLogManager  mLogManager;

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

        // Load attributes
        try {
            mTutorId = context.getResources().getResourceEntryName(mOwnerView.getId());
        }
        catch(Exception e) {
            Log.w(TAG, "Warning: Unnamed Delegate" + e);
        }

    }

    public CTutor tutor() {
        return mTutor;
    }


    // Tutor Object Methods
    @Override
    public void   setName(String name) { mTutorId = name; }
    @Override
    public String name() { return mTutorId; }

    @Override
    public void setParent(ITutorSceneImpl parent) { mParent = parent; }

    @Override
    public void setTutor(CTutor tutor) { mTutor = tutor; }

    @Override
    public void setNavigator(ITutorNavigator navigator) { mNavigator = navigator; }

    @Override
    public void setLogManager(ITutorLogManager logManager) { mLogManager = logManager; }


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


    private Animator createFloatAnimator(String prop, float endPt, long duration ,int repeat, int mode ) {

        ValueAnimator   vAnimator = null;

        vAnimator = ObjectAnimator.ofFloat(mOwnerView, prop, endPt).setDuration(duration);

        vAnimator.setInterpolator(new AccelerateInterpolator(2.0f));
        vAnimator.setRepeatCount(repeat);
        vAnimator.setRepeatMode(mode);

        vAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOwnerView.invalidate();
            }
        });

        return vAnimator;
    }


    public void zoomInOut(float scale, long duration ) {

        AnimatorSet animation = new AnimatorSet();

        animation.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationCancel(Animator arg0) {
                //Functionality here
            }

            @Override
            public void onAnimationStart(Animator arg0) {
                //Functionality here
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                //Functionality here
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                //Functionality here
            }
        });

        animation.play(createFloatAnimator("scaleX", scale, duration, 1, ValueAnimator.REVERSE)).with(createFloatAnimator("scaleY", scale, duration, 1, ValueAnimator.REVERSE));

        animation.start();
    }


    @Override
    public void onClick(View v) {
        IScriptable2 obj = null;

        switch(mClickBehavior) {
            case TCONST.GOTONEXTSCENE:
            case TCONST.GOTONEXT:
                mTutor.mTutorNavigator.onButtonNext();
                break;

            case TCONST.STOP:
                mTutor.mSceneAnimator.stop();
                break;

            default:
                try {
                    obj = mTutor.getScope().mapSymbol(mClickBehavior);
                    obj.applyNode();

                } catch (Exception e) {
                    // TODO: Manage invalid Button Behavior
                    e.printStackTrace();
                }
        }
    }

}
