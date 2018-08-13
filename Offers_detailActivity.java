package pharmacy.fastmeds;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Offers_detailActivity extends CommonAppCompatActivity {

    private TextView tv_title, tv_from_date, tv_to_date, tv_detail, tv_offer_code;
    private ImageView iv_copy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers_detail);

        tv_title = (TextView) findViewById(R.id.tv_offer_detail_title);
        tv_offer_code = (TextView) findViewById(R.id.tv_offer_detail_code);
        tv_from_date = (TextView) findViewById(R.id.tv_offer_detail_date_from);
        tv_to_date = (TextView) findViewById(R.id.tv_offer_detail_date_to);
        tv_detail = (TextView) findViewById(R.id.tv_offer_detail_detail);
        iv_copy = (ImageView) findViewById(R.id.iv_offer_detail_copy_code);

        String title = getIntent().getStringExtra("title");
        String coupon = getIntent().getStringExtra("coupon");
        String start_date = getIntent().getStringExtra("start_date");
        String end_date = getIntent().getStringExtra("end_date");
        String description = getIntent().getStringExtra("description");

        // set title in actionbar
        getSupportActionBar().setTitle(title);

        tv_title.setText(title);
        tv_offer_code.setText(coupon);
        tv_from_date.setText(start_date);
        tv_to_date.setText(end_date);
        tv_detail.setText(description);

        iv_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //place your TextView's text in clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(tv_offer_code.getText());

                CommonAppCompatActivity.showToast(Offers_detailActivity.this, getResources().getString(R.string.text_copied));
            }
        });

    }
}
