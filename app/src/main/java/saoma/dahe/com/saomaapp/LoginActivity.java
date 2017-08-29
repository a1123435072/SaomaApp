package saoma.dahe.com.saomaapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import org.senydevpkg.net.HttpLoader;
import org.senydevpkg.net.HttpParams;
import org.senydevpkg.utils.CookieManager;
import org.senydevpkg.utils.SPUtil;

import java.util.Map;

/**
 * Created by YF02017 on 2017/7/20.
 */

public class LoginActivity extends AppCompatActivity {
    private Button loginBtn;
    private Button tabipBtn;
    private EditText et_username;
    private EditText et_password;
    private String username;
    private String password;



    private SharedPreferences sharedPreferences;//用于存取本地内容  例如用户名部门等
    private Context mContext;

    private Util util;

    private long exitTime = 0;//设置退出时间

    //再按一次退出程序，这个方法是可以设置通用
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //再按一次退出程序 方法结束

    //重启设备方法
    public void reboot(){
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_login);
        //本地化读取
        sharedPreferences = getSharedPreferences("userinfo",0);
        mContext = getApplicationContext();
        //对按钮进行绑定
        loginBtn = (Button)findViewById(R.id.btn_login);
        tabipBtn = (Button)findViewById(R.id.btn_tabip);
        et_username = (EditText)findViewById(R.id.et_username);
        et_password = (EditText)findViewById(R.id.et_password);

        util = new Util(mContext);

        //切换IP
        tabipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText editText = new EditText(LoginActivity.this);
                new AlertDialog.Builder(LoginActivity.this).setTitle("设置IP").setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String input = editText.getText().toString();
                                if ("".equals(input)){
                                    Toast.makeText(getApplicationContext(), "输入ip不能为空" + input, Toast.LENGTH_LONG).show();
                                }else {
                                    util.getIp(input);
                                    Log.e("设置的ip===",input);
                                    reboot();
                                }
                            }
                        }).setNegativeButton("取消",null).show();
            }
        });
        //对登录按钮进行监听
        loginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                username = et_username.getText().toString();//这里需要将数值进行转换
                password = et_password.getText().toString();

                String url = "http://www.oschina.net/action/api/login_validate";
                HttpParams params = new HttpParams();
                params.put("keep_login", "1");
                params.put("username", username);
                params.put("pwd", password);

                HttpLoader.getInstance(LoginActivity.this).post(url, params, null, 0x22, new HttpLoader.HttpListener<String>() {
                    /*@Override
                    public void onParaseNetWorkResponse(NetworkResponse networkResponse) {
                        super.onParaseNetWorkResponse(networkResponse);
                        //这个网络数据里面有headers变量，里面包含了Cookie值
                        Map<String,String> headers = networkResponse.headers;
                        //这个save方法，将Cookie保存在sp中，Manager类在老师给的框架中
                        CookieManager.saveCookie(LoginActivity.this,headers);
                    }*/

                    @Override
                    public void onGetResponseSuccess(int requestCode, String response) {
                        /**
                         在登录的相应结果中，会包含一个用户唯一标识uid
                         此时用户需要把uid持久化到文件中
                         */
                        Toast.makeText(LoginActivity.this,"请求成功",Toast.LENGTH_SHORT).show();
                        /*LoginUserBean loginUserBean = XmlUtils.toBean(LoginUserBean.class, response.getBytes());
                        Result result = loginUserBean.getResult();
                        Toast.makeText(getApplicationContext(), result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        if(result.getErrorCode()==0){
                            //用户名密码错误

                        }else if(result.getErrorCode()==1){
                            //登录成功
                            //将uid存到sp中，其他界面使用
                            User user = loginUserBean.getUser();
                            SPUtil.newInstance(getApplicationContext()).putString("uid",user.getId()+"");
                            finish();
                        }*/
                    }

                    @Override
                    public void onGetResponseError(int requestCode, VolleyError error) {
                        System.out.println("错误---------");
                    }
                });

              /*  if (!"".equals(username) && !"".equals(password)){
                    final AsyncHttpClient client = new AsyncHttpClient();
                    RequestParams params = new RequestParams();
                    params.add("username",username);
                    params.add("password",password);
                    Log.e("params", String.valueOf(params));
                    Log.e("url----",sharedPreferences.getString("login", ""));
                    client.post(sharedPreferences.getString("login", ""), params, new AsyncHttpResponseHandler() {

                        @Override
                        public void onSuccess(int i, Header[] headers, byte[] bytes) {
                            String respone = new String(bytes);

                            final SharedPreferences.Editor editor = sharedPreferences.edit();
                            Log.e("respone",respone);
                        }

                        @Override
                        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                            Toast.makeText(LoginActivity.this, "网络错误，请重试", Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    Toast.makeText(LoginActivity.this, "用户名或者密码不能为空，请重新输入", Toast.LENGTH_LONG).show();
                }*/
            }
        });
    }
}
