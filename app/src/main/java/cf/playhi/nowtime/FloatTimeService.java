package cf.playhi.nowtime;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;


public class FloatTimeService extends Service {
    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    WindowManager mWindowManager;
    static Button mFloatButton;
    TimeChangeReceiver mTimeChangeReceiver;
    IntentFilter mFilter;

    static final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
                mFloatButton.setText(df.format(new Date()));
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatView();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createFloatView() {
        wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        //设置window type
        wmParams.type = TYPE_SYSTEM_ALERT;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.START | Gravity.TOP;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        //浮动窗口按钮
        mFloatButton = (Button) mFloatLayout.findViewById(R.id.float_button);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        //监听浮动窗口的触摸移动
        mFloatButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                wmParams.x = (int) event.getRawX() - mFloatButton.getMeasuredWidth() / 2;
                //减25状态栏高度
                wmParams.y = (int) event.getRawY() - mFloatButton.getMeasuredHeight() / 2 - 25;
                //刷新
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                return false;
            }
        });
        //通知立即更新时间
        Message msg = new Message();
        msg.what = 1;
        FloatTimeService.handler.sendMessage(msg);

        //启动每分钟更新时间
        mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_TIME_TICK); //每分钟变化的action
        mFilter.addAction(Intent.ACTION_TIME_CHANGED); //设置了系统时间的action
        mTimeChangeReceiver = new TimeChangeReceiver();
        registerReceiver(mTimeChangeReceiver, mFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeFloatView();
    }

    private void closeFloatView(){
        if (mFloatLayout != null) {
            //移除悬浮窗口
            mWindowManager.removeView(mFloatLayout);
            //停止对时间变化的监听
            unregisterReceiver(mTimeChangeReceiver);
        }
    }
}
