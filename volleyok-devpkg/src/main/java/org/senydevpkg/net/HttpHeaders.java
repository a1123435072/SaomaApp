package org.senydevpkg.net;

import java.util.HashMap;
import java.util.Map;


public class HttpHeaders {

    private final Map<String, String> mHeaders = new HashMap<>();

    /**
     * 获取某个key对应的value
     *
     * @return 返回某个key对应的value
     */
    public String get(String key) {

        return mHeaders.get(key);
    }


    /**
     * 设置一个key=value的http 参数
     *
     * @param key   参数的key
     * @param value 参数的value
     */
    public HttpHeaders put(String key, String value) {
        mHeaders.put(key, value);
        return this;
    }



    /**
     * 返回封装了http headers的Map集合
     *
     * @return
     */
    public Map<String, String> getHeaders() {
        return mHeaders;
    }
}
