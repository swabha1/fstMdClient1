package pharmacy.fastmeds;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import Config.BaseURL;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.NameValuePair;
import util.Session_management;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = LoginActivity.class.getSimpleName();

    private TextInputLayout ti_email, ti_password;
    private EditText et_email, et_password;
    private TextView tv_register, tv_forgot;
    private Button btn_login;

    private boolean isFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // title remove
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        if (getIntent().getStringExtra("setfinish") != null) {
            isFinish = true;
        }

        ti_email = (TextInputLayout) findViewById(R.id.ti_login_email);
        ti_password = (TextInputLayout) findViewById(R.id.ti_login_password);
        et_email = (EditText) findViewById(R.id.et_login_email);
        et_password = (EditText) findViewById(R.id.et_login_password);
        btn_login = (Button) findViewById(R.id.btn_login);
        tv_forgot = (TextView) findViewById(R.id.tv_login_forgot);
        tv_register = (TextView) findViewById(R.id.tv_login_register);

        btn_login.setOnClickListener(this);
        tv_forgot.setOnClickListener(this);
        tv_register.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        Intent i = null;

        if (id == R.id.btn_login) {
            attemptLogin();
        } else if (id == R.id.tv_login_register) {
            i = new Intent(LoginActivity.this, RegisterActivity.class);
        } else if (id == R.id.tv_login_forgot) {
            i = new Intent(LoginActivity.this, ForgotActivity.class);
        }

        if (i != null) {
            startActivity(i);
        }
    }

    public void Cancle(View view) {
        continueIntent();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        ti_email.setError(null);
        ti_password.setError(null);

        // Store values at the time of the login attempt.
        String email = et_email.getText().toString();
        String password = et_password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            ti_email.setError(getString(R.string.error_field_required));
            focusView = et_email;
            cancel = true;
        } else if (!isEmailValid(email)) {
            ti_email.setError(getString(R.string.error_invalid_email));
            focusView = et_email;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            ti_password.setError(getString(R.string.error_field_required));
            focusView = et_password;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            ti_password.setError(getString(R.string.error_invalid_password));
            focusView = et_password;
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
                makeLogin(email, password);
            } else {
                // display snackbar
                ConnectivityReceiver.showSnackbar(this);
            }

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void makeLogin(String user_email, String user_password) {

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("user_email", user_email));
        params.add(new NameValuePair("user_password", user_password));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.LOGIN_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                try {
                    // convert string to jsonobject
                    JSONObject jsonObject = new JSONObject(response);

                    // getting string from jsonobject
                    String user_id = jsonObject.getString("user_id");
                    String user_type_id = jsonObject.getString("user_type_id");
                    String user_fullname = jsonObject.getString("user_fullname");
                    String user_email = jsonObject.getString("user_email");
                    String user_phone = jsonObject.getString("user_phone");
                    String user_bdate = jsonObject.getString("user_bdate");
                    String user_image = jsonObject.getString("user_image");

                    // intialize session menagement class
                    Session_management sessionManagement = new Session_management(LoginActivity.this);
                    // create and store data in session
                    sessionManagement.createLoginSession(user_id, user_email, user_fullname, user_type_id, user_bdate, user_phone, user_image, "", "", "");

                    // after login success then user redirect to home screen of app
                    continueIntent();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                Toast.makeText(LoginActivity.this, responce, Toast.LENGTH_SHORT).show();
            }
        }, true, this);
        task.execute();
    }

    private void continueIntent() {
        if (!isFinish) {
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(mainIntent);
        }
        finish();
    }

}
