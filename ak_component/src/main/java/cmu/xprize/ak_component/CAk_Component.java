package cmu.xprize.ak_component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.json.JSONObject;

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
public class CAk_Component extends FrameLayout implements ILoadableObject{
    static public Context mContext;

    protected String        mDataSource;
    private   int           _dataIndex = 0;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    static final String TAG = "CAk_Component";


    static final int WIDTH = 960, HEIGHT = 600;

    private Background bg;
    private CAk_Data _currData;
    private long startTime;
    private Player player;

    private long treeTime1;
    private long treeTime2;
    private long questionTime;
    private int boardCount;

    //json loadable
    public int          gameSpeed          ;
    public CAk_Data[]   dataSource         ;


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

        treeTime1 = treeTime2 = questionTime = startTime = System.nanoTime();

        bg = new Background(context);
        player = new Player(context);

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


    /**
     *
     * Draw game panel
     * Remember to draw all static object, like background, car, etc.
     */
    @Override
    public void onDraw(Canvas canvas) {

        float scaleFactorX = getWidth() * 1.0f / CAk_Component.WIDTH;
        float scaleFactorY = getHeight() * 1.0f / CAk_Component.HEIGHT;

        canvas.save();
        canvas.scale(scaleFactorX, scaleFactorY);

        bg.draw(canvas);
        player.draw(canvas);

        canvas.restore();
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
            long elapse1 = (System.nanoTime() - treeTime1) / 1000000;
            long elapse2 = (System.nanoTime() - treeTime2) / 1000000;
            long elapse = (System.nanoTime() - questionTime) / 1000000;

            final float scaleFactorX = getWidth() * 1.f / CAk_Component.WIDTH;
            final float scaleFactorY = getHeight() * 1.f / CAk_Component.HEIGHT;

            /**
             * Add side view
             *
             * TODO
             * Different side view object, random elapse and position
             */


            if(elapse1 > 3500) {
                final ImageView tree =new ImageView(mContext);

                tree.setImageResource(R.drawable.tree);
                tree.setLayoutParams(new LayoutParams((int)(50 * scaleFactorX), (int)(100 * scaleFactorY)));
                addView(tree);
                tree.setX(580 * scaleFactorX);
                tree.setY(135 * scaleFactorY);

                tree.animate().setDuration(3500 - gameSpeed * 100).x(760 * scaleFactorX).y(610 * scaleFactorY).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        removeView(tree);
                    }
                });
                treeTime1 = System.nanoTime();
            }

            if(elapse2 > 2500) {
                final ImageView tree = new ImageView(mContext);
                tree.setImageResource(R.drawable.tree);
                tree.setLayoutParams(new LayoutParams((int)(50 * scaleFactorX), (int)(100 * scaleFactorY)));
                addView(tree);
                tree.setX(300 * scaleFactorX);
                tree.setY(135 * scaleFactorY);
                tree.animate().setDuration(3500 - gameSpeed * 100).x(165 * scaleFactorX).y(610 * scaleFactorY).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        removeView(tree);
                    }
                });
                treeTime2 = System.nanoTime();
            }


            /**
             *  Add questionboard
             *
             *  TODO
             *  Using drawable as questionboard
             */

            if(elapse > 5000) {
                final QuestionBoard questionBoard = new QuestionBoard(mContext);
                addView(questionBoard, new LayoutParams((int)(90 * scaleFactorX), (int)(30 * scaleFactorY)));
                questionBoard.setX(430 * scaleFactorX);
                questionBoard.setY(100 * scaleFactorY);


                ObjectAnimator animator1 = ObjectAnimator.ofFloat(questionBoard, "y", 330 * scaleFactorY);
                ObjectAnimator animator2 = ObjectAnimator.ofFloat(questionBoard, "scaleX", 4f);
                ObjectAnimator animator3 = ObjectAnimator.ofFloat(questionBoard, "scaleY", 4f);
                ObjectAnimator animator4 = ObjectAnimator.ofFloat(questionBoard, "x", 470 * scaleFactorX);

                AnimatorSet set = new AnimatorSet();
                set.playTogether(animator1, animator2, animator3, animator4);
                set.setDuration(3500 - gameSpeed * 100);
                set.setTarget(questionBoard);
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        removeView(questionBoard);
                    }
                });
                set.start();

                questionTime = System.nanoTime();
            }


            mainHandler.postDelayed(gameRunnable, 100);
        }
    };


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
            player.onTouchEvent(event, scaleFactorX);
            player.update();
            invalidate();
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
