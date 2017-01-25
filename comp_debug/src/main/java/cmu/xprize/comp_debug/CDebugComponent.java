package cmu.xprize.comp_debug;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import java.util.HashMap;

import cmu.xprize.util.CAs_Data;
import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.IButtonController;

import static cmu.xprize.comp_debug.CD_CONST.SELECT_MATH;
import static cmu.xprize.comp_debug.CD_CONST.SELECT_SHAPES;
import static cmu.xprize.comp_debug.CD_CONST.SELECT_STORIES;
import static cmu.xprize.comp_debug.CD_CONST.SELECT_WRITING;

public class CDebugComponent extends PercentRelativeLayout implements IDebugLauncher {

    private Context             mContext;
    private ViewGroup           mContainer;
    protected IButtonController mButtonController;

    private TextView SskillTitle;
    private TextView ScurrentTutorName;
    private TextView SnextTutorName;
    private TextView SharderTutorName;
    private TextView SeasierTutorName;

    private Button   SlaunchTutor;
    private Button   SresetTutor;

    CAt_Data         currentTransition;
    private String   initialTutor;
    private String   currentTutor;
    private String   initialSkill;

    private HashMap<String, CAt_Data>  transitionMap;
    private HashMap<Integer, CAt_Data> indexTransitionMap;
    private HashMap<String, CAs_Data>  initiatorMap;

    private GridView      gridView;
    private CDebugAdapter gridAdapter;

    private static String TAG = "CDebugComponent";

    public CDebugComponent(Context context) {
        super(context);
        init(context,null);
    }

    public CDebugComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public CDebugComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    public void init(Context context, AttributeSet attrs) {


        inflate(getContext(), R.layout.debug_layout, this);

        mContext = context;

        gridView = (GridView) findViewById(R.id.SdebugGrid);

        mContainer = (ViewGroup) findViewById(R.id.SdebugContainer);

        SskillTitle       = (TextView) findViewById(R.id.SskillTitle);
        ScurrentTutorName = (TextView) findViewById(R.id.ScurrentTutorName);
        SnextTutorName    = (TextView) findViewById(R.id.SnextTutorName);
        SharderTutorName  = (TextView) findViewById(R.id.SharderTutorName);
        SeasierTutorName  = (TextView) findViewById(R.id.SeasierTutorName);

        SlaunchTutor = (Button) findViewById(R.id.SlaunchTutor);
        SresetTutor  = (Button) findViewById(R.id.SresetTutor);

        SlaunchTutor.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                Log.d(TAG, "Click on launch: ");

                // Support direct connection to button action manager
                //
                if(mButtonController != null) {
                    mButtonController.doButtonBehavior(currentTutor);
                }
            }
        });

        SresetTutor.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                String tutorName = "";

                tutorName = gridAdapter.updateCurrentTutorByIndex(transitionMap.get(initialTutor).gridIndex);

                if(!tutorName.equals("")) {
                    changeCurrentTutor(tutorName);
                }

                Log.d(TAG, "Click on reset: ");
            }
        });
    }


    public void setButtonController(IButtonController controller) {

        mButtonController = controller;
    }


    public void initDisplay() {

        String text  = "Skill: Unknown";
        int    color = 0;

        // Init the skill pointers
        //
        switch (initialSkill) {

            case SELECT_WRITING:
                color = 0x00DD00;
                text  = "Skill: Reading & Writing";
                break;

            case SELECT_STORIES:
                color = 0xFFB000;
                text  = "Skill: Stories";
                break;

            case SELECT_MATH:
                color = 0xFF0000;
                text  = "Skill: Math";
                break;

            case SELECT_SHAPES:
                color = 0x2DA4FC;
                text  = "Skill: Shapes";
                break;

        }

        mContainer.setBackgroundColor(color);
        SskillTitle.setText(text);
    }


    public void initGrid(String _activeSkill, String _activeTutor, HashMap _transitionMap, HashMap _initiatorMap) {

        initialSkill  = _activeSkill;
        initialTutor  = _activeTutor;
        transitionMap = _transitionMap;
        initiatorMap  = _initiatorMap;

        initDisplay();

        changeCurrentTutor(_activeTutor);

        gridView    = (GridView) findViewById(R.id.SdebugGrid);
        gridAdapter = new CDebugAdapter(mContext, _activeTutor, _transitionMap, _initiatorMap, this);

        gridView.setNumColumns(gridAdapter.getGridColumnCount());

        gridView.setAdapter(gridAdapter);
    }


    private void udpateViewVectorNames() {

        ScurrentTutorName.setText("Current Tutor:   " + currentTransition.tutor_id);
        SnextTutorName.setText("Next Tutor:   " + currentTransition.next);
        SharderTutorName.setText("Harder Tutor:   " + currentTransition.harder);
        SeasierTutorName.setText("Easier Tutor:   " + currentTransition.easier);
    }


    //******************************************************************************
    // IDebugLauncher Interface
    //
    @Override
    public void changeCurrentTutor(String transitionID) {

        currentTutor = transitionID;

        currentTransition = transitionMap.get(currentTutor);
        udpateViewVectorNames();
    }


}
