package cmu.xprize.comp_nd.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cmu.xprize.comp_nd.CNd_Component;
import cmu.xprize.comp_nd.R;
import cmu.xprize.util.MathUtil;

/**
 * CNd_LayoutManager_BaseTen
 *
 * Uses a Constraint Layout (nd_layout) to display BaseTen blocks as scaffolding.
 *
 * <p>
 * Created by kevindeland on 7/17/18.
 */

public class CNd_LayoutManager_BaseTen implements CNd_LayoutManagerInterface {

    CNd_Component _component;
    Context _context;

    public CNd_LayoutManager_BaseTen(CNd_Component component, Context context) {
        this._component = component;
        this._context = context;
    }


    @Override
    public void initialize() {
        CNd_Component.inflate(_context, R.layout.nd_layout_2, _component);
    }

    @Override
    public void resetView() {
        _component.setVisibility(CNd_Component.GONE);
    }

    @Override
    public void displayDigits(int left, int right) {

        // set left number
        ((TextView) _component.findViewById(R.id.symbol_left_hun)).setText(String.valueOf(MathUtil.getHunsDigit(left)));
        ((TextView) _component.findViewById(R.id.symbol_left_ten)).setText(String.valueOf(MathUtil.getTensDigit(left)));
        ((TextView) _component.findViewById(R.id.symbol_left_one)).setText(String.valueOf(MathUtil.getOnesDigit(left)));

        // set right number
        ((TextView) _component.findViewById(R.id.symbol_right_hun)).setText(String.valueOf(MathUtil.getHunsDigit(right)));
        ((TextView) _component.findViewById(R.id.symbol_right_ten)).setText(String.valueOf(MathUtil.getTensDigit(right)));
        ((TextView) _component.findViewById(R.id.symbol_right_one)).setText(String.valueOf(MathUtil.getOnesDigit(right)));
    }

    @Override
    public void displayConcreteRepresentations(int left, int right) {

        displayConcrete("left", left);
        displayConcrete("right", right);
    }

    /**
     * Display concrete number representations
     * @param numberLoc either left or right.
     * @param numberValue number value to display
     */
    private void displayConcrete(String numberLoc, int numberValue) {


        ImageView[] ndHuns = new ImageView[10];
        ImageView[] ndTens = new ImageView[10];
        ImageView[] ndOnes = new ImageView[10];

        int hunsDigit, tensDigit, onesDigit;


        hunsDigit = MathUtil.getHunsDigit(numberValue);
        for (int i=1; i < ndHuns.length; i++) {

            ndHuns[i] = getBaseTenConcreteUnitView(numberLoc, "hun", i);
            ndHuns[i].setVisibility( i <= hunsDigit ? View.VISIBLE : View.GONE); // only show first N, N=hunsDigit
        }


        tensDigit = MathUtil.getTensDigit(numberValue);
        for(int i = 1; i < ndTens.length; i++) {

            ndTens[i] = getBaseTenConcreteUnitView(numberLoc, "ten", i);
            ndTens[i].setVisibility( i <= tensDigit ? View.VISIBLE : View.GONE); // only show first N, N=tensDigit
        }

        onesDigit = MathUtil.getOnesDigit(numberValue);
        for (int i = 1; i < ndOnes.length; i++) {
            ndOnes[i] = getBaseTenConcreteUnitView(numberLoc, "one", i);
            ndOnes[i].setVisibility( i <= onesDigit ? View.VISIBLE : View.GONE); // only show first N, N=onesDigit
        }

    }

    // ------------
    // VIEW HELPER
    // ------------
    /**
     * Helper function to return a View of a Concrete Representation by its id.
     *
     * "top_ten_1" will be the first ten in the top number
     * or "left_hun_4" will be the 4th hundred in the left number
     *
     * @param numberLoc top, bottom, left, right, etc
     * @param digit one, ten, hun
     * @param value the index of the thing
     * @return The ImageView of the concrete representation
     */
    private ImageView getBaseTenConcreteUnitView(String numberLoc, String digit, int value) {

        String viewId = numberLoc + "_" + digit + "_" + value;
        int resID = _context.getResources().getIdentifier(viewId, "id", _context.getPackageName());

        return (ImageView) _component.findViewById(resID);
    }



    @Override
    public void enableChooseNumber(boolean enable) {

        View chooseLeft = _component.findViewById(R.id.symbol_left_num);
        chooseLeft.setOnClickListener(enable ? new ChooseListener("left"): null);

        View chooseRight = _component.findViewById(R.id.symbol_right_num);
        chooseRight.setOnClickListener(enable ? new ChooseListener("right"): null);
    }

    /**
     * private ClickListener which sends student response back the the Component.
     */
    private class ChooseListener implements View.OnClickListener {

        String _choice; // "left" or "right"
        ChooseListener(String choice) {
            this._choice = choice;
        }

        @Override
        public void onClick(View view) {
            _component.registerStudentChoice(_choice);
            _component.applyBehavior("SELECT_ANSWER_EVENT");
        }
    }


}
