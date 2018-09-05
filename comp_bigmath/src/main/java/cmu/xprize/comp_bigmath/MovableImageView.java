package cmu.xprize.comp_bigmath;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * MovableImageView
 *
 * Extends ImageView and uses a boolean field to track whether View is moving.
 *
 * <p>
 * Created by kevindeland on 7/31/18.
 */

@SuppressLint("AppCompatCustomView")
public class MovableImageView extends ImageView {

    public boolean isMoving = false;
    public boolean isMovable = true;

    public MovableImageView(Context context) {
        super(context);
    }

    public MovableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MovableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


}
