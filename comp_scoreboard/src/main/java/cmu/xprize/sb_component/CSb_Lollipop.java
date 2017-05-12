package cmu.xprize.sb_component;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.HashSet;

/**
 * Created by jacky on 2016/6/23.
 */

public class CSb_Lollipop extends RelativeLayout {
    public ImageView[] coins;

    private int mCoinWidth;
    private int mCoinHeight;
    private Context context;

    private ImageView bound;

    private enum Mode{STICK, CIRCLE};

    private static float[][]    LAYOUT_CIRCLE = {
            {0.25f, 0.14f},
            {0.55f, 0.14f},
            {0.1f, 0.34f},
            {0.4f, 0.34f},
            {0.7f, 0.34f},
            {0.1f, 0.54f},
            {0.4f, 0.54f},
            {0.7f, 0.54f},
            {0.25f, 0.74f},
            {0.55f, 0.74f}
    };

    private Mode mode;

    public CSb_Lollipop(Context context) {
        super(context);
        init(context, null);
    }

    public CSb_Lollipop(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CSb_Lollipop(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        coins = new ImageView[10];
        this.context = context;
        for(int i = 0; i < 10; i++) {
            coins[i] = new ImageView(context);
            coins[i].setImageResource(R.drawable.coin_gold);
            coins[i].setScaleType(ImageView.ScaleType.FIT_XY);
            addView(coins[i]);
        }
        bound = new ImageView(context);
        addView(bound);
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int widthSize = r - l;
        int heightSize = b - t;
        mCoinWidth = (int) (0.2 * widthSize);
        mCoinHeight = (int) (heightSize / 302.0 * 20);
        bound.setScaleType(ImageView.ScaleType.FIT_XY);
        bound.layout(0 , 0, widthSize, heightSize);
        for(int i = 0; i < 10; i++){
            coins[i].getLayoutParams().height = mCoinHeight;
            coins[i].getLayoutParams().width = mCoinWidth;
        }
    }

    public void setImageResoure(int id){
        for(ImageView v : coins) v.setImageDrawable(getResources().getDrawable(id));
    }

    public void setToCircle() {
        final int width = getWidth();
        final int height = getHeight();
        for(int i = 0; i < 10; i++){
            coins[i].setX(width * LAYOUT_CIRCLE[i][0] );
            coins[i].setY(height / 302.0f * 100 * LAYOUT_CIRCLE[i][1]);
        }
        requestLayout();
        AnimatedVectorDrawable d = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.avd_to_stick);
        bound.setImageDrawable(d);
    }

    public void animateToStick(){
        final int width = getWidth();
        final int height = getHeight();
        AnimatedVectorDrawable d = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.avd_to_stick);
        d.setTint(Color.LTGRAY);
        bound.setImageDrawable(d);

        this.mode = Mode.CIRCLE;
        HashSet<Integer> set = new HashSet<>();
        for(int i = 0; i < 10; i++){
            coins[i].setX(width * LAYOUT_CIRCLE[i][0]);
            coins[i].setY(height / 302.0f * 100 * LAYOUT_CIRCLE[i][1]);
            coins[i].getLayoutParams().height = mCoinHeight;
            coins[i].getLayoutParams().width = mCoinWidth;
            set.add(i);
        }
        requestLayout();

        toStickHelper(9, coins, set);
        Drawable drawable = bound.getDrawable();
        if (drawable != null && drawable instanceof Animatable) {
            Animatable animatable = (Animatable) drawable;
            animatable.start();
        }
    }

    private void toStickHelper(final int i, final ImageView[] list, final HashSet<Integer> set){
        if(!set.contains(i)) return;
        set.remove(i);
        final int width = getWidth();
        final int height = getHeight();
        final ImageView coin = list[i];

        Path path = new Path();
        path.moveTo(coin.getX(), coin.getY());
        path.cubicTo(width / 100.0f * 41 ,
                height/302.0f * 60 ,
                width/100.0f * 41 ,
                height/302.0f * 95 ,
                width/100.0f * 41 ,
                height/302.0f * (92 + 20 * i));
        ValueAnimator pathAnimator = ObjectAnimator.ofFloat(coin, "x", "y", path);
        pathAnimator.setDuration(1700);
        pathAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pathAnimator.start();
        if(i > 6) toStickHelper(i-1, list, set);
        else
            pathAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedFraction() > 0.000001)
                        toStickHelper(i - 1, list, set);
                }
            });
    }

    public void animateToCircle(){
        final int width = getWidth();
        final int height = getHeight();
        mCoinWidth = (int) (0.2 * width);
        mCoinHeight = (int) (height / 302.0 * 20);
        AnimatedVectorDrawable d = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.avd_to_circle);
        d.setTint(Color.LTGRAY);
        bound.setImageDrawable(d);
        this.mode = Mode.STICK;
        HashSet<Integer> set = new HashSet<>();
        for(int i = 0; i < 10; i++){
            coins[i].setX(width/100.0f * 41);
            coins[i].setY(height/302.0f * (92 + 20 * i));
            coins[i].getLayoutParams().height = mCoinHeight;
            coins[i].getLayoutParams().width = mCoinWidth;
            requestLayout();
            set.add(i);
        }


        toCircleHelper(0, coins, set);
        Drawable drawable = bound.getDrawable();
        if (drawable != null && drawable instanceof Animatable) {
            Animatable animatable = (Animatable) drawable;
            animatable.start();
        }
    }

    private void toCircleHelper(final int i, final ImageView[] list, final HashSet<Integer> set){
        if(!set.contains(i)) return;
        set.remove(i);

        final int width = getWidth();
        final int height = getHeight();
        final ImageView coin = list[i];
        Path path = new Path();
        path.moveTo(coin.getX(), coin.getY());
        path.cubicTo(width/100.0f * 41 ,
                height/302.0f * 95 ,
                width/100.0f * 41 ,
                height/302.0f * 60 ,
                width * LAYOUT_CIRCLE[i][0] ,
                height/302.0f * 100 * LAYOUT_CIRCLE[i][1]);
        ValueAnimator pathAnimator = ObjectAnimator.ofFloat(coin, "x", "y", path);
        pathAnimator.setDuration(1750);
        pathAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pathAnimator.start();
        if(i < 3) toCircleHelper(i+1, list, set);
        else
            pathAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedFraction() > 0.000001)
                        toCircleHelper(i + 1, list, set);
                }
            });
    }
}
