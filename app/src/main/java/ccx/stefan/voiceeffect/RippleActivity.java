package ccx.stefan.voiceeffect;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;


import java.util.Timer;
import java.util.TimerTask;

public class RippleActivity extends AppCompatActivity {
    private int[] voice = new int[]{0, 2, 32, 90, 0, 35, 64, 100, 23, 5, 36, 84, 43, 89, 21, 93, 75, 23, 97, 23, 12, 92, 13, 23, 0, 89, 21, 93, 75, 0, 97, 23, 0, 92, 0, 23};
    int x = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_ripple);

        final CustomActiveView rippleView = (CustomActiveView) findViewById(R.id.ripple_effect);
        rippleView.setRippleNum(1);
//        rippleView.setMode(RippleView.MODE_OUT);
//        rippleView.startRippleAnimation();

        rippleView.setTargetAnimProgress(100);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                x++;
                x %= voice.length;
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        rippleView.setTargetAnimProgress(voice[x]);
                    }
                });

            }
        }, 3000, 400);
    }
}
