package com.xa.okhttpdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

//    OkHttpClient okHttpClient = new OkHttpClient();
OkHttpClient okHttpClient = new OkHttpClient.Builder()
    .cookieJar(new CookieJar() {
        private Map<String, List<Cookie>> cookieStore = new HashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url.host(), cookies);
            }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url.host());
            return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        }).build();

    private TextView mTvResult;
    private ImageView mImageView;
    private String mBaseUrl = "http://192.168.1.107:8080/okhttpDemo/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        CookieManager cookieManager = new CookieManager();
//
//        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
//
//        CookieJar cookieJar = new JavaNetCookieJar(cookieManager);
//
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//
//        builder.cookieJar(cookieJar);
//
//        okHttpClient = builder.build();


        mTvResult = (TextView) findViewById(R.id.id_tv_result);
        mImageView = (ImageView) findViewById(R.id.id_im_result);

    }

    //post数据
    public void doPost(View view) {
        //1.拿到okHttpClient对象
        //OkHttpClient okHttpClient = new OkHttpClient();
        //2.构造Request
        Request.Builder builder = new Request.Builder();
        //FormBody extends RequestBody
        FormBody formbody = new FormBody.Builder().add("username", "xa").add("password", "123456").build();
        Request request = builder
                .url(mBaseUrl + "login")
                .post(formbody)
                .build();
        executeRequest(request);
    }

    //Post一个字符串
    public void doPostString(View view) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;chaset=UTF-8"),"{username:xa,password:654321}");
        //2.构造Request
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(mBaseUrl + "postString")
                .post(requestBody)
                .build();

        executeRequest(request);
    }

    //Post文件
    public void doPostFile(View view) {
        File file=new File(Environment.getExternalStorageDirectory(),"picture8.jpg");
        if (!file.exists()) {
            L.e(file.getAbsolutePath() + " not exist!");
            return;
        }
        //mime type
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"),file);
        //2.构造Request
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(mBaseUrl + "postFile")
                .post(requestBody)
                .build();

        executeRequest(request);
    }

    //UpLoad 数据
    public void doUpload(View view) {
        final File file=new File(Environment.getExternalStorageDirectory(),"picture8.jpg");
        if (!file.exists()) {
            L.e(file.getAbsolutePath() + " not exist!");
            return;
        }
        MultipartBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("username","xa")
                .addFormDataPart("password","445566")
                .addFormDataPart("mPhoto","picture.jpg",RequestBody.create(MediaType.parse("image/jpeg"),file))
                .build();

        CountingRequestBody countingRequrstBody=new CountingRequestBody(requestBody, new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(long byteWrited, long contentLength) {
                L.e(byteWrited + "/" + contentLength);
                final long finalbyteWrited=byteWrited;
                final long finalcontentLength=contentLength;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvResult.setText(finalbyteWrited + "/" + finalcontentLength);
                    }
                });
            }
        });

        //2.构造Request
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(mBaseUrl + "uploadInfo")
                .post(countingRequrstBody)
                .build();

        executeRequest(request);
    }


    //下载图片
    public void doDownload(View view) {
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .get()
                .url(mBaseUrl+"files/picture.jpg")
                .build();
        //3.将Request封装为Call
        Call call = okHttpClient.newCall(request);
        //4.执行call
        //Reponse response = call execute();
        call.enqueue(new Callback() {
            private Response response;

            @Override
            public void onFailure(Call call, IOException e) {
                L.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                L.e("onResponse");

                final long total = response.body().contentLength();
                long sum=0L;

                InputStream is = response.body().byteStream();

                int len = 0;
                //new File(Environment.getExternalStorageDirectory().toString()).mkdirs();
                String path=Environment.getExternalStorageDirectory()+"/films";
                File folder = new File(path);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                File file = new File(path,"xa123.jpg");
                byte[] buf = new byte[128];
                FileOutputStream fos = new FileOutputStream(file);

                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                    sum=sum+len;
                    L.e(sum + "/" + total);
                    final long finalSum = sum;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvResult.setText(finalSum + "/" + total);
                        }
                    });
                }
                fos.flush();
                fos.close();
                is.close();
                L.e("download success!");
                }
        });
    }

    //下载文件
    public void doDownloadImg(View view) {
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .get()
                .url(mBaseUrl+"files/picture.jpg")
                .build();
        //3.将Request封装为Call
        Call call = okHttpClient.newCall(request);
        //4.执行call
        //Reponse response = call execute();
        call.enqueue(new Callback() {
            private Response response;

            @Override
            public void onFailure(Call call, IOException e) {
                L.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                L.e("onResponse");
                InputStream is = response.body().byteStream();

                //BitmapFactory.Options

                final Bitmap bitmap = BitmapFactory.decodeStream(is);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(bitmap);
                    }
                });
                L.e("download success!");
            }
        });
    }


    //Get
    public void doGet(View view) {
        //1.拿到okHttpClient对象
//        OkHttpClient okHttpClient = new OkHttpClient();
        //2.构造Request
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .get()
                .url(mBaseUrl+"login?username=xa&password=1234")
                .build();
        executeRequest(request);

    }

    //将Request封装为Call,执行Call
    private void executeRequest(Request request) {
        //3.将Request封装为Call
        Call call = okHttpClient.newCall(request);
        //4.执行call
        //Reponse response = call execute();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                L.e("onResponse");
                final String res = response.body().string();
                L.e(res);
//              InputStream is = response.body().byteStream();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvResult.setText(res);
                    }
                });

            }
        });
    }
}
