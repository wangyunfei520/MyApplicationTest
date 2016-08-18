package com.wyf.application;

import android.app.ProgressDialog;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * 测试
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView textView;
    private EditText input;
    private Handler handler = new Handler();
    private ProgressDialog m_progressDlg;

    private static String path = Environment.getRootDirectory() + File.separator + "app";

    private static String url = "";
    File file = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.show);
        input = (EditText) findViewById(R.id.input);
        m_progressDlg = new ProgressDialog(this);
        textView.setText(Contant.BASE_URL);
        file = new File(path);
        String[] list = file.list();
        for (int i = 0; i < list.length; i++) {
            Log.d("@@@@@@@@@@@@@@@", "onCreate: " + i + list[i]);
        }
        textView.append("\n" + list.length);
        path = path + File.separator +"flavors2.apk";
        file = new File(path);
        Log.d("MainActivity", "path: " + path);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = input.getText().toString();
                Log.d(TAG, "设置的s："+s);
                if (TextUtils.isEmpty(s)){
                    return;
                }
                JPushInterface.setAlias(MainActivity.this, s, new TagAliasCallback() {
                    @Override
                    public void gotResult(int code, String s, Set<String> set) {
                        String logs;
                        switch (code) {
                            case 0:
                                logs = "Set tag and alias success";
                                Log.i(TAG, logs);
                                break;

                            case 6002:
                                logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                                Log.i(TAG, logs);
                                break;

                            default:
                                logs = "Failed with errorCode = " + code;
                                Log.e(TAG, logs);
                        }

                    }
                });
//                down();
//                downFile(url);
            }
        });
    }

    //更新
    private void downFile(final String url) {
        if (url == null || TextUtils.isEmpty(url))
            return;
        Log.d("MainActivity", "发现新版本，开始下载");
        m_progressDlg.setTitle("正在下载");
        m_progressDlg.setMessage("请稍候...");
        m_progressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        m_progressDlg.show();
        new Thread() {
            public void run() {
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {

                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(url);
                    HttpResponse response;
                    try {
                        response = client.execute(get);
                        Log.d("MainActivity", "发现新版本，开始下载"
                                + response.getStatusLine().getStatusCode());
                        HttpEntity entity = response.getEntity();
                        long length = entity.getContentLength();
                        m_progressDlg.setMax((int) length);// 设置进度条的最大值
                        InputStream is = entity.getContent();
                        FileOutputStream fileOutputStream = null;
                        if (is != null) {
                            fileOutputStream = new FileOutputStream(file);
                            byte[] buf = new byte[1024];
                            int ch = -1;
                            int count = 0;
                            while ((ch = is.read(buf)) != -1) {
                                fileOutputStream.write(buf, 0, ch);
                                count += ch;
                                if (length > 0) {
                                    m_progressDlg.setProgress(count);
                                }
                            }
                        }
                        fileOutputStream.flush();
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        down(); // 告诉HANDER已经下载完成了，可以安装了

                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            client.getConnectionManager().shutdown();// 关闭连接
                            // 这两种释放连接的方法都可以
                        } catch (Exception e) {
                            Log.e("MainActivity", e.getMessage());
                        }
                    }
                } else {
                    error();//告诉HANDER没有内存卡，下载失败
                }
            }
        }.start();
    }

    private void down() {
//        Log.d("MainActivity", "下载完成");
        handler.post(new Runnable() {
            public void run() {
//                m_progressDlg.cancel();
                Log.d("MainActivity", "onCreate: " + file.exists());
                if (file.exists()) {
                    Log.d("start", "run: ");
                    String updata = Utils.updata(path);
                    Log.d("MainActivity", "onCreate: " + updata);
                }
            }
        });
    }

    private void error() {
        Log.d("MainActivity", "下载失败");
        handler.post(new Runnable() {
            public void run() {
                m_progressDlg.cancel();
                Log.i("error error", "没有内存卡，下载失败");
            }
        });
    }
}
