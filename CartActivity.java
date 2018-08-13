package pharmacy.fastmeds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import Config.BaseURL;
import adapter.Cart_adapter;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.DatabaseHandler;
import util.NameValuePair;
import util.Session_management;

public class CartActivity extends CommonAppCompatActivity implements View.OnClickListener {

    private static String TAG = CartActivity.class.getSimpleName();

    private RecyclerView rv_cart;
    private EditText et_voucher;
    private TextInputLayout ti_voucher;
    private TextView tv_subtotal, tv_total, tv_discount, tv_total_items, tv_voucher, tv_viewoffers;
    private Button btn_process;

    private DatabaseHandler dbcart;
    private Session_management sessionManagement;

    private String offer_coupon, offer_discount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // intialize user session
        sessionManagement = new Session_management(this);
        // intialize local database
        dbcart = new DatabaseHandler(this);

        tv_subtotal = (TextView) findViewById(R.id.tv_cart_sub_total);
        tv_total_items = (TextView) findViewById(R.id.tv_cart_total_items);
        tv_total = (TextView) findViewById(R.id.tv_cart_total);
        tv_discount = (TextView) findViewById(R.id.tv_cart_discount);
        btn_process = (Button) findViewById(R.id.btn_cart_process);
        et_voucher = (EditText) findViewById(R.id.et_cart_voucher);
        ti_voucher = (TextInputLayout) findViewById(R.id.ti_cart_voucher);
        tv_voucher = (TextView) findViewById(R.id.tv_cart_check_voucher);
        tv_viewoffers = (TextView) findViewById(R.id.tv_cart_view_offers);
        rv_cart = (RecyclerView) findViewById(R.id.rv_cart);

        rv_cart.setLayoutManager(new LinearLayoutManager(this));

        // false recyclerview nested scrollin. so user can scroll wall screen
        rv_cart.setNestedScrollingEnabled(false);

        updateData();

        // get all cart data from database and store in map list
        ArrayList<HashMap<String, String>> map = dbcart.getCartAll();

        Cart_adapter adapter = new Cart_adapter(this, map, false);
        rv_cart.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        btn_process.setOnClickListener(this);
        tv_voucher.setOnClickListener(this);
        tv_viewoffers.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.tv_cart_check_voucher) {
            ti_voucher.setError(null);

            String getcode = et_voucher.getText().toString();

            if (sessionManagement.isLoggedIn()) {
                if (!TextUtils.isEmpty(getcode)) {

                    // check internet connection is available or not
                    if (ConnectivityReceiver.isConnected()) {
                        String user_id = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);
                        makeCheckOffer(user_id, getcode);
                    } else {
                        // show snackbar in activity
                        ConnectivityReceiver.showSnackbar(CartActivity.this);
                    }
                } else {
                    ti_voucher.setError(getResources().getString(R.string.please_enter_valid_voucher_code));
                }
            } else {
                Intent loginIntent = new Intent(CartActivity.this, LoginActivity.class);
                loginIntent.putExtra("setfinish", "true");
                startActivity(loginIntent);
            }
        } else if (id == R.id.btn_cart_process) {
            if (sessionManagement.isLoggedIn()) {
                // start confirm detail activity
                Intent i = new Intent(CartActivity.this, Confirm_detailActivity.class);
                i.putExtra("offer_coupon", offer_coupon);
                i.putExtra("offer_discount", offer_discount);
                startActivity(i);
            } else {
                Intent loginIntent = new Intent(CartActivity.this, LoginActivity.class);
                loginIntent.putExtra("setfinish", "true");
                startActivity(loginIntent);
            }
        } else if (id == R.id.tv_cart_view_offers) {
            // start offers activity
            Intent i = new Intent(CartActivity.this, OffersActivity.class);
            startActivity(i);
        }

        // Check if no view has focus:
        View view2 = this.getCurrentFocus();
        if (view2 != null) {
            // hide keyboard on view
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
        }
    }

    // update UI
    private void updateData() {
        // check local database has at list 1 record or not
        if (dbcart.getCartCount() > 0) {
            //tv_subtotal.setText(dbcart.getTotalAmount());
            tv_subtotal.setText(dbcart.getTotalDiscountAmount());
            tv_total_items.setText("" + dbcart.getCartCount());
            tv_total.setText(dbcart.getTotalDiscountAmount());
            Double total_save = Double.parseDouble(dbcart.getTotalAmount()) - Double.parseDouble(dbcart.getTotalDiscountAmount());

            tv_discount.setText(String.format("%.2f", total_save));

            CommonAppCompatActivity commonAppCompatActivity = new CommonAppCompatActivity();
            commonAppCompatActivity.updateCounter(this);
        } else {
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregister reciver
        unregisterReceiver(mCart);
    }

    @Override
    public void onResume() {
        super.onResume();
        // register reciver
        registerReceiver(mCart, new IntentFilter("GetPills_cart"));
    }

    // broadcast receiver for receive data
    private BroadcastReceiver mCart = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String type = intent.getStringExtra("type");

            if (type.contentEquals("update")) {
                updateData();
            }
        }
    };

    private void makeCheckOffer(String user_id, String offer_coupons) {

        // adding post values in arraylist
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("user_id", user_id));
        params.add(new NameValuePair("offer_coupon", offer_coupons));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.CHECK_OFFERS_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String offer_id = jsonObject.getString("offer_id");
                    String offer_title = jsonObject.getString("offer_title");
                    offer_coupon = jsonObject.getString("offer_coupon");
                    offer_discount = jsonObject.getString("offer_discount");

                    ti_voucher.setErrorTextAppearance(R.style.error_appearance_primey);
                    ti_voucher.setError(offer_title + ". " + offer_discount + "% Discount available");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                ti_voucher.setErrorTextAppearance(R.style.error_appearance_red);
                ti_voucher.setError(responce);
            }
        }, true, this);
        task.execute();
    }

}
