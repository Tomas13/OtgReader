package otgroup.kz.otgreader;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity {
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
                startActivity(intent);


                balance -= Integer.parseInt(editTextSumm.getText().toString());
                Snackbar.make(textViewBalance, getResources().getString(R.string.success_minus) + " " +
                                editTextSumm.getText().toString() + " тенге",
                        Snackbar.LENGTH_LONG).show();
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
        if (requestCode == SCAN_QR_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

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


}
