package com.emotionrobotics.apps.sanbotmotion;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.emotionrobotics.apps.sanbotmotion.Entity.User;
import com.emotionrobotics.apps.sanbotmotion.FaceRec.AuthService;
import com.emotionrobotics.apps.sanbotmotion.FaceRec.Base64Util;
import com.emotionrobotics.apps.sanbotmotion.FaceRec.GsonUtils;
import com.emotionrobotics.apps.sanbotmotion.FaceRec.HttpUtil;
import com.emotionrobotics.apps.sanbotmotion.FaceRec.SingleMediaScanner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qihancloud.opensdk.base.BindBaseActivity;
import com.qihancloud.opensdk.beans.FuncConstant;
import com.qihancloud.opensdk.function.unit.SpeechManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Camera_Activity extends BindBaseActivity {
    public static final int REQUEST_TAKE_PHOTO_CODE=1;
    public static final int REQUST_TAKE_PHOTTO_CODE2=2;
    private ImageView imageView;
    private Button startbtn;
    private Button startface;
    private Handler handler;
    // /storage/emulated/0/pic
    public final static String SAVED_IMAGE_PATH1= Environment.getExternalStorageDirectory().getAbsolutePath()+"/pic";//+"/pic";
    // /storage/emulated/0/Pictures
    public final static String SAVED_IMAGE_PATH=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();//.getAbsolutePath()+"/pic";//+"/pic";
    private String photoPath;
    private EditText userInfoEt;
    private String result;
    private TextView userInfoTv;
    private static final int MESSAGE_TYPE_SECOND = 1;
    private static final int MESSAGE_TYPE_FINISH = 2;
    private Button addface;


    @Override
    public void onCreate(Bundle savedInstanceState)

    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d("SAVED_IMAGE_PATH1",SAVED_IMAGE_PATH1);
        Log.d("SAVED_IMAGE_PATH",SAVED_IMAGE_PATH);

        //布局初始化绑定
        imageView = (ImageView) findViewById(R.id.pic);
        startbtn = (Button) findViewById(R.id.startCamera);
        startface = (Button) findViewById(R.id.startFaceRec);
        userInfoEt = (EditText) findViewById(R.id.userinfoEt);
        userInfoTv = findViewById(R.id.user_infoTv);
        addface=findViewById(R.id.addFaceBtn);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case MESSAGE_TYPE_SECOND:{
                        userInfoTv.setText("Hi,"+String.valueOf(msg.obj));
                        final SpeechManager speechManager = (SpeechManager) getUnitManager(FuncConstant.SPEECH_MANAGER);
                        Log.i("info", "speak: =================== ");
                        speechManager.startSpeak("Welcom back "+String.valueOf(msg.obj));
                        break;
                    }
                    case MESSAGE_TYPE_FINISH:{

                        final SpeechManager speechManager = (SpeechManager) getUnitManager(FuncConstant.SPEECH_MANAGER);
                        speechManager.startSpeak("If you are not sure that your face already exists in the database, Please add face first.");
                        break;
                    }
                }

            }
        };

        addface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExecutorService es = Executors.newSingleThreadExecutor();
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        String access_key = AuthService.getAuth();
                        Log.i("access_key", "run:===== "+access_key);
                        File photoFile=new File(photoPath);
                        if (photoFile.exists()){
                            //通过图片地址将图片加载到bitmap里面
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true; // 只获取图片的大小信息，而不是将整张图片载入在内存中，避免内存溢出
                            BitmapFactory.decodeFile(photoFile.getAbsolutePath(),options);
                            int height = options.outHeight;
                            int width= options.outWidth;
                            int inSampleSize = 1; // 默认像素压缩比例，压缩为原图的1/2
                            int minLen = Math.min(height, width); // 原图的最小边长
                            if(minLen > 100) { // 如果原始图像的最小边长大于100dp（此处单位我认为是dp，而非px）
                                float ratio = (float)minLen / 100.0f; // 计算像素压缩比例
                                inSampleSize = (int)ratio;
                            }
                            options.inJustDecodeBounds = false; // 计算好压缩比例后，这次可以去加载原图了
                            options.inSampleSize = inSampleSize; // 设置为刚才计算的压缩比例
                            Bitmap bm = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
                            // 解码文件
                            String image=null;
                            ByteArrayOutputStream bStream=new ByteArrayOutputStream();
                            bm.compress(Bitmap.CompressFormat.PNG,10,bStream);
                            byte[]bytes=bStream.toByteArray();
                            image= Base64Util.encode(bytes);
                            try {
                                String url = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add";
                                Map<String, Object> map = new HashMap<>();
                                SimpleDateFormat sdf=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                                Date date=new java.util.Date();
                                String userID=sdf.format(date);
                                String userInfo = String.valueOf(userInfoEt.getText());
                                map.put("image", image);
                                map.put("group_id", "Robot");
                                map.put("user_id", userID);
                                map.put("user_info", userInfo);
                                map.put("liveness_control", "NONE");
                                map.put("image_type", "BASE64");
                                map.put("quality_control", "NONE");
                                String param = GsonUtils.toJson(map);
                                // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
                                String accessToken = access_key;
                                String result = HttpUtil.post(url, accessToken, "application/json", param);
                                Log.i("result", "run:==== "+result);
                            } catch (Exception e) {
                                    e.printStackTrace();
                            }
                        }
                        else{
                            Toast.makeText(Camera_Activity.this,"图片文件不存在",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        //调用按钮监听
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //获取SD卡安装状态
                String state= Environment.getExternalStorageState();
                if (state.equals(Environment.MEDIA_MOUNTED)){

                    //设置图片保存路径
                    photoPath=SAVED_IMAGE_PATH+"/"+System.currentTimeMillis()+".png";
                    Log.i("path", "onClick======: "+photoPath);

                    File imageDir=new File(photoPath);
                    if(!imageDir.exists()){
                        try {
                            //根据一个 文件地址生成一个新的文件用来存照片
                            imageDir.createNewFile();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    takePhotoByMethod1();

                    //takePhotoByMethod2();
                }else {
                    Toast.makeText(Camera_Activity.this,"SD卡未插入",Toast.LENGTH_SHORT).show();
                }

            }
        });
        startface.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ExecutorService es = Executors.newSingleThreadExecutor();
                                    es.submit(new Runnable() {
                                        @Override
                                        public void run() {
                                            String access_key = AuthService.getAuth();
                                            Log.i("access_key", "run:===== "+access_key);
                                            File photoFile=new File(photoPath);
                                            if (photoFile.exists()){
                                                //通过图片地址将图片加载到bitmap里面
                                                BitmapFactory.Options options = new BitmapFactory.Options();
                                                options.inJustDecodeBounds = true; // 只获取图片的大小信息，而不是将整张图片载入在内存中，避免内存溢出
                                                BitmapFactory.decodeFile(photoFile.getAbsolutePath(),options);
                                                int height = options.outHeight;
                                                int width= options.outWidth;
                                                int inSampleSize = 1; // 默认像素压缩比例，压缩为原图的1/2
                                                int minLen = Math.min(height, width); // 原图的最小边长
                                                if(minLen > 100) { // 如果原始图像的最小边长大于100dp（此处单位我认为是dp，而非px）
                                                    float ratio = (float)minLen / 100.0f; // 计算像素压缩比例
                                                    inSampleSize = (int)ratio;
                                                }
                                                options.inJustDecodeBounds = false; // 计算好压缩比例后，这次可以去加载原图了
                                                options.inSampleSize = inSampleSize; // 设置为刚才计算的压缩比例
                                                Bitmap bm = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
                                                // 解码文件
                                                String image=null;
                                                ByteArrayOutputStream bStream=new ByteArrayOutputStream();
                                                bm.compress(Bitmap.CompressFormat.PNG,10,bStream);
                                                byte[]bytes=bStream.toByteArray();
                                                image= Base64Util.encode(bytes);
                                                //人脸搜索
                                                try {
                                                    String url = "https://aip.baidubce.com/rest/2.0/face/v3/search";
                                                    Map<String, Object> map = new HashMap<>();
                                                    map.put("image", image);
                                                    map.put("liveness_control", "NONE");
                                                    map.put("group_id_list", "Robot");
                                                    map.put("image_type", "BASE64");
                                                    map.put("quality_control", "NONE");

                                                    String param = GsonUtils.toJson(map);

                                                    // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
                                                    String accessToken = access_key;
                                                    result = HttpUtil.post(url, accessToken, "application/json", param);
                                                    Log.i("result", "run:==== "+result);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                Gson gson = new Gson();
                                                Map<String,Object> user_map =  gson.fromJson(result,new TypeToken<Map<String,Object>>(){}.getType());
                                                Map<String,Object> user_result = (Map<String, Object>) user_map.get("result");
                                                List<Map<String,Object>> user_list = (List<Map<String,Object>>) user_result.get("user_list");

                                                Log.i("user", "run: ========");
                                                Message message = Message.obtain();
                                                message.obj = user_list.get(0).get("user_info");
                                                handler.sendMessage(message);
                                                message.what = MESSAGE_TYPE_SECOND;

                            //人脸入库
//                            try {
//                                String url = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add";
//                                Map<String, Object> map = new HashMap<>();
//                                SimpleDateFormat sdf=new SimpleDateFormat("yyyy_MM_dd");
//                                Date date=new java.util.Date();
//                                String userID=sdf.format(date);
//                                String userInfo = String.valueOf(userInfoEt.getText());
//                                map.put("image", image);
//                                map.put("group_id", "Robot");
//                                map.put("user_id", userID);
//                                map.put("user_info", userInfo);
//                                map.put("liveness_control", "NORMAL");
//                                map.put("image_type", "BASE64");
//                                map.put("quality_control", "LOW");
//                                String param = GsonUtils.toJson(map);
//                                // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
//                                String accessToken = access_key;
//                                String result = HttpUtil.post(url, accessToken, "application/json", param);
//                                Log.i("result", "run:==== "+result);
//                            } catch (Exception e) {
//                                    e.printStackTrace();
//                            }
                        }
                        else{
                        Toast.makeText(Camera_Activity.this,"图片文件不存在",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });




    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode==REQUEST_TAKE_PHOTO_CODE&&resultCode== Activity.RESULT_OK){
            File photoFile=new File(photoPath);
            new SingleMediaScanner(this, photoFile);
            if (photoFile.exists()){
                //通过图片地址将图片加载到bitmap里面
                Bitmap bm= BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(bm);
            }else {
                Toast.makeText(Camera_Activity.this,"图片文件不存在",Toast.LENGTH_LONG).show();
            }
        }else if (requestCode==REQUST_TAKE_PHOTTO_CODE2&&resultCode==Activity.RESULT_OK){
            Bundle bundle=data.getExtras();
            if (bundle!=null){
                Bitmap bm= (Bitmap) bundle.get("data");
                if (bm!=null){
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bm);
                }
            }else {
                Toast.makeText(Camera_Activity.this,"没有压缩的图片数据",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void takePhotoByMethod1(){
        //实例化intent,指向摄像头
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //根据路径实例化图片文件
        File photoFile=new File(photoPath);
        //设置拍照后图片保存到文件中
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        //启动拍照activity并获取返回数据
        startActivityForResult(intent,REQUEST_TAKE_PHOTO_CODE);
    }
    private void takePhotoByMethod2(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,REQUST_TAKE_PHOTTO_CODE2);
    }
//    public String bitmapToString(Bitmap bitmap){
//        //将Bitmap转换成字符串
//        String string=null;
//        ByteArrayOutputStream bStream=new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG,30,bStream);
//        byte[]bytes=bStream.toByteArray();
//
//        string= Base64.encodeToString(bytes, Base64.DEFAULT);
//        return string;
//    }

    @Override
    protected void onMainServiceConnected() {

    }

}
