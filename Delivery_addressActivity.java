package pharmacy.fastmeds;

import android.content.Intent;
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
import adapter.Delivery_address_adapter;
import model.Delivery_address_model;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.NameValuePair;
import util.Session_management;

public class Delivery_addressActivity extends CommonAppCompatActivity {

    private static String TAG = Delivery_addressActivity.class.getSimpleName();

    private TextView tv_add_address;
    private RecyclerView rv_address;

    private Session_management sessionManagement;

    private boolean is_select = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_address);

        sessionManagement = new Session_management(this);

        tv_add_address = (TextView) findViewById(R.id.tv_delivery_address_add);
        rv_address = (RecyclerView) findViewById(R.id.rv_address);

        rv_address.setLayoutManager(new LinearLayoutManager(this));

        tv_add_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Delivery_addressActivity.this, Add_delivery_addressActivity.class);
                startActivity(i);
            }
        });

        if(getIntent().getStringExtra("select") != null){
            is_select = true;
        }

    }

    private void makeGetDeliveryAddress(String user_id) {

        // adding post parameter in arraylist
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("user_id", user_id));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.GET_DELIVERY_ADDRESS_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                List<Delivery_address_model> delivery_address_modelList = new ArrayList<>();

                // getting json data form string using gson library
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Delivery_address_model>>() {
                }.getType();

                // store gson data in list
                delivery_address_modelList = gson.fromJson(response, listType);

                // bind adapter using list
                Delivery_address_adapter adapter = new Delivery_address_adapter(delivery_address_modelList,is_select);
                rv_address.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                // display toast message
                CommonAppCompatActivity.showListToast(Delivery_addressActivity.this, delivery_address_modelList.isEmpty());
            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                // display toast message
                CommonAppCompatActivity.showToast(Delivery_addressActivity.this, responce);
            }
        }, true, this);
        task.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sessionManagement != null) {
            // check internet connection is available or not
            if (ConnectivityReceiver.isConnected()) {
                String userid = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);

                makeGetDeliveryAddress(userid);
            } else {
                // show snackbar in activity
                ConnectivityReceiver.showSnackbar(this);
            }
        }
    }
}
