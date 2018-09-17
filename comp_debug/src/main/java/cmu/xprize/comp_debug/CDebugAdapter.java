package cmu.xprize.comp_debug;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.util.CAt_Data;

import static cmu.xprize.comp_debug.CD_CONST.STATE_CURRENT;
import static cmu.xprize.comp_debug.CD_CONST.STATE_EASIER;
import static cmu.xprize.comp_debug.CD_CONST.STATE_HARDER;
import static cmu.xprize.comp_debug.CD_CONST.STATE_NEXT;
import static cmu.xprize.comp_debug.CD_CONST.STATE_NORMAL;
import static cmu.xprize.comp_debug.CD_CONST.STATE_NULL;
import static cmu.xprize.util.TCONST.QGRAPH_MSG;


public class CDebugAdapter extends BaseAdapter {

    private Context          mContext;
    private IDebugLauncher   mLauncher;

    private String  initialTutor;
    private String  currentTutor;

    private int   currentIndex;
    private int   nextIndex;
    private int   harderIndex;
    private int   easierIndex;

    CAt_Data currentTransition;

    private String currentID;
    private String nextID;
    private String harderID;
    private String easierID;

    private HashMap<String, CAt_Data>  transitionMap;
    private HashMap<Integer, CAt_Data> indexTransitionMap;

    private HashMap<Integer, CDebugButton> buttonMap;

    private int tableRows = 1;
    private int tableCols = 1;
    private int gridViewRows = 1;
    private int gridViewCols = 1;


    private double dRows  = 1;
    private double dCols  = 1;


    static private String TAG = "CDebugAdapter";

    // Constructor
    public CDebugAdapter(Context _context, String _activeTutor, HashMap _transitionMap, IDebugLauncher  _launcher) {

        mContext      = _context;
        mLauncher     = _launcher;
        initialTutor  = _activeTutor;
        currentTutor  = _activeTutor;
        transitionMap = _transitionMap;

        buttonMap          = new HashMap();
        indexTransitionMap = new HashMap();

        updateTransitionsByName(_activeTutor);

        calcDimensions();
    }


    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }


    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }


    public int  getGridColumnCount() {
        return tableRows;
    }

    public void updateTransitionsByName(String _activeTutor) {

        currentTransition = transitionMap.get(_activeTutor);

        currentID = currentTransition.tutor_id;
        nextID    = currentTransition.next;
        harderID  = currentTransition.harder;
        easierID  = currentTransition.easier;

    }


    public void udpateVectorIndices() {

        currentIndex = transitionMap.get(currentID).gridIndex;
        nextIndex    = transitionMap.get(nextID).gridIndex;
        harderIndex  = transitionMap.get(harderID).gridIndex;
        easierIndex  = transitionMap.get(easierID).gridIndex;
    }


    public void calcDimensions() {

        Iterator<?> tObjects = transitionMap.entrySet().iterator();

        while (tObjects.hasNext()) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            CAt_Data vector = (CAt_Data) entry.getValue();

            tableRows = (vector.row > tableRows) ? vector.row : tableRows;
            tableCols = (vector.col > tableCols) ? vector.col : tableCols;
        }

        // NOTE: We populate the grid view in a flipped format with table rows in grid columns
        // and viceversa. This was due to the fact that the GridView does not support horizontal
        // scrolling.  So the Levels are in the columns and the incremental difficulty within levels
        // are in the rows. i.e. as you go across horizontally you go up/down in grade
        // as you go vertically you get harder and erasier within a grade level.
        //
        // NOTE: The adapter works in one dimensional linear offsets NOT in two dimensional rows/cols
        // so we have to translate between the row/col of the transition table to the flipped
        // col/row linear offset where in untransformed gridviews - Index = row_ndx * numcols + col_ndx.
        // using zero-based indices.
        //
        gridViewRows = tableCols;
        gridViewCols = tableRows;

        tObjects = transitionMap.entrySet().iterator();

        while (tObjects.hasNext()) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            CAt_Data tableVector = (CAt_Data) entry.getValue();

            // So if the 3row/5col transition table looks like this
            //
            //   A  B  C  D  E
            //   F  G  H  I  J
            //   K  L  M  N  O
            //
            //   Those elements go into the gridview like this:
            //   with ordingal indices on the right
            //   A  F  K          0  1  2
            //   B  G  L          3  4  5
            //   C  H  M          6  7  8
            //   D  I  N          9  10 11
            //   E  J  O          12 13 14
            //
            // So the indices relative to the transition table are like this
            //
            //   A  B  C  D  E     0  3  6  9   12
            //   F  G  H  I  J     1  4  7  10  13
            //   K  L  M  N  O     2  5  8  11  14
            //
            // i.e A=0 B=3 etc

            // Put the gridIndex in the transition vector itself for later ref
            //
            tableVector.gridIndex = (tableVector.col - 1) * gridViewCols + (tableVector.row - 1);

            // Provide an ordinal gridIndex for use by adapter
            //
            indexTransitionMap.put(tableVector.gridIndex, tableVector);
        }

        udpateVectorIndices();
    }


    public int getCount() {

        return tableCols * tableRows;
    }


    public Object getItem(int position) {

        CAt_Data found = null;

        if(indexTransitionMap.containsKey(position)) {

            found = indexTransitionMap.get(position);
        }

//        Log.d(TAG, "position: " + position + " - object: " + found);

        return found;
    }


    /**
     * Get the row id associated with the gridIndex
     *
     * @param position
     * @return
     */
    public long getItemId(int position) {

        int row = 0;

        CAt_Data item = (CAt_Data) getItem(position);

        // return the zero base row gridIndex - Note that our transition table is flipped
        // row = cols
        //
        row = position / gridViewCols;

//        Log.d(TAG, "position: " + position + " - on row: " + row);

        return row;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    private String mapKeyState(int position) {

        String buttonState;

        // BOJACK where button states are assigned
        if(indexTransitionMap.containsKey(position)) {

            if(position == currentIndex)
                buttonState = STATE_CURRENT;

            else if(position == nextIndex)
                buttonState = STATE_NEXT;

            else if(position == harderIndex)
                buttonState = STATE_HARDER;

            else if(position == easierIndex)
                buttonState = STATE_EASIER;

            else
                buttonState = STATE_NORMAL;
        }
        else {
            buttonState = STATE_NULL;
        }

//        Log.d(TAG, "Mapped KeyState: " + position + " - in State: " + buttonState);

        return buttonState;
    }


    // BOJACK gets tutor data
    private CAt_Data mapIndexToTutorData(int gridPosition) {

        if(indexTransitionMap.containsKey(gridPosition)) {
            return indexTransitionMap.get(gridPosition);
        }

        return null;

    }


    // create a new ImageView for each item referenced by the Adapter
    //
    // BOJACK where is this called from?
    public View getView(int gridPosition, View convertView, ViewGroup parent) {

        CDebugButton tutorSelector;
        // BOJACK this might be useful to set the button's tutor
        Log.d("BOJACK", "CDebugAdapter.getView(" + gridPosition +")");
        Log.wtf("BOJACK", "CDebugAdapter" + " -- getView -- " + gridPosition);

        String       buttonState = mapKeyState(gridPosition);
        CAt_Data buttonTutorData = mapIndexToTutorData(gridPosition);

//        Log.d(TAG, "GetView: " + gridPosition + " - convertible: " + convertView);

        if((gridPosition == 0)) {

            if(buttonMap.containsKey(0)) {

                tutorSelector = buttonMap.get(0);

                buttonMap.remove(tutorSelector);

                Log.d(TAG, "GetView: Found 0-debug button: " + tutorSelector + " - convertible: " + convertView);

                mLauncher.invalidate();
            }
            else {
                Log.d(TAG, "GetView: No Map 0-debug button: " + " - convertible: " + convertView);
            }
        }


        if (convertView == null) {
            // BOJACK this is where the buttons are created
            tutorSelector = new CDebugButton(mContext);
            Log.v("BOJACK", "Creating new CDebugButton");
            Log.wtf("BOJACK", tutorSelector.hashCode() + " -- Constructor -- " + gridPosition);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tutorSelector.setImageDrawable(mContext.getResources().getDrawable(R.drawable.debugbutton, null)); // BOJACK setting image drawable...
            } else {
                tutorSelector.setImageDrawable(mContext.getResources().getDrawable(R.drawable.debugbutton)); // BOJACK setting image drawable...
            }
            tutorSelector.setTutorData(buttonTutorData);
            tutorSelector.setLayoutParams(new GridView.LayoutParams(130, 130)); // BOJACK adjust these
            tutorSelector.setScaleType(CDebugButton.ScaleType.FIT_CENTER);
            tutorSelector.setPadding(12, 12, 12, 12);
            tutorSelector.setGridPosition(gridPosition);
            tutorSelector.setState(buttonState);
            tutorSelector.refreshDrawableState(); // BOJACK refreshes
        }
        else
        {
            tutorSelector = (CDebugButton) convertView;
            Log.wtf("BOJACK", tutorSelector.hashCode() + " -- ConvertView -- " + gridPosition);

            buttonMap.remove(tutorSelector.getGridPosition());

            tutorSelector.setTutorData(buttonTutorData);
            tutorSelector.setGridPosition(gridPosition);
            tutorSelector.setState(buttonState);
            tutorSelector.refreshDrawableState(); // BOJACK refreshes

            Log.d(TAG, "GetView: reusing debug button");
        }

        Log.wtf("BOJACK", "buttonMap size = " + buttonMap.size());

        // Keep track of the buttons so we can invalidate them if we change their state
        //

        Log.wtf("BOJACK", "CDebugAdapter" + " -- puttingButtonMap -- " + gridPosition);
        buttonMap.put(gridPosition, tutorSelector);

        if(buttonState.equals(STATE_NULL)) {
            tutorSelector.setEnabled(false);
        }
        else {
            tutorSelector.setEnabled(true);
        }

        Log.v(TAG, "GetView: " + gridPosition + " - new buttonMap: " + tutorSelector);

        tutorSelector.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

            int          gridPosition;
            CDebugButton tutorSelector;

            tutorSelector = (CDebugButton)view;
            gridPosition  = tutorSelector.getGridPosition();

            Log.v(QGRAPH_MSG, "event.click: " + " CDebugAdapter:Click on item: " + gridPosition);

            String tutorName = "";

            tutorName = updateCurrentTutorByIndex(gridPosition);

            if(!tutorName.equals("")) {
                mLauncher.changeCurrentTutor(tutorName, null);
            }

            }
        });

        return tutorSelector;
    }



    // BOJACK where the click updates
    public String updateCurrentTutorByIndex(int gridPosition) {

        String currentTutorName = "";

        // If we are switching the "current" tutor selection update the button states
        //
        if(gridPosition != currentIndex) {

            // reset indices
            setButtonSafeState(currentIndex, STATE_NORMAL);
            setButtonSafeState(nextIndex,    STATE_NORMAL);
            setButtonSafeState(harderIndex,  STATE_NORMAL);
            setButtonSafeState(easierIndex,  STATE_NORMAL);

            // Update the transitions based on the newly selected "current" tutor
            //
            currentTutorName = indexTransitionMap.get(gridPosition).tutor_id;

            updateTransitionsByName(currentTutorName);

            udpateVectorIndices();

            setButtonSafeState(easierIndex,  STATE_EASIER);
            setButtonSafeState(harderIndex,  STATE_HARDER);
            setButtonSafeState(nextIndex,    STATE_NEXT);
            setButtonSafeState(currentIndex, STATE_CURRENT);
        }

        return currentTutorName;
    }


    private void setButtonSafeState(int gridPosition, String newState) {

        if(buttonMap.containsKey(gridPosition)) {


            CDebugButton debugButton = buttonMap.get(gridPosition);
            debugButton.setState(newState);
            debugButton.refreshDrawableState(); // BOJACK refreshes
        }
    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }


    @Override
    public int getViewTypeCount() {
        return 1;
    }


    @Override
    public boolean isEmpty() {
        return false;
    }


    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }


    @Override
    public boolean isEnabled(int position) {
        return true;
    }
}

