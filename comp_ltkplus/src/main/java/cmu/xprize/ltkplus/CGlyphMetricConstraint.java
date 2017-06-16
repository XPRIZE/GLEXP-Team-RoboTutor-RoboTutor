//*********************************************************************************
//
//    Copyright(c) 2016-2017  Kevin Willows All Rights Reserved
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

import cmu.xprize.util.IPublisher;

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

    public boolean testConstraint(CGlyph glyph, IPublisher pub) {
        return testConstraint(glyph.getMetric(), pub);
    }


    public boolean testConstraint(CGlyphMetrics metric, IPublisher pub) {

        boolean result = false;

        pub.retractFeature(GCONST.FTR_POSHORZ_VIOLATION);
        pub.retractFeature(GCONST.FTR_LEFT_VIOLATION   );
        pub.retractFeature(GCONST.FTR_RIGHT_VIOLATION  );

        pub.retractFeature(GCONST.FTR_POSVERT_VIOLATION);
        pub.retractFeature(GCONST.FTR_HIGH_VIOLATION   );
        pub.retractFeature(GCONST.FTR_LOW_VIOLATION    );

        pub.retractFeature(GCONST.FTR_WIDTH_VIOLATION );
        pub.retractFeature(GCONST.FTR_WIDE_VIOLATION  );
        pub.retractFeature(GCONST.FTR_NARROW_VIOLATION);

        pub.retractFeature(GCONST.FTR_HEIGHT_VIOLATION);
        pub.retractFeature(GCONST.FTR_TALL_VIOLATION  );
        pub.retractFeature(GCONST.FTR_SHORT_VIOLATION );

        // Check for any metric that violates the constraint - The CG? deltas are relative to the
        // size of the draw box.
        //
        if(metric.getDeltaCGX() > XConst) {
            Log.d("Metrics", "X - Violation : " + (metric.getDeltaCGX()-XConst));
            result = true;

            pub.publishFeature(GCONST.FTR_POSHORZ_VIOLATION);
            pub.publishFeature(metric.getIsLeft()? GCONST.FTR_LEFT_VIOLATION:GCONST.FTR_RIGHT_VIOLATION);
        }
        if(metric.getDeltaCGY() > YConst) {
            Log.d("Metrics", "Y - Violation : " + (metric.getDeltaCGY()-YConst));
            result = true;

            pub.publishFeature(GCONST.FTR_POSVERT_VIOLATION);
            pub.publishFeature(metric.getIsHigh()? GCONST.FTR_HIGH_VIOLATION:GCONST.FTR_LOW_VIOLATION);
        }
        if(metric.getDeltaCGW() > WConst) {
            Log.d("Metrics", "W - Violation : " + (metric.getDeltaCGW()-WConst));
            result = true;

            pub.publishFeature(GCONST.FTR_WIDTH_VIOLATION);
            pub.publishFeature(metric.getIsWide()? GCONST.FTR_WIDE_VIOLATION:GCONST.FTR_NARROW_VIOLATION);
        }
        if(metric.getDeltaCGH() > HConst) {
            Log.d("Metrics", "H - Violation : " + (metric.getDeltaCGH()-HConst));
            result = true;

            pub.publishFeature(GCONST.FTR_HEIGHT_VIOLATION);
            pub.publishFeature(metric.getIsTall()? GCONST.FTR_TALL_VIOLATION:GCONST.FTR_SHORT_VIOLATION);
        }
        if(metric.getDeltaA() > aspRConst) {
            Log.d("Metrics", "Aspect - Violation : " + (metric.getDeltaA()-aspRConst));
            result = true;
        }

        // TODO: These need to be normalized somehow before they can be used
        //
//        result |= metric.getVisualMatch() > VisConst;
//        result |= metric.getVisualError() > ErrConst;

        // Return true if nothing violates the constraint
        return !result;
    }



}
