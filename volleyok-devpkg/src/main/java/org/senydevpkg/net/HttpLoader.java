package org.senydevpkg.net;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.http.Headers;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.OkRequest;
import com.android.volley.toolbox.OkVolley;
import com.android.volley.toolbox.Volley;

import org.senydevpkg.net.resp.IResponse;
import org.senydevpkg.utils.ALog;
import org.senydevpkg.utils.MD5Utils;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * ━━━━ Code is far away from ━━━━━━
 * 　　  () 　　　  ()
 * 　　  ( ) 　　　( )
 * 　　  ( ) 　　　( )
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　┻　　　┃
 * 　　┗━┓　　　┏━┛
 * 　　　　┃　　　┃
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━ bug with the XYY protecting━━━
 * <p/>
 * Created by Seny on 2015/12/1.
 * <p/>
 * 网络请求核心类。负责封装get，post请求(GsonRequest)，支持添加自定义Request。初始化RequestQueue及ImageLoader
 */
public class HttpLoader {

    /**
     * 保存ImageView上正在发起的网络请求
     */
    private final Map<ImageView, ImageLoader.ImageContainer> mImageContainers = new HashMap<>();
    private static HttpLoader sInstance;
    /**
     * 过滤重复请求。保存当前正在消息队列中执行的Request.key为对应的requestCode.
     */
    private final HashMap<Integer, Request<?>> mInFlightRequests =
            new HashMap<>();
    /**
     * 消息队列，全局使用一个
     */
    private RequestQueue mRequestQueue;
    /**
     * 图片加载工具，自定义缓存机制
     */
    private ImageLoader mImageLoader;
    private Context mContext;

    private HttpLoader(Context context) {
        mContext = context.getApplicationContext();
        //传输层使用okhttp3，进行网络通讯
        mRequestQueue = OkVolley.newRequestQueue(mContext);
        mImageLoader = new ImageLoader(mRequestQueue, new VolleyImageCacheImpl(mContext));
    }


    /**
     * 返回HttpLoader 单例对象
     *
     * @param context
     * @return
     */
    public static synchronized HttpLoader getInstance(Context context) {
        if (sInstance == null) {
            Assert.notNull(context);
            sInstance = new HttpLoader(context);
        }
        return sInstance;
    }

    /**
     * 添加一个请求到请求队列.支持任意Request
     *
     * @return 返回该Request，方便链式编程
     */
    public Request<?> addRequest(Request<?> request) {
        if (mRequestQueue != null && request != null) {
            mRequestQueue.add(request);
        }
        return request;

    }


    /**
     * 添加一个HttpLoader管理和创建的的Request请求到请求队列.
     *
     * @param requestCode 请求的唯一标识码
     * @return 返回该Request，方便链式编程
     */
    private Request<?> addRequest(Request<?> request, int requestCode) {
        if (mRequestQueue != null && request != null) {
            mRequestQueue.add(request);
            mInFlightRequests.put(requestCode, request);//添加到正在处理请求中
        }
        return request;
    }

    /**
     * 取消请求
     *
     * @param tag 请求TAG
     */

    public void cancelRequest(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);//从请求队列中取消对应的任务
        }
        //同时在mInFlightRequests删除保存所有TAG匹配的Request
        Iterator<Map.Entry<Integer, Request<?>>> it = mInFlightRequests.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Request<?>> entry = it.next();
            Object rTag = entry.getValue().getTag();
            if (rTag != null && rTag.equals(tag)) {
                it.remove();
            }
        }
    }

    /**
     * 返回已初始化过的ImageLoader类。
     *
     * @return ImageLoader 对象
     */
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * 请求网络图片并设置给ImageView
     *
     * @param view       The imageView
     * @param requestUrl The URL of the image to be loaded.
     */
    public void display(ImageView view, String requestUrl) {
        display(view, requestUrl, 0, 0);
    }

    /**
     * 请求网络图片并设置给ImageView，可以设置默认显示图片和加载错误显示图片
     *
     * @param view              The imageView
     * @param requestUrl        The image url to request
     * @param defaultImageResId Default image resource ID to use, or 0 if it doesn't exist.
     * @param errorImageResId   Error image resource ID to use, or 0 if it doesn't exist.
     */
    public void display(ImageView view, String requestUrl, int defaultImageResId, int errorImageResId) {
        display(view, requestUrl, defaultImageResId, errorImageResId, view.getWidth(), view.getHeight(), ImageView.ScaleType.FIT_XY);
    }

    /**
     * 发起图片网络请求
     *
     * @param requestUrl The url of the remote image
     * @param maxWidth   The maximum width of the returned image.
     * @param maxHeight  The maximum height of the returned image.
     * @param scaleType  The ImageViews ScaleType used to calculate the needed image size.
     * @return A container object that contains all of the properties of the request, as well as
     * the currently available image (default if remote is not loaded).
     */
    public void display(final ImageView view, String requestUrl, final int defaultImageResId, final int errorImageResId, int maxWidth, int maxHeight, ImageView.ScaleType scaleType) {
        if (mImageContainers.containsKey(view)) {//如果已经在给该View请求一张网络图片
            mImageContainers.get(view).cancelRequest();//那么就把之前的取消掉，保证一个ImageView身上只有一个任务。
        }
        ImageLoader.ImageContainer imageContainer = mImageLoader.get(requestUrl, new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorImageResId != 0) {
                    view.setImageResource(errorImageResId);
                    ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).setDuration(800).start();//渐变动画
                }
                mImageContainers.remove(view);//请求失败，移除
            }

            @Override
            public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    view.setImageBitmap(response.getBitmap());
                    ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).setDuration(800).start();//渐变动画
                    mImageContainers.remove(view);//请求成功，移除
                } else if (defaultImageResId != 0) {
                    view.setImageResource(defaultImageResId);
                }
            }
        }, maxWidth, maxHeight, scaleType);
        mImageContainers.put(view, imageContainer);//将View身上的请求任务进行保存
    }

    /**
     * 发送Request请求
     *
     * @param method      请求方式
     * @param url         请求地址
     * @param params      请求参数,可以为null
     * @param clazz       Clazz类型，用于GSON解析json字符串封装数据
     * @param requestCode 请求码 每次请求对应一个code作为改Request的唯一标识
     * @param listener    处理响应的监听器
     * @param isCache     是否需要缓存本次响应的结果
     */

    private Request<?> request(int method, String url, HttpParams params, HttpHeaders headers, Class<? extends IResponse> clazz, final int requestCode, final HttpListener listener, boolean isCache) {
        ALog.d("Request URL:" + url);
        //在请求任务队列中，如果已经存在该请求，直接获取请求对象，无需创建请求对象。
        //作用： 过滤重复请求。
        Request request = mInFlightRequests.get(requestCode);

        if (request == null) {
            if (clazz != null) {
                //构建请求对象， 将json格式转换为javabean类型
                request = makeGsonRequest(method, url, params, headers, clazz, requestCode, listener, isCache);
            } else {
                //构建请求对象，将byte[] 数据转换为string类型
                request = makeStringRequest(method, url, params, headers, requestCode, listener, isCache);
            }

            //如果是GET请求，则首先尝试解析本地缓存供界面显示，然后再发起网络请求
            if (method == Request.Method.GET) {
                tryLoadCacheResponse(request, requestCode, listener);
            }
            ALog.i("Handle request by network!");
            //添加请求对象到请求队列中
            return addRequest(request, requestCode);
        } else {
            ALog.i("Hi guy,the request (RequestCode is " + requestCode + ")  is already in-flight , So Ignore!");
            return request;
        }
    }



    /**
     * 发送get方式的GsonRequest请求,默认缓存请求结果
     *
     * @param url         请求地址
     * @param params      GET请求参数，拼接在URL后面。可以为null
     * @param requestCode 请求码 每次请求对应一个code作为改Request的唯一标识
     * @param listener    处理响应的监听器
     */
    public Request get(String url, HttpParams params, HttpHeaders headers, final int requestCode, final HttpListener<String> listener) {
        return request(Request.Method.GET, url, params, headers, null, requestCode, listener, true);
    }

    /**
     * 发送get方式的GsonRequest请求,默认缓存请求结果
     *
     * @param url         请求地址
     * @param params      GET请求参数，拼接在URL后面。可以为null
     * @param clazz       Clazz类型，用于GSON解析json字符串封装数据
     * @param requestCode 请求码 每次请求对应一个code作为改Request的唯一标识
     * @param listener    处理响应的监听器
     */
    public Request<?> get(String url, HttpParams params, HttpHeaders headers, Class<? extends IResponse> clazz, final int requestCode, final HttpListener<? extends IResponse> listener) {
        return request(Request.Method.GET, url, params, headers, clazz, requestCode, listener, true);
    }

    /**
     * 发送get方式的GsonRequest请求
     *
     * @param url         请求地址
     * @param params      GET请求参数，拼接在URL后面。可以为null
     * @param clazz       Clazz类型，用于GSON解析json字符串封装数据
     * @param requestCode 请求码 每次请求对应一个code作为改Request的唯一标识
     * @param listener    处理响应的监听器
     * @param isCache     是否需要缓存本次响应的结果,没有网络时会使用本地缓存
     */
    private Request<?> get(String url, HttpParams params, HttpHeaders headers, Class<? extends IResponse> clazz, final int requestCode, final HttpListener listener, boolean isCache) {
        return request(Request.Method.GET, url, params, headers, clazz, requestCode, listener, isCache);
    }

    /**
     * 发送post方式的GsonRequest请求，默认缓存请求结果
     *
     * @param url         请求地址
     * @param params      请求参数，可以为null
     * @param clazz       Clazz类型，用于GSON解析json字符串封装数据
     * @param requestCode 请求码 每次请求对应一个code作为改Request的唯一标识
     * @param listener    处理响应的监听器
     */
    public Request<?> post(String url, HttpParams params, HttpHeaders headers, Class<? extends IResponse> clazz, final int requestCode, final HttpListener<? extends IResponse> listener) {
        return request(Request.Method.POST, url, params, headers, clazz, requestCode, listener, false);//POST请求不缓存
    }


    /**
     * 发送post方式的GsonRequest请求，默认缓存请求结果
     *
     * @param url         请求地址
     * @param params      请求参数，可以为null
     * @param requestCode 请求码 每次请求对应一个code作为改Request的唯一标识
     * @param listener    处理响应的监听器
     */
    public Request post(String url, HttpParams params, HttpHeaders headers, final int requestCode, final HttpListener<String> listener) {
        return request(Request.Method.POST, url, params, headers, null, requestCode, listener, false);//POST请求不缓存
    }



    /**
     * 初始化一个GsonRequest
     *
     * @param method      请求方法
     * @param url         请求地址
     * @param params      请求参数，可以为null
     * @param clazz       Clazz类型，用于GSON解析json字符串封装数据
     * @param requestCode 请求码 每次请求对应一个code作为改Request的唯一标识
     * @param listener    监听器用来响应结果
     * @return 返回一个GsonRequest对象
     */
    private GsonRequest<IResponse> makeGsonRequest(int method, String url, HttpParams params, HttpHeaders headers, Class<? extends IResponse> clazz, int requestCode, HttpListener<IResponse> listener, boolean isCache) {

        //创建响应监听器，用于接受响应结果
        ResponseListener responseListener = new ResponseListener(requestCode, listener);

        //创建请求对象
        GsonRequest<IResponse> request = new GsonRequest<>(method, url, clazz, responseListener, responseListener, isCache, mContext);

        //设置请求头信息
        request.headers(headers.getHeaders());
        //设置请求参数
        if(params != null){
            setRequestParams(method, params, request);
        }

        //设置超时时间，重试次数
        request.setRetryPolicy(new DefaultRetryPolicy());

        return request;
    }

    private StringRequest makeStringRequest(int method, String url, HttpParams params, HttpHeaders headers, int requestCode, HttpListener<String> listener, boolean isCache) {

        //创建响应监听器，用于接受响应结果
        ResponseListener responseListener = new ResponseListener(requestCode, listener);

        //创建请求对象
        StringRequest request = new StringRequest(method, url, responseListener, responseListener, isCache, mContext);

        //设置请求头信息
        if(headers != null){
        request.headers(headers.getHeaders());
        }

        //设置请求参数
        if(params != null){
            setRequestParams(method, params, request);
        }

        //设置超时时间，重试次数
        request.setRetryPolicy(new DefaultRetryPolicy());

        return request;

    }

    private void setRequestParams(int method, HttpParams params, OkRequest request) {

        //如果是get请求，进行url拼接
        if(method == Request.Method.GET){
            for(Map.Entry<String, Object>  entry: params.getParams().entrySet()){
                request.param(entry.getKey(), String.valueOf(entry.getValue()));
            }

        }else{ //其它请求方式，封装请求参数
            for(Map.Entry<String, Object>  entry: params.getParams().entrySet()){
                // 请求参数是file类型
                if(entry.getValue() instanceof File){
                    try {
                        request.part(entry.getKey(),((File) entry.getValue()).getName(), (File) entry.getValue());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{//请求参数为其他类型，转换为string类型
                    try {
                        request.part(entry.getKey(),String.valueOf(entry.getValue()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    /**
     * 尝试从缓存中读取json数据
     *
     * @param request 要寻找缓存的request
     */
    private void tryLoadCacheResponse(Request request, int requestCode, HttpListener listener) {
        ALog.i("Try to  load cache response first !");
        if (listener != null && request != null) {
            try {
                //获取缓存文件
                File cacheFile = new File(mContext.getCacheDir(), "" + MD5Utils.encode(request.getUrl()));
                StringWriter sw = new StringWriter();
                //读取缓存文件
                FileCopyUtils.copy(new FileReader(cacheFile), sw);
                if (request instanceof GsonRequest) {
                    //如果是GsonRequest，那么解析出本地缓存的json数据为GsonRequest
                    GsonRequest gr = (GsonRequest) request;
                    //面向接口的实现方法
                    IResponse response = (IResponse) gr.gson.fromJson(sw.toString(), gr.getClazz());
                    //传给onResponse，让前面的人用缓存数据
                    listener.onGetResponseSuccess(requestCode, response);
                    ALog.i("Load cache response success !");
                }
            } catch (Exception e) {
                ALog.w("No cache response ! " + e.getMessage());
            }
        }

    }


    /**
     * 成功获取到服务器响应结果的监听，供UI层注册.
     */
    public abstract static class HttpListener<T> {
        /**
         * 当成功获取到服务器响应结果的时候调用
         *
         * @param requestCode response对应的requestCode
         * @param response    返回的response
         */
        public abstract void onGetResponseSuccess(int requestCode, T response);

        /**
         * 网络请求失败，做一些释放性的操作，比如关闭对话框
         *
         * @param requestCode 请求码
         * @param error       异常详情
         */
        public  abstract  void onGetResponseError(int requestCode, VolleyError error);


        /**
         * 解析网络响应内容。 常见操作：解析响应头信息，请求耗时
         * 注意：该方法在子线程中执行， 不能执行UI的操作
         * @param networkResponse
         */
        public void onParaseNetWorkResponse(NetworkResponse  networkResponse){

        }
    }

    /**
     * ResponseListener，封装了Volley错误和成功的回调监，并执行一些默认处理，同时会将事件通过HttpListener抛到UI层
     */
    public class ResponseListener<T> implements Response.ErrorListener, Response.Listener<T> {

        private HttpListener listener;
        private int requestCode;

        public ResponseListener(int requestCode, HttpListener listener) {
            this.requestCode = requestCode;
            this.listener = listener;
        }

        public void  onParaseNetWorkResponse(NetworkResponse networkResponse){
            this.listener.onParaseNetWorkResponse(networkResponse);

        }

        @Override
        public void onErrorResponse(VolleyError error) {
            ALog.w("Request error from network!");
            error.printStackTrace();
            mInFlightRequests.remove(requestCode);//请求错误，从正在飞的集合中删除该请求
            if (listener != null) {
                listener.onGetResponseError(requestCode, error);
            }

        }


        @Override
        public void onResponse(T response) {
            mInFlightRequests.remove(requestCode);//请求成功，从正在飞的集合中删除该请求
            if (response != null) {
                ALog.i("Request success from network!");
                if (listener != null) {
                    listener.onGetResponseSuccess(requestCode, response);
                }
            }
        }
    }


}
