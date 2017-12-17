package kkk.linkbasic.app.com.popupwin;

import android.app.Application;

/**
 * Created by linkbasic on 2017-09-18.
 */

public class MyApplication extends Application {


    private static MyApplication instance;


    public static MyApplication getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

}
