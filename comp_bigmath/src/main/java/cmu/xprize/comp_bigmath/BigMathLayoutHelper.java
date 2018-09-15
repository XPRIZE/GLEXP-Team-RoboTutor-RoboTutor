package cmu.xprize.comp_bigmath;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * BigMathLayoutHelper
 * This class was designed as an interface to directly access views in the "layout.xml" file.
 * Here, the rules are defined for how each View is named.
 *
 * <p>
 * Created by kevindeland on 8/6/18.
 */

class BigMathLayoutHelper {

    private Context _activity;
    private ViewGroup _viewGroup;

    BigMathLayoutHelper(Context activity, ViewGroup viewGroup) {
        _activity = activity;
        _viewGroup = viewGroup;
    }


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
    MovableImageView getBaseTenConcreteUnitView(String numberLoc, String digit, int value) {
        return getBaseTenConcreteUnitView(numberLoc, digit, value, false);
    }

    /**
     * Helper function to return a View of a Concrete Representation by its id.
     *
     * "top_ten_1" will be the first ten in the top number
     * or "left_hun_4" will be the 4th hundred in the left number
     *
     * @param numberLoc top, bottom, left, right, etc
     * @param digit one, ten, hun
     * @param value the index of the thing
     * @param isHelper is it an animation helper view?
     * @return The ImageView of the concrete representation
     */
    MovableImageView getBaseTenConcreteUnitView(String numberLoc, String digit, int value, boolean isHelper) {

        if ((digit.equals("hun") || digit.equals("ten") || digit.equals("one"))
                && (!numberLoc.equals("carry"))) {
            String parentId = numberLoc + "_" + digit + "s";
            // the result hundreds row has an extra row
            if(digit.equals("hun") && numberLoc.equals("result")) {
                parentId += "_row";
                if(value <= 5) {
                    parentId += "1"; // result_huns_row1
                } else if(value <= 10) {
                    parentId += "2"; // result_huns_row2
                    value -= 5; // adjust value index to be [1,5]
                }
            }
            // check for helpers
            if(isHelper) {
                parentId += "_helpers";
            }
            Log.v("GET_RESOURCE", parentId);
            int parentResID = _activity.getResources().getIdentifier(parentId, "id", _activity.getPackageName());
            ViewGroup parentView = (ViewGroup) _viewGroup.findViewById(parentResID);

            String childId = digit + "_" + value;
            Log.v("GET_RESOURCE", childId);
            int resID = _activity.getResources().getIdentifier(childId, "id", _activity.getPackageName());
            return (MovableImageView) parentView.findViewById(resID);


        } else {
            String viewId = numberLoc + "_" + digit + "_" + value;
            if(isHelper) {
                viewId += "_helper";
            }
            Log.v("GET_RESOURCE", viewId);
            int resID = _activity.getResources().getIdentifier(viewId, "id", _activity.getPackageName());

            return (MovableImageView) _viewGroup.findViewById(resID);
        }

    }

    /**
     * Returns a TextView displaying a digit.
     *
     * @param numberLoc top, bottom, left, right, etc
     * @param digit one, ten, hun
     * @return The TextView of the digit
     */
    TextView getBaseTenDigitView(String numberLoc, String digit) {

        String viewId = "symbol_" + numberLoc + "_" + digit;
        int resID = _activity.getResources().getIdentifier(viewId, "id", _activity.getPackageName());

        return (TextView) _viewGroup.findViewById(resID);
    }

    /**
     * Get one of the two ten carry digits
     * @param digit
     * @return
     */
    TextView getCarryDigitView(String digit) {
        String viewId = "symbol_carry_" + digit;
        int resID = _activity.getResources().getIdentifier(viewId, "id", _activity.getPackageName());

        return (TextView) _viewGroup.findViewById(resID);
    }

    /**
     * Returns the View that contains the concrete representations.
     * @param numberLoc opA, opB, result, etc
     * @param digit one, ten, hun
     * @return
     */
    View getContainingBox(String numberLoc, String digit) {

        String viewId = digit + "_" + numberLoc + "_box";
        int resID = _activity.getResources().getIdentifier(viewId, "id", _activity.getPackageName());

        return _viewGroup.findViewById(resID);
    }

    /**
     * Get the carry unit.
     *
     * @param digit ten or hun
     * @return
     */
    MovableImageView getCarryConcreteUnitView(String digit) {

        String viewId = "carry_" + digit;
        int resID = _activity.getResources().getIdentifier(viewId, "id", _activity.getPackageName());
        return (MovableImageView) _viewGroup.findViewById(resID);
    }

    MovableImageView getBorrowConcreteUnitView(String digit, int i) {
        int parentResID = _activity.getResources().getIdentifier("borrow_" + digit + "s", "id", _activity.getPackageName());
        ViewGroup parentView = (ViewGroup) _viewGroup.findViewById(parentResID);

        String childId = digit + "_" + i;
        int resID = _activity.getResources().getIdentifier(childId, "id", _activity.getPackageName());
        return (MovableImageView) parentView.findViewById(resID);
    }
}
