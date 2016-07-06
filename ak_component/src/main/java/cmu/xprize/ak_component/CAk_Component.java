package cmu.xprize.ak_component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import org.json.JSONObject;

import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

/**
 * Created by jacky on 2016/7/6.
 */

public class CAk_Component extends FrameLayout implements ILoadableObject{
    static public Context mContext;

    protected String        mDataSource;
    private   int           _dataIndex = 0;

    static final String TAG = "CAk_Component";

    private CAk_Data _currData;
    public CAk_Data[] dataSource;

    public CAk_Component(Context context) {
        super(context);
        init(context, null);
    }

    public CAk_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAk_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
//        inflate(getContext(), R.layout.bubblepop_layout, this);
//
//        mContext = context;
//
//        if(attrs != null) {
//
//            TypedArray a = context.getTheme().obtainStyledAttributes(
//                    attrs,
//                    R.styleable.RoboTutor,
//                    0, 0);
//
//            try {
//                mDataSource  = a.getString(R.styleable.RoboTutor_dataSource);
//            } finally {
//                a.recycle();
//            }
//        }
//
//        // Allow onDraw to be called to start animations
//        //
//        setWillNotDraw(false);
    }

    public void setmDataSource(CAk_Data[] _dataSource) {
        dataSource = _dataSource;
        _dataIndex = 0;
    }

    public void next() {
        try {
            if (dataSource != null) {
                updateDataSet(dataSource[_dataIndex]);

                _dataIndex++;
            } else {
                CErrorManager.logEvent(TAG,  "Error no DataSource : ", null, false);
            }
        }
        catch(Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }
    }

    public boolean dataExhausted() {
        return (_dataIndex >= dataSource.length)? true:false;
    }

    protected void updateDataSet(CAk_Data data) {

    }

    //************ Serialization

    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);
        _dataIndex = 0;

//        addView(view_background);
    }

}
