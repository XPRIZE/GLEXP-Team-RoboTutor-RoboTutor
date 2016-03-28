package cmu.xprize.util;

import android.content.Intent;
import android.graphics.PointF;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

public class CPersonaObservable {

    static private LocalBroadcastManager bManager;

    public CPersonaObservable() {
    }

    static public void broadcastLocation(View src, String Action, PointF touchPt) {
        int[] _screenCoord = new int[2];

        // Capture the local broadcast manager
        if(bManager == null)
            bManager = LocalBroadcastManager.getInstance(src.getContext());

        src.getLocationOnScreen(_screenCoord);

        // Let the persona know where to look
        Intent msg = new Intent(Action);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{touchPt.x + _screenCoord[0], (float) touchPt.y + _screenCoord[1]});

        bManager.sendBroadcast(msg);
    }

}
