package pharmacy.fastmeds;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import Config.BaseURL;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.NameValuePair;
import util.Session_management;

public class Add_delivery_addressActivity extends CommonAppCompatActivity implements View.OnClickListener {

    private static String TAG = Add_delivery_addressActivity.class.getSimpleName();

    private TextInputLayout ti_zipcode, ti_fname, ti_mobile, ti_address, ti_landmark, ti_city;
    private EditText et_zipcode, et_fname, et_mobile, et_address, et_landmark, et_city;
    private Button btn_zip, btn_add_address;

    private boolean isEdit = false;
    private String delivery_id;
    private String zipcode_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_delivery_address);

        ti_zipcode = (TextInputLayout) findViewById(R.id.ti_add_address_zipcode);
        ti_fname = (TextInputLayout) findViewById(R.id.ti_add_address_fname);
        ti_mobile = (TextInputLayout) findViewById(R.id.ti_add_address_mobile);
        ti_address = (TextInputLayout) findViewById(R.id.ti_add_address_address);
        ti_landmark = (TextInputLayout) findViewById(R.id.ti_add_address_landmark);
        ti_city = (TextInputLayout) findViewById(R.id.ti_add_address_city);
        et_zipcode = (EditText) findViewById(R.id.et_add_address_zipcode);
        et_fname = (EditText) findViewById(R.id.et_add_address_fname);
        et_mobile = (EditText) findViewById(R.id.et_add_address_mobile);
        et_address = (EditText) findViewById(R.id.et_add_address_address);
        et_landmark = (EditText) findViewById(R.id.et_add_address_landmark);
        et_city = (EditText) findViewById(R.id.et_add_address_city);
        btn_zip = (Button) findViewById(R.id.btn_add_address_zipcheck);
        btn_add_address = (Button) findViewById(R.id.btn_add_address);

        btn_zip.setOnClickListener(this);
        btn_add_address.setOnClickListener(this);

        Intent args = getIntent();
        // check intent is null or not
        if (args.getStringExtra("delivery_id") != null) {
            isEdit = true;
            btn_add_address.setText(getResources().getString(R.string.edit_address));

            delivery_id = args.getStringExtra("delivery_id");
            String delivery_user_id = args.getStringExtra("delivery_user_id");
            String delivery_zipcode = args.getStringExtra("delivery_zipcode");
            String delivery_address = args.getStringExtra("delivery_address");
            String delivery_landmark = args.getStringExtra("delivery_landmark");
            String delivery_fullname = args.getStringExtra("delivery_fullname");
            String delivery_mobilenumber = args.getStringExtra("delivery_mobilenumber");
            String delivery_city = args.getStringExtra("delivery_city");

            et_fname.setText(delivery_fullname);
            et_zipcode.setText(delivery_zipcode);
            et_mobile.setText(delivery_mobilenumber);
            et_address.setText(delivery_address);
            et_landmark.setText(delivery_landmark);
            et_city.setText(delivery_city);

        }else{
            btn_add_address.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btn_add_address_zipcheck) {
            String getzip = et_zipcode.getText().toString();

            ti_zipcode.setError(null);

            if (getzip.isEmpty()) {
                // set error text style black from style.xml file
                ti_zipcode.setErrorTextAppearance(R.style.error_appearance_black);
                ti_zipcode.setError(getResources().getString(R.string.error_field_required));
                et_zipcode.requestFocus();
            } else {
                // check internet connection is available or not
                if (ConnectivityReceiver.isConnected()) {
                    makeCheckZipcode(getzip);
                } else {
                    // else internet not available then show snackbar in activity
                    ConnectivityReceiver.showSnackbar(this);
                }
            }
        } else if (id == R.id.btn_add_address) {
            if (zipcode_id == null){
                // set error text style black from style.xml file
                ti_zipcode.setErrorTextAppearance(R.style.error_appearance_black);
                ti_zipcode.setError("Please check zipcode");
                et_zipcode.requestFocus();
            }else {
                attemptAddAddress();
            }
        }

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptAddAddress() {

        // Reset errors.
        ti_zipcode.setError(null);
        ti_fname.setError(null);
        ti_mobile.setError(null);
        ti_address.setError(null);
        ti_landmark.setError(null);
        ti_city.setError(null);

        // Store values at the time of the login attempt.
        String getzip = et_zipcode.getText().toString();
        String fname = et_fname.getText().toString();
        String mobile = et_mobile.getText().toString();
        String address = et_address.getText().toString();
        String landmark = et_landmark.getText().toString();
        String city = et_city.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (getzip.isEmpty()) {
            ti_zipcode.setErrorTextAppearance(R.style.error_appearance_black);
            ti_zipcode.setError(getResources().getString(R.string.error_field_required));
            focusView = et_zipcode;
            cancel = true;
        }

        if (TextUtils.isEmpty(fname)) {
            ti_fname.setError(getString(R.string.error_field_required));
            focusView = et_fname;
            cancel = true;
        }

        if (TextUtils.isEmpty(mobile)) {
            ti_mobile.setError(getString(R.string.error_field_required));
            focusView = et_mobile;
            cancel = true;
        } else if (!isPhoneValid(mobile)) {
            ti_mobile.setError(getString(R.string.phone_to_short));
            focusView = et_mobile;
            cancel = true;
        }

        if (TextUtils.isEmpty(address)) {
            ti_address.setError(getString(R.string.error_field_required));
            focusView = et_address;
            cancel = true;
        }

        if (TextUtils.isEmpty(landmark)) {
            ti_landmark.setError(getString(R.string.error_field_required));
            focusView = et_landmark;
            cancel = true;
        }

        if (TextUtils.isEmpty(city)) {
            ti_city.setError(getString(R.string.error_field_required));
            focusView = et_city;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            // check internet connection is available or not
            if (ConnectivityReceiver.isConnected()) {
                Session_management sessionManagement = new Session_management(this);
                String userid = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);

                makeAddAddress(userid, getzip, address, landmark, fname, mobile, city);
            } else {
                // else internet not available then show snackbar in activity
                ConnectivityReceiver.showSnackbar(this);
            }
        }
    }

    // this function use for check phone number string length gretter then 9 then return true otherwise false
    private boolean isPhoneValid(String phoneno) {
        //TODO: Replace this with your own logic
        return phoneno.length() > 9;
    }

    private void makeCheckZipcode(String zipcode) {

        // adding post parameters in arraylist
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("zipcode", zipcode));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.CHECK_ZIPCODE_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                try {
                    // convert string to jsonobject
                    JSONObject jsonObject = new JSONObject(response);

                    // getting json string form jsonobject
                    zipcode_id = jsonObject.getString("zipcode_id");
                    String zipcode = jsonObject.getString("zipcode");
                    String delivery_charge = jsonObject.getString("delivery_charge");

                    // set error text color style as app primery color form style.xml file
                    ti_zipcode.setErrorTextAppearance(R.style.error_appearance_primey);
                    ti_zipcode.setError(getResources().getString(R.string.delivery_available) + zipcode + ". " + getResources().getString(R.string.delivery_charge) + delivery_charge);

                    btn_add_address.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                ti_zipcode.setErrorTextAppearance(R.style.error_appearance_red);
                ti_zipcode.setError(responce);
                btn_add_address.setVisibility(View.GONE);
                CommonAppCompatActivity.showToast(Add_delivery_addressActivity.this, responce);
            }
        }, true, this);
        task.execute();
    }

    private void makeAddAddress(String delivery_user_id, String delivery_zipcode, String delivery_address, String delivery_landmark,
                                String delivery_fullname, String delivery_mobilenumber, String delivery_city) {

        // adding post parameters in arraylist
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("delivery_user_id", delivery_user_id));
        params.add(new NameValuePair("delivery_zipcode", delivery_zipcode));
        params.add(new NameValuePair("delivery_address", delivery_address));
        params.add(new NameValuePair("delivery_landmark", delivery_landmark));
        params.add(new NameValuePair("delivery_fullname", delivery_fullname));
        params.add(new NameValuePair("delivery_mobilenumber", delivery_mobilenumber));
        params.add(new NameValuePair("delivery_city", delivery_city));

        final String url;

        if (isEdit) {
            params.add(new NameValuePair("delivery_id", delivery_id));
            url = BaseURL.EDIT_DELIVERY_ADDRESS_URL;
        } else {
            url = BaseURL.ADD_DELIVERY_ADDRESS_URL;
        }

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                url, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response+url);

                // display toast message
                CommonAppCompatActivity.showToast(Add_delivery_addressActivity.this, getResources().getString(R.string.added_delivery_ddress));
                finish();
            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                // display toast message
                CommonAppCompatActivity.showToast(Add_delivery_addressActivity.this, responce);
            }
        }, true, this);
        task.execute();
    }

}
