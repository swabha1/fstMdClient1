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
import android.widget.Toast;

import java.util.ArrayList;

import Config.BaseURL;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.NameValuePair;

public class ForgotActivity extends AppCompatActivity {

    private static String TAG = ForgotActivity.class.getSimpleName();

    private TextInputLayout ti_email;
    private EditText et_email;
    private Button btn_forgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // title remove
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_forgot);

        ti_email = (TextInputLayout) findViewById(R.id.ti_forgot_email);
        et_email = (EditText) findViewById(R.id.et_forgot_email);
        btn_forgot = (Button) findViewById(R.id.btn_forgot);

        btn_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptForgot();
            }
        });

    }

    public void Cancle(View view){
        finish();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptForgot() {

        // Reset errors.
        ti_email.setError(null);

        // Store values at the time of the login attempt.
        String email = et_email.getText().toString();

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

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            // checking internet connection is available or not
            if (ConnectivityReceiver.isConnected()) {
                makeForgot(email);
            } else {
                // show snackbar in activity
                ConnectivityReceiver.showSnackbar(this);
            }
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private void makeForgot(String email) {

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("email", email));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.FORGOT_PASSWORD_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                Toast.makeText(ForgotActivity.this, response, Toast.LENGTH_SHORT).show();

                Intent loginIntent = new Intent(ForgotActivity.this,LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                Toast.makeText(ForgotActivity.this, responce, Toast.LENGTH_SHORT).show();
            }
        }, true, this);
        task.execute();

    }

}
