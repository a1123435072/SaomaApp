/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley.toolbox;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.AndroidHttpClient;
import android.os.Build;

import com.android.volley.Network;
import com.android.volley.RequestQueue;

import java.io.File;
import java.util.UUID;

public class Volley {

    /** Default on-disk cache directory. */
    private static final String DEFAULT_CACHE_DIR = "volley";

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @param stack An {@link HttpStack} to use for the network, or null for default.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context, HttpStack stack) {
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);

        String userAgent = "volley/0"; //User Agent: 用户代理，简称 UA，它是一个特殊字符串头，使得服务器能够识别客户使用的操作系统及版本
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            userAgent = packageName + "/" + info.versionCode;
            userAgent = getUserAgent(context);

        } catch (Exception e) {
        }

        if (stack == null) {
            if (Build.VERSION.SDK_INT >= 9) {
                stack = new HurlStack();
            }
              else {
                // Prior to Gingerbread, HttpUrlConnection was unreliable.
                // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
                AndroidHttpClient androidHttpClient = AndroidHttpClient.newInstance(userAgent);
                stack = new HttpClientStack(androidHttpClient);
            }
        }

        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
        queue.start();

        return queue;
    }

    private static String getUserAgent(Context context) {
          String packageName = context.getPackageName();
            PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder ua = new StringBuilder("OSChina.NET");
            ua.append('/'+info.versionName+'_'+info.versionCode);//App版本
            ua.append("/Android");//手机系统平台
            ua.append("/"+android.os.Build.VERSION.RELEASE);//手机系统版本
            ua.append("/"+android.os.Build.MODEL); //手机型号
            ua.append("/"+ UUID.randomUUID().toString());//客户端唯一标识
            return ua.toString();
    }

//    public String getAppId() {
//        String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
//        if(StringUtils.isEmpty(uniqueID)){
//            uniqueID = UUID.randomUUID().toString();
//            setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
//        }
//        return uniqueID;
//    }

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context) {
        return newRequestQueue(context, null);
    }
}
