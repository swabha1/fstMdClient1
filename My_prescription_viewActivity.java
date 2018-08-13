package pharmacy.fastmeds;

import android.content.Context;
import android.content.Intent;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Config.BaseURL;
import adapter.My_order_detail_adapter;
import model.My_order_detail_model;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.NameValuePair;
import util.Session_management;

public class My_prescription_viewActivity extends CommonAppCompatActivity implements View.OnClickListener {

    private static String TAG = My_prescription_viewActivity.class.getSimpleName();

    private List<My_order_detail_model> my_order_detail_modelList = new ArrayList<>();

    private EditText et_voucher;
    private TextInputLayout ti_voucher;
    private TextView tv_subtotal, tv_total, tv_discount, tv_total_items, tv_shipping_charge, tv_voucher, tv_viewoffers;
    private RecyclerView rv_prescription_item;
    private Button btn_process;

    private String offer_coupon, offer_discount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_prescription_view);

        tv_subtotal = (TextView) findViewById(R.id.tv_presc_sub_total);
        tv_total_items = (TextView) findViewById(R.id.tv_presc_total_items);
        tv_total = (TextView) findViewById(R.id.tv_presc_total);
        tv_shipping_charge = (TextView) findViewById(R.id.tv_presc_charge);
        tv_discount = (TextView) findViewById(R.id.tv_presc_discount);
        btn_process = (Button) findViewById(R.id.btn_confirm_process);
        et_voucher = (EditText) findViewById(R.id.et_cart_voucher);
        ti_voucher = (TextInputLayout) findViewById(R.id.ti_cart_voucher);
        tv_voucher = (TextView) findViewById(R.id.tv_cart_check_voucher);
        tv_viewoffers = (TextView) findViewById(R.id.tv_cart_view_offers);

        rv_prescription_item = (RecyclerView) findViewById(R.id.rv_my_prescription_view);
        rv_prescription_item.setLayoutManager(new LinearLayoutManager(this));
        rv_prescription_item.setNestedScrollingEnabled(false);

        String sale_id = getIntent().getStringExtra("sale_id");

        getSupportActionBar().setTitle(getResources().getString(R.string.pres) + getResources().getString(R.string.order_no) + sale_id);

        // check internet connection available or not
        if (ConnectivityReceiver.isConnected()) {
            makeGetPrescriptionItems(sale_id);
        } else {
            // display snackbar in activity
            ConnectivityReceiver.showSnackbar(this);
        }

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

            if (!TextUtils.isEmpty(getcode)) {

                // check internet connection is available or not
                if (ConnectivityReceiver.isConnected()) {
                    Session_management sessionManagement = new Session_management(My_prescription_viewActivity.this);
                    String user_id = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);
                    makeCheckOffer(user_id, getcode);
                } else {
                    // show snackbar in activity
                    ConnectivityReceiver.showSnackbar(My_prescription_viewActivity.this);
                }
            } else {
                ti_voucher.setError(getResources().getString(R.string.please_enter_valid_voucher_code));
            }

        } else if (id == R.id.btn_confirm_process) {
            // start confirm detail activity
            /*Intent i = new Intent(CartActivity.this, Confirm_detailActivity.class);
            i.putExtra("offer_coupon", offer_coupon);
            i.putExtra("offer_discount", offer_discount);
            startActivity(i);*/
        } else if (id == R.id.tv_cart_view_offers) {
            // start offers activity
            Intent i = new Intent(My_prescription_viewActivity.this, OffersActivity.class);
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

    private void makeGetPrescriptionItems(String sale_id) {

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("sale_id", sale_id));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.GET_PRESCRIPTION_ITEMS_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                // gson library use for getting api response from api and store as over model
                Gson gson = new Gson();
                Type listType = new TypeToken<List<My_order_detail_model>>() {
                }.getType();

                // store gson values in list
                my_order_detail_modelList = gson.fromJson(response, listType);

                My_order_detail_adapter adapter = new My_order_detail_adapter(my_order_detail_modelList);
                rv_prescription_item.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                updatePriceDetail();

                // display toast message
                CommonAppCompatActivity.showListToast(My_prescription_viewActivity.this, my_order_detail_modelList.isEmpty());
            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                // display toast message
                CommonAppCompatActivity.showToast(My_prescription_viewActivity.this, responce);
            }
        }, true, this);
        task.execute();
    }

    private void updatePriceDetail() {

        int total_items = my_order_detail_modelList.size();
        Double sub_total = 0.0;

        for (int i = 0; i < my_order_detail_modelList.size(); i++) {
            My_order_detail_model model = my_order_detail_modelList.get(i);

            sub_total = (sub_total + Double.parseDouble(model.getPrice()));
        }

        tv_subtotal.setText(String.format("%.2f", sub_total));
        tv_total_items.setText("" + total_items);
        tv_total.setText(String.format("%.2f", sub_total));

    }

}
