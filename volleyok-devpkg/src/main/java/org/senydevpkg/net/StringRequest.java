package org.senydevpkg.net;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.OkRequest;

import org.senydevpkg.utils.ALog;
import org.senydevpkg.utils.CookieManager;
import org.senydevpkg.utils.MD5Utils;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义Request。带缓存请求功能。
 *
 */
public class StringRequest extends OkRequest<String> {

    private boolean mIsCache;
    private Context mContext;
    private HttpLoader.ResponseListener<String>  responseListener;

    /**
     * 初始化
     *
     * @param method        请求方式
     * @param url           请求地址
     * @param listener      处理响应的监听器
     * @param errorListener 处理错误信息的监听器
     */
    public StringRequest(int method, String url, HttpLoader.ResponseListener listener,
                         Response.ErrorListener errorListener, boolean isCache, Context context) {
        super(method, url, errorListener);
        setReseponseListener(listener);
        this.responseListener = listener;
        mIsCache = isCache;
        mContext = context;
        Assert.notNull(mContext);
    }





    @Override
    protected void deliverResponse(String response) {
        super.deliverResponse(response);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {

           this.responseListener.onParaseNetWorkResponse(response);

            //将响应结果转换为string类型
            String result = new String(response.data, HttpHeaderParser.parseCharset(response.headers));


            ALog.d(result);

            if (mIsCache) {
                //如果解析成功，并且需要缓存则将字符串缓存到本地
                ALog.i("Save response to local!");
                FileCopyUtils.copy(response.data, new File(mContext.getCacheDir(), "" + MD5Utils.encode(getUrl())));
            }
            return Response.success(
                    result,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (IOException e) {
            return Response.error(new ParseError(e));
        }
    }

}