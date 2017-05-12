package cmu.xprize.ak_component;

import android.app.Application;

/**
 * Created by Iris on 16/7/8.
 */

public class CAk_ContextUtil extends Application {
    private static CAk_ContextUtil instance;

    public static CAk_ContextUtil getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
    }
}
