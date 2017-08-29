package org.senydevpkg.utils;

import android.content.Context;

import java.util.Map;
import java.util.Set;

/**
 * Created by ywf on 2017/2/9.
 */
public class CookieManager {

    /**
     * 获取持久化的Cookie值
     * @param context
     * @return
     */
    public static  String getCookie(Context context){
        return SPUtil.newInstance(context).getString("Cookie");
    }

    /**
     * 保存Cookie值
     * @param context
     * @param headers
     */
    public static void saveCookie(Context context, Map<String, String>  headers){
        if(headers != null) {
            String cookie = headers.get("Set-Cookie");
            ALog.d("[ save cookie ] Cookie = " + cookie);
            SPUtil.newInstance(context).putString("Cookie", cookie);
        }
    }

    /**
     * 清空Cookie值
     */
    public static void clearCookie(Context context){
        SPUtil.newInstance(context).remove("Cookie");
    }
}
