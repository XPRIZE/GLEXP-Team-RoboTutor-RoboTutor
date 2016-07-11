package cmu.xprize.ar_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;


/**
 * Created by mayankagrawal on 6/27/16.
 */
public class CAr_Component extends LinearLayout implements ILoadableObject {

    private Context mContext;

    protected String mDataSource;

    // private int[][] dataset = {{3, 4, 7}, {1, 3, 1, 1, 6}, {1, 1, 3, 5}};
    //private int[][] dataset = {{2, 1, 1}, {7, 4, 3}, {6, 1, 5}, {4, 3, 1}};
    private int[] numbers;
    private Integer corValue;

    private String operation; // = "-";

    private String[] images; //= {"star", "triangle", "hexagon"};
    private String currImage;

    private int numAlleys = 0;
    private int _dataIndex;

    private float scale = getResources().getDisplayMetrics().density;
    private int alleyMargin = (int) (AR_CONST.alleyMargin * scale);

    private ArrayList<CAr_Alley> allAlleys = new ArrayList<CAr_Alley>();

    // json loadable
    public CAr_Data[] dataSource;

    static final String TAG = "CAr_Component";

    public CAr_Component(Context context) {

        super(context);
        init(context, null);

    }

    public CAr_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAr_Component(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs);
        init(context, attrs);

    }

    public void init(Context context, AttributeSet attrs) {

        mContext = context;

        if (attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
                mDataSource = a.getString(R.styleable.RoboTutor_dataSource);
            } finally {
                a.recycle();
            }
        }

    }

    public void setDataSource(CAr_Data[] _dataSource) {

        dataSource = _dataSource;
        _dataIndex = 0;
    }

    public void next() {

        try {
            if (dataSource != null) {
                updateDataSet(dataSource[_dataIndex]);

                _dataIndex++;
            } else {
                CErrorManager.logEvent(TAG, "Error no DataSource : ", null, false);
            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }

    }

    public boolean dataExhausted() {
        return (_dataIndex >= dataSource.length) ? true : false;
    }


    protected void updateDataSet(CAr_Data data) {

        numbers = data.dataset;
        currImage = data.image;
        corValue = numbers[numbers.length - 1];
        operation = data.operation;

        int val, id;
        boolean clickable = true;

        // update alleys
        for (int i = 0; i < numbers.length; i++) {

            val = numbers[i];

            if (i == numbers.length - 2) {
                id = AR_CONST.OPERATION;
            } else if (i == numbers.length - 1) {
                id = AR_CONST.RESULT;
                val = 0;
                clickable = false;
            } else {
                id = AR_CONST.REGULAR;
            }

            setAlley(i, val, id, operation, clickable);
        }

        // delete extra alleys
        int delta = numAlleys - numbers.length;

        if (delta > 0) {
            for (int i = 0; i < delta; i++) {
                delAlley();
            }
        }

        if (operation == "-") {
            animateSubtract();
        }

        _dataIndex++;
    }

    private void setAlley(int index, int val, int id, String operation, boolean clickable) {

        if (index + 1 > numAlleys) {
            addAlley(index, val, id, operation, clickable);
        } else {
            CAr_Alley currAlley = allAlleys.get(index);
            currAlley.update(val, currImage, id, operation, clickable);
        }
    }

    private CAr_Alley addAlley(int index, int val, int id, String operation, boolean clickable) {

        CAr_Alley newAlley = new CAr_Alley(mContext);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, alleyMargin);
        newAlley.setLayoutParams(lp);

        newAlley.setParams(val, currImage, id, operation, clickable);

        addView(newAlley, index);
        allAlleys.add(index, newAlley);

        numAlleys++;

        return newAlley;
    }

    private void delAlley() {

        int index = numAlleys - 1;

        allAlleys.get(index).removeAllViews();
        allAlleys.remove(index);

        numAlleys--;

    }

    public boolean isCorrect() {

        boolean correct = corValue.equals(allAlleys.get(numAlleys - 1).getNum());

        if (!correct) {
            //allAlleys.get(numAlleys-1).getEditText().setText(""); // reset answer text
        }

        return correct;
    }

    public void UpdateValue(int value) {
    }

    public boolean allCorrect(int numCorrect) {
        return (numCorrect == dataSource.length);
    }


//  TODO: fix the onTouch to see results

    private OnClickListener clickListener = new OnClickListener() {
        public void onClick(View v) {

            switch (operation) {
                case "+":
                    handleAddClick();
                    break;

                case "-":
                    handleSubtractClick();
                    break;
            }
        }
    };


    private void handleAddClick() {

        DotBag currBag;
        DotBag clickedBag = null;
        int alleyNum = 0;

        for (int i = 0; i < allAlleys.size(); i++) {
            currBag = allAlleys.get(i).getDotBag();
            if (currBag.getIsClicked()) {
                clickedBag = currBag;
                alleyNum = i;
                break;
            }
        }

        if (clickedBag == null) {
            return;
        }

        Dot clickedDot = clickedBag.findClickedDot();

        if (clickedDot != null) {
            animateAdd(clickedDot, alleyNum);
        }


    }

    public void animateAdd(Dot clickedDot, int alleyNum) {

        DotBag resultBag = allAlleys.get(allAlleys.size() - 1).getDotBag();

        int dx = determineDX(clickedDot, resultBag.getCols());
        int dy = determineDY(alleyNum);

        final Dot oldDot = clickedDot;
        final Dot newDot = resultBag.addDot(0, resultBag.getCols());
        newDot.setTranslationX(-dx);
        newDot.setTranslationY(-dy);
        newDot.setIsClickable(false);

        ObjectAnimator animX = ObjectAnimator.ofFloat(newDot, "translationX", 0);
        ObjectAnimator animY = ObjectAnimator.ofFloat(newDot, "translationY", 0);

        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY);
        animSetXY.setDuration(1000);
        animSetXY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                oldDot.setHollow(true);
                setAllParentsClip(newDot, false);

            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        animSetXY.start();
    }

    private int determineDX(Dot startDot, int targetCol) {

        int dotSize = startDot.getWidth();
        return dotSize * (targetCol - startDot.getCol());
    }

    private int determineDY(int alleyNum) {

        int dy = 0;
        for (int i = alleyNum + 1; i < allAlleys.size(); i++) {
            dy += alleyMargin + allAlleys.get(i).getHeight();
        }
        return dy;
    }

    private void animateSubtract() {

        int numAlleys, rows, cols, size, dy;
        String imageName;

        numAlleys = allAlleys.size();

        DotBag firstDotBag = allAlleys.get(0).getDotBag();
        DotBag resultDotBag = allAlleys.get(numAlleys - 1).getDotBag();

        rows = firstDotBag.getRows();
        cols = firstDotBag.getCols();
        imageName = firstDotBag.getImageName();
        size = firstDotBag.getSize();

        resultDotBag.setParams(size, rows, cols, false, imageName);
        setAllParentsClip(resultDotBag, false);

        firstDotBag.setHollow(true);

        // calc distance
        dy = 0;
        for (int i = numAlleys - 1; i > 0; i--) {
            dy += allAlleys.get(i).getHeight() + alleyMargin;
        }

        resultDotBag.setTranslationY(-dy);
        ObjectAnimator anim = ObjectAnimator.ofFloat(resultDotBag, "translationY", 0);
        anim.setDuration(3000);
        anim.start();

    }

    private void handleSubtractClick() {

        Dot clickedDot = null;
        DotBag resultDotBag = allAlleys.get(allAlleys.size() - 1).getDotBag();
//      clickedBag will always be the middle number. 3 - 2 = 1. It is referring to 2
        DotBag clickedBag = allAlleys.get(1).getDotBag(); // only one possible dotbag to look at

        if (clickedBag.getIsClicked()) {
            clickedDot = clickedBag.findClickedDot();
        }

        // make sure dot was clicked
        if (clickedDot == null) {
            return;
        }

        int clickedDotCol = clickedDot.getCol();

        clickedDot.setHollow(true);

        Dot correspondingDot = resultDotBag.getDot(0, clickedDotCol);
        correspondingDot.setVisibility(INVISIBLE);

        if (resultDotBag.getVisibleDots().size() == corValue) {
            subtractLayoutChange(clickedDot);
        }
    }

    private void subtractLayoutChange(Dot clickedDot) {

        final DotBag resultDotBag = allAlleys.get(allAlleys.size() - 1).getDotBag();

        final ArrayList<Dot> visibleDots = resultDotBag.getVisibleDots();
        final int numVisibleDots = visibleDots.size();
        int numInvisibleDots = allAlleys.get(0).getNum() - numVisibleDots;

        int dotSize = clickedDot.getWidth();
        float currRight = resultDotBag.getBounds().right;
        float newRight = currRight - dotSize * numInvisibleDots;

        float dx = newRight - currRight;

        ArrayList<Animator> allAnimations = new ArrayList<>();

        for (int i = 0; i < numVisibleDots; i++) {
            allAnimations.add(ObjectAnimator.ofFloat(visibleDots.get(i), "translationX", dx));
        }
        allAnimations.add(ObjectAnimator.ofFloat(resultDotBag, "right", currRight, newRight));

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(allAnimations);
        animSet.setDuration(1000);
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                for (int i = 0; i < numVisibleDots; i++) {
                    visibleDots.get(i).setTranslationX(0); // reset dots
                }

                resultDotBag.removeInvisibleDots();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        animSet.start();

    }

    // TODO: fix this function - copied from stack overflow
    public static void setAllParentsClip(View v, boolean enabled) {
        while (v.getParent() != null && v.getParent() instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) v.getParent();
            viewGroup.setClipChildren(enabled);
            viewGroup.setClipToPadding(enabled);
            v = viewGroup;
        }
    }

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


//    TODO: Put the correct donitions for lifitng up finger
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//
//        final int action = MotionEventCompat.getActionMasked(ev);
//        if (action == MotionEvent.ACTION_DOWN) {
//            return true;
//        }else {
//            return super.onInterceptTouchEvent(ev);
//        }
//    }

}