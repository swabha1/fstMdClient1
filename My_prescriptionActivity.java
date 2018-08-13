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
import adapter.My_prescription_adapter;
import model.My_prescription_model;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.NameValuePair;
import util.Session_management;

public class My_prescriptionActivity extends CommonAppCompatActivity {

    private static String TAG = My_prescriptionActivity.class.getSimpleName();

    private RecyclerView rv_prescription;
    private TextView tv_upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_prescription);

        tv_upload = (TextView) findViewById(R.id.tv_home_upload);
        rv_prescription = (RecyclerView) findViewById(R.id.rv_my_prescription);
        rv_prescription.setLayoutManager(new LinearLayoutManager(this));

        tv_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(My_prescriptionActivity.this, Upload_prescriptionActivity.class);
                startActivity(i);
            }
        });

        // check internet connection available or not
        if (ConnectivityReceiver.isConnected()) {
            // get user id from session management class
            Session_management sessionManagement = new Session_management(this);
            String userid = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);
            makeGetMyPrescription(userid);
        } else {
            // display snackbar in activity
            ConnectivityReceiver.showSnackbar(this);
        }

    }

    private void makeGetMyPrescription(String user_id) {

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("user_id", user_id));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.GET_MY_PRESCRIPTION_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                List<My_prescription_model> my_prescription_modelList = new ArrayList<>();

                Gson gson = new Gson();
                Type listType = new TypeToken<List<My_prescription_model>>() {
                }.getType();

                // store gson data in list
                my_prescription_modelList = gson.fromJson(response, listType);

                // bind adapter with list data
                My_prescription_adapter adapter = new My_prescription_adapter(my_prescription_modelList);
                rv_prescription.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                // display toast message
                CommonAppCompatActivity.showListToast(My_prescriptionActivity.this, my_prescription_modelList.isEmpty());

            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
            }
        }, true, this);
        task.execute();
    }
}
