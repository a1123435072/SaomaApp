package org.senydevpkg.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ywf on 2017/2/9.
 */
public class SPUtil {
    private static  SPUtil  spUtil;
    private final SharedPreferences sp;
    private final SharedPreferences.Editor mEdit;

    private SPUtil(Context context){
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        mEdit = sp.edit();

    }

    public static SPUtil newInstance(Context context) {
       if(spUtil == null){
           spUtil = new SPUtil(context);
       }
        return spUtil;
    }

    public void putString(String key, String value){
        mEdit.putString(key, value).apply();

    }

    public String getString(String key){
       return sp.getString(key, "");
    }

    public void remove(String key){
        mEdit.remove(key).apply();
    }

    public void clear(){
        mEdit.clear().apply();
    }


}
