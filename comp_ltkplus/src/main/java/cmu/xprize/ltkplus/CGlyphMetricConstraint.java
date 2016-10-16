//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

package cmu.xprize.ltkplus;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class CGlyphMetricConstraint {

    private float XConst;
    private float WConst;
    private float YConst;
    private float HConst;
    private float aspRConst;

    private float VisConst;
    private float ErrConst;

    public CGlyphMetricConstraint() {

        XConst = Float.MAX_VALUE;
        WConst = Float.MAX_VALUE;
        YConst = Float.MAX_VALUE;
        HConst = Float.MAX_VALUE;
        aspRConst = Float.MAX_VALUE;

        VisConst = Float.MAX_VALUE;
        ErrConst = Float.MAX_VALUE;
    }

    public void setConstraint(String constraint) {

        // We demand a parm list of the form X:0.100|A:0.234| ...
        //
        List<String> parmList = Arrays.asList(constraint.split(";"));

        for (String element : parmList) {

            String[] constr = element.split(",");

            switch (constr[0]) {
                case GCONST.X_CONSTR:
                    XConst = Float.parseFloat(constr[1]);
                    break;

                case GCONST.Y_CONSTR:
                    YConst = Float.parseFloat(constr[1]);
                    break;

                case GCONST.W_CONSTR:
                    WConst = Float.parseFloat(constr[1]);
                    break;

                case GCONST.H_CONSTR:
                    HConst = Float.parseFloat(constr[1]);
                    break;

                case GCONST.A_CONSTR:
                    aspRConst = Float.parseFloat(constr[1]);
                    break;
            }
        }
    }

    public boolean testConstraint(CGlyph glyph) {
        return testConstraint(glyph.getMetric());
    }


    public boolean testConstraint(CGlyphMetrics metric) {

        boolean result = false;

        // Check for any metric that violates the constraint - The CG? deltas are relative to the
        // size of the draw box.
        //
        if(metric.getDeltaCGX() > XConst) {
            Log.d("Metrics", "X - Violation : " + (metric.getDeltaCGX()-XConst));
            result = true;
        }
        if(metric.getDeltaCGY() > YConst) {
            Log.d("Metrics", "Y - Violation : " + (metric.getDeltaCGY()-YConst));
            result = true;
        }
        if(metric.getDeltaCGW() > WConst) {
            Log.d("Metrics", "W - Violation : " + (metric.getDeltaCGW()-WConst));
            result = true;
        }
        if(metric.getDeltaCGH() > HConst) {
            Log.d("Metrics", "H - Violation : " + (metric.getDeltaCGH()-HConst));
            result = true;
        }
        if(metric.getDeltaA() > aspRConst) {
            Log.d("Metrics", "Aspect - Violation : " + (metric.getDeltaA()-aspRConst));
            result = true;
        }

        // TODO: These need to be normalized somehow before they can be used
        //
//        result |= metric.getVisualDelta() > VisConst;
//        result |= metric.getErrorDelta()  > ErrConst;

        // Return true if nothing violates the constraint
        return !result;
    }



}
