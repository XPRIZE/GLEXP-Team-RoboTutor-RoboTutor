package cmu.xprize.comp_debug;

import android.content.Context;
import android.graphics.Color;
import android.support.percent.PercentRelativeLayout;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.CTutorData_Metadata;
import cmu.xprize.util.IButtonController;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TCONST.Thumb;

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

    private ImageView ScurrentTutorImage;
    private LinearLayout ScurrentTutorMetadata;


    private Button   SlaunchTutor;
    private Button   ScustomLaunch;
    private Button   SresetTutor;

    CAt_Data         currentTransition;
    private String   initialTutor;
    private String   currentTutor;
    private String   initialSkill;

    private HashMap<String, CAt_Data>  transitionMap;

    // BOJACK what is the role of gridView vs gridAdapter?
    private GridView      gridView;
    //private CSm_Component customView;
    private CDebugAdapter gridAdapter;


    // CUSTOM_MENU (0) stash changes, checkout new branch from dev, unstash and commit
    // CUSTOM_MENU (a2) merge num_compare
    // CUSTOM_MENU (a2) merge kevin_asm into development
    // CUSTOM_MENU (a3) add numcompare data sources
    // CUSTOM_MENU (a3) test numcompare data sources briefly
    // CUSTOM_MENU (a4) add numcompare menu (mimic 1)

    // CUSTOM_MENU (b2) merge sentence_writing
    // CUSTOM_MENU (b2) checkout amogh branch https://github.com/RoboTutorLLC/RoboTutor/pull/374
    // CUSTOM_MENU (b2) test amogh branch
    // CUSTOM_MENU (b2) merge amogh into development
    // CUSTOM_MENU (b3) get list of tutor_desc, tutor_id, tutor_data.
    // CUSTOM_MENU (b4) add sentence menu (mimic 1)

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

    // num_compare
    Button SNumCompareMenuButton;
    boolean viewNumCompareMenu = false;
    LinearLayout SNumCompareMenu;

    // story
    Button SComprehensionMenuButton;
    boolean viewComprehensionMenu = false;
    LinearLayout SComprehensionMenu;

    // writing
    Button SSentenceWritingMenuButton;
    boolean viewSentenceWritingMenu = false;
    LinearLayout SSentenceWritingMenu;

    // place value
    Button SPlaceValueMenuButton;
    boolean viewPlaceValueMenu = false;
    LinearLayout SPlaceValueMenu;

    // spelling
    Button SSpellingMenuButton;
    boolean viewSpellingMenu = false;
    LinearLayout SSpellingMenu;

    // picture match
    Button SPictureMatchMenuButton;
    boolean viewPictureMatchMenu = false;
    LinearLayout SPictureMatchMenu;

    // big math
    Button SBigMathMenuButton;
    boolean viewBigMathMenu = false;
    LinearLayout SBigMathMenu;

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
        // CUSTOM_MENU √√√ (mimic) make a similar menu for each new one
        SBpopMenuButton = (Button) findViewById(R.id.SBPopMenuButton);
        SBpopMenu = (LinearLayout) findViewById(R.id.SBPopMenu);
        SBPopLetters = (Button) findViewById(R.id.SBPopLetters);
        SBPopWords = (Button) findViewById(R.id.SBPopWords);
        SBPopPhonemes = (Button) findViewById(R.id.SBPopPhonemes);
        SBPopNumbers = (Button) findViewById(R.id.SBPopNumbers);
        SBPopShapes = (Button) findViewById(R.id.SBPopShapes);
        SBPopExpressions = (Button) findViewById(R.id.SBPopExpressions);

        /* num compare debug views */
        SNumCompareMenuButton = findViewById(R.id.SNumCompareMenuButton);
        SNumCompareMenu = findViewById(R.id.SNumCompareMenu);

        /* comprehension debug views */
        SComprehensionMenuButton = findViewById(R.id.SComprehensionMenuButton);
        SComprehensionMenu = findViewById(R.id.SComprehensionMenu);

        /* Sentence Writing views */
        SSentenceWritingMenuButton = findViewById(R.id.SSentenceWritingMenuButton);
        SSentenceWritingMenu = findViewById(R.id.SSentenceWritingMenu);

        /* Place Value */
        SPlaceValueMenuButton = findViewById(R.id.SPlaceValueMenuButton);
        SPlaceValueMenu = findViewById(R.id.SPlaceValueMenu);

        /* Spelling */
        SSpellingMenuButton = findViewById(R.id.SSpellingMenuButton);
        SSpellingMenu = findViewById(R.id.SSpellingMenu);

        /* Picture Match */
        SPictureMatchMenuButton = findViewById(R.id.SPictureMatchMenuButton);
        SPictureMatchMenu = findViewById(R.id.SPictureMatchMenu);

        /* Big Math */
        SBigMathMenuButton = findViewById(R.id.SBigMathMenuButton);
        SBigMathMenu = findViewById(R.id.SBigMathMenu);




        initializeQA_Code_Drop2_DebugTutors();

        mContainer = (ViewGroup) findViewById(R.id.SdebugContainer);

        SskillTitle       = (TextView) findViewById(R.id.SskillTitle);
        ScurrentTutorName = (TextView) findViewById(R.id.ScurrentTutorName);
        SnextTutorName    = (TextView) findViewById(R.id.SnextTutorName);
        SharderTutorName  = (TextView) findViewById(R.id.SharderTutorName);
        SeasierTutorName  = (TextView) findViewById(R.id.SeasierTutorName);

        // BOJACK new labels
        ScurrentTutorImage = (ImageView) findViewById(R.id.ScurrentTutorImage);
        ScurrentTutorMetadata = (LinearLayout) findViewById(R.id.ScurrentTutorMetadata);


        SlaunchTutor  = (Button) findViewById(R.id.SlaunchTutor);
        ScustomLaunch = (Button) findViewById(R.id.ScustomButton); // CUSTOM_MENU √√√
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
         * CUSTOM_MENU √√√ (keep)
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

                // CUSTOM_MENU √√√ (keep) this
                mButtonController.doDebugTagLaunchAction((String) view.getTag());
            }
        };

        STapCountCustom.setOnClickListener(roboDebuggerClickListener);

        SAkiraCustom.setOnClickListener(roboDebuggerClickListener);

        SAsmCustom.setOnClickListener(roboDebuggerClickListener);

        // CUSTOM_MENU √√√ (mimic) menu display for each new activity
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

        // CUSTOM_MENU √√√ new click listeners
        SNumCompareMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewNumCompareMenu = !viewNumCompareMenu;
                SNumCompareMenu.setVisibility(viewNumCompareMenu ? VISIBLE : GONE);
            }
        });

        SComprehensionMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewComprehensionMenu = !viewComprehensionMenu;
                SComprehensionMenu.setVisibility(viewComprehensionMenu ? VISIBLE : GONE);
            }
        });

        SSentenceWritingMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewSentenceWritingMenu= !viewSentenceWritingMenu;
                SSentenceWritingMenu.setVisibility(viewSentenceWritingMenu ? VISIBLE : GONE);
            }
        });

        // CUSTOM_MENU...
        SPlaceValueMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPlaceValueMenu= !viewPlaceValueMenu;
                SPlaceValueMenu.setVisibility(viewPlaceValueMenu ? VISIBLE : GONE);
            }
        });

        SSpellingMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewSpellingMenu= !viewSpellingMenu;
                SSpellingMenu.setVisibility(viewSpellingMenu ? VISIBLE : GONE);
            }
        });

        SPictureMatchMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPictureMatchMenu= !viewPictureMatchMenu;
                SPictureMatchMenu.setVisibility(viewPictureMatchMenu ? VISIBLE : GONE);
            }
        });

        SBigMathMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewBigMathMenu= !viewBigMathMenu;
                SBigMathMenu.setVisibility(viewBigMathMenu ? VISIBLE : GONE);
            }
        });



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


    private void initializeQA_Code_Drop2_DebugTutors() {
        ArrayList<CAt_Data> readingCompTutors = new ArrayList<>();

        // CUSTOM_MENU √√√ (1) initialize new CAt_Data for each one we want to create
        // CUSTOM_MENU √√√ (1) make an array of data sources, and make a custom button with a custom OnClickListener class that takes in args... for each class
        // CUSTOM_MENU √√√ (1) we need tutor_desc, tutor_data, and tutor_id

        String[] storyTypes = {"story.gen.hide",
        "story.clo.hear",
        "story.pic.hear",
        "story.gen.hear",
        "story.pic.hide"};

        // CUSTOM_MENU √√√ (1) try it with story data (see RoboTutor)
        final String tutor_desc = "story.pic.hear";
        final String tutor_id = "story.pic.hear::story_30";
        final String tutor_data = "[encfolder]story_30";

        String[][] storyTutors = {
                {"story.clo.hear", "story.clo.hear::story_30", "[encfolder]story_30"},
                {"story.pic.hear", "story.pic.hear::story_30", "[encfolder]story_30"},
                {"story.gen.hear", "story.gen.hear::story_30", "[encfolder]story_30"}
        };


        // CUSTOM_MENU (4) mimic this for each of the layouts
        createCustomMenu(SComprehensionMenu, storyTutors);


        String[][] numCompareTutors = {
                {"numdiscr", "numcompare::1d", "[file]numcompare.1d.json"},
                {"numdiscr", "numcompare::2d.compare.mix", "[file]numcompare.2d.compare.mix.json"},
                {"numdiscr", "numcompare::3d.compare.mix", "[file]numcompare.3d.compare.mix.json"}
        };
        // CUSTOM_MENU (4)
        createCustomMenu(SNumCompareMenu, numCompareTutors);


        // AMOGH add data sources
        String[][] sentenceWriteTutors = {
                {"write.sen.copy.wrd", "write.sen.copy.wrd::1", "[file]write.sen.copy.wrd.1.json"},
                {"write.sen.copy.wrd", "write.sen.copy.wrd::2", "[file]write.sen.copy.wrd.2.json"},
                {"write.sen.copy.wrd", "write.sen.copy.wrd::3", "[file]write.sen.copy.wrd.3.json"},
                {"write.sen.copy.wrd", "write.sen.copy.wrd::4", "[file]write.sen.copy.wrd.4.json"}
        };
        createCustomMenu(SSentenceWritingMenu, sentenceWriteTutors);

        String[][] placeValueTutors = {
                {"countingx", "place.value::pv-11..99.2D.diff0.1", "[file]place.value__pv-11..99.2D.diff0.1.json"},
                {"countingx", "place.value::pv-11..99.2D.diff0.2", "[file]place.value__pv-11..99.2D.diff0.2.json"}
        };
        createCustomMenu(SPlaceValueMenu, placeValueTutors);

        String[][] spellingTutors = {};
        createCustomMenu(SSpellingMenu, spellingTutors);

        String[][] picMatchTutors = {
                {"picmatch", "picmatch:animals_1", "[file]picmatch_animals_1.json"},
                {"picmatch", "picmatch:animals_2", "[file]picmatch_animals_2.json"},
                {"picmatch", "picmatch:food_1", "[file]picmatch_food_1.json"},
                {"picmatch", "picmatch:food_2", "[file]picmatch_food_2.json"},
                {"picmatch", "picmatch:objects_1", "[file]picmatch_objects_1.json"},
                {"picmatch", "picmatch:objects_2", "[file]picmatch_objects_2.json"},
                {"picmatch", "picmatch:objects_3", "[file]picmatch_objects_3.json"}
        };
        createCustomMenu(SPictureMatchMenu, picMatchTutors);

        String[][] bigMathTutors = {};
        createCustomMenu(SBigMathMenu, bigMathTutors);

    }

    private void createCustomMenu(LinearLayout menu, String[][] numCompareTutors) {
        for (String[] numCompareTutor : numCompareTutors) {
            CAt_Data thisTutor = new CAt_Data();
            thisTutor.tutor_desc = numCompareTutor[0];
            thisTutor.tutor_id = numCompareTutor[1];
            thisTutor.tutor_data = numCompareTutor[2];

            Button button = makeCustomButton(thisTutor.tutor_id);
            button.setOnClickListener(new CustomDebugClickListener(thisTutor));
            menu.addView(button);
        }
    }

    /**
     * generate a custom button to add to custom debug launch menu
     * @param text
     * @return
     */
    private Button makeCustomButton(String text) {
        Button button = new Button(mContext);
        button.setText(text);
        button.setBackgroundColor(mContext.getResources().getColor(R.color.newCustomButton));
        button.setTextColor(Color.WHITE);
        button.setBackground(mContext.getDrawable(R.drawable.launchnormal));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 50, 15);
        button.setLayoutParams(layoutParams);

        return button;
    }

    class CustomDebugClickListener implements OnClickListener {

        private CAt_Data tutor;

        CustomDebugClickListener(CAt_Data tutor) {
            this.tutor = tutor;
        }

        @Override
        public void onClick(View view) {
            mButtonController.doLaunch(tutor.tutor_desc, TCONST.TUTOR_NATIVE, tutor.tutor_data, tutor.tutor_id);
        }
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

        // BOJACK why does GridView need an adapter?
        gridView.setAdapter(gridAdapter);
    }


    /**
     * CHUNT this is called when a new tutor is selected
     * CHUNT it updates the display names and the display image
     */
    private void udpateViewVectorNames() {

        ScurrentTutorName.setText("Current Tutor:   " + currentTransition.tutor_id);
        SnextTutorName.setText("Next Tutor:   " + currentTransition.next);
        SharderTutorName.setText("Harder Tutor:   " + currentTransition.harder);
        SeasierTutorName.setText("Easier Tutor:   " + currentTransition.easier);

        // CHUNT here's where you set the names


        ArrayList<String> metadata;
        metadata = CTutorData_Metadata.parseNameIntoLabels(currentTransition);

        //metadata.add(currentTransition.tutor_id);
        //metadata.add("Tutor Type: " + currentTransition.tutor_desc);
        //metadata.add("Level: " + currentTransition.row);
        //metadata.add("Task: " + currentTransition.col);

        ScurrentTutorMetadata.removeAllViews();

        for (int i=0; i<metadata.size(); i++) {
            String m = metadata.get(i);

            TextView textView = new TextView(mContext);
            textView.setText(Html.fromHtml(m));
            textView.setTextSize(30f);
            if(i == 0) {
                //textView.setTypeface(null, Typeface.BOLD); // make first one bold
            }

            ScurrentTutorMetadata.addView(textView);
        }

        // CHUNT change tutor image
        Thumb tutorThumb = CTutorData_Metadata.getThumbImage(currentTransition);

        int thumbId;

        switch(tutorThumb) {
            case AKIRA:
                thumbId = R.drawable.thumb_akira;
                break;

            case BPOP_NUM:
                thumbId = R.drawable.thumb_bpop_num;
                break;

            case BPOP_LTR:
                thumbId = R.drawable.thumb_bpop_ltr_lc;
                break;

            case MN:
                thumbId = R.drawable.thumb_missingno;
                break;

            case GL:
                thumbId = R.drawable.thumb_compare;
                break;

            case MATH:
                thumbId = R.drawable.thumb_math;
                break;

            // begin counting x
            case CX_1:
                thumbId = R.drawable.thumb_countingx_1;
                break;

            case CX_10:
                thumbId = R.drawable.thumb_countingx_10;
                break;

            case CX_100:
                thumbId = R.drawable.thumb_countingx_100;
                break;

            case NUMSCALE:
                thumbId = R.drawable.thumb_numscale;
                break;

            case STORY_1:
                thumbId = R.drawable.thumb_story_blue;
                break;

            case STORY_2:
                thumbId = R.drawable.thumb_story_pink;
                break;

            case STORY_3:
                thumbId = R.drawable.thumb_story_green;
                break;

            case STORY_4:
                thumbId = R.drawable.thumb_story_violet;
                break;

            case STORY_5:
                thumbId = R.drawable.thumb_story_red;
                break;

            case STORY_NONSTORY:
                thumbId = R.drawable.thumb_story_gray;
                break;

            case SONG:
                thumbId = R.drawable.thumb_song;
                break;

            case WRITE:
                thumbId = R.drawable.thumb_write;
                break;

            default:
                thumbId = R.drawable.debugnull;
                break;

        }
        ScurrentTutorImage.setImageResource(thumbId);
        ScurrentTutorImage.setPadding(36, 36, 36, 36);
        ScurrentTutorImage.setBackground(getResources().getDrawable(R.drawable.outline_current_large, null));

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
