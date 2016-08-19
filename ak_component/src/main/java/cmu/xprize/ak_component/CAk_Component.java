package cmu.xprize.ak_component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cmu.xprize.sb_component.CSb_Scoreboard;
import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

/**
 * Created by jacky on 2016/7/6.
 */


/**
 *
 * Akira Game panel as component. This class implements ILoadableObject which make it data-driven
 *
 * TODO
 * 1. Decide what fields should be driven by JSON data.
 * 2. Convert all drawable image files into vector image.
 * 3. Integrate scoreboard
 * 4. Add speedometer
 *
 */
public class CAk_Component extends RelativeLayout implements ILoadableObject{
    static public Context mContext;

    protected String        mDataSource;
    private   int           _dataIndex = 0;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    static final String TAG = "CAk_Component";


    static final int WIDTH = 960, HEIGHT = 600;

    protected CAk_Data _currData;
    protected long startTime;
    protected CAkPlayer player;
    protected CAkTeachFinger teachFinger;
    protected CSb_Scoreboard scoreboard;
    protected Button[] speedometerButton;

    protected CAkQuestionBoard questionBoard;

    protected TextView score;
    protected long sidewalkRightTime;
    private long sidewalkLeftTime;
    private long questionTime;
    private ImageView cityBackground;
    protected View mask;

    protected boolean isRunning = true;

    private Random random;
    protected SoundPool soundPool;

    protected boolean lastCorrect = true;
    protected Boolean isFirstInstall;

    private PointF[] sidewalkLeftPoints;
    private PointF[] sidewalkRightPoints;

    protected int carscreechMedia, correctMedia, incorrectMedia, numberchangedMedia;
    protected boolean flag=true;

    protected boolean speedIsZero=false;
    protected int extraSpeed = 1;

    //json loadable
    public    int          gameSpeed          ;
    public    CAk_Data[]   dataSource         ;


    protected List<Animator> ongoingAnimator;
    protected Animator cityAnimator;
    protected CAkQuestionBoard stopQuestionBoard;
    public int errornum=0;



    public CAk_Component(Context context) {
        super(context);
        init(context, null);
    }

    public CAk_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAk_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final float width = right - left;
        final float height = bottom - top;

        sidewalkLeftPoints[0].x = width * 0.3f;
        sidewalkLeftPoints[0].y = height * 0.25f;
        sidewalkLeftPoints[1].x = - width * 0.1f;
        sidewalkLeftPoints[1].y = height;

        sidewalkRightPoints[0].x = width * 0.6f;
        sidewalkRightPoints[0].y = height * 0.25f;
        sidewalkRightPoints[1].x = width;
        sidewalkRightPoints[1].y = height;

    }

    /**
     *
     * Init method for game
     * Init all objects which will be allocated only once here,
     * like background, player and background city animation.
     *
     */

    public void init(Context context, AttributeSet attrs) {
        inflate(getContext(), R.layout.akira_layout, this);

        mContext = context;

        sidewalkLeftTime = sidewalkRightTime = questionTime = startTime = System.nanoTime();

        player = (CAkPlayer) findViewById(R.id.player);
        cityBackground = (ImageView) findViewById(R.id.city);
        scoreboard = (CSb_Scoreboard) findViewById(R.id.scoreboard);
        mask = findViewById(R.id.mask);

        speedometerButton = new Button[11];
        for(int i = 0; i < 11; i++) {
            int resID = getResources().getIdentifier("button" + i, "id",
                    "cmu.xprize.robotutor");
            speedometerButton[i] = (Button) findViewById(resID);
            final int speed = i;
            speedometerButton[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSpeedChange(speed);
                    extraSpeed = speed;
                    for(Button b : speedometerButton)
                        b.getBackground().clearColorFilter();
                    v.getBackground().setColorFilter(0xFFFFCC00,PorterDuff.Mode.SRC);
                    if(v == speedometerButton[0])
                        scoreboard.setVisibility(INVISIBLE);
                    else
                        scoreboard.setVisibility(VISIBLE);
                }
            });
        }

        speedometerButton[1].getBackground().setColorFilter(0xFFFFCC00,PorterDuff.Mode.SRC);

        teachFinger = (CAkTeachFinger) findViewById(R.id.finger);
        teachFinger.finishTeaching = true;

        cityAnimator = CAnimatorUtil.configTranslate(cityBackground,
                400000, 0, new PointF(0, -HEIGHT));


        sidewalkLeftPoints = new PointF[2];
        sidewalkRightPoints = new PointF[2];

        sidewalkLeftPoints[0] = new PointF();
        sidewalkLeftPoints[1] = new PointF();

        sidewalkRightPoints[0] = new PointF();
        sidewalkRightPoints[1] = new PointF();

        random = new Random();
        ongoingAnimator=new ArrayList<>();


        isFirstInstall=true;
        if(isFirstInstall==false)
            teachFinger.finishTeaching=true;

        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        carscreechMedia=soundPool.load(mContext, R.raw.carscreech, 1);
        correctMedia=soundPool.load(mContext, R.raw.correct, 1);
        incorrectMedia=soundPool.load(mContext, R.raw.incorrect, 1);
        numberchangedMedia=soundPool.load(mContext, R.raw.numberchanged, 1);

        mainHandler.post(gameRunnable);
        if(attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
                mDataSource  = a.getString(R.styleable.RoboTutor_dataSource);
            } finally {
                a.recycle();
            }
        }

        cityAnimator.start();

//         Allow onDraw to be called to start animations

        setWillNotDraw(false);



    }

    public void setmDataSource(CAk_Data[] _dataSource) {
        dataSource = _dataSource;
        _dataIndex = 0;
    }

    public void next() {
        try {
            if (dataSource != null) {
                updateDataSet(dataSource[_dataIndex]);
                _dataIndex++;
            } else {
                CErrorManager.logEvent(TAG,  "Error no DataSource : ", null, false);
            }
        }
        catch(Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }
    }

    public boolean dataExhausted() {
        return _dataIndex >= dataSource.length;
    }

    protected void updateDataSet(CAk_Data data) {
        questionBoard = new CAkQuestionBoard(mContext, data.answerLane, data.choices);
        player.setText(data.aboveString, data.belowString);
    }

    public void post(String command, Object target) {

    }

    public void UpdateValue(int value) {
    }

    protected void onSpeedChange(int speed) {
        int s = speed * 400;
        for(int i = 0; i < ongoingAnimator.size(); i++) {
            Animator animator = ongoingAnimator.get(i);
            if(animator.getClass() == AnimatorSet.class) {
                AnimatorSet set = (AnimatorSet)animator;
                set.pause();
                ArrayList<Animator> list = set.getChildAnimations();
                for(int j = 0; j < list.size(); j++) {
                    ObjectAnimator objectAnimator = (ObjectAnimator) list.get(j);
                    objectAnimator.setDuration(5000 - s);
                }
                set.resume();
            }
        }
    }


    /**
     * Main game loop runnable
     *
     * Add repeat animation, game logic here
     * Remember multiply by scaleFactorX and scaleFactorY while setting position of object
     *
     * TODO
     * 1. Adjust animation duration with Game speed
     * 2. Game logic, +/- score, right/wrong answer
     */

    private Runnable gameRunnable = new Runnable() {

        @Override
        public void run() {
            long elapseRight = (System.nanoTime() - sidewalkRightTime) / 1000000;
            long elapseLeft = (System.nanoTime() - sidewalkLeftTime) / 1000000;
            long elapse = (System.nanoTime() - questionTime) / 1000000;

            int s = extraSpeed * 400;

            final PercentRelativeLayout percentLayout = (PercentRelativeLayout) getChildAt(0);

            /**
             * Add side view
             *
             */

            if(isRunning && elapseRight > 3500) {

                int r = random.nextInt() % 2;

                final ImageView sidewalkStuff  = new ImageView(mContext);
                if(r == 0){
                    sidewalkStuff.setImageResource(R.drawable.sidewalkcrack);
                    sidewalkStuff.setLayoutParams(new LayoutParams(getWidth() / 10, getHeight() / 5));
                }else if(r == 1) {
                    sidewalkStuff.setImageResource(R.drawable.tirepile);
                    sidewalkStuff.setLayoutParams(new LayoutParams(getWidth() / 10, getHeight() / 5));
                }

                percentLayout.addView(sidewalkStuff);
                scoreboard.bringToFront();

                final Animator sidewalkAnimator = CAnimatorUtil.configTranslate(sidewalkStuff,
                        5000-s, 0, sidewalkRightPoints[0], sidewalkRightPoints[1]
                );
                ongoingAnimator.add(sidewalkAnimator);

                sidewalkAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        percentLayout.removeView(sidewalkStuff);
                        ongoingAnimator.remove(sidewalkAnimator);
                    }
                });

                sidewalkAnimator.start();
                sidewalkRightTime = System.nanoTime();
            }

            if(isRunning && elapseLeft > 2500) {
                int r = random.nextInt() % 2;

                final ImageView sidewalkStuff  = new ImageView(mContext);
                if(r == 0){
                    sidewalkStuff.setImageResource(R.drawable.sidewalkcrack);
                    sidewalkStuff.setLayoutParams(new LayoutParams(getWidth() / 10, getHeight() / 5));
                }else if(r == 1) {
                    sidewalkStuff.setImageResource(R.drawable.tirepile);
                    sidewalkStuff.setLayoutParams(new LayoutParams(getWidth() / 10, getHeight() / 5));
                }

                percentLayout.addView(sidewalkStuff);
                scoreboard.bringToFront();

                final Animator sidewalkAnimator = CAnimatorUtil.configTranslate(sidewalkStuff,
                        5000-s, 0, sidewalkLeftPoints[0], sidewalkLeftPoints[1]
                );
                ongoingAnimator.add(sidewalkAnimator);


                sidewalkAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        percentLayout.removeView(sidewalkStuff);
                        ongoingAnimator.remove(sidewalkAnimator);
                    }
                });
                sidewalkAnimator.start();

                sidewalkLeftTime = System.nanoTime();
            }

            mainHandler.postDelayed(gameRunnable, 400);
        }
    };


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
//            if(!player.getPlaying())
//            {
//                player.setPlaying(true);
//            }
//            else
//            {
//                player.setUp(true);
//            }

            if(isFirstInstall && !teachFinger.finishTeaching)
                teachFinger.onTouch(event, player);
            player.onTouch(event);
            soundPool.play(carscreechMedia, 0.1f, 0.1f, 1, 0, 1.0f);
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            return true;
        }

        return super.onTouchEvent(event);
    }

    //************ Serialization

    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);
        _dataIndex = 0;

    }



}
