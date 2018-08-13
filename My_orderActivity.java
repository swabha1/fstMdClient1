package pharmacy.fastmeds;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Config.BaseURL;
import adapter.My_order_adapter;
import model.My_order_model;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.DatabaseHandler;
import util.NameValuePair;
import util.RecyclerTouchListener;
import util.Session_management;

public class My_orderActivity extends AppCompatActivity {

    private static String TAG = My_orderActivity.class.getSimpleName();

    private List<My_order_model> my_order_modelList = new ArrayList<>();

    private RecyclerView rv_order;

    private boolean startmain = false;

    private DatabaseHandler dbcart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);

        dbcart = new DatabaseHandler(My_orderActivity.this);

        rv_order = (RecyclerView) findViewById(R.id.rv_my_order);
        rv_order.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent().getStringExtra("startmain") != null) {
            startmain = true;
        }

        rv_order.addOnItemTouchListener(new RecyclerTouchListener(this, rv_order, new RecyclerTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                My_order_model model = my_order_modelList.get(position);

                // this intent for go to order detail screen
                Intent detailIntent = new Intent(My_orderActivity.this, My_order_detailActivity.class);
                detailIntent.putExtra("sale_id", model.getSale_id());
                detailIntent.putExtra("on_date", model.getOn_date());
                detailIntent.putExtra("payment_type", model.getPayment_type());
                detailIntent.putExtra("delivery_charge", model.getDelivery_charge());
                detailIntent.putExtra("total_amount", model.getTotal_amount());
                detailIntent.putExtra("status", model.getStatus());
                startActivity(detailIntent);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

    }

    private void makeGetMyOrder(String user_id) {

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("user_id", user_id));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.GET_MY_ORDER_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                Gson gson = new Gson();
                Type listType = new TypeToken<List<My_order_model>>() {
                }.getType();

                // store gson value in list
                my_order_modelList = gson.fromJson(response, listType);

                // bind adapter using list
                My_order_adapter adapter = new My_order_adapter(my_order_modelList);
                rv_order.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                // display toast message
                CommonAppCompatActivity.showListToast(My_orderActivity.this, my_order_modelList.isEmpty());
            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                // display toast message
                CommonAppCompatActivity.showToast(My_orderActivity.this, responce);
            }
        }, true, this);
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getMenuInflater().inflate(R.menu.main, menu);

        final MenuItem cart = menu.findItem(R.id.action_cart);

        View count = cart.getActionView();
        count.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                menu.performIdentifierAction(cart.getItemId(), 0);
            }
        });

        TextView totalBudgetCount = (TextView) count.findViewById(R.id.tv_action_cart);

        if (totalBudgetCount != null) {
            totalBudgetCount.setText("" + dbcart.getCartCount());
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (startmain) {
                    Intent i = new Intent(My_orderActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
                finish();
                return true;
            case R.id.action_cart:
                if (dbcart.getCartCount() > 0) {
                    Intent cartIntent = new Intent(this, CartActivity.class);
                    startActivity(cartIntent);
                } else {
                    CommonAppCompatActivity.showToast(this, getResources().getString(R.string.cart_empty));
                }
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (startmain) {
            Intent i = new Intent(My_orderActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // check internet connection available or not
        if (ConnectivityReceiver.isConnected()) {
            // get user id from session management class
            Session_management sessionManagement = new Session_management(this);
            String userid = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);

            makeGetMyOrder(userid);
        } else {
            ConnectivityReceiver.showSnackbar(this);
        }
    }
}
