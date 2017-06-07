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

import cmu.xprize.sm_component.CSm_Component;
import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.IButtonController;

import static cmu.xprize.comp_debug.CD_CONST.SELECT_MATH;
import static cmu.xprize.comp_debug.CD_CONST.SELECT_SHAPES;
import static cmu.xprize.comp_debug.CD_CONST.SELECT_STORIES;
import static cmu.xprize.comp_debug.CD_CONST.SELECT_WRITING;
import static cmu.xprize.util.TCONST.QGRAPH_MSG;

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
    private Button   ScustomLaunch;
    private Button   SresetTutor;

    CAt_Data         currentTransition;
    private String   initialTutor;
    private String   currentTutor;
    private String   initialSkill;

    private HashMap<String, CAt_Data>  transitionMap;

    private GridView      gridView;
    private CSm_Component customView;
    private CDebugAdapter gridAdapter;

    private boolean       viewGrid = true;

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

        gridView   = (GridView) findViewById(R.id.SdebugGrid);
        customView = (CSm_Component) findViewById(R.id.SdbgComponent);

        mContainer = (ViewGroup) findViewById(R.id.SdebugContainer);

        SskillTitle       = (TextView) findViewById(R.id.SskillTitle);
        ScurrentTutorName = (TextView) findViewById(R.id.ScurrentTutorName);
        SnextTutorName    = (TextView) findViewById(R.id.SnextTutorName);
        SharderTutorName  = (TextView) findViewById(R.id.SharderTutorName);
        SeasierTutorName  = (TextView) findViewById(R.id.SeasierTutorName);

        SlaunchTutor  = (Button) findViewById(R.id.SlaunchTutor);
        ScustomLaunch = (Button) findViewById(R.id.ScustomButton);
        SresetTutor   = (Button) findViewById(R.id.SresetTutor);

        SlaunchTutor.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                Log.v(QGRAPH_MSG, "event.click: " + " CDebugComponent:Click on launch");

                // Support direct connection to button action manager
                //
                if(mButtonController != null) {
                    mButtonController.doDebugLaunchAction(currentTutor);
                }
            }
        });

        ScustomLaunch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                Log.v(QGRAPH_MSG, "event.click: " + " CDebugComponent:Click on custom");

                viewGrid = !viewGrid;

                gridView.setVisibility(viewGrid? VISIBLE:GONE);
                customView.setVisibility(viewGrid? GONE:VISIBLE);
            }
        });

        SresetTutor.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                Log.v(QGRAPH_MSG, "event.click: " + " CDebugComponent:Click on reset");

                String tutorName = "";

                tutorName = gridAdapter.updateCurrentTutorByIndex(transitionMap.get(initialTutor).gridIndex);

                if(!tutorName.equals("")) {
                    changeCurrentTutor(tutorName);
                }
            }
        });
    }


    public void setButtonController(IButtonController controller) {

        mButtonController = controller;
        customView.setButtonController(controller);
    }


    public String getLanguage() {
        return "implemented in T subclass";
    }

    public void initDisplay() {

        String skillType = "";
        String text      = "Skill: Unknown";
        int    color     = 0;

        // Init the skill pointers
        //
        switch (initialSkill) {

            case SELECT_WRITING:
                color = 0x00DD00;
                text  = "Skill: Reading & Writing";
                skillType = "reading_writing";
                break;

            case SELECT_STORIES:
                color = 0xFFB000;
                text  = "Skill: Stories";
                skillType = "stories" + "/" + getLanguage();
                break;

            case SELECT_MATH:
                color = 0xFF0000;
                text  = "Skill: Math";
                skillType = "math";
                break;

            case SELECT_SHAPES:
                color = 0x2DA4FC;
                text  = "Skill: Shapes";
                skillType = "shapes";
                break;

        }

        customView.setDataSource("[local_file]" + skillType);
        mContainer.setBackgroundColor(color);
        SskillTitle.setText(text);
    }


    public void initGrid(String _activeSkill, String _activeTutor, HashMap _transitionMap) {

        initialSkill  = _activeSkill;
        initialTutor  = _activeTutor;
        transitionMap = _transitionMap;

        initDisplay();

        changeCurrentTutor(_activeTutor);

        gridView    = (GridView) findViewById(R.id.SdebugGrid);
        gridAdapter = new CDebugAdapter(mContext, _activeTutor, _transitionMap, this);

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


    @Override
    public void invalidate() {
        super.invalidate();
    }


}
