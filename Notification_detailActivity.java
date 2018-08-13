package pharmacy.fastmeds;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import Config.BaseURL;

public class Notification_detailActivity extends CommonAppCompatActivity {

    private TextView tv_date, tv_detail, tv_title;
    private ImageView iv_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        Intent args = getIntent();

        String title = args.getStringExtra("title");
        String date = args.getStringExtra("date");
        String message = args.getStringExtra("desc");
        String image = args.getStringExtra("image_path");

        tv_title = (TextView) findViewById(R.id.tv_notific_title);
        tv_detail = (TextView) findViewById(R.id.tv_notific_detail);
        tv_date = (TextView) findViewById(R.id.tv_notific_date);
        iv_img = (ImageView) findViewById(R.id.iv_notific_img);

        tv_title.setText(title);
        tv_date.setText(date);
        tv_detail.setText(message);

        if (image == null) {
            iv_img.setVisibility(View.GONE);
        } else {
            iv_img.setVisibility(View.VISIBLE);
            Picasso.with(this)
                    .load(BaseURL.IMG_NOTIFICATION_URL + image)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(iv_img);
        }

    }
}
