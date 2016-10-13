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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainAc";
    static final int SCAN_QR_REQUEST = 1;  // The request code

    @BindView(R.id.text_view_read)
    TextView textViewRead;

    @BindView(R.id.edit_text_summ)
    EditText editTextSumm;

    @OnClick(R.id.btn_scan)
    public void scan(View view) {
        startActivityForResult(new Intent(this, ScanActivity.class), SCAN_QR_REQUEST);
    }

    @OnClick(R.id.btn_generate_qr_positive)
    public void generatePositive(View view){
        Intent intent = new Intent(this, QrResultActivity.class);
        String fromThis = "+" + editTextSumm.getText();
        intent.putExtra("QrBitmap", net.glxn.qrgen.android.QRCode.from(fromThis).bitmap());
        startActivity(intent);
    }


    @OnClick(R.id.btn_generate_qr_negative)
    public void generateNegative(View view){
        Intent intent = new Intent(this, QrResultActivity.class);
        String fromThis = "-" + editTextSumm.getText();
        intent.putExtra("QrBitmap", net.glxn.qrgen.android.QRCode.from(fromThis).bitmap());
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
                textViewRead.setText(data.getExtras().get("summ").toString());
                // Do something with the contact here (bigger example below)
            }
        }
    }


}
