package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;

import java.io.IOException;
import java.util.Map;


/**
 * Created by GoogolMo on 6/4/14.
 */
public interface OkStack {
    okhttp3.Response performRequest(Request<?> request, Map<String, String> map) throws IOException, AuthFailureError;
}
