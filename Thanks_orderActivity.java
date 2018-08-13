package pharmacy.fastmeds;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class Thanks_orderActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_msg, tv_countinue, tv_track;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanks_order);

        tv_msg = (TextView) findViewById(R.id.tv_thanks_msg);
        tv_countinue = (TextView) findViewById(R.id.tv_thanks_countinue);
        tv_track = (TextView) findViewById(R.id.tv_thanks_track);

        String msg = getIntent().getStringExtra("msg");
        tv_msg.setText(Html.fromHtml(msg));

        tv_countinue.setOnClickListener(this);
        tv_track.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.tv_thanks_countinue) {
            startMain();
        } else if (id == R.id.tv_thanks_track) {
            Intent i = new Intent(Thanks_orderActivity.this, My_orderActivity.class);
            i.putExtra("startmain", "startmain");
            startActivity(i);
            finish();
        }
    }

    private void startMain() {
        Intent i = new Intent(Thanks_orderActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem cart = menu.findItem(R.id.action_cart);
        cart.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                startMain();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startMain();
    }

}
