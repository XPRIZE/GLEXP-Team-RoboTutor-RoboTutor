package cmu.xprize.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Window;
import android.widget.ProgressBar;

import cmu.xprize.common.R;

public class ProgressLoading {

    private Movie mMovie;
    private long mMovieStart;

    private final Dialog dialog;

    public ProgressLoading(Context context) {

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_layout);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
