package cmu.xprize.comp_debug;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import cmu.xprize.sm_component.CSm_Component;
import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.IButtonController;
import cmu.xprize.util.TCONST;

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
    //private CSm_Component customView;

    private CDebugAdapter gridAdapter;

    private LinearLayout customView;
    //private Button SBpopCustom;
    Button SAsmCustom;
    Button SAkiraCustom;
    Button STapCountCustom;
    // bubble pop
    Button SBpopMenuButton;
    boolean viewBpopMenu = false;
    LinearLayout SBpopMenu;
    Button SBPopLetters;
    Button SBPopWords;
    Button SBPopPhonemes;
    Button SBPopNumbers;
    Button SBPopShapes;
    Button SBPopExpressions;
    //private Button SWrCustom;

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

        /* debug views */
        customView = (LinearLayout) findViewById(R.id.SdbgComponent);
        //SBpopCustom = (Button) findViewById(R.id.SBpopCustom);
        STapCountCustom = (Button) findViewById(R.id.STapCountCustom);
        SAkiraCustom = (Button) findViewById(R.id.SAkiraCustom);
        SAsmCustom = (Button) findViewById(R.id.SAsmCustom);
        /* bubble pop debug views */
        SBpopMenuButton = (Button) findViewById(R.id.SBPopMenuButton);
        SBpopMenu = (LinearLayout) findViewById(R.id.SBPopMenu);
        SBPopLetters = (Button) findViewById(R.id.SBPopLetters);
        SBPopWords = (Button) findViewById(R.id.SBPopWords);
        SBPopPhonemes = (Button) findViewById(R.id.SBPopPhonemes);
        SBPopNumbers = (Button) findViewById(R.id.SBPopNumbers);
        SBPopShapes = (Button) findViewById(R.id.SBPopShapes);
        SBPopExpressions = (Button) findViewById(R.id.SBPopExpressions);


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

        /*
         * Custom launch!
         */
        ScustomLaunch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                Log.v(QGRAPH_MSG, "event.click: " + " CDebugComponent:Click on custom");

                viewGrid = !viewGrid;

                if(mButtonController != null) {
                    gridView.setVisibility(viewGrid? VISIBLE:GONE);
                    customView.setVisibility(viewGrid? GONE:VISIBLE);

                    return;
                }

                gridView.setVisibility(viewGrid? VISIBLE:GONE);
                customView.setVisibility(viewGrid? GONE:VISIBLE);
            }
        });

        /*
         * A click listener that sends the android:tag to the ActivitySelector
         */
        View.OnClickListener roboDebuggerClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mButtonController.doDebugTagLaunchAction((String) view.getTag());
            }
        };

        STapCountCustom.setOnClickListener(roboDebuggerClickListener);

        SAkiraCustom.setOnClickListener(roboDebuggerClickListener);

        SAsmCustom.setOnClickListener(roboDebuggerClickListener);

        SBpopMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewBpopMenu = !viewBpopMenu;
                SBpopMenu.setVisibility(viewBpopMenu ? VISIBLE : GONE);
            }
        });

        // add listeners for BubblePop
        SBPopLetters.setOnClickListener(roboDebuggerClickListener);
        SBPopWords.setOnClickListener(roboDebuggerClickListener);
        SBPopPhonemes.setOnClickListener(roboDebuggerClickListener);
        SBPopNumbers.setOnClickListener(roboDebuggerClickListener);
        SBPopShapes.setOnClickListener(roboDebuggerClickListener);
        SBPopExpressions.setOnClickListener(roboDebuggerClickListener);

        /*
         * Reset Tutor to original
         */
        SresetTutor.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                Log.v(QGRAPH_MSG, "event.click: " + " CDebugComponent:Click on reset");

                String tutorName = "";

                tutorName = gridAdapter.updateCurrentTutorByIndex(transitionMap.get(initialTutor).gridIndex);

                if(!tutorName.equals("")) {
                    changeCurrentTutor(tutorName, null);
                }
            }
        });
    }


    public void setButtonController(IButtonController controller) {

        mButtonController = controller;
        //customView.setButtonController(controller);
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

        //customView.setDataSource("[local_file]" + skillType);
        mContainer.setBackgroundColor(color);
        SskillTitle.setText(text);
    }


    /**
     * Initialize grid for the active skill.
     * RootTutor is passed along just in case we've switched tables between updates.
     *
     * @param _activeSkill
     * @param _activeTutor
     * @param _transitionMap
     * @param rootTutor
     */
    public void initGrid(String _activeSkill, String _activeTutor, HashMap _transitionMap, String rootTutor) {

        initialSkill  = _activeSkill;
        initialTutor  = _activeTutor;
        transitionMap = _transitionMap;

        initDisplay();

        // BUG this could be a value not in current matrix
        currentTutor = _activeTutor;
        changeCurrentTutor(currentTutor, rootTutor);

        gridView    = (GridView) findViewById(R.id.SdebugGrid);
        gridAdapter = new CDebugAdapter(mContext, currentTutor, transitionMap, this);

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
    public void changeCurrentTutor(String transitionID, String rootTutor) {

        currentTutor = transitionID;

        currentTransition = transitionMap.get(currentTutor);
        // this happens when we're debugging and we suddenly switch to a new Transition Table and the previous currentTutor doesn't exist
        if(currentTransition == null) {
            currentTutor = rootTutor;
            currentTransition = transitionMap.get(currentTutor);
        }
        udpateViewVectorNames();
    }


    @Override
    public void invalidate() {
        super.invalidate();
    }


}
