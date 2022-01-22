package com.cc.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements IGetMessageCallBack{
    private MyServiceConnection serviceConnection;
    private MQTTService mqttService;
    public StandardGSYVideoPlayer videoPlayer;
    private int move_direction=0;    //左侧摇杆值
    private int speed=0;             //速度值
    private int right_direction=0;  //右侧摇杆值
    int led=0;  //灯光
    TextView mLogLeft;
    TextView mLogRight;
    TextView move_text;
    TextView deep_text;
    TextView tem_text;
    TextView gps_text;
    TextView energy_text;
    TextView rpy_text;
    TextView led_label;
//    private final String path = "rtmp://58.200.131.2:1935/livetv/cctv1";
    private final String path = "rtmp://rtmp01open.ys7.com:1935/v3/openlive/F77671789_1_1?expire=1672904832&id=400676963705032704&t=27a8ecaf4ff569c0198fdc5cd71affe53b37c90520f60fe497656da95d0ce643&ev=100";
    private String receive_move;
    private String receive_deep;
    private String receive_tem;
    private String receive_gps;
    private String receive_energy;
    private String receive_pox_r;
    private String receive_pox_p;
    private String receive_pox_y;

    @Override
    public void onCreate(Bundle icicle) {
        //EXOPlayer内核，支持格式更多
        PlayerFactory.setPlayManager(Exo2PlayerManager.class);
        super.onCreate(icicle);
        //设置窗体为没有标题的模式
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        init();
        initv();
        serviceConnection = new MyServiceConnection();
        serviceConnection.setIGetMessageCallBack(MainActivity.this);
        Intent intent = new Intent(this, MQTTService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        // 定时发送消息
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true)
                    {
                        Thread.sleep(200);//延时发送
                        //do something
                        if (MQTTService.is_connect)
                        {
                            send_data();
                        }
//
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    private void send_data()
    {
        String mqtt_topic =  String.format("control_data_%s",MQTTService.device_code);
        String mqtt_data="";
        mqtt_data=String.format("{\"move_direction\":%d,\"right_direction\":%d,\"speed\":%d}", move_direction,right_direction,speed);
        MQTTService.publish_topic(mqtt_topic,mqtt_data);
    }


    private void initv() {
        mLogLeft = findViewById(R.id.log_left);
        mLogRight = findViewById(R.id.log_right);
        RockerView rockerViewLeft = findViewById(R.id.rockerView_left);
        if (rockerViewLeft != null) {
            rockerViewLeft.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
            rockerViewLeft.setOnAngleChangeListener(new RockerView.OnAngleChangeListener(){
                @Override
                public void onStart() {
                    mLogLeft.setText(null);
                }

                @Override
                public void angle(double angle,int speed_){
                    angle = ((-angle+360)%360+270)%360;
                    move_direction=(int) angle;
                    speed=speed_;
                    mLogLeft.setText("摇动 : " + angle+" "+speed);
                }

                @Override
                public void onFinish() {
                    move_direction=0;
                    mLogLeft.setText("回中");
                }
            });
        }

        RockerView rockerViewRight = findViewById(R.id.rockerView_right);
        if (rockerViewRight != null) {
            rockerViewRight.setOnShakeListener(RockerView.DirectionMode.DIRECTION_4_ROTATE_45, new RockerView.OnShakeListener() {
                @Override
                public void onStart() {
                    mLogRight.setText(null);
                }

                @Override
                public void direction(RockerView.Direction direction) {
                    mLogRight.setText("摇动方向 : " + getDirectionRight(direction));
                }

                @Override
                public void onFinish() {
                    right_direction=0;
                    mLogRight.setText("回中");
                }
            });
        }
    }

    private void init() {
        videoPlayer = findViewById(R.id.videoPlayer);
        /**此中内容：优化加载速度，降低延迟*/
        VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp");
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "allowed_media_types", "video"); //根据媒体类型来配置
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 20000);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1316);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 1);  // 无限读
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1); //需要准备好后自动播放
        list.add(videoOptionModel);
        /**rtmp秒开*/
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10240);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1);
        list.add(videoOptionModel);
        //关闭播放器缓冲，这个必须关闭，否则会出现播放一段时间后，一直卡主，控制台打印 FFP_MSG_BUFFERING_START
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        list.add(videoOptionModel);
        /**rtmp实时*/
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        list.add(videoOptionModel);
        videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 8);
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);

        videoPlayer.setUp(path, true, "");
        videoPlayer.getTitleTextView().setVisibility(View.GONE);
        videoPlayer.getBackButton().setVisibility(View.GONE);
        videoPlayer.setIsTouchWiget(false);
        videoPlayer.startPlayLogic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayer.onVideoResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayer.setVideoAllCallBack(null);
        GSYVideoManager.releaseAllVideos();
    }

    @Override
    public void onBackPressed() {
//        //先返回正常状态
//        if (orientationUtils != null && orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
//            videoPlayer.getFullscreenButton().performClick();
//            return;
//        }
//        //释放所有
//        videoPlayer.setVideoAllCallBack(null);
        super.onBackPressed();
    }

    private String getDirection(RockerView.Direction direction) {
        String message = null;

        String mqtt_topic =  String.format("control_data_%s",MQTTService.device_code);
        String mqtt_data = "{\"move_direction\":\"" + 0 + "\"}";
//        停止  0
//        前进  1
//        后退  2
//        左转  3
//        右转  4
//        上升  5
//        下降  6
        switch (direction) {
            case DIRECTION_LEFT:
                mqtt_data = "{\"move_direction\":\"" + 3 + "\"}";
//                message = "左";
                message = "左";
                move_direction=3;
                break;
            case DIRECTION_RIGHT:
                mqtt_data = "{\"move_direction\":\"" + 4 + "\"}";
//                message = "右";
                message = "右";
                move_direction=4;
                break;
            case DIRECTION_UP:
                mqtt_data = "{\"move_direction\":\"" + 1 + "\"}";
//                message = "上";
                message = "前";
                move_direction=1;
                break;
            case DIRECTION_DOWN:
                mqtt_data = "{\"move_direction\":\"" + 2 + "\"}";
//                message = "下";
                message = "后";
                move_direction=2;
                break;
//            case DIRECTION_UP_LEFT:
//                mqtt_data = "{\"move_direction\":\"" + 5 + "\"}";
//                message = "左上";
//                break;
//            case DIRECTION_UP_RIGHT:
//                mqtt_data = "{\"move_direction\":\"" + 6 + "\"}";
//                message = "右上";
//                break;
//            case DIRECTION_DOWN_LEFT:
//                mqtt_data = "{\"move_direction\":\"" + 7 + "\"}";
//                message = "左下";
//                break;
//            case DIRECTION_DOWN_RIGHT:
//                mqtt_data = "{\"move_direction\":\"" + 8 + "\"}";
//                message = "右下";
//                break;
            default:
                mqtt_data = "{\"move_direction\":\"" + 0 + "\"}";
                message = "default";
                move_direction=0;
                break;
        }
        switch (move_direction)
        {
            case 0:
                mqtt_data = "{\"move_direction\":\"" + 0 + "\"}";
                break;
            case 1:
                mqtt_data = "{\"move_direction\":\"" + 1 + "\"}";
                break;
            case 2:
                mqtt_data = "{\"move_direction\":\"" + 2 + "\"}";
                break;
            case 3:
                mqtt_data = "{\"move_direction\":\"" + 3 + "\"}";
                break;
            case 4:
                mqtt_data = "{\"move_direction\":\"" + 4 + "\"}";
                break;
        }

        MQTTService.publish_topic(mqtt_topic,mqtt_data);
        return message;
    }


    private String getDirectionRight(RockerView.Direction direction) {
        String message = null;

        String mqtt_topic =  String.format("control_data_%s",MQTTService.device_code);
        String mqtt_data = "{\"move_direction\":\"" + 0 + "\"}";
//        停止  0
//        前进  1
//        后退  2
//        左转  3
//        右转  4
//        上升  5
//        下降  6
        switch (direction) {
            case DIRECTION_LEFT:
                mqtt_data = "{\"move_direction\":\"" + 3 + "\"}";
//                message = "左";
                message = "左";
                right_direction=7;
                break;
            case DIRECTION_RIGHT:
                mqtt_data = "{\"move_direction\":\"" + 4 + "\"}";
//                message = "右";
                message = "右";
                right_direction=8;
                break;
            case DIRECTION_UP:
                message = "上";
                right_direction=5;
                break;
            case DIRECTION_DOWN:
                right_direction=6;
                message = "下";
                break;
            default:
                message = "default";
                right_direction=0;
                break;
        }
        switch (move_direction)
        {
            case 0:
                mqtt_data = "{\"move_direction\":\"" + 0 + "\"}";
                break;
            case 5:
                mqtt_data = "{\"move_direction\":\"" + 5 + "\"}";
                break;
            case 6:
                mqtt_data = "{\"move_direction\":\"" + 6 + "\"}";
                break;
        }
//        MQTTService.publish_topic(mqtt_topic,mqtt_data);
        return message;
    }
    /*
     * 复位按钮
     * */
    public void reset(View view) {
        MQTTService.publish("测试一下子");

        if(!ButtonDelayUtil.isFastClick()){
            return;
        }
        System.out.println("xxxx");
        showToast("复位按钮！");
        okHttp.reset(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }

    /*
     * 自动模式按钮
     * */
    public void automaticMode(View view) {
        if(!ButtonDelayUtil.isFastClick()){
            return;
        }
        showToast("自动模式按钮！");
        okHttp.automaticMode(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }
    public void ledToggle(View view) {
        if(!ButtonDelayUtil.isFastClick()){
            return;
        }
        led_label = findViewById(R.id.led);
        if (led==0)
        {
            led_label.setText("开");
            led=1;
            showToast("打开led！");
        }
        else if (led==1)
        {
            led_label.setText("关");
            led=0;
            showToast("关闭led！");
        }
        String mqtt_topic =  String.format("switch_%s",MQTTService.device_code);
        String mqtt_data = "{\"led\":\"" + led + "\"}";
        MQTTService.publish_topic(mqtt_topic,mqtt_data);
    }
    /*
     * 关闭影像按钮
     * */
    public void closeCamera(View view) {
        if(!ButtonDelayUtil.isFastClick()){
            return;
        }
        showToast("关闭影像按钮！");
        okHttp.closeCamera(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }

    /*
     * 加速
     * */
    public void addition(View view) {
        if(!ButtonDelayUtil.isFastClick()){
            return;
        }
        showToast("放！");
        String mqtt_topic =  String.format("switch_%s",MQTTService.device_code);
        String mqtt_data = "{\"line\":\"" + 1 + "\"}";
        MQTTService.publish_topic(mqtt_topic,mqtt_data);
        okHttp.addition(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }

    /*
     * 减速按钮
     * */
    public void reduce(View view) {
        if(!ButtonDelayUtil.isFastClick()){
            return;
        }
        showToast("收！");
        String mqtt_topic =  String.format("switch_%s",MQTTService.device_code);
        String mqtt_data = "{\"line\":\"" + 2 + "\"}";
        MQTTService.publish_topic(mqtt_topic,mqtt_data);
        okHttp.reduce(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }

    /*接受消息回调
     * 参数消息
     * */
    @Override
    public void setMessage(String message) {
        Log.i("test","111");
    }

    /*接受消息回调
    * 第一个参数话题  第二个参数消息
    * */
    @Override
    public void setTopicMessage(String topic,String message) throws JSONException {
        Log.i(topic,message);
        if (topic.equals(MQTTService.myTopicList[0]))
        {
            Map map = JSON.parseObject(message);
            receive_move = (String) map.get("move");
            receive_deep = (String) map.get("deep");
            receive_tem = (String) map.get("tem");
            receive_gps = (String) map.get("gps");
            receive_energy = (String) map.get("energy");
            // 设置基础信息标签
            move_text = findViewById(R.id.move_text);
            deep_text = findViewById(R.id.deep_text);
            tem_text = findViewById(R.id.tem_text);
            gps_text = findViewById(R.id.gps_text);
            energy_text = findViewById(R.id.energy_text);
            if (receive_move.equals("0"))
            {
                move_text.setText("停止");
            }
            else if (receive_move.equals("1"))
            {
                move_text.setText("前进");
            }
            else if (receive_move.equals("2"))
            {
                move_text.setText("后退");
            }
            else if (receive_move.equals("3"))
            {
                move_text.setText("左转");
            }
            else if (receive_move.equals("4"))
            {
                move_text.setText("右转");
            }
            else if (receive_move.equals("5"))
            {
                move_text.setText("上升");
            }
            else if (receive_move.equals("6"))
            {
                move_text.setText("下降");
            }
            deep_text.setText(" 深度: " + receive_deep+"m");
            tem_text.setText(" 温度: " + receive_tem+"\u2103");
            if (receive_gps.length()>5)
            {
                gps_text.setText(" GPS状态 : 正常");
            }
            else
            {
                gps_text.setText(" GPS状态 : 异常");

            }
            energy_text.setText(" 电量 : " + receive_energy);

        }
        else if (topic.equals(MQTTService.myTopicList[1]))
        {
            Map map1 = JSON.parseObject(message);
            receive_pox_r = (String) map1.get("pos_r");
            receive_pox_p = (String) map1.get("pos_p");
            receive_pox_y = (String) map1.get("pos_y");
            // 设置角度标签
            rpy_text = findViewById(R.id.rpy_text);
            rpy_text.setText("姿态: " + receive_pox_r+" "+receive_pox_p+" "+receive_pox_y);
        }
    }
}


