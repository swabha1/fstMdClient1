package pharmacy.fastmeds;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalProfileSharingActivity;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import Config.BaseURL;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.DatabaseHandler;
import util.NameValuePair;
import util.Session_management;

public class Payment_detailActivity extends CommonAppCompatActivity implements View.OnClickListener {

    private static String TAG = Payment_detailActivity.class.getSimpleName();

    /* paypal integration start */

    /**
     * - Set to PayPalConfiguration.ENVIRONMENT_PRODUCTION to move real money.
     * <p>
     * - Set to PayPalConfiguration.ENVIRONMENT_SANDBOX to use your test credentials
     * from https://developer.paypal.com
     * <p>
     * - Set to PayPalConfiguration.ENVIRONMENT_NO_NETWORK to kick the tires
     * without communicating to PayPal's servers.
     */
    private static String CONFIG_ENVIRONMENT = "";

    // note that these credentials will differ between live & sandbox environments.
    private static String CONFIG_CLIENT_ID = "";

    private static String MERCHANT_NAME = "";

    private static final int REQUEST_CODE_PAYMENT = 1;

    private static PayPalConfiguration config;

    /* paypal integration end */

    private RadioButton rb_cash, rb_paypal;
    private TextView tv_total_price;
    private Button btn_payment;
    private CardView cv_paypal;

    private String total_price = "";
    private String delivery_id = "";
    private String payment_ref = "";
    private String payment_paypal_amount = "";
    private String payment_type = "";
    private String offer_coupon = "";
    private String getuser_id = "";
    private JSONArray passArray = null;

    private DatabaseHandler dbcart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_detail);

        dbcart = new DatabaseHandler(this);

        // intialize Session_management class
        Session_management sessionManagement = new Session_management(this);
        // get user id from session
        getuser_id = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);

        rb_cash = (RadioButton) findViewById(R.id.rb_payment_cash);
        rb_paypal = (RadioButton) findViewById(R.id.rb_payment_paypal);
        tv_total_price = (TextView) findViewById(R.id.tv_payment_price);
        btn_payment = (Button) findViewById(R.id.btn_payment);
        cv_paypal = (CardView) findViewById(R.id.cv_payment_paypal);

        // getting values from another activity
        total_price = getIntent().getStringExtra("total_price");
        delivery_id = getIntent().getStringExtra("delivery_id");

        if (getIntent().getStringExtra("offer_coupon") != null) {
            offer_coupon = getIntent().getStringExtra("offer_coupon");
        }

        tv_total_price.setText(total_price);

        rb_cash.setOnClickListener(this);
        rb_paypal.setOnClickListener(this);
        btn_payment.setOnClickListener(this);

        attemptOrder();

        // check internet connection is available or not
        if (ConnectivityReceiver.isConnected()) {
            makeGetPaypal();
        } else {
            // display snackbar in activity
            ConnectivityReceiver.showSnackbar(this);
        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.rb_payment_cash) {
            rb_paypal.setChecked(false);
        } else if (id == R.id.rb_payment_paypal) {
            rb_cash.setChecked(false);
        } else if (id == R.id.btn_payment) {
            if (rb_cash.isChecked()) {
                payment_type = "COD";

                // check internet connection is available or not
                if (ConnectivityReceiver.isConnected()) {
                    makeAddOrder(getuser_id, payment_type, delivery_id, payment_ref, payment_paypal_amount, passArray, offer_coupon, true);
                } else {
                    // display snackbar
                    ConnectivityReceiver.showSnackbar(this);
                }
            } else if (rb_paypal.isChecked()) {

                payment_type = "Paypal";

                // start paypal intent for config paypal information
                Intent intent = new Intent(this, PayPalService.class);
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                startService(intent);

                // start paypal payment screen
                onBuyPressed();
            }
        }
    }

    private void attemptOrder() {

        // retrive data from cart database
        ArrayList<HashMap<String, String>> items = dbcart.getCartAll();
        if (items.size() > 0) {
            passArray = new JSONArray();
            for (int i = 0; i < items.size(); i++) {
                HashMap<String, String> map = items.get(i);

                JSONObject jObjP = new JSONObject();

                try {
                    jObjP.put("product_id", map.get("product_id"));
                    jObjP.put("qty", map.get("qty"));
                    jObjP.put("unit", map.get("unit"));
                    jObjP.put("unit_value", map.get("unit_value"));
                    jObjP.put("price", map.get("price"));
                    jObjP.put("discount", map.get("discount"));

                    // put json data in arraylist
                    passArray.put(jObjP);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void makeAddOrder(String user_id, String payment_type, String delivery_id, String payment_ref,
                              String payment_paypal_amount, JSONArray passArray, String offer_coupon, boolean show_progressbar) {

        // adding parameters in arraylist
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("user_id", user_id));
        params.add(new NameValuePair("payment_type", payment_type));
        params.add(new NameValuePair("delivery_id", delivery_id));
        params.add(new NameValuePair("payment_ref", payment_ref));
        params.add(new NameValuePair("payment_paypal_amount", payment_paypal_amount));
        params.add(new NameValuePair("data", passArray.toString()));
        params.add(new NameValuePair("offer_coupon", offer_coupon));

        Log.e(TAG, user_id + "," + payment_type + "," + delivery_id + "," + payment_ref + ","
                + payment_paypal_amount + "," + passArray.toString() + "," + offer_coupon);

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.SEND_ORDER_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                dbcart.clearCart();

                Intent i = new Intent(Payment_detailActivity.this, Thanks_orderActivity.class);
                i.putExtra("msg", response);
                startActivity(i);

            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
            }
        }, show_progressbar, this);
        task.execute();
    }

    private void makeGetPaypal() {

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(new ArrayList<NameValuePair>(),
                BaseURL.GET_PAYPAL_STATUS_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String paypal_id = jsonObject.getString("id");
                    //String paypal_client_email = jsonObject.getString("client_secret");
                    String paypal_environment = jsonObject.getString("mode");
                    String paypal_config_client_id = jsonObject.getString("client_id");
                    String paypal_merchant_name = jsonObject.getString("paypal_merchant_name");
                    String status = jsonObject.getString("status");

                    CONFIG_CLIENT_ID = paypal_config_client_id;
                    MERCHANT_NAME = paypal_merchant_name;
                    //CONFIG_ENVIRONMENT = "PayPalConfiguration.ENVIRONMENT_SANDBOX";

                    if (status.equals("0")) {
                        cv_paypal.setVisibility(View.GONE);
                    } else {
                        cv_paypal.setVisibility(View.VISIBLE);
                    }

                    if (paypal_environment.equals("SANDBOX")){
                        CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
                    }else{
                        CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;
                    }

                    // config paypal payment gateway with payment method
                    config = new PayPalConfiguration()
                            .environment(CONFIG_ENVIRONMENT)
                            .clientId(CONFIG_CLIENT_ID)
                            // The following are only used in PayPalFuturePaymentActivity.
                            .merchantName(MERCHANT_NAME)
                            .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
                            .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
            }
        }, true, this);
        task.execute();
    }

    public void onBuyPressed() {
        /*
         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
         * Change PAYMENT_INTENT_SALE to
         *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
         *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
         *     later via calls from your server.
         *
         * Also, to include additional payment details and an item list, see getStuffToBuy() below.
         */
        PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_ORDER);

        /*
         * See getStuffToBuy(..) for examples of some available payment options.
         */

        Intent intent = new Intent(Payment_detailActivity.this, PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    private PayPalPayment getThingToBuy(String paymentIntent) {
        return new PayPalPayment(new BigDecimal(total_price), "USD", "Get Pills total price",
                paymentIntent);
    }

    protected void displayResultText(String result) {
        //((TextView)findViewById(R.id.txtResult)).setText("Result : " + result);

        CommonAppCompatActivity.showToast(this, result);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        /**
                         *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         * or consent completion.
                         * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         *
                         * For sample mobile backend interactions, see
                         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                         */

                        //displayResultText("PaymentConfirmation info received from PayPal");

                        JSONObject jsonObject2 = new JSONObject(confirm.toJSONObject().toString(4));
                        //JSONObject jsonObject3 = new JSONObject(jsonObject2.getString("response"));
                        Log.e(TAG, jsonObject2.getJSONObject("response").getString("id"));
                        payment_ref = jsonObject2.getJSONObject("response").getString("id");

                        JSONObject jsonObject = new JSONObject(confirm.getPayment().toJSONObject().toString(4));
                        Log.e(TAG, jsonObject.getString("amount"));
                        payment_paypal_amount = jsonObject.getString("amount");

                        // call api for add data to webserver
                        makeAddOrder(getuser_id, payment_type, delivery_id, payment_ref, payment_paypal_amount, passArray, offer_coupon, false);

                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }

    @Override
    public void onDestroy() {
        // Stop service when done
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

}
