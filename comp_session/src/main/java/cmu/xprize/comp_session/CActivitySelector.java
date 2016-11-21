package cmu.xprize.comp_session;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import org.json.JSONObject;

import cmu.xprize.comp_ask.CAsk_Data;
import cmu.xprize.comp_ask.IButtonController;
import cmu.xprize.util.CEvent;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;


public class CActivitySelector extends FrameLayout implements IButtonController, ILoadableObject {

    protected Context           mContext;

    // json loadable
    public CAsk_Data[]             dataSource;

    final private String  TAG = "CActivitySelector";



    public CActivitySelector(Context context) {
        super(context);
        init(context, null);
    }

    public CActivitySelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CActivitySelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    public void init(Context context, AttributeSet attrs) {

        mContext = context;
    }


    @Override
    protected void onDraw(Canvas canvas) {
    }


    @Override
    public void doButtonAction(String actionid) {

    }



    //************ Serialization



    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, cmu.xprize.util.CClassMap.classMap, scope);
    }



}
