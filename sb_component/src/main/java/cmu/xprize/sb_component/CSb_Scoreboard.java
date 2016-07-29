package cmu.xprize.sb_component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cmu.xprize.util.CAnimatorUtil;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by jacky on 2016/6/22.
 */

public class CSb_Scoreboard extends android.support.percent.PercentRelativeLayout {
    private CSb_Lollipop[] lollipops;
    private CSb_Coinbag[] coinbags;
    private int mScore;
    private int mColNum;
    private int widthScale;
    private Paint mPaint;
    private int[] resoureImageIds;
    public boolean isAnimating;


    private boolean init = true;

    private static float DPTOPX;
    private Context context;

    private ImageView largeCoin;


    protected static float[][]    LAYOUT_CIRCLE = {
            {0.25f, 0.1f},
            {0.55f, 0.1f},
            {0.1f, 0.3f},
            {0.4f, 0.3f},
            {0.7f, 0.3f},
            {0.1f, 0.5f},
            {0.4f, 0.5f},
            {0.7f, 0.5f},
            {0.25f, 0.7f},
            {0.55f, 0.7f}
    };


    public CSb_Scoreboard(Context context) {
        super(context);
        init(context, null);
    }

    public CSb_Scoreboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CSb_Scoreboard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int widthSize = r - l;
        int heightSize = b - t;

        int bagWidth = (int)(widthSize * 40.0 / widthScale);
        int bagHeight = (int)(heightSize * 0.528);
        int scoreHeight = (int)(heightSize * 0.2);

        int lollipopWidth = (int)(widthSize * 100.0 / widthScale);
        int bagGap = (int)(widthSize * 40.0 / widthScale);
        int bagMarginLeft = (int)(widthSize * 30.0 / widthScale);
        int lollipopGap = bagGap;
        int lollipopMarginLeft = 0;


        for(int i = mColNum - 1; i >= 0; i--){
            coinbags[i].layout(bagMarginLeft,
                    heightSize - bagHeight - scoreHeight,
                    bagMarginLeft + bagWidth,
                    heightSize - scoreHeight);
            lollipops[i].layout(lollipopMarginLeft,
                    0, lollipopMarginLeft + lollipopWidth,
                    heightSize - scoreHeight);
            bagMarginLeft += bagGap;
            lollipopMarginLeft += lollipopGap;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int showingCol = getShowingCol();

        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(4);

        canvas.drawLine(coinbags[showingCol].getX(), 0.82f * height,
                coinbags[0].getX() + coinbags[0].getWidth(), 0.82f * height, mPaint);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(70);
        for(int i = 0; i < showingCol+1; i++) {
            //Prevent showing 2 digit at a column
            canvas.drawText(String.valueOf(coinbags[i].getCoinNumber() > 9? 9 : coinbags[i].getCoinNumber()),
                    coinbags[i].getX() + coinbags[i].getWidth() / 2,
                    0.9f * height, mPaint);
        }

    }

    private int getShowingCol() {
        int n = (int)Math.pow(10, mColNum);
        int showingCol = mColNum;
        if(mScore == 0) return 0;
        while(n > mScore) {
            n /= 10;
            showingCol--;
        }
        return showingCol;
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;

        if(attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CSb_Scoreboard,
                    0, 0);

            try {
                //inflate(getContext(), R.layout.sb_scoreboard, this);
                this.context = context;
                this.mScore = a.getInteger(R.styleable.CSb_Scoreboard_score, 0);
                this.mColNum = a.getInteger(R.styleable.CSb_Scoreboard_col_num, 3);
                int score = mScore;
                coinbags = new CSb_Coinbag[mColNum];
                lollipops = new CSb_Lollipop[mColNum];
                resoureImageIds = new int[mColNum];
                for(int i = 0; i < mColNum; i++){
                    switch (i){
                        case 0:
                            resoureImageIds[i] = R.drawable.coin_bronze;
                            break;
                        case 1:
                            resoureImageIds[i] = R.drawable.coin_silver;
                            break;
                        default:
                            resoureImageIds[i] = R.drawable.coin_gold;
                    }
                }

                for(int i = 0; i < mColNum; i++){
                    coinbags[i] = new CSb_Coinbag(context);
                    lollipops[i] = new CSb_Lollipop(context);
                    coinbags[i].setImageResource(resoureImageIds[i]);
                    lollipops[i].setImageResoure(resoureImageIds[i]);
                    coinbags[i].setCoinNumber(score % 10);
                    score /= 10;
                    addView(coinbags[i]);
                    addView(lollipops[i]);
                    lollipops[i].setVisibility(INVISIBLE);
                }

                widthScale = 50 * (mColNum + 1);

                largeCoin = new ImageView(context);
                largeCoin.setImageResource(R.drawable.coin_gold);
                largeCoin.setVisibility(INVISIBLE);
                largeCoin.setScaleType(ImageView.ScaleType.FIT_XY);

                int showingCol = getShowingCol();
                for(int j = 0; j < mColNum; j++){
                    if(j <= showingCol) coinbags[j].setVisibility(VISIBLE);
                    else coinbags[j].setVisibility(INVISIBLE);
                }

                addView(largeCoin);

                setWillNotDraw(false);

                mPaint = new Paint();

            } finally {
                a.recycle();
            }
        }
    }

    public void increase() {
        mScore += 1;
        isAnimating = true;
        coinbags[0].increase();
        if(coinbags[0].getCoinNumber() == 10) {
            carryAnimation(0);
        }
        else isAnimating = false;
    }

    public void decrease() {
        if(mScore == 0) return;
        isAnimating = true;
        if(coinbags[0].getCoinNumber() > 0){
            coinbags[0].decrease();
            isAnimating = false;
        }
        else {
            for(int i = 1; i < mColNum; i++){
                if(coinbags[i].getCoinNumber() > 0){
                    borrowAnimation(i);
                    break;
                }
            }
        }
        mScore -= 1;
    }

    public Animator reward(float x, float y) {
        TextView textView = new TextView(context);
        textView.setText("+1");
        textView.setTextSize(20);
        textView.setTextColor(Color.WHITE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(textView, params);
        Animator rewardAnimator = CAnimatorUtil.configTranslate(textView,
                1500, 0, new PointF(x, y), new PointF(
                        coinbags[0].getX(),
                        coinbags[0].getY() + coinbags[0].getHeight()));
        return rewardAnimator;
    }

    public Animator penalty(float x, float y) {
        TextView textView = new TextView(context);
        textView.setText("-1");
        textView.setTextSize(30);
        textView.setTextColor(Color.WHITE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(textView, params);

        Animator rewardAnimator = CAnimatorUtil.configTranslate(textView,
                1500, 0, new PointF(x, y), new PointF(
                        coinbags[0].getX() + coinbags[0].getWidth(),
                        coinbags[0].getY() + coinbags[0].getHeight()));
        return rewardAnimator;
    }

    public void removeTextView(){
        for(int i = 0; i < getChildCount(); i++) {
            if(getChildAt(i).getClass() == TextView.class)
                removeView(getChildAt(i));
        }
    }


    private void setPosition() {
        int widthSize = getWidth();
        int widthScale = 50 * (mColNum + 1);
        int bagGap = (int)(widthSize * 40.0 / widthScale);
        int bagMarginLeft = (int)(widthSize * 30.0 / widthScale);
        int lollipopGap = bagGap;
        int lollipopMarginLeft = 0;

        for(int i = mColNum - 1; i >= 0; i--){
            coinbags[i].setX(bagMarginLeft);
            lollipops[i].setX(lollipopMarginLeft);
            lollipops[i].setToCircle();
            lollipops[i].forceLayout();
            bagMarginLeft += bagGap;
            lollipopMarginLeft += lollipopGap;
        }
    }

    private void carryAnimation(final int index) {
        if(index >= mColNum) return;

        final int width = getWidth();
        final int height = getHeight();

        setPosition();

        final CSb_Coinbag bag = coinbags[index];
        final CSb_Coinbag carryToBag = coinbags[index+1];
        final CSb_Lollipop lollipop = lollipops[index];

        bag.setVisibility(INVISIBLE);


        LayoutParams params_coin = (LayoutParams) largeCoin.getLayoutParams();
        params_coin.width = (int)(width * 100.0f / widthScale);
        params_coin.height = (int)(height / 302.0f * 80 );
        requestLayout();


        lollipop.setVisibility(VISIBLE);

        lollipop.animateToCircle();
        lollipop.postDelayed(new Runnable() {
            @Override
            public void run() {
                largeCoin.setImageResource(resoureImageIds[index+1]);
                largeCoin.setVisibility(VISIBLE);
                largeCoin.getDrawable().setAlpha(0);
                largeCoin.setX(lollipop.getX());
                largeCoin.setY(height/302.0f * 4);

                AnimatorSet animatorSet = new AnimatorSet();

                ValueAnimator alpha1 = ValueAnimator.ofInt(0, 255);
                alpha1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        largeCoin.getDrawable().setAlpha((Integer)animation.getAnimatedValue());
                    }
                });
                alpha1.setDuration(800);

                ValueAnimator alpha2 = ValueAnimator.ofFloat(1, 0);
                alpha2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        lollipop.setAlpha((Float)animation.getAnimatedValue());
                    }
                });
                alpha2.setDuration(800);

                ValueAnimator translateX = ValueAnimator.ofFloat(largeCoin.getX(),
                        carryToBag.getX() - (width * 30.0f / widthScale) );
                translateX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        largeCoin.setX((Float) animation.getAnimatedValue());
                        lollipop.setX((Float) animation.getAnimatedValue());
                        requestLayout();
                    }
                });
                translateX.setDuration(1000);

                animatorSet.playTogether(alpha1, alpha2,translateX);

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        lollipop.setAlpha(1);
                        lollipop.setVisibility(INVISIBLE);

                        ValueAnimator scale = ValueAnimator.ofFloat(1, 0.2f);
                        scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                LayoutParams params = (LayoutParams) largeCoin.getLayoutParams();
                                params.width = (int) (width * 100.0f / widthScale * (Float) animation.getAnimatedValue());
                                params.height = (int) (height / 302.0f * 80  * (Float) animation.getAnimatedValue());
                                requestLayout();
                            }
                        });
                        scale.setDuration(1000);

                        ValueAnimator translateX2 = ValueAnimator.ofFloat(largeCoin.getX(),
                                carryToBag.getX() + carryToBag.coins[0].getX());
                        translateX2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                largeCoin.setX((Float) animation.getAnimatedValue());
                                requestLayout();
                            }
                        });
                        translateX2.setDuration(1000);

                        ValueAnimator translateY2;
                        translateY2 = ValueAnimator.ofFloat(largeCoin.getY(),
                                carryToBag.getY() +
                                        carryToBag.coins[carryToBag.getCoinNumber()].getY());


                        translateY2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                largeCoin.setY((Float) animation.getAnimatedValue());
                                requestLayout();
                            }
                        });
                        translateY2.setDuration(1000);


                        AnimatorSet set = new AnimatorSet();
                        set.playTogether(translateX2, translateY2, scale);


                        set.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                largeCoin.setVisibility(INVISIBLE);
                                bag.setVisibility(VISIBLE);
                                bag.setCoinNumber(0);
                                carryToBag.increase();
                                if(carryToBag.getCoinNumber() == 10)
                                    carryAnimation(index+1);
                                else
                                    isAnimating = false;
                                int showingCol = getShowingCol();
                                coinbags[showingCol].setVisibility(VISIBLE);
                            }
                        });

                        set.start();
                    }
                });

                animatorSet.start();



            }
        }, 2500);

    }

    private void borrowAnimation(final int index){
        final int width = getWidth();
        final int height = getHeight();

        setPosition();
        final CSb_Coinbag bag = coinbags[index];
        final CSb_Coinbag lendToBag = coinbags[index-1];
        final CSb_Lollipop lollipop = lollipops[index-1];

        bag.decrease();

        lendToBag.setCoinNumber(10);
        lendToBag.setVisibility(INVISIBLE);

        LayoutParams params = (LayoutParams) largeCoin.getLayoutParams();
        params.width = (int) (width * 20.0 / widthScale);
        params.height = (int) (height / 302.0f * 16 );

        largeCoin.setImageResource(resoureImageIds[index]);
        largeCoin.setX(bag.getX() + bag.coins[0].getX());
        largeCoin.setY(bag.getY() + bag.coins[bag.getCoinNumber()].getY());
        largeCoin.setVisibility(VISIBLE);


        lollipop.setAlpha(0);
        lollipop.setToCircle();
        lollipop.setVisibility(VISIBLE);
        lollipop.setToCircle();

        ValueAnimator translateX = ValueAnimator.ofFloat(largeCoin.getX(), bag.getX() - width * 30.0f / widthScale );
        translateX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                largeCoin.setX((Float) animation.getAnimatedValue());
                requestLayout();
            }
        });
        translateX.setDuration(1000);


        ValueAnimator translateY = ValueAnimator.ofFloat(largeCoin.getY(), height/302.0f * 4);
        translateY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                largeCoin.setY((Float) animation.getAnimatedValue());
                requestLayout();
            }
        });
        translateY.setDuration(1000);

        ValueAnimator scale = ValueAnimator.ofFloat(0.2f, 1);
        scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LayoutParams params = (LayoutParams) largeCoin.getLayoutParams();
                params.width = (int) (width * 100.0f / widthScale  * (Float) animation.getAnimatedValue());
                params.height = (int) (height / 302.0f * 80  * (Float) animation.getAnimatedValue());

            }
        });
        scale.setDuration(1000);

        AnimatorSet set = new AnimatorSet();

        ValueAnimator translateX2 = ValueAnimator.ofFloat(bag.getX() - width * 30.0f / widthScale , lollipop.getX());
        translateX2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                largeCoin.setX((Float) animation.getAnimatedValue());
                lollipop.setX((Float) animation.getAnimatedValue());

            }
        });
        translateX2.setDuration(1000);


        ValueAnimator alpha1 = ValueAnimator.ofInt(255, 0);
        alpha1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                largeCoin.getDrawable().setAlpha((Integer)animation.getAnimatedValue());
            }
        });
        alpha1.setDuration(800);

        ValueAnimator alpha2 = ValueAnimator.ofFloat(0, 1);
        alpha2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lollipop.setAlpha((Float)animation.getAnimatedValue());
            }
        });
        alpha2.setDuration(800);


        set.playTogether(translateY, scale, translateX);
        set.playTogether(translateX2, alpha1, alpha2);
        set.play(translateX2).after(translateX);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                largeCoin.setVisibility(INVISIBLE);
                largeCoin.getDrawable().setAlpha(255);
                lollipop.animateToStick();
                lollipop.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lollipop.setVisibility(INVISIBLE);
                        lendToBag.setVisibility(VISIBLE);
                        if (index > 1) {
                            borrowAnimation(index-1);
                        } else{
                            lendToBag.setCoinNumber(9);
                            isAnimating = false;
                        }
                        int showingCol = getShowingCol();
                        for(int i = 0; i < mColNum; i++){
                            if(i > showingCol) coinbags[i].setVisibility(INVISIBLE);
                        }
                    }
                }, 2500);

            }
        });

        set.start();
    }
}
