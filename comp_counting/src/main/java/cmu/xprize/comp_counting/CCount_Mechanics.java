package cmu.xprize.comp_counting;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

import cmu.xprize.util.TCONST;

/**
 * Created by kevindeland on 10/31/17.
 */

public class CCount_Mechanics implements ICountMechanic {

    protected Context mContext;
    protected CCount_Component mComponent;
    protected PercentRelativeLayout mParent;

    protected CCount_Data _currData;

    // at the top
    protected int goalNumber;
    protected TextView goalNumberView;

    // count table and its children
    protected TableLayout countTable;

    protected ArrayList<TableRow> tableRows;

    // ones row
    protected TextView textOnes;
    protected CCount_Increment incOnes;
    protected CCount_Decrement decOnes;
    protected CCount_DotBag dotBagOnes;
    protected ViewGroup dotOnesHolder;

    // tens row
    protected TextView textTens;
    protected CCount_Increment incTens;
    protected CCount_Decrement decTens;
    protected CCount_DotBag dotBagTens;
    protected View dotsTensPlaceholder;

    // hundreds row
    protected TextView textHuns;
    protected CCount_Increment incHuns;
    protected CCount_Decrement decHuns;
    protected CCount_DotBag dotBagHuns;
    protected View dotsHunsPlaceholder;

    // sum
    private int startingNumber;
    private int currentNumber;
    protected TextView currentNumberView;

    public TextView getGoalNumberView() {
        return goalNumberView;
    }



    public CCount_Mechanics(Context context, CCount_Component parent) {

        mContext = context;
        mComponent = parent;
        mParent = parent.getContainer();
    }


    /**
     * Populates the view based on data.
     * @param data
     */
    @Override
    public void populateView(CCount_Data data) {

        _currData = data;

        //mComponent.removeAllViews();
        initializeVariables();

        Log.d(TCONST.COUNTING_DEBUG_LOG, "selected layout=" + data.layout);

        switch(data.layout) {
            case COUNT_CONST.DEFAULT_LAYOUT:
                Log.d(TCONST.COUNTING_DEBUG_LOG, "selected default");

                // set visibilities
                goalNumberView.setVisibility(View.VISIBLE);
                countTable.setVisibility(View.VISIBLE);
                //tableRows.get(0).setVisibility(View.INVISIBLE);
                //tableRows.get(1).setVisibility(View.INVISIBLE);

                // goal View
                startingNumber = data.dataset[0];
                currentNumber = startingNumber;
                goalNumber = data.dataset[1];
                Log.d(TCONST.COUNTING_DEBUG_LOG, "setting target to " + goalNumber);
                goalNumberView.setText("" + goalNumber);

                textOnes.setText("" + (startingNumber % 10));
                currentNumberView.setText("" + startingNumber);


                initializeDotBags();
                initializeOnTapListeners();

                break;
        }
    }

    private void initializeVariables() {

        countTable = (TableLayout) mParent.findViewById(R.id.STable);

        tableRows = new ArrayList<>(5);

        goalNumberView = (TextView) mParent.findViewById(R.id.SGoalText);

        // hundreds
        tableRows.add(0, (TableRow) mParent.findViewById(R.id.SRowHuns));
        incHuns = (CCount_Increment) mParent.findViewById(R.id.SIncHuns);
        decHuns = (CCount_Decrement) mParent.findViewById(R.id.SDecHuns);
        // TODO

        // tens
        tableRows.add(1, (TableRow) mParent.findViewById(R.id.SRowTens));
        // TODO


        // ones
        tableRows.add(2, (TableRow) mParent.findViewById(R.id.SRowOnes));
        textOnes = (TextView) mParent.findViewById(R.id.STextOnes);
        incOnes = (CCount_Increment) mParent.findViewById(R.id.SIncOnes);
        decOnes = (CCount_Decrement) mParent.findViewById(R.id.SDecOnes);

        tableRows.add(3, (TableRow) mParent.findViewById(R.id.SRowLine));
        tableRows.add(4, (TableRow) mParent.findViewById(R.id.SRowSum));
        // TODO initialize the other variables


        currentNumberView = (TextView) mParent.findViewById(R.id.STextSum);

    }

    private void initializeDotBags() {

        // ones
        dotBagOnes = new CCount_DotBag(mContext);
        dotOnesHolder = (ViewGroup) mParent.findViewById(R.id.SDotBagOnes);
        dotOnesHolder.removeAllViews();
        dotOnesHolder.addView(dotBagOnes);

        dotBagOnes.setRows(1);
        dotBagOnes.setCols(startingNumber);
        dotBagOnes.setImage("token_1");
    }

    private void initializeOnTapListeners() {

        incOnes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO make look clickable

                Log.d(TCONST.COUNTING_DEBUG_LOG, "Increment Tapped in Mechanics: " + currentNumber);
                dotBagOnes.addDot(0, currentNumber++);
                currentNumberView.setText("" + currentNumber);
                textOnes.setText("" + (currentNumber % 10));


                if (currentNumber == goalNumber) {
                    mComponent.applyBehavior(COUNT_CONST.DONE_COUNTING_TOUCH);
                }
                return false;
            }
        });
    }



}
