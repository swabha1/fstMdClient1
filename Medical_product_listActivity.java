package pharmacy.fastmeds;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Config.BaseURL;
import adapter.Medical_product_list_adapter;
import model.Medical_category_list_model;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.NameValuePair;
import util.RecyclerTouchListener;

public class Medical_product_listActivity extends CommonAppCompatActivity {

    private static String TAG = Medical_product_listActivity.class.getSimpleName();

    private List<Medical_category_list_model> medical_category_list_modelList = new ArrayList<>();

    private RecyclerView rv_medical_product_list;

    private Medical_product_list_adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_product_list);

        String gettitle = getIntent().getStringExtra("title");
        String getcat_id = getIntent().getStringExtra("id");

        getSupportActionBar().setTitle(gettitle);

        rv_medical_product_list = (RecyclerView) findViewById(R.id.rv_medical_product_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rv_medical_product_list.setLayoutManager(gridLayoutManager);

        // check internet connection available or not
        if (ConnectivityReceiver.isConnected()) {
            makeGetProduct(getcat_id);
        } else {
            // show snackbar in activity
            ConnectivityReceiver.showSnackbar(this);
        }

    }

    private void makeGetProduct(String cat_id) {

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("cat_id", cat_id));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.GET_PRODUCT_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                // gson library for get string response from api and getting values one by one as model
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Medical_category_list_model>>() {
                }.getType();

                // store gson model data in list
                medical_category_list_modelList = gson.fromJson(response, listType);

                // bing adapter using list
                adapter = new Medical_product_list_adapter(medical_category_list_modelList,Medical_product_listActivity.this);
                rv_medical_product_list.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                // display toast message
                CommonAppCompatActivity.showListToast(Medical_product_listActivity.this, medical_category_list_modelList.isEmpty());

            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
            }
        }, true, this);
        task.execute();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

}
