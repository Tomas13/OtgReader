package otgroup.kz.otgreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    @OnClick(R.id.btn_generate_qr_positive)
    public void generatePositive(View view) {
        Intent intent = new Intent(this, QrResultActivity.class);

        String timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";
        String fromThis = "+" + editTextSumm.getText() + "&" + timeStamp;
        intent.putExtra("QrBitmap", net.glxn.qrgen.android.QRCode.from(fromThis).bitmap());
        intent.putExtra("Summ", "+" + editTextSumm.getText());
        startActivity(intent);
    }


    @OnClick(R.id.btn_generate_qr_negative)
    public void generateNegative(View view) {
        Intent intent = new Intent(this, QrResultActivity.class);

        String timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";
        String fromThis = "-" + editTextSumm.getText() + "&" + timeStamp;
        intent.putExtra("QrBitmap", net.glxn.qrgen.android.QRCode.from(fromThis).bitmap());
        intent.putExtra("Summ", "-" + editTextSumm.getText());
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SCAN_QR_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                String resultString = data.getExtras().get("summ").toString();

                String[] resultArray = resultString.split("&");

                String number = resultArray[0];
                String sign = resultArray[1];

                int current;
                if (sign.equals("plus")) {
                    current = Integer.parseInt(number);
                }else{
                    current = -Integer.parseInt(number);
                }
                textViewRead.setText(String.valueOf(current));
                textViewBalance.setText(R.string.balance + String.valueOf(current));

//                "2000&plus&sdf";
            }
        }
    }


}
