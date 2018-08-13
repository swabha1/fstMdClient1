package pharmacy.fastmeds;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

public class My_order_detailActivity extends CommonAppCompatActivity {

    private static String TAG = My_order_detailActivity.class.getSimpleName();

    private TextView tv_date, tv_type, tv_charge, tv_price, tv_cancel;
    private RecyclerView rv_order_detail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order_detail);

        tv_date = (TextView) findViewById(R.id.tv_order_detail_date);
        tv_type = (TextView) findViewById(R.id.tv_order_detail_type);
        tv_charge = (TextView) findViewById(R.id.tv_order_detail_charge);
        tv_price = (TextView) findViewById(R.id.tv_order_detail_price);
        tv_cancel = (TextView) findViewById(R.id.tv_order_detail_cancel);
        rv_order_detail = (RecyclerView) findViewById(R.id.rv_order_detail);
        rv_order_detail.setLayoutManager(new LinearLayoutManager(this));

        final String sale_id = getIntent().getStringExtra("sale_id");
        String on_date = getIntent().getStringExtra("on_date");
        String payment_type = getIntent().getStringExtra("payment_type");
        String delivery_charge = getIntent().getStringExtra("delivery_charge");
        String total_amount = getIntent().getStringExtra("total_amount");
        String status = getIntent().getStringExtra("status");

        getSupportActionBar().setTitle(getResources().getString(R.string.order_no) + sale_id);

        tv_date.setText(on_date);
        tv_type.setText(payment_type);
        tv_charge.setText(delivery_charge);
        tv_price.setText(total_amount);

        if (status.equals("0")) {
            tv_cancel.setVisibility(View.VISIBLE);
        } else {
            tv_cancel.setVisibility(View.GONE);
        }

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show cancel order dialog
                showCancleDialog(sale_id);
            }
        });

        // check internet connection available or not
        if (ConnectivityReceiver.isConnected()) {
            makeGetMyOrderDetail(sale_id);
        } else {
            // display snackbar in activity
            ConnectivityReceiver.showSnackbar(this);
        }

    }

    // display alertdialog with custom view
    private void showCancleDialog(final String sale_id) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.are_you_sure));
        alert.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (ConnectivityReceiver.isConnected()) {
                    Session_management sessionManagement = new Session_management(My_order_detailActivity.this);
                    String user_id = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);
                    makeCancelOrder(sale_id, user_id);
                } else {
                    ConnectivityReceiver.showSnackbar(My_order_detailActivity.this);
                }
            }
        });
        alert.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        final AlertDialog dialog = alert.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                // change color of dialog button
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        dialog.show();

    }

    private void makeGetMyOrderDetail(String sale_id) {

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("sale_id", sale_id));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.GET_ORDER_DETAIL_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                List<My_order_detail_model> my_order_detail_modelList = new ArrayList<>();

                // gson library use for getting api response from api and store as over model
                Gson gson = new Gson();
                Type listType = new TypeToken<List<My_order_detail_model>>() {
                }.getType();

                // store gson values in list
                my_order_detail_modelList = gson.fromJson(response, listType);

                My_order_detail_adapter adapter = new My_order_detail_adapter(my_order_detail_modelList);
                rv_order_detail.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                // display toast message
                CommonAppCompatActivity.showListToast(My_order_detailActivity.this, my_order_detail_modelList.isEmpty());
            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                // display toast message
                CommonAppCompatActivity.showToast(My_order_detailActivity.this, responce);
            }
        }, true, this);
        task.execute();
    }

    private void makeCancelOrder(String sale_id, String user_id) {

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("sale_id", sale_id));
        params.add(new NameValuePair("user_id", user_id));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.CANCEL_ORDER_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);
                // display toast message
                CommonAppCompatActivity.showToast(My_order_detailActivity.this, response);
                finish();
            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                // display toast message
                CommonAppCompatActivity.showToast(My_order_detailActivity.this, responce);
            }
        }, true, this);
        task.execute();
    }

}
