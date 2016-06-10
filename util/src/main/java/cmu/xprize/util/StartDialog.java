package cmu.xprize.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import cmu.xprize.common.R;


public class StartDialog {

    private final Dialog dialog;
    private ImageButton  start;
    private IRoboTutor   callback;

    public StartDialog(Context context) {

        callback = (IRoboTutor) context;

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.start_layout);
        dialog.setCancelable(false);

        start = (ImageButton) dialog.findViewById(R.id.SstartSelector);

        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callback.onStartTutor();
            }
        });
    }

    public void show() {
        dialog.show();
    }

    public void hide() {
        dialog.dismiss();
    }

    public Boolean isShowing() {
        return dialog.isShowing();
    }


}
