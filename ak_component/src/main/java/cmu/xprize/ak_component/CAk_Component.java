package cmu.xprize.ak_component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Random;

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

    private CAk_Data _currData;
    private long startTime;
    private CAkPlayer player;
    private CAkTeachFinger teachFinger;

    private TextView score;
    private long sidewalkRightTime;
    private long sidewalkLeftTime;
    private long questionTime;
    private int boardCount;
    private ImageView cityBackground;
    private Button minusSpeed;
    private Button plusSpeed;

    private Random random;
    private SoundPool soundPool;

    private boolean lastCorrect = true;
    private Boolean isFirstInstall;

    private PointF[] sidewalkLeftPoints;
    private PointF[] sidewalkRightPoints;

    private int carscreechMedia, correctMedia, incorrectMedia, numberchangedMedia;
    private boolean flag=true;

    private boolean speedIsZero=false;
    private int extraSpeed=0;

    //json loadable
    public int          gameSpeed          ;
    public CAk_Data[]   dataSource         ;

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
        score = (TextView) findViewById(R.id.score);
        teachFinger = (CAkTeachFinger) findViewById(R.id.finger);
        minusSpeed = (Button) findViewById(R.id.minusspeed);
        plusSpeed = (Button) findViewById(R.id.plusspeed);

        Animator cityAnimator = CAnimatorUtil.configTranslate(cityBackground,
                100000, 0, new PointF(0, -HEIGHT));


        sidewalkLeftPoints = new PointF[2];
        sidewalkRightPoints = new PointF[2];

        sidewalkLeftPoints[0] = new PointF();
        sidewalkLeftPoints[1] = new PointF();

        sidewalkRightPoints[0] = new PointF();
        sidewalkRightPoints[1] = new PointF();

        random = new Random();

        minusSpeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(extraSpeed>=-4)
                    extraSpeed--;
                if(extraSpeed==-5)
                    speedIsZero=true;
                System.out.println(""+extraSpeed);
            }
        });
        plusSpeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(extraSpeed==-5)
                    speedIsZero=false;
                if(extraSpeed<=4)
                    extraSpeed++;
                System.out.println(""+extraSpeed);
            }
        });

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
        return (_dataIndex >= dataSource.length)? true:false;
    }

    protected void updateDataSet(CAk_Data data) {

    }

    public void post(String command, Object target) {

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

            final float scaleFactorX = getWidth() * 1.f / CAk_Component.WIDTH;
            final float scaleFactorY = getHeight() * 1.f / CAk_Component.HEIGHT;

            int s=extraSpeed*500;

            final Animator sidewalkAnimator,sidewalkAnimator1;

            /**
             * Add side view
             *
             * TODO
             * Different side view object, random elapse and position
             */

            if(elapseRight > 3500-s) {

                int r = random.nextInt() % 3;

                final ImageView sidewalkStuff  = new ImageView(mContext);
                if(r == 0){
                    sidewalkStuff.setImageResource(R.drawable.tree);
                    sidewalkStuff.setLayoutParams(new LayoutParams(getWidth() / 10, getHeight() / 5));
                }
                else if(r == 1) {
                    sidewalkStuff.setImageResource(R.drawable.sidewalkcrack);
                    sidewalkStuff.setLayoutParams(new LayoutParams(getWidth() / 10, getHeight() / 5));
                }else if(r == 2) {
                    sidewalkStuff.setImageResource(R.drawable.tirepile);
                    sidewalkStuff.setLayoutParams(new LayoutParams(getWidth() / 10, getHeight() / 5));
                }

                addView(sidewalkStuff);

                sidewalkAnimator = CAnimatorUtil.configTranslate(sidewalkStuff,
                        3500-s, 0, sidewalkRightPoints[0], sidewalkRightPoints[1]
                );

                sidewalkAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        removeView(sidewalkStuff);
                    }
                });

                sidewalkAnimator.start();
                sidewalkRightTime = System.nanoTime();
            }

            if(elapseLeft > 2500-s) {
                int r = random.nextInt() % 3;

                final ImageView sidewalkStuff  = new ImageView(mContext);
                if(r == 0){
                    sidewalkStuff.setImageResource(R.drawable.tree);
                    sidewalkStuff.setLayoutParams(new LayoutParams(getWidth() / 10, getHeight() / 5));
                }
                else if(r == 1) {
                    sidewalkStuff.setImageResource(R.drawable.sidewalkcrack);
                    sidewalkStuff.setLayoutParams(new LayoutParams(getWidth() / 10, getHeight() / 5));
                }else if(r == 2) {
                    sidewalkStuff.setImageResource(R.drawable.tirepile);
                    sidewalkStuff.setLayoutParams(new LayoutParams(getWidth() / 10, getHeight() / 5));
                }

                addView(sidewalkStuff);

                sidewalkAnimator1 = CAnimatorUtil.configTranslate(sidewalkStuff,
                        3500-s, 0, sidewalkLeftPoints[0], sidewalkLeftPoints[1]
                );
                sidewalkAnimator1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        removeView(sidewalkStuff);
                    }
                });
                sidewalkAnimator1.start();

                sidewalkLeftTime = System.nanoTime();
            }


            /**
             *  Add questionboard
             *
             *  TODO
             *  Using drawable as questionboard
             */

            if(elapse > 5000-s && teachFinger.finishTeaching) {
                final CAkQuestionBoard questionBoard = new CAkQuestionBoard(mContext);

                LayoutParams params = new LayoutParams((int)(90 * scaleFactorX), (int)(30 * scaleFactorY));
                params.addRule(CENTER_HORIZONTAL);
                addView(questionBoard, params);

                final AnimatorSet questionboardAnimator = CAnimatorUtil.configZoomIn(questionBoard, 3500,
                        0, new LinearInterpolator(), 4f);

                ValueAnimator questionboardTranslationAnimator = ObjectAnimator.ofFloat(questionBoard,
                        "y", getHeight() * 0.25f, getHeight() * 0.75f);
                questionboardAnimator.setDuration(3500-s);
                questionboardAnimator.setInterpolator(new LinearInterpolator());

                questionboardAnimator.playTogether(questionboardTranslationAnimator);

                questionboardAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        removeView(questionBoard);
                        if(judge(questionBoard)){
                            player.score += 1;
                            soundPool.play(correctMedia, 1.0f, 1.0f, 1, 0, 1.0f);
                            lastCorrect=true;
                            errornum=0;
                        }else{
                            if(speedIsZero==false) {
                                player.score -= 1;
                                soundPool.play(incorrectMedia, 1.0f, 1.0f, 1, 0, 1.0f);
                                lastCorrect = false;
                                errornum += 1;
                                if (errornum == 3)
                                    dialog();
                            }
                            else{
                                /*if(sidewalkAnimator1!= null)
                                    sidewalkAnimator1.pause();
                                if(sidewalkAnimator!=null) {
                                    sidewalkAnimator.pause();
                                }*/
                                questionboardAnimator.pause();
                            }
                        }
                    }
                });

                questionboardAnimator.start();

                if(flag && teachFinger != null) {
                    teachFinger.setVisibility(INVISIBLE);
                    flag = false;
                }

                questionTime = System.nanoTime();
            }

            score.setText("score: "+ player.score);
            mainHandler.postDelayed(gameRunnable, 100);
        }
    };


    public void dialog()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
        builder.setMessage("Do you need help?");
        builder.setTitle("Help");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //show finger tutor
                errornum = 0;
                player.lane= CAkPlayer.Lane.MID;
                isFirstInstall=true;
                teachFinger.finishTeaching=false;
                teachFinger.setVisibility(VISIBLE);
                teachFinger.reset(mContext,null);
                flag=true;
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                errornum = 0;
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }


    /**
     * TODO
     * Judge touchEvent position.
     * Should the game only allow users to touch on lane to change lane,
     * or should the car change lane according to the relative touch position?
     *
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final float scaleFactorX = getWidth() * 1.f / CAk_Component.WIDTH;
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
            player.onTouchEvent(event, scaleFactorX);
            soundPool.play(carscreechMedia, 1.0f, 1.0f, 1, 0, 1.0f);

            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            return true;
        }

        return super.onTouchEvent(event);
    }

    private boolean judge(CAkQuestionBoard CAkQuestionBoard){
        if(player.rearNum < CAkQuestionBoard.leftNum && player.getLane() == cmu.xprize.ak_component.CAkPlayer.Lane.LEFT ||
                player.rearNum > CAkQuestionBoard.rightNum && player.getLane() == cmu.xprize.ak_component.CAkPlayer.Lane.RIGHT ||
                (player.rearNum < CAkQuestionBoard.rightNum && player.rearNum > CAkQuestionBoard.leftNum
                        && player.getLane() == cmu.xprize.ak_component.CAkPlayer.Lane.MID)){
            return true;
        }
        return false;
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
