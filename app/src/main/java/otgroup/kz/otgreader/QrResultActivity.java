package otgroup.kz.otgreader;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QrResultActivity extends AppCompatActivity {

    Timer timer;
    TimerTask timerTask;

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    @BindView(R.id.image_view_qr_result)
    ImageView imageViewQrResult;

    String summ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_result);

        ButterKnife.bind(this);
        Bitmap bitmap = (Bitmap) getIntent().getExtras().get("QrBitmap");
        imageViewQrResult.setImageBitmap(bitmap);

        summ = getIntent().getExtras().getString("Summ");
    }

    @Override
    protected void onResume() {
        super.onResume();

        //onResume we start our timer so it can start when the app comes from the background
        startTimer();
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 5000, 5000); //
    }

    @Override
    protected void onPause() {
        super.onPause();
        stoptimertask();
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //get the current timeStamp
//                        Calendar calendar = Calendar.getInstance();
//                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
//                        final String strDate = simpleDateFormat.format(calendar.getTime());

                        String timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";

                        Bitmap bitmap = net.glxn.qrgen.android.QRCode.from(summ + "&" + timeStamp).bitmap();
                        imageViewQrResult.setImageBitmap(bitmap);

                        //show the toast
//                        int duration = Toast.LENGTH_SHORT;
//                        Toast toast = Toast.makeText(getApplicationContext(), timeStamp, duration);
//                        toast.show();
                    }
                });
            }
        };
    }
}
