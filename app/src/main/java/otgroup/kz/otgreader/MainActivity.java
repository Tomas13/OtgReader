package otgroup.kz.otgreader;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    private static final int SCAN_RESULT = 2;
    private static final double FREQ = 17000;
    String TAG = "MainAc";
    static final int SCAN_QR_REQUEST = 1;  // The request code

    private int balance = 1000;

    @BindView(R.id.text_view_read)
    TextView textViewRead;

    @BindView(R.id.text_view_balance)
    TextView textViewBalance;

    @BindView(R.id.edit_text_summ)
    EditText editTextSumm;

    @OnClick(R.id.btn_scan)
    public void scan(View view) {
        startActivityForResult(new Intent(this, ScanActivity.class), SCAN_QR_REQUEST);
    }

    private AudioTrack generateTone(double freqHz, int durationMs) {
        int count = (int) (44100.0 * 2.0 * (durationMs / 1000.0)) & ~1;
        short[] samples = new short[count];
        for (int i = 0; i < count; i += 2) {
            short sample = (short) (Math.sin(2 * Math.PI * i / (44100.0 / freqHz)) * 0x7FFF);
            samples[i + 0] = sample;
            samples[i + 1] = sample;
        }
        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                count * (Short.SIZE / 8), AudioTrack.MODE_STATIC);
        track.write(samples, 0, count);
        return track;
    }


    @OnClick(R.id.btn_generate_qr_positive)
    public void generateSound(View view) {
        AudioTrack track = generateTone(FREQ, 500);
        track.play();
    }


  /*  @OnClick(R.id.btn_generate_qr_positive)
    public void generatePositive(View view) {
        Intent intent = new Intent(this, QrResultActivity.class);

        if (!editTextSumm.getText().toString().equals("")) {

            String timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";
            String fromThis = editTextSumm.getText() + "&plus&" + timeStamp;
            intent.putExtra("QrBitmap", net.glxn.qrgen.android.QRCode.from(fromThis).bitmap());
            intent.putExtra("Summ", editTextSumm.getText() + "&plus&");
            startActivity(intent);

            balance += Integer.parseInt(editTextSumm.getText().toString());

            Snackbar.make(textViewBalance, getResources().getString(R.string.success_plus) +
                    " " + editTextSumm.getText().toString() + " у.е.",
                    Snackbar.LENGTH_LONG).show();
        } else {
//            Snackbar.make(textViewBalance, getResources().getString(R.string.enter_summ),
//                    Snackbar.LENGTH_LONG).show();

            Toast toast = Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.enter_summ),
                    Toast.LENGTH_SHORT);

            toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
            toast.show();
        }
    }*/


    @OnClick(R.id.btn_generate_qr_negative)
    public void generateNegative(View view) {
        Intent intent = new Intent(this, QrResultActivity.class);

        if (!editTextSumm.getText().toString().equals("")) {
            int summ = Integer.parseInt(String.valueOf(editTextSumm.getText()));
            if (balance >= summ) {
                String timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";
                String fromThis = editTextSumm.getText() + "&minus&" + timeStamp;
                intent.putExtra("QrBitmap", net.glxn.qrgen.android.QRCode.from(fromThis).bitmap());
                intent.putExtra("Summ", editTextSumm.getText() + "&minus&");
                startActivityForResult(intent, SCAN_RESULT);


                /*balance -= Integer.parseInt(editTextSumm.getText().toString());
                Snackbar.make(textViewBalance, getResources().getString(R.string.success_minus) + " " +
                                editTextSumm.getText().toString() + " тенге",
                        Snackbar.LENGTH_LONG).show();*/
            } else {

                Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_enough_balance),
                        Toast.LENGTH_SHORT);

                toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                toast.show();
            }

        } else {

            Toast toast = Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.enter_summ),
                    Toast.LENGTH_SHORT);

            toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        textViewBalance.append(" " + balance);


        //get permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String result = getResources().getString(R.string.balance) + String.valueOf(balance);
        textViewBalance.setText(result);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SCAN_RESULT){
            if (resultCode == RESULT_OK){
                balance -= Integer.parseInt(editTextSumm.getText().toString());
                Snackbar.make(textViewBalance, getResources().getString(R.string.success_minus) + " " +
                                editTextSumm.getText().toString() + " тенге",
                        Snackbar.LENGTH_LONG).show();
            }
        }
        if (requestCode == SCAN_QR_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                //make a 17kHz sound
                AudioTrack track = generateTone(FREQ, 250);
                track.play();


                String resultString = data.getExtras().get("summ").toString();

                String[] resultArray = resultString.split("&");

                String number = resultArray[0];
                String sign = resultArray[1];

                int current;
                if (sign.equals("plus")) {
                    current = Integer.parseInt(number);

                    if (balance >= current) {

                        balance -= current;
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.not_enough_balance),
                                Toast.LENGTH_LONG);

                        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                        toast.show();
//                        Snackbar.make(textViewBalance, getResources().getString(R.string.not_enough_balance),
//                                Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    current = Integer.parseInt(number);
                    balance += current;

                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    // 2. Chain together various setter methods to set the dialog characteristics
                    builder.setMessage("Ваш баланс пополнен на " + current + " тенге");
//                            .setTitle(R.string.dialog_title);

                    // 3. Get the AlertDialog from create()


                    // Add the buttons
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            dialog.cancel();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }


                textViewRead.setText(String.valueOf(current));
                String result = getResources().getString(R.string.balance) + String.valueOf(balance);
                textViewBalance.setText(result);

            }
        }
    }




   /* @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission is granted", Toast.LENGTH_SHORT).show();

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    Toast.makeText(this, "Permission is denied", Toast.LENGTH_SHORT).show();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }*/


}
