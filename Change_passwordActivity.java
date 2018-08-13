package pharmacy.fastmeds;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import Config.BaseURL;
import util.CommonAsyTask;
import util.NameValuePair;
import util.Session_management;

public class Change_passwordActivity extends AppCompatActivity {

    private static String TAG = Change_passwordActivity.class.getSimpleName();

    private TextInputLayout ti_new_pass, ti_old_pass, ti_con_pass;
    private TextInputEditText et_new_pass, et_old_pass, et_con_pass;
    private Button btn_change_pass;

    private Session_management sessionManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        sessionManagement = new Session_management(this);

        ti_new_pass = (TextInputLayout) findViewById(R.id.ti_change_new_password);
        ti_old_pass = (TextInputLayout) findViewById(R.id.ti_change_old_password);
        ti_con_pass = (TextInputLayout) findViewById(R.id.ti_change_con_password);
        et_new_pass = (TextInputEditText) findViewById(R.id.et_change_new_password);
        et_old_pass = (TextInputEditText) findViewById(R.id.et_change_old_password);
        et_con_pass = (TextInputEditText) findViewById(R.id.et_change_con_password);
        btn_change_pass = (Button) findViewById(R.id.btn_change_password);

        btn_change_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptChangePassword();
            }
        });

    }

    private void attemptChangePassword() {

        // Reset errors.
        ti_new_pass.setError(null);
        ti_old_pass.setError(null);
        ti_con_pass.setError(null);

        String get_new_pass = et_new_pass.getText().toString();
        String get_old_pass = et_old_pass.getText().toString();
        String get_con_pass = et_con_pass.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(get_old_pass)) {
            ti_old_pass.setError(getResources().getString(R.string.error_field_required));
            focusView = et_old_pass;
            cancel = true;
        } else if (!isPasswordValid(get_old_pass)) {
            ti_old_pass.setError(getString(R.string.error_invalid_password));
            focusView = et_old_pass;
            cancel = true;
        }

        if (TextUtils.isEmpty(get_new_pass)) {
            ti_new_pass.setError(getResources().getString(R.string.error_field_required));
            focusView = et_new_pass;
            cancel = true;
        } else if (!isPasswordValid(get_new_pass)) {
            ti_new_pass.setError(getString(R.string.error_invalid_password));
            focusView = et_new_pass;
            cancel = true;
        }

        if (TextUtils.isEmpty(get_con_pass)) {
            ti_con_pass.setError(getResources().getString(R.string.error_field_required));
            focusView = et_con_pass;
            cancel = true;
        } else if (!get_con_pass.equals(get_new_pass)) {
            ti_con_pass.setError(getResources().getString(R.string.password_not_match));
            focusView = et_con_pass;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if (focusView != null)
                focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            String user_id = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);

            makeChangePassword(user_id, get_old_pass, get_new_pass, get_con_pass);

        }
    }

    // check password is gretter then 4 or not
    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void makeChangePassword(String user_id, String current_password, String new_password, String r_password) {

        // adding post values in arraylist
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("user_id", user_id));
        params.add(new NameValuePair("c_password", current_password));
        params.add(new NameValuePair("n_password", new_password));
        params.add(new NameValuePair("r_password", r_password));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.CHANGE_PASSWORD_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                CommonAppCompatActivity.showToast(Change_passwordActivity.this,response);

                sessionManagement.logoutSession();
                finish();
            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                CommonAppCompatActivity.showToast(Change_passwordActivity.this,responce);
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

        MenuItem cart = menu.findItem(R.id.action_cart);
        MenuItem change_password = menu.findItem(R.id.action_change_password);

        // invisible actionbar items
        cart.setVisible(false);
        change_password.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                //onBackPressed();
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

}
