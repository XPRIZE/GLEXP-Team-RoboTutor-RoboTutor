package cmu.xprize.comp_counting;

import android.widget.TextView;

/**
 * Created by kevindeland on 10/31/17.
 */

interface ICountMechanic {


    /**
     * Populates the view based on the data.
     *
     * @param data
     */
    public void populateView(CCount_Data data);

    public TextView getGoalNumberView();
}
