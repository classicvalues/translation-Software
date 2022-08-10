package edu.lit.test02;

import static edu.lit.test02.tool.getDst;
import static edu.lit.test02.tool.getSrc;
import static edu.lit.test02.tool.isContainChinese;
import static edu.lit.test02.tool.taskStr;
import static edu.lit.test02.tool.unicodeDecode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.lit.test02.demo.TransApi;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //百度翻译接口id 密钥
    private static final String APP_ID = "20220721001279009";
    private static final String SECURITY_KEY = "irg2GrTeSjZyQjhMc1Tx";

    private MediaPlayer mediaPlayer = new MediaPlayer();

    //放音频路径
    private String curMusic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setContentView(R.layout.activity_main);

        findViewById(R.id.query1).setOnClickListener(this);
        findViewById(R.id.query2).setOnClickListener(this);
        findViewById(R.id.play).setOnClickListener(this);

        //播放音频 权限判断，如果没有权限就请求权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            initMediaPlayer();//初始化播放器 MediaPlayer
        }


    }

    @Override
    public void onClick(View view) {
        System.out.println("是否在运行："+mediaPlayer.isPlaying());
        EditText re = (EditText)findViewById(R.id.result);
        EditText getText = (EditText)findViewById(R.id.text);
        String text = getText.getText().toString();
        System.out.println("获取的需要翻译的文本："+text);
        System.out.println("开始翻译");
        TransApi api = new TransApi(APP_ID, SECURITY_KEY);
        System.out.println("获取了api");
        String query = text;
        String result = null;
        if (view.getId() == R.id.query1){
            try {
                System.out.println("准备把输入词语的翻译-->汉语");
                result = api.getTransResult(query, "auto", "zh");
                //打印一下返回的结果
                System.out.println("返回的结果："+unicodeDecode(result));
                //把返回的结果做一下处理再输出到页面
                String taskedStr = taskStr(unicodeDecode(result));
                //把处理过的的结果输出界面
                re.setText(taskedStr);
                //读音
                read(unicodeDecode(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (view.getId() == R.id.query2){
            try {
                System.out.println("准备把输入词语的翻译-->英语");
                result = api.getTransResult(query, "auto", "en");
                //打印一下返回的结果
                System.out.println("返回的结果："+unicodeDecode(result));
                //把返回的结果做一下处理再输出到页面
                String taskedStr = taskStr(unicodeDecode(result));
                //把处理过的的结果输出界面
                re.setText(taskedStr);
                //读音
                read(unicodeDecode(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (view.getId() == R.id.play){
            AlertDialog alertDialog1 = new AlertDialog.Builder(this)
                    .setTitle("不会就翻译一下把")//标题
                    .setMessage("目前已实现英翻汉，汉翻英，读音 \n  Self use")//内容
                    .setIcon(R.drawable.fy)//图标
                    .create();
            alertDialog1.show();
        }

    }


    //播放音频
    //初始化
    private void initMediaPlayer() {
        EditText getText = (EditText)findViewById(R.id.text);
        String text = getText.getText().toString();
        if (text.equals("")){
            text="hello";
        }
        System.out.println("text:"+text);
        String netPath = "https://dict.youdao.com/dictvoice?audio=";
        String path = netPath+text;
        System.out.println("最终音频路径为："+path);
        try {
//            File file = new File(Environment.getExternalStorageDirectory(), "good.mp3");
//            mediaPlayer.setDataSource(file.getPath());//指定音频文件路径
            mediaPlayer.setDataSource(path);//指定音频文件路径
            mediaPlayer.setLooping(true);//设置为循环播放
            mediaPlayer.prepare();//初始化播放器MediaPlayer

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initMediaPlayer();
                }else{
                    Toast.makeText(this, "拒绝权限，将无法使用程序。", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
        }

    }

    @Override
    protected void onDestroy() {
        //销毁进程
        super.onDestroy();
        if(mediaPlayer != null){
            System.out.println("关闭进程。");
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }





    //此方法可以中途切换音频
    private void playMusic(){
        try{
            if (mediaPlayer==null){
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(curMusic);
                mediaPlayer.prepare();
                mediaPlayer.start();
            }else if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        try{
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(curMusic);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        }catch (Exception e){
                            curMusic = "";
                            e.printStackTrace();
                        }
                    }
                }, 1000);

            }else{
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(curMusic);
                mediaPlayer.prepare();
                mediaPlayer.start();
            }

        }catch (Exception e){
            curMusic = "";
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                curMusic = "";
            }
        });
    }

    //点击翻译自动发音
    private void read(String s){
        String netPath = "https://dict.youdao.com/dictvoice?audio=";
        String path = "";
//        if (text.equals("")){
//            text="hello";
//            path = netPath+text;
//        }else {
            String src = getSrc(s);
            boolean containChinese = isContainChinese(src);
            if (containChinese){//为true则包含中文 读音则读结果dst
                String dst = getDst(s);
                path = netPath+dst;
            }else {
                path = netPath + src;
            }
        //}
        System.out.println("音频路径为："+path);
        curMusic=path;
        playMusic();
    }
}


