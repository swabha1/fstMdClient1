package pharmacy.fastmeds;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Config.BaseURL;
import adapter.Home_suggetion_adapter;
import fragment.Home_fragment;
import model.Medical_category_list_model;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.DatabaseHandler;
import util.ExpandableTextview;
import util.NameValuePair;
import util.RecyclerTouchListener;

public class Producat_detailActivity extends CommonAppCompatActivity implements View.OnClickListener {

    private static String TAG = Producat_detailActivity.class.getSimpleName();

    private List<Medical_category_list_model> medical_category_list_modelList = new ArrayList<>();

    private ImageView iv_img, iv_plus, iv_minus, iv_add;
    private TextView tv_title, tv_about, tv_stock, tv_corporation, tv_generic, tv_discount, tv_price, tv_currency, tv_qty;
    private RecyclerView rv_suggest;

    private HashMap<String, String> map = new HashMap<>();
    private DatabaseHandler dbcart;

    private String getimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producat_detail);

        dbcart = new DatabaseHandler(Producat_detailActivity.this);

        tv_title = (TextView) findViewById(R.id.tv_detail_title);
        tv_about = (TextView) findViewById(R.id.tv_detail_desc);
        tv_stock = (TextView) findViewById(R.id.tv_detail_stock);
        tv_corporation = (TextView) findViewById(R.id.tv_detail_company);
        tv_generic = (TextView) findViewById(R.id.tv_detail_available);
        tv_discount = (TextView) findViewById(R.id.tv_detail_discount);
        tv_currency = (TextView) findViewById(R.id.tv_detail_currency);
        tv_price = (TextView) findViewById(R.id.tv_detail_price);
        iv_img = (ImageView) findViewById(R.id.iv_detail_img);
        iv_plus = (ImageView) findViewById(R.id.iv_detail_plus);
        iv_minus = (ImageView) findViewById(R.id.iv_detail_minus);
        tv_qty = (TextView) findViewById(R.id.tv_detail_qty);
        iv_add = (ImageView) findViewById(R.id.iv_product_detail_add);
        rv_suggest = (RecyclerView) findViewById(R.id.rv_detail_suggested);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rv_suggest.setLayoutManager(linearLayoutManager2);

        String getid = getIntent().getStringExtra("id");
        String getname = getIntent().getStringExtra("product_name");
        getimage = getIntent().getStringExtra("product_image");
        String description = getIntent().getStringExtra("description");
        String category_id = getIntent().getStringExtra("category_id");
        String getstock = getIntent().getStringExtra("in_stock");
        String getprice = getIntent().getStringExtra("price");
        String unit_value = getIntent().getStringExtra("unit_value");
        String unit = getIntent().getStringExtra("unit");
        String mfg_id = getIntent().getStringExtra("mfg_id");
        String getgeneric = getIntent().getStringExtra("isgeneric");
        String group_name = getIntent().getStringExtra("group_name");
        String discount = getIntent().getStringExtra("discount");
        String stock = getIntent().getStringExtra("stock");
        String title = getIntent().getStringExtra("title");
        String mfg_name = getIntent().getStringExtra("mfg_name");

        map.put("product_id", getid);
        map.put("product_name", getname);
        map.put("product_image", getimage);
        map.put("category_id", category_id);
        map.put("in_stock", getstock);
        map.put("price", getprice);
        map.put("unit_value", unit_value);
        map.put("unit", unit);
        map.put("mfg_id", mfg_id);
        map.put("isgeneric", getgeneric);
        map.put("group_name", group_name);
        map.put("discount", discount);
        map.put("stock", stock);
        map.put("title", title);
        map.put("mfg_name", mfg_name);

        Picasso.with(this)
                .load(BaseURL.IMG_PRODUCT_URL + getimage)
                .placeholder(R.drawable.ic_loading)
                .into(iv_img);

        tv_title.setText(getname);
        tv_about.setText(description);
        ExpandableTextview.makeTextViewResizable(this, tv_about, 3, getResources().getString(R.string.view_more), true, false);

        tv_stock.setText(getstock);
        tv_corporation.setText(mfg_name);

        if (getgeneric.equals("1")) {
            tv_generic.setText(getResources().getString(R.string.generic_available));
        } else {
            tv_generic.setText(getResources().getString(R.string.generic_not_available));
        }
        tv_discount.setText(discount + " % Off");
        tv_currency.setText(getResources().getString(R.string.rs));

        if (dbcart.isInCart(map.get("product_id"))) {
            tv_qty.setText(dbcart.getCartItemQty(getid));
            iv_add.setBackgroundResource(R.drawable.ic_cart_update);
        }

        Double items = Double.parseDouble(tv_qty.getText().toString());
        Double get_price = Double.parseDouble(map.get("price"));

        if (!discount.isEmpty() && !discount.equalsIgnoreCase("0")) {
            tv_price.setText(getDiscountPrice(discount, "" + get_price * items, true));
        } else {
            tv_price.setText("" + get_price * items);
        }

        iv_minus.setOnClickListener(this);
        iv_plus.setOnClickListener(this);
        iv_add.setOnClickListener(this);
        iv_img.setOnClickListener(this);

        if (ConnectivityReceiver.isConnected()) {
            makeGetSuggest(category_id);
        } else {
            ConnectivityReceiver.showSnackbar(this);
        }

        rv_suggest.addOnItemTouchListener(new RecyclerTouchListener(this, rv_suggest, new RecyclerTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Medical_category_list_model model = medical_category_list_modelList.get(position);

                Intent i = new Intent(Producat_detailActivity.this, Producat_detailActivity.class);
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
                startActivity(i);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.iv_detail_minus) {
            int qty = 0;
            if (!tv_qty.getText().toString().equalsIgnoreCase(""))
                qty = Integer.valueOf(tv_qty.getText().toString());

            if (qty > 0) {
                qty = qty - 1;
                tv_qty.setText(String.valueOf(qty));

                Double items = Double.parseDouble(tv_qty.getText().toString());
                Double get_price = Double.parseDouble(map.get("price"));

                if (!map.get("discount").isEmpty() && !map.get("discount").equalsIgnoreCase("0")) {

                    tv_price.setText(getDiscountPrice(map.get("discount"), "" + get_price * items, true));
                } else {
                    tv_price.setText("" + get_price * items);
                }

            }

        } else if (id == R.id.iv_detail_plus) {
            int qty = Integer.valueOf(tv_qty.getText().toString());
            qty = qty + 1;

            tv_qty.setText(String.valueOf(qty));

            Double items = Double.parseDouble(tv_qty.getText().toString());
            Double get_price = Double.parseDouble(map.get("price"));

            if (!map.get("discount").isEmpty() && !map.get("discount").equalsIgnoreCase("0")) {

                tv_price.setText(getDiscountPrice(map.get("discount"), "" + get_price * items, true));
            } else {
                tv_price.setText("" + get_price * items);
            }

        } else if (id == R.id.iv_product_detail_add) {

            if (!tv_qty.getText().toString().equalsIgnoreCase("0")) {

                Double items = Double.parseDouble(tv_qty.getText().toString());
                Double price = Double.parseDouble(map.get("price"));

                if (!map.get("discount").isEmpty() && !map.get("discount").equalsIgnoreCase("0")) {

                    dbcart.setCart(map, Float.valueOf(tv_qty.getText().toString()),
                            Double.parseDouble(getDiscountPrice(map.get("discount"), "" + price * items, false)),
                            Double.parseDouble(getDiscountPrice(map.get("discount"), "" + price * items, true)));
                } else {
                    dbcart.setCart(map, Float.valueOf(tv_qty.getText().toString()),
                            price * items,
                            price * items);
                }

                iv_add.setBackgroundResource(R.drawable.ic_cart_update);
            } else {
                dbcart.removeItemFromCart(map.get("product_id"));
                iv_add.setBackgroundResource(R.drawable.ic_menu_cart);
            }

            CommonAppCompatActivity commonAppCompatActivity = new CommonAppCompatActivity();
            commonAppCompatActivity.updateCounter(Producat_detailActivity.this);

        } else if (id == R.id.iv_detail_img) {
            showImages(getimage);
        }
    }

    // get discount from price and discount amount
    private String getDiscountPrice(String discount, String price, boolean getEffectedprice) {
        Double discount1 = Double.parseDouble(discount);
        Double price1 = Double.parseDouble(price);
        Double discount_amount = discount1 * price1 / 100;

        if (getEffectedprice) {
            Double effected_price = price1 - discount_amount;
            return effected_price.toString();
        } else {
            return discount_amount.toString();
        }
    }

    private void makeGetSuggest(String cat_id) {

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("cat_id", cat_id));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.GET_SUGGEST_DETAILS_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                Gson gson = new Gson();
                Type listType = new TypeToken<List<Medical_category_list_model>>() {
                }.getType();

                medical_category_list_modelList = gson.fromJson(response, listType);

                Home_suggetion_adapter adapter = new Home_suggetion_adapter(medical_category_list_modelList, Producat_detailActivity.this);
                rv_suggest.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
            }
        }, true, this);
        task.execute();
    }

    // showing custom alertdialog with viewpager images
    private void showImages(String imagepath) {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_zoom);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        ImageView iv_dialoge_cancle = (ImageView) dialog.findViewById(R.id.iv_dialog_cancle);
        ImageView iv_dialoge_img = (ImageView) dialog.findViewById(R.id.iv_dialog_img);

        Picasso.with(this)
                .load(BaseURL.IMG_PRODUCT_URL + imagepath)
                .placeholder(R.drawable.ic_loading)
                .skipMemoryCache()
                .resize(1024, 1024)
                .into(iv_dialoge_img);

        iv_dialoge_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!dbcart.getCartItemQty(map.get("product_id")).equals("0")) {
            tv_qty.setText(dbcart.getCartItemQty(map.get("product_id")));
        }
    }
}
