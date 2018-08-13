package pharmacy.fastmeds;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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

public class SearchActivity extends CommonAppCompatActivity implements View.OnClickListener {

    private static String TAG = SearchActivity.class.getSimpleName();

    private List<Medical_category_list_model> medical_category_list_modelList = new ArrayList<>();

    private EditText et_search;
    private Spinner sp_search;
    private TextInputLayout ti_search;
    private Button btn_search;
    private RecyclerView rv_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        et_search = (EditText) findViewById(R.id.et_search);
        ti_search = (TextInputLayout) findViewById(R.id.ti_search);
        btn_search = (Button) findViewById(R.id.btn_search);
        sp_search = (Spinner) findViewById(R.id.sp_search);
        rv_search = (RecyclerView) findViewById(R.id.rv_search);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rv_search.setLayoutManager(gridLayoutManager);

        List<String> items = new ArrayList<>();
        items.add(getResources().getString(R.string.prescription));
        items.add(getResources().getString(R.string.medical_product));

        // bind adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_spinner_text_white, R.id.tv_sp, items);
        sp_search.setAdapter(adapter);

        btn_search.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        ti_search.setError(null);

        String search = et_search.getText().toString();

        if (TextUtils.isEmpty(search)) {
            ti_search.setError(getResources().getString(R.string.error_field_required));
            et_search.requestFocus();
        } else {
            // check internet connection available or not
            if (ConnectivityReceiver.isConnected()) {
                makeGetProduct(search);
            } else {
                ConnectivityReceiver.showSnackbar(SearchActivity.this);
            }
        }
    }

    private void makeGetProduct(String search) {

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("search", search));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.GET_PRODUCT_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                Gson gson = new Gson();
                Type listType = new TypeToken<List<Medical_category_list_model>>() {
                }.getType();

                // store data in list using gson values
                medical_category_list_modelList = gson.fromJson(response, listType);

                Medical_product_list_adapter adapter = new Medical_product_list_adapter(medical_category_list_modelList, SearchActivity.this);
                rv_search.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                CommonAppCompatActivity.showListToast(SearchActivity.this, medical_category_list_modelList.isEmpty());

            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
            }
        }, true, this);
        task.execute();

    }

}
