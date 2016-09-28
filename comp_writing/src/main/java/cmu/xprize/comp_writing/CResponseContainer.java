package cmu.xprize.comp_writing;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.widget.RemoteViews;
import android.widget.TextView;


/**
 */
@RemoteViews.RemoteView
public class CResponseContainer extends PercentRelativeLayout {

    private TextView mRecogChar;

    public CResponseContainer(Context context) {
        super(context);
    }

    public CResponseContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CResponseContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context) {

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mRecogChar     = (TextView)findViewById(R.id.recog_char);
    }

    public void setResponseChar(String resp) {
        mRecogChar.setText(resp);
    }


}