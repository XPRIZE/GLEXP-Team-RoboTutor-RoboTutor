package cmu.xprize.sb_component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by jacky on 2016/6/22.
 */

public class CSb_Coinbag extends RelativeLayout {
    protected ImageView[]         coins;
    private static float        DPTOPX;

    private int width;
    private int height;
    private int mCoinWidth;
    private int mCoinHeight;


    private ImageView bound;
    private int coinNumber;

    private static float[][] LAYOUT_LINE = {
            {0.25f, 0.045f},
            {0.25f, 0.136f},
            {0.25f, 0.227f},
            {0.25f, 0.318f},
            {0.25f, 0.409f},
            {0.25f, 0.5f},
            {0.25f, 0.591f},
            {0.25f, 0.682f},
            {0.25f, 0.773f},
            {0.25f, 0.864f},
    };

    public int getCoinNumber() {
        return coinNumber;
    }

    public void setCoinNumber(int coinNumber) {
        this.coinNumber = coinNumber;
        for (int i = 0; i < 10; i++) {
            if (i < coinNumber)
                coins[i].setVisibility(VISIBLE);
            else
                coins[i].setVisibility(INVISIBLE);
        }
        requestLayout();

    }

    public void setImageResource(int id) {
        for(ImageView v : coins) v.setImageDrawable(getResources().getDrawable(id));
        requestLayout();
    }


    public CSb_Coinbag(Context context) {
        super(context);
        init(context, null);
    }

    public CSb_Coinbag(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CSb_Coinbag(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        bound = new ImageView(context);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.TRANSPARENT);
        gd.setStroke(2, Color.WHITE);
        bound.setImageDrawable(gd);
        bound.setScaleType(ImageView.ScaleType.FIT_XY);

        addView(bound);
        coins = new ImageView[10];
        for(int i = 0; i < 10; i++) {
            coins[i] = new ImageView(context);
            coins[i].setImageResource(R.drawable.coin_bronze);
            coins[i].setVisibility(INVISIBLE);
            coins[i].setScaleType(ImageView.ScaleType.FIT_XY);
            addView(coins[i]);
        }
        coinNumber = 0;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        int widthSize = r - l;
        int heightSize = b - t;
        int marginLeft = (int)(0.25 * widthSize);
        mCoinHeight = (int)(heightSize / 11.0);
        mCoinWidth = (int)(0.5 * widthSize);
        int marginTop = mCoinHeight / 2;
        int marginBottom = mCoinHeight / 2;


        for(int i = 0; i < 10; i++){
            coins[i].layout(marginLeft, heightSize - marginBottom - mCoinHeight,
                    marginLeft + mCoinWidth, heightSize - marginBottom);
            marginTop += mCoinHeight;
            marginBottom += mCoinHeight;
        }

        GradientDrawable gd = (GradientDrawable) bound.getDrawable();
        gd.setCornerRadius(0.5f * widthSize);

        int boundHeight;

        if (coinNumber == 9)
            boundHeight = 10 * mCoinHeight;
        else if(coinNumber > 1)
            boundHeight = (coinNumber + 1) * mCoinHeight;
        else
            boundHeight = 2 * mCoinHeight;

        bound.layout(0, heightSize - boundHeight, widthSize, heightSize);

    }

    public void increase() {
        if(coinNumber < 9) {
            if(coinNumber > 0) {
                coins[coinNumber].setVisibility(VISIBLE);
                coinNumber += 1;
            }
            else {
                coins[coinNumber].setVisibility(VISIBLE);
                coinNumber += 1;
            }
            requestLayout();
        }else
            coinNumber++;
    }

    public void decrease() {
        if(coinNumber > 0){
            coinNumber -= 1;
            if(coinNumber > 0) {
                coins[coinNumber].setVisibility(INVISIBLE);
            }else {
                coins[coinNumber].setVisibility(INVISIBLE);
            }
            requestLayout();
        }
    }

}
