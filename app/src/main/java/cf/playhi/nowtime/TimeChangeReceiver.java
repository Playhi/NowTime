package cf.playhi.nowtime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

public class TimeChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK) || intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                Message msg = new Message();
                msg.what = 1;
                FloatTimeService.handler.sendMessage(msg);
            }
        }
}
