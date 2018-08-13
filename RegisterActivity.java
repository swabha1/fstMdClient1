package pharmacy.fastmeds;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Config.BaseURL;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.NameValuePair;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = RegisterActivity.class.getSimpleName();

    private TextInputLayout ti_firstname, ti_lastname, ti_email, ti_dob, ti_mobile, ti_password, ti_repassword;
    private EditText et_firstname, et_lastname, et_email, et_mobile, et_password, et_repassword;
    private TextView tv_dob;
    private ImageView iv_dob;
    private CheckBox chk_terms;
    private Button btn_register;

    private String get_dob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // title remove
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        ti_firstname = (TextInputLayout) findViewById(R.id.ti_reg_firstname);
        ti_lastname = (TextInputLayout) findViewById(R.id.ti_reg_lastname);
        ti_email = (TextInputLayout) findViewById(R.id.ti_reg_email);
        ti_dob = (TextInputLayout) findViewById(R.id.ti_reg_dob);
        ti_mobile = (TextInputLayout) findViewById(R.id.ti_reg_mobile);
        ti_password = (TextInputLayout) findViewById(R.id.ti_reg_password);
        ti_repassword = (TextInputLayout) findViewById(R.id.ti_reg_conf_password);
        et_firstname = (EditText) findViewById(R.id.et_reg_firstname);
        et_lastname = (EditText) findViewById(R.id.et_reg_lastname);
        et_email = (EditText) findViewById(R.id.et_reg_email);
        tv_dob = (TextView) findViewById(R.id.tv_reg_dob);
        iv_dob = (ImageView) findViewById(R.id.iv_reg_dob);
        et_mobile = (EditText) findViewById(R.id.et_reg_mobile);
        et_password = (EditText) findViewById(R.id.et_reg_password);
        et_repassword = (EditText) findViewById(R.id.et_reg_conf_password);
        chk_terms = (CheckBox) findViewById(R.id.chk_reg_terms);
        btn_register = (Button) findViewById(R.id.btn_register);

        iv_dob.setOnClickListener(this);
        tv_dob.setOnClickListener(this);
        btn_register.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.tv_reg_dob || id == R.id.iv_reg_dob) {
            setBOD();
        } else if (id == R.id.btn_register) {
            attemptRegister();
        }
    }

    public void Cancle(View view){
        finish();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {

        // Reset errors.
        ti_firstname.setError(null);
        ti_lastname.setError(null);
        ti_email.setError(null);
        ti_dob.setError(null);
        ti_mobile.setError(null);
        ti_password.setError(null);
        ti_repassword.setError(null);

        // Store values at the time of the login attempt.
        String firstname = et_firstname.getText().toString();
        String lastname = et_lastname.getText().toString();
        String email = et_email.getText().toString();
        String dob = tv_dob.getText().toString();
        String mobile = et_mobile.getText().toString();
        String password = et_password.getText().toString();
        String repassword = et_repassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(firstname)) {
            ti_firstname.setError(getString(R.string.error_field_required));
            focusView = et_firstname;
            cancel = true;
        }

        if (TextUtils.isEmpty(lastname)) {
            ti_lastname.setError(getString(R.string.error_field_required));
            focusView = et_lastname;
            cancel = true;
        }

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

        if (dob.equals(getResources().getString(R.string.dob))) {
            ti_dob.setError(getString(R.string.error_field_required));
            focusView = tv_dob;
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

        if (TextUtils.isEmpty(repassword)) {
            ti_repassword.setError(getString(R.string.error_field_required));
            focusView = et_repassword;
            cancel = true;
        } else if (!repassword.equals(password)) {
            ti_repassword.setError(getString(R.string.password_not_match));
            focusView = et_repassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            if (chk_terms.isChecked()) {
                // check internet connection is available or not
                if (ConnectivityReceiver.isConnected()) {
                    String fullname = firstname + " " + lastname;
                    makeRegister(fullname, email, mobile, get_dob, password);
                } else {
                    // display snackbar
                    ConnectivityReceiver.showSnackbar(this);
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.reg_chk_note), Toast.LENGTH_SHORT).show();
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

    private boolean isPhoneValid(String phoneno) {
        //TODO: Replace this with your own logic
        return phoneno.length() > 9;
    }

    // showing date picker dialog
    private void setBOD() {

        int mYear, mMonth, mDay;

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                R.style.datepicker,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        get_dob = "" + year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;

                        try {
                            String inputPattern = "yyyy-MM-dd";
                            String outputPattern = "dd-MM-yyyy";
                            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
                            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

                            Date date = inputFormat.parse(get_dob);
                            String str = outputFormat.format(date);

                            get_dob = inputFormat.format(date);

                            tv_dob.setText(str);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            tv_dob.setText(get_dob);
                        }

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void makeRegister(String user_fullname, String user_email, String user_phone, String user_bdate, String user_password) {

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("user_fullname", user_fullname));
        params.add(new NameValuePair("user_bdate", user_bdate));
        params.add(new NameValuePair("user_email", user_email));
        params.add(new NameValuePair("user_phone", user_phone));
        params.add(new NameValuePair("user_password", user_password));

        Log.e(TAG, user_fullname + "," + user_email + "," + user_phone + "," + user_bdate + "," + user_password);

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.REGISTER_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.registration_successfull), Toast.LENGTH_SHORT).show();

                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                Toast.makeText(RegisterActivity.this, responce, Toast.LENGTH_SHORT).show();
            }
        }, true, this);
        task.execute();

    }

}
