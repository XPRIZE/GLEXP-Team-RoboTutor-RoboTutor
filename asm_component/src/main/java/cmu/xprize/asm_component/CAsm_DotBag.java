package cmu.xprize.asm_component;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;


/**
 *
 */
public class CAsm_DotBag extends TableLayout {

    private Context context;


    final float scale = getResources().getDisplayMetrics().density;

    private int rows = 0;
    private int cols = 0;
    private int size = (int)(ASM_CONST.textBoxHeight*scale);

    private boolean isClickable = false;
    private boolean isClicked = false;
    private boolean isHollow = false;

    private boolean drawBorder = true;

    private String imageName = "star"; // default

    private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);


    private ArrayList<TableRow> allTableRows = new ArrayList<TableRow>();

    Paint borderPaint = new Paint();
    int borderWidth = (int)(ASM_CONST.borderWidth * scale);
    RectF bounds = new RectF();


    public CAsm_DotBag(Context context, AttributeSet attributeSet) {

        super(context, attributeSet);
        this.context = context;
        init();

    }

    public CAsm_DotBag(Context context) {

        super(context);
        this.context = context;
        init();

    }


    private void init() {

        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);
        setOnClickListener(clickListener);
        setPaint();
        setZero();

    }


    public void update(int _rows, int _cols, String _imageName, boolean clickable) {

        TableRow currTableRow;

        int deltaRows = _rows - this.rows;
        int deltaCols = _cols - this.cols;

        this.imageName = _imageName;
        this.isClickable = clickable;
        this.isHollow = false;

        if (_rows == 0 || _cols == 0) {
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
            for (int i = this.rows; i < _rows; i++) {
                for (int j = 0; j < this.cols; j++) {
                    addDot(i, j);
                }
            }
        }

        if (deltaCols < 0) {
            for (int i = 0; i < this.rows; i++) {
                currTableRow = allTableRows.get(i);

                for (int j = this.cols - 1; j >= _cols; j-- ) {
                    currTableRow.removeViewAt(j);
                }
            }
        }

        else if (deltaCols > 0) {
            for (int i = 0; i < _rows; i++) {
                for (int j = this.cols; j < _cols; j++ ) {
                    addDot(i, j);
                }
            }
        }

        this.rows = _rows;
        this.cols = _cols;

        resetDots();
        resetBounds();

    }

    public void setZero() {

        rows = 0;
        cols = 0;
        removeAllViews();
        allTableRows.clear();
        params.width = size;
        params.height = size;
        setLayoutParams(params);
        resetBounds();

    }

    public void resetDots() {

        TableRow currTableRow;

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);

            for (int j = 0; j < currTableRow.getVirtualChildCount(); j++){

                CAsm_Dot dot = (CAsm_Dot) currTableRow.getVirtualChildAt(j);

                dot.setImageName(imageName);
                dot.setHollow(isHollow);
                dot.setIsClickable(isClickable);
            }
        }
    }

    public CAsm_Dot addDot(int row, int col) {

        while (allTableRows.size() < row + 1) {
            addRow(allTableRows.size());
        }
        TableRow tableRow = allTableRows.get(row);

        CAsm_Dot dot = new CAsm_Dot(context);
        dot.setParams(isClickable, imageName, row, col);
        dot.setLayoutParams(new TableRow.LayoutParams(size, size));

        tableRow.addView(dot, col);

        updateRows();
        updateCols();
        resetBounds();

        return dot;
    }

    private TableRow addRow(int index) {

        if (allTableRows.size() == 0) {
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            setLayoutParams(params);
        }

        TableRow tableRow = new TableRow(context);

        CAsm_DotBag.LayoutParams lp = new CAsm_DotBag.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
        }

        resetBounds();

    }

    private OnClickListener clickListener = new OnClickListener(){
        public void onClick(View v) {

            if (isClickable) {
                isClicked = true;
            }

            View Component = (View) v.getParent().getParent();
            Component.performClick();
        }
    };



    public CAsm_Dot findClickedDot() {

        TableRow currTableRow;
        CAsm_Dot currDot;

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);

            for (int j = 0; j < currTableRow.getVirtualChildCount(); j++) {
                currDot = (CAsm_Dot) currTableRow.getVirtualChildAt(j);
                if (currDot.getIsClicked()) {
                    return currDot;
                }
            }
        }
        return null;
    }


    public void removeDot(CAsm_Dot toRemove) {

        int row = toRemove.getRow();

        allTableRows.get(row).removeView(toRemove);
        removeView(toRemove);

        updateRows();
        updateCols();

        resetBounds();
    }

    public ArrayList<CAsm_Dot> getVisibleDots(){

        TableRow currTableRow;

        ArrayList<CAsm_Dot> toReturn = new ArrayList<>();

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);

            for (int j = 0; j < currTableRow.getVirtualChildCount(); j++){
                CAsm_Dot dot = (CAsm_Dot) currTableRow.getVirtualChildAt(j);

                if (dot.getVisibility() == VISIBLE) {
                    toReturn.add(dot);
                }
            }
        }

        return toReturn;
    }

    public void removeInvisibleDots(){

        TableRow currTableRow;
        int numChildren;

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);
            numChildren = currTableRow.getVirtualChildCount();
            for (int j = numChildren - 1; j >= 0; j--){
                CAsm_Dot dot = (CAsm_Dot) currTableRow.getVirtualChildAt(j);

                if (dot.getVisibility() == INVISIBLE) {
                    removeDot(dot);
                }
            }
        }

        // now reset dot col index

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);
            numChildren = currTableRow.getVirtualChildCount();
            for (int j = 0; j < numChildren; j++){
                CAsm_Dot dot = (CAsm_Dot) currTableRow.getVirtualChildAt(j);
                dot.setCol(j);
            }
        }

    }
    /* Adapted from Kevin's CAnimatorUtil. Using translationX instead of X. */
    public void wiggle(long duration, int repetition, long delay, float magnitude) {

        float offset = magnitude*getWidth();
        float[] pts = {0, offset, 0, -offset, 0};
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "translationX", pts);
        anim.setDuration(duration);
        anim.setRepeatCount(repetition);
        anim.setStartDelay(delay);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }

    private void resetBounds() {

        int rowsToUse = (rows == 0)?1:rows; // to enable drawing of zero dotbag

        bounds.set(borderWidth, borderWidth, size*(cols+1) - borderWidth, rowsToUse*size - borderWidth);

    }

    public void setIsClickable(boolean _isClickable) {

        TableRow currTableRow;

        this.isClickable = _isClickable;

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);

            for (int j = 0; j < currTableRow.getVirtualChildCount(); j++){
                CAsm_Dot dot = (CAsm_Dot) currTableRow.getVirtualChildAt(j);
                dot.setIsClickable(isClickable);
            }
        }
    }

    public void setHollow(boolean _isHollow) {

        TableRow currTableRow;

        this.isHollow = _isHollow;

        for (int i = 0; i < allTableRows.size(); i++) {
            currTableRow = allTableRows.get(i);

            for (int j = 0; j < currTableRow.getVirtualChildCount(); j++){
                CAsm_Dot dot = (CAsm_Dot) currTableRow.getVirtualChildAt(j);
                dot.setHollow(isHollow);
            }
        }

    }

    public void setRows(int _rows) {this.rows = _rows;}
    public void setCols(int _cols) {
        this.cols = _cols;
    }
    public void setSize(int _size) {this.size = size;}

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

    public TableRow getRow(int index) {return allTableRows.get(index); }

    public RectF getBounds() {return this.bounds; }

    public CAsm_Dot getDot(int row, int col) {
        return (CAsm_Dot) allTableRows.get(row).getVirtualChildAt(col);
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



}
