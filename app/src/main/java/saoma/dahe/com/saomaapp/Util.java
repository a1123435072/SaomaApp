package saoma.dahe.com.saomaapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by YF02017 on 2017/7/20.
 */

public class Util {
    private String Ip;
    private Context mContext;
    private String login;

    public Util(){

    }

    public Util(Context mContext){
        this.mContext = mContext;
    }
    /*Context.MODE_PRIVATE：为默认操作模式,代表该文件是私有数据,只能被应用本身访问,在该模式下,写入的内容会覆盖原文件的内容
    Context.MODE_APPEND：模式会检查文件是否存在,存在就往文件追加内容,否则就创建新文件.
    Context.MODE_WORLD_READABLE和Context.MODE_WORLD_WRITEABLE用来控制其他应用是否有权限读写该文件.
    MODE_WORLD_READABLE：表示当前文件可以被其他应用读取.
    MODE_WORLD_WRITEABLE：表示当前文件可以被其他应用写入.*/
    public void getIp(String ip){
        SharedPreferences sp = mContext.getSharedPreferences("userinfo",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("ipEditor",ip);
        Log.e("在getIp方法中的ip",ip);
        editor.commit();
        read();
    }

    private void read(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("userinfo",0);
        String data = sharedPreferences.getString("ipEditor","");

        Log.e("data是多少",data);

        Ip = "http://"+data+":8080/NEWmes/";
        login = Ip+"face/login";

        Log.e("login是啥",login);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("Ip",Ip);
        editor.putString("login",login);

        editor.commit();
    }
}
