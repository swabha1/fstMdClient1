package pharmacy.fastmeds;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Config.BaseURL;
import adapter.Offer_adapter;
import model.Offer_model;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.NameValuePair;

public class OffersActivity extends CommonAppCompatActivity {

    private static String TAG = OffersActivity.class.getSimpleName();

    private RecyclerView rv_offers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);

        rv_offers = (RecyclerView) findViewById(R.id.rv_offers);
        rv_offers.setLayoutManager(new LinearLayoutManager(this));

        // check internet connection available or not
        if (ConnectivityReceiver.isConnected()) {
            makeGetOffers();
        } else {
            ConnectivityReceiver.showSnackbar(this);
        }

    }

    private void makeGetOffers() {

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(new ArrayList<NameValuePair>(),
                BaseURL.GET_OFFERS_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                List<Offer_model> offer_modelList = new ArrayList<>();

                Gson gson = new Gson();
                Type listType = new TypeToken<List<Offer_model>>() {
                }.getType();

                // store gson data in list
                offer_modelList = gson.fromJson(response, listType);

                // bind adapter using list
                Offer_adapter adapter = new Offer_adapter(offer_modelList);
                rv_offers.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                CommonAppCompatActivity.showListToast(OffersActivity.this, offer_modelList.isEmpty());

            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
            }
        }, true, this);
        task.execute();

    }

}
