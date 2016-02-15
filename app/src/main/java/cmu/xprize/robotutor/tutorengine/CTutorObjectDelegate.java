package cmu.xprize.robotutor.tutorengine;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.IScriptable;

// This is just a convenience to simplify the syntax in type_action execution

public class CTutorObjectDelegate implements ITutorObject, Button.OnClickListener {

    private View                mOwnerView;

    private String              mTutorId;
    private String              mInstanceId;
    private Context             mContext;

    protected ITutorSceneImpl   mParent;
    protected CTutor            mTutor;
    protected ITutorNavigator   mNavigator;
    protected ITutorLogManager  mLogManager;

    private String              mClickBehavior;


    final private String TAG = "ITutorObject";


    // Attach this functionality to the View
    public CTutorObjectDelegate(View owner) {
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


    @Override
    public void onClick(View v) {
        IScriptable obj = null;

        switch(mClickBehavior) {
            case TCONST.GOTONEXTSCENE:
            case TCONST.GOTONEXT:
                CTutor.mTutorNavigator.onButtonNext();
                break;

            case TCONST.STOP:
                CTutor.mTutorAnimator.stop();
                break;

            default:
                try {
                    obj = CTutor.getScope().mapSymbol(mClickBehavior);
                    obj.applyNode();

                } catch (Exception e) {
                    // TODO: Manage invalid Button Behavior
                    e.printStackTrace();
                }
        }
    }

}
