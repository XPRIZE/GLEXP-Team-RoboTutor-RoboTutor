package cmu.xprize.comp_counting;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;


/**
 *
 */
public class CCount_DotBag extends TableLayout {

    private Context context;


    final float scale = getResources().getDisplayMetrics().density;

    private int rows = 0;
    private int cols = 0;
    private int size = (int)(COUNT_CONST.textBoxHeight*scale);

    private boolean isClickable = false;
    private boolean isClicked = false;
    private boolean isHollow = false;

    private boolean drawBorder = true;

    private String[] chimes;
    private int chimeIndex= -1;
    private String currentChime;
    private boolean isAudible;

    private String imageName = "star"; // default

    private int overflowNum = 0;

    private RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


    private ArrayList<TableRow> allTableRows = new ArrayList<TableRow>();

    Paint borderPaint = new Paint();
    int borderWidth = (int)(COUNT_CONST.borderWidth * scale);
    RectF bounds = new RectF();


    public CCount_DotBag(Context context, AttributeSet attributeSet) {

        super(context, attributeSet);
        this.context = context;
        init();

    }

    public CCount_DotBag(Context context) {

        super(context);
        this.context = context;
        init();

    }

    private void init() {

        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);
        setPaint();
        setZero();

    }

    /**
     * Sets the number of rows in the dotbag.
     *
     * @param _rows
     */
    public void setRows(int _rows) {

        TableRow currTableRow;

        int deltaRows = _rows - this.rows;


        if (_rows == 0) {
            setZero();
            return;
        }

        if (deltaRows < 0) {
            for (int i = this.rows - 1; i >= _rows; i--) {
                currTableRow = allTableRows.remove(i);
                removeView(currTableRow);
            }
        }

        else if (deltaRows > 0) {

            while (allTableRows.size() < _rows) {
                addRow(allTableRows.size());
            }

            for (int i = this.rows; i < _rows; i++) {
                for (int j = 0; j < this.cols; j++) {
                    addDot(i, j);
                }
            }
        }

        this.rows = _rows;

        resetDotTranslations();
        resetBounds();

    }

    public void setCols(int _cols) {

        TableRow currTableRow;

        if (_cols == 0) {
            setZero();
            return;
        }

        int origCols = this.cols;
        int deltaCols = _cols - origCols;

        if (deltaCols < 0) {
            for (int i = 0; i < this.rows; i++) {
                currTableRow = allTableRows.get(i);

                for (int j = currTableRow.getVirtualChildCount() - 1; j >= _cols; j-- ) {
                    currTableRow.removeViewAt(j);
                }

            }
        } else {
            for (int i = 0; i < this.rows; i++) {
                for(int j = 0; j < origCols; j++)
                    getDot(i, j).setVisibility(View.VISIBLE);
                for (int j = origCols; j < _cols; j++ )
                    addDot(i, j);
            }
        }

        this.cols = _cols;

        resetDotTranslations();
        resetBounds();

    }

    /**
     * Sets the image used for dots, then updates every dot with the new image.
     *
     * @param _imageName
     */
    public void setImage(String _imageName) {

        TableRow currTableRow;

        this.imageName = _imageName;

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);
            for (int j = 0; j < currTableRow.getVirtualChildCount(); j++){
                CCount_Dot dot = (CCount_Dot) currTableRow.getVirtualChildAt(j);
                dot.setImageName(imageName);
            }
        }

    }


    public void setChimeIndex(int chimeIndex) {
        this.chimeIndex = chimeIndex;
    }

    public void setChimes(String[] chimes) {
        this.chimes = chimes;
//        currentChime = chimes[chimeIndex % chimes.length];
    }

    public void setIsAudible(boolean isAudible) {
        this.isAudible = isAudible;
    }

    public String getCurrentChime() {
        return currentChime;
    }

    public boolean getIsAudible(){
        return isAudible;
    }


    /**
     * Add a dot at the specified row, col
     *
     * @param row
     * @param col
     * @return
     */
    public CCount_Dot addDot(int row, int col) {

        // add a row until you have enough
        while (allTableRows.size() < row + 1) {
            addRow(allTableRows.size());
        }
        // get the row in which to place the dot
        TableRow tableRow = allTableRows.get(row);

        // initialize a new dot
        CCount_Dot dot = new CCount_Dot(context);
        dot.setParams(isClickable, imageName, row, col);
        dot.setLayoutParams(new TableRow.LayoutParams(size, size));

        // add the dot to the table
        tableRow.addView(dot, col);

        // updates
        updateRows();
        updateCols();
        resetBounds();
        if (isAudible) {
            setChimeIndex(chimeIndex + 1);
            currentChime = chimes[this.chimeIndex];

        }

        return dot;
    }

    public void setHallowChime(){
        if (isAudible) {
            setChimeIndex(chimeIndex + 1);
            currentChime = chimes[this.chimeIndex % chimes.length];
        }
    }

    /**
     * Resets number of dots to zero, and adjusts size accordingly.
     */
    protected void setZero() {

        rows = 0;
        cols = 0;
        removeAllViews();
        allTableRows.clear();
        params.width = size;
        params.height = size;
        setLayoutParams(params);
        resetBounds();

    }

    private TableRow addRow(int index) {

        if (allTableRows.size() == 0) {
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            setLayoutParams(params);
        }

        TableRow tableRow = new TableRow(context);

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.setMargins(size / 2, 0, size / 2, 0);
        tableRow.setLayoutParams(lp);

        addView(tableRow, index);
        allTableRows.add(index, tableRow);

        return tableRow;
    }


    private void setPaint() {

        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeCap(Paint.Cap.ROUND);
        borderPaint.setStrokeJoin(Paint.Join.ROUND);
        borderPaint.setAntiAlias(true);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (drawBorder) {
            canvas.drawRoundRect(bounds, size / 2, size / 2, borderPaint);
            // TODO: make opaque

        }
        resetBounds();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            if (isClickable) {
                isClicked = true;
            }
        }
        return false;
    }

    /**
     * Finds which dot was recently cicked by iterating through each dot and checking "isClicked" variable.
     *
     * @return
     */
    public CCount_Dot findClickedDot() {

        TableRow currTableRow;
        CCount_Dot currDot;

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);

            for (int j = 0; j < currTableRow.getVirtualChildCount(); j++) {
                currDot = (CCount_Dot) currTableRow.getVirtualChildAt(j);
                if (currDot.getIsClicked()) {
                    return currDot;
                }
            }
        }
        return null;
    }

    public ArrayList<CCount_Dot> getVisibleDots(){

        TableRow currTableRow;

        ArrayList<CCount_Dot> toReturn = new ArrayList<>();

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);

            for (int j = 0; j < currTableRow.getVirtualChildCount(); j++){
                CCount_Dot dot = (CCount_Dot) currTableRow.getVirtualChildAt(j);

                if (dot.getVisibility() == VISIBLE) {
                    toReturn.add(dot);
                }
            }
        }

        return toReturn;
    }

    /* Adapted from Kevin's CAnimatorUtil. Using setTranslationX instead of setX. */
    public void wiggle(long duration, int repetition, long delay, float magnitude) {

        float currTranslation = getTranslationX();
        float offset = magnitude*getWidth();
        float[] pts = {currTranslation, offset + currTranslation, currTranslation,
                currTranslation-offset, currTranslation};

        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "translationX", pts);
        anim.setDuration(duration);
        anim.setRepeatCount(repetition);
        anim.setStartDelay(delay);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();

    }

    /**
     * Adjusts the size of the DotBag to hold all dots
     */
    private void resetBounds() {

        int rowsToUse = (rows == 0)?1:rows; // to enable drawing of zero dotbag

        bounds.set(borderWidth, borderWidth, size*(cols+1) - borderWidth, rowsToUse*size - borderWidth);

    }

    /**
     * Sets "isClickable" var of DotBag and all of its Dot children to _isClickable.
     *
     * @param _isClickable
     */
    public void setIsClickable(boolean _isClickable) {

        TableRow currTableRow;

        this.isClickable = _isClickable;

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);

            for (int j = 0; j < currTableRow.getVirtualChildCount(); j++){
                CCount_Dot dot = (CCount_Dot) currTableRow.getVirtualChildAt(j);
                dot.setIsClickable(isClickable);
            }
        }
    }

    /**
     * Changes hollowness of DotBag and all of its Dot children.
     *
     * @param _isHollow
     */
    public void setHollow(boolean _isHollow) {

        TableRow currTableRow;

        this.isHollow = _isHollow;

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);

            for (int j = 0; j < currTableRow.getVirtualChildCount(); j++){
                CCount_Dot dot = (CCount_Dot) currTableRow.getVirtualChildAt(j);
                dot.setHollow(isHollow);
            }
        }

    }

    public void setSize(int _size) {this.size = _size;}

    public void setRight(float newRight) {

        bounds.right = newRight;
        invalidate();

    }

    public void setDrawBorder(boolean _drawBorder) {this.drawBorder = _drawBorder;}

    public String getImageName() {
        return imageName;
    }
    public int getSize() {return this.size; }
    public int getRows(){ return this.rows;}
    public int getCols(){ return this.cols;}
    public boolean getDrawBorder() {
        return drawBorder;
    }

    public TableRow getRow(int index) {return allTableRows.get(index); }

    public RectF getBounds() {return this.bounds; }

    public CCount_Dot getDot(int row, int col) {
        return (CCount_Dot) allTableRows.get(row).getVirtualChildAt(col);
    }

    public boolean getIsClicked(){

        if (isClicked) {
            isClicked = false; // reset
            return true;
        }
        else {
            return false;
        }
    }

    public void updateSize() {
        size = (int)(COUNT_CONST.textBoxHeight * scale);
    }

    private void updateRows() {this.rows = allTableRows.size(); }

    private void updateCols() {
        int currCols;
        int maxCols = 0;
        TableRow currTableRow;

        for (int i = 0; i < this.rows; i++) {

            currTableRow = allTableRows.get(i);
            currCols = currTableRow.getVirtualChildCount();
            maxCols = (currCols > maxCols)?currCols:maxCols;
        }

        this.cols = maxCols;

    }

    public boolean dotsStatic() {

        TableRow currTableRow;
        CCount_Dot dot;

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);

            for (int j = 0; j < currTableRow.getVirtualChildCount(); j++){
                dot = (CCount_Dot) currTableRow.getVirtualChildAt(j);
                if (dot.getTranslationX() != 0) {
                    return false;
                };
            }
        }

        return true;

    }

    private void resetDotTranslations() {

        TableRow currTableRow;
        CCount_Dot dot;

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);

            for (int j = 0; j < currTableRow.getVirtualChildCount(); j++){
                dot = (CCount_Dot) currTableRow.getVirtualChildAt(j);
                dot.setTranslationX(0);
                dot.setTranslationY(0);

            }
        }
    }

    public void resetOverflowNum() {
        this.overflowNum = 0;
    }

    public void addOverflowNum() {
        this.overflowNum++;
    }

    public int getOverflowNum() {
        return this.overflowNum;
    }

    public void setIsisAudible(boolean isAudible){
        this.isAudible = isAudible;
    }

    public void copyFrom(CCount_DotBag _dotBag) {
        this.drawBorder = _dotBag.drawBorder;
        this.imageName = _dotBag.imageName;
        setRows(_dotBag.getRows());
        setCols(_dotBag.getCols());

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (_dotBag.getDot(i, j) == null || _dotBag.getDot(i, j).getVisibility() == INVISIBLE)
                    getDot(i, j).setVisibility(INVISIBLE);
            }
        }
    }
}
