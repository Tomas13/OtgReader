package otgroup.kz.otgreader;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QrResultActivity extends AppCompatActivity {

    @BindView(R.id.image_view_qr_result)
    ImageView imageViewQrResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_result);

        ButterKnife.bind(this);
        Bitmap bitmap = (Bitmap) getIntent().getExtras().get("QrBitmap");
        imageViewQrResult.setImageBitmap(bitmap);
    }
}
