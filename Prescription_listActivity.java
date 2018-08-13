package pharmacy.fastmeds;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viethoa.RecyclerViewFastScroller;
import com.viethoa.models.AlphabetItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Config.BaseURL;
import adapter.Medical_product_list_adapter;
import adapter.Prescription_list_adapter;
import adapter.Prescription_list_adapter2;
import model.Medical_category_list_model;
import model.Medical_category_model;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.NameValuePair;
import util.RecyclerTouchListener;

public class Prescription_listActivity extends CommonAppCompatActivity {

    private static String TAG = Prescription_listActivity.class.getSimpleName();

    private EditText et_search;
    private RecyclerView rv_prescription;
    private RecyclerViewFastScroller fastScroller;

    private List<Medical_category_model> medical_category_modelList = new ArrayList<>();
    private Prescription_list_adapter adapter;

    private List<Medical_category_list_model> medical_category_list_modelList = new ArrayList<>();
    private Prescription_list_adapter2 adapter2;

    private boolean is_second = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_list);

        et_search = (EditText) findViewById(R.id.et_prescription_search);
        rv_prescription = (RecyclerView) findViewById(R.id.rv_prescription);
        // this recyclerview is from library
        fastScroller = (RecyclerViewFastScroller) findViewById(R.id.fast_scroller);

        rv_prescription.setLayoutManager(new LinearLayoutManager(this));

        fastScroller.setRecyclerView(rv_prescription);

        String gettitle = getIntent().getStringExtra("title");
        String getcat_id = getIntent().getStringExtra("id");

        // check internet connection available or not
        if (ConnectivityReceiver.isConnected()) {
            if (getcat_id == null) {
                makeGetPrescription();
            } else {
                is_second = true;
                getSupportActionBar().setTitle(gettitle);
                makeGetProduct(getcat_id);
            }
        } else {
            // display snackbar
            ConnectivityReceiver.showSnackbar(this);
        }

        rv_prescription.addOnItemTouchListener(new RecyclerTouchListener(this, rv_prescription, new RecyclerTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Intent i = null;

                if (is_second) {
                    Medical_category_list_model model = medical_category_list_modelList.get(position);

                    // start detail screen
                    i = new Intent(Prescription_listActivity.this, Producat_detailActivity.class);
                    i.putExtra("id", model.getProduct_id());
                    i.putExtra("product_name", model.getProduct_name());
                    i.putExtra("product_image", model.getProduct_image());
                    i.putExtra("description", model.getDescription());
                    i.putExtra("category_id", model.getCategory_id());
                    i.putExtra("in_stock", model.getIn_stock());
                    i.putExtra("price", model.getPrice());
                    i.putExtra("unit_value", model.getUnit_value());
                    i.putExtra("unit", model.getUnit());
                    i.putExtra("mfg_id", model.getMfg_id());
                    i.putExtra("isgeneric", model.getIsgeneric());
                    i.putExtra("group_name", model.getGroup_name());
                    i.putExtra("discount", model.getDiscount());
                    i.putExtra("stock", model.getStock());
                    i.putExtra("title", model.getTitle());
                    i.putExtra("mfg_name", model.getMfg_name());
                } else {
                    Medical_category_model modeldate = medical_category_modelList.get(position);

                    if (modeldate.getCount().equals("0")) {
                        i = new Intent(Prescription_listActivity.this, Prescription_listActivity.class);
                        i.putExtra("id", modeldate.getId());
                        i.putExtra("title", modeldate.getTitle());
                    } else {
                        i = new Intent(Prescription_listActivity.this, Prescription_listActivity.class);
                        i.putExtra("id", modeldate.getId());
                        i.putExtra("title", modeldate.getTitle());
                    }
                }

                if (i != null) {
                    startActivity(i);
                }

            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0 && charSequence != "") {
                    if (is_second) {
                        adapter2.filter(medical_category_list_modelList, charSequence.toString());
                    } else {
                        adapter.filter(medical_category_modelList, charSequence.toString());
                    }
                } else {
                    if (is_second) {
                        adapter2 = new Prescription_list_adapter2(medical_category_list_modelList);
                        rv_prescription.setAdapter(adapter2);
                        adapter2.notifyDataSetChanged();
                    } else {
                        adapter = new Prescription_list_adapter(medical_category_modelList);
                        rv_prescription.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void makeGetPrescription() {

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(new ArrayList<NameValuePair>(),
                BaseURL.GET_PRESCRIPTIONS_CATEGORY_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                Gson gson = new Gson();
                Type listType = new TypeToken<List<Medical_category_model>>() {
                }.getType();

                medical_category_modelList = gson.fromJson(response, listType);

                adapter = new Prescription_list_adapter(medical_category_modelList);
                rv_prescription.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                CommonAppCompatActivity.showListToast(Prescription_listActivity.this, medical_category_modelList.isEmpty());

                generate_alphabet();

            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
            }
        }, true, this);
        task.execute();

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

                Gson gson = new Gson();
                Type listType = new TypeToken<List<Medical_category_list_model>>() {
                }.getType();

                medical_category_list_modelList = gson.fromJson(response, listType);

                adapter2 = new Prescription_list_adapter2(medical_category_list_modelList);
                rv_prescription.setAdapter(adapter2);
                adapter2.notifyDataSetChanged();

                CommonAppCompatActivity.showListToast(Prescription_listActivity.this, medical_category_list_modelList.isEmpty());

                generate_alphabet();
            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
            }
        }, true, this);
        task.execute();

    }

    // generate alphabet for bind list in fastscrool recyclerview
    private void generate_alphabet() {
        ArrayList<AlphabetItem> mAlphabetItems = new ArrayList<>();
        List<String> strAlphabets = new ArrayList<>();
        if (is_second) {
            for (int i = 0; i < medical_category_list_modelList.size(); i++) {
                String name = medical_category_list_modelList.get(i).getProduct_name();
                if (name == null || name.trim().isEmpty())
                    continue;

                String word = name.substring(0, 1);
                if (!strAlphabets.contains(word)) {
                    strAlphabets.add(word);
                    mAlphabetItems.add(new AlphabetItem(i, word, false));
                }
            }
        } else {
            for (int i = 0; i < medical_category_modelList.size(); i++) {
                String name = medical_category_modelList.get(i).getTitle();
                if (name == null || name.trim().isEmpty())
                    continue;

                String word = name.substring(0, 1);
                if (!strAlphabets.contains(word)) {
                    strAlphabets.add(word);
                    mAlphabetItems.add(new AlphabetItem(i, word, false));
                }
            }
        }

        fastScroller.setUpAlphabet(mAlphabetItems);
    }

}
