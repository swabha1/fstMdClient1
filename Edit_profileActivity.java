package pharmacy.fastmeds;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import Config.BaseURL;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.JSONParser;
import util.NameValuePair;
import util.Session_management;

public class Edit_profileActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = RegisterActivity.class.getSimpleName();

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE1 = 101;
    private static final int GALLERY_REQUEST_CODE1 = 201;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_GALLERY = 2;

    private Uri fileUri;
    File imagefile1 = null;

    private TextInputLayout ti_firstname, ti_lastname, ti_email, ti_dob, ti_mobile, ti_address, ti_city;
    private EditText et_firstname, et_lastname, et_email, et_mobile, et_address, et_city;
    private TextView tv_dob;
    private ImageView iv_dob, iv_profile;
    private Spinner sp_gender;
    private Button btn_update;

    private String get_dob;
    private String firstname, lastname, email, dob, mobile, address, city, gender;

    private Session_management sessionManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManagement = new Session_management(this);

        ti_firstname = (TextInputLayout) findViewById(R.id.ti_profile_firstname);
        ti_lastname = (TextInputLayout) findViewById(R.id.ti_profile_lastname);
        ti_email = (TextInputLayout) findViewById(R.id.ti_profile_email);
        ti_dob = (TextInputLayout) findViewById(R.id.ti_profile_dob);
        ti_mobile = (TextInputLayout) findViewById(R.id.ti_profile_mobile);
        ti_address = (TextInputLayout) findViewById(R.id.ti_profile_address);
        ti_city = (TextInputLayout) findViewById(R.id.ti_profile_city);
        et_firstname = (EditText) findViewById(R.id.et_profile_firstname);
        et_lastname = (EditText) findViewById(R.id.et_profile_lastname);
        et_email = (EditText) findViewById(R.id.et_profile_email);
        tv_dob = (TextView) findViewById(R.id.tv_profile_dob);
        iv_dob = (ImageView) findViewById(R.id.iv_profile_dob);
        et_mobile = (EditText) findViewById(R.id.et_profile_mobile);
        et_address = (EditText) findViewById(R.id.et_profile_address);
        et_city = (EditText) findViewById(R.id.et_profile_city);
        btn_update = (Button) findViewById(R.id.btn_profile);
        sp_gender = (Spinner) findViewById(R.id.sp_profile_gender);
        iv_profile = (ImageView) findViewById(R.id.iv_profile_img);

        ArrayList<String> gender = new ArrayList<>();
        gender.add(getResources().getString(R.string.male));
        gender.add(getResources().getString(R.string.female));

        // bind adapter of gender value
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_spinner_text, R.id.tv_sp, gender);
        sp_gender.setAdapter(adapter);

        String fistname = sessionManagement.getUserDetails().get(BaseURL.KEY_NAME);
        String email = sessionManagement.getUserDetails().get(BaseURL.KEY_EMAIL);
        String mobile = sessionManagement.getUserDetails().get(BaseURL.KEY_MOBILE);
        String dob = sessionManagement.getUserDetails().get(BaseURL.KEY_BDATE);
        String getgender = sessionManagement.getUserDetails().get(BaseURL.KEY_GENDER);
        String getaddress = sessionManagement.getUserDetails().get(BaseURL.KEY_ADDRESS);
        String getcity = sessionManagement.getUserDetails().get(BaseURL.KEY_CITY);
        String getimage = sessionManagement.getUserDetails().get(BaseURL.KEY_IMAGE);

        String[] separated = fistname.split(" ");
        et_firstname.setText(separated[0]);
        et_lastname.setText(separated[1]);

        et_email.setText(email);
        et_mobile.setText(mobile);
        tv_dob.setText(dob);

        if (getaddress != null && !getaddress.isEmpty()) {
            sp_gender.setSelection(gender.indexOf(getgender));
            et_address.setText(getaddress);
            et_city.setText(getcity);
        }

        if (getimage != null && !getimage.isEmpty()) {
            Picasso.with(this)
                    .load(BaseURL.IMG_PROFILE_URL + getimage)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(iv_profile);
        }

        et_email.setKeyListener(null);

        iv_dob.setOnClickListener(this);
        tv_dob.setOnClickListener(this);
        btn_update.setOnClickListener(this);
        iv_profile.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.tv_profile_dob || id == R.id.iv_profile_dob) {
            setBOD();
        } else if (id == R.id.btn_profile) {
            attemptEditProfile();
        } else if (id == R.id.iv_profile_img) {
            showImageChooser();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptEditProfile() {

        // Reset errors.
        ti_firstname.setError(null);
        ti_lastname.setError(null);
        ti_email.setError(null);
        ti_dob.setError(null);
        ti_mobile.setError(null);
        ti_address.setError(null);
        ti_city.setError(null);

        // Store values at the time of the login attempt.
        firstname = et_firstname.getText().toString();
        lastname = et_lastname.getText().toString();
        email = et_email.getText().toString();
        dob = tv_dob.getText().toString();
        mobile = et_mobile.getText().toString();
        address = et_address.getText().toString();
        city = et_city.getText().toString();
        gender = sp_gender.getSelectedItem().toString();

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

        if (TextUtils.isEmpty(address)) {
            ti_address.setError(getString(R.string.error_field_required));
            focusView = et_address;
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

            if (ConnectivityReceiver.isConnected()) {
                String fullname = firstname + " " + lastname;
                String user_id = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);
                makeEditProfile(fullname, gender, dob, mobile, address, city, user_id);
            } else {
                ConnectivityReceiver.showSnackbar(this);
            }
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPhoneValid(String phoneno) {
        //TODO: Replace this with your own logic
        return phoneno.length() > 9;
    }

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

    private void makeEditProfile(String user_fullname, String user_gender, String user_bdate,
                                 String user_phone, String user_address, String user_city, String user_id) {

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("user_fullname", user_fullname));
        params.add(new NameValuePair("user_gender", user_gender));
        params.add(new NameValuePair("user_bdate", user_bdate));
        params.add(new NameValuePair("user_phone", user_phone));
        params.add(new NameValuePair("user_address", user_address));
        params.add(new NameValuePair("user_city", user_city));
        params.add(new NameValuePair("user_id", user_id));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.EDIT_PROFILE_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                Toast.makeText(Edit_profileActivity.this, response, Toast.LENGTH_SHORT).show();

                String fullname = firstname + " " + lastname;
                sessionManagement.updateData(fullname, gender, dob, mobile, address, city);

            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                Toast.makeText(Edit_profileActivity.this, responce, Toast.LENGTH_SHORT).show();
            }
        }, true, this);
        task.execute();

    }

    // show alertdialog with custom layout
    private void showImageChooser() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        // ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_profile, null);
        dialogBuilder.setView(dialogView);

        TextView tv_camera = (TextView) dialogView.findViewById(R.id.tv_camera);
        TextView tv_gallery = (TextView) dialogView.findViewById(R.id.tv_gallery);
        TextView tv_cancle = (TextView) dialogView.findViewById(R.id.tv_cancle);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        tv_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                // start the image capture Intent
                startActivityForResult(cameraIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE1);
            }
        });

        tv_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE1);
            }
        });

        tv_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "GetPills");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Camera", "Oops! Failed create "
                        + "CarOnDeal" + " directory");
                //return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE1) {
            if (resultCode == RESULT_OK) {

                // successfully captured the image
                // launching upload activity
                //launchUploadActivity(true);
                BitmapFactory.Options options = new BitmapFactory.Options();

                // down sizing image as it throws OutOfMemory Exception for larger
                // images
                options.inSampleSize = 20;

                Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);

                Bitmap out = Bitmap.createScaledBitmap(bitmap, 1200, 1024, false);

                File file = new File(fileUri.getPath());
                FileOutputStream fOut;
                try {
                    fOut = new FileOutputStream(file);
                    out.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
                    fOut.flush();
                    fOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE1) {

                    imagefile1 = file;
                    iv_profile.setImageBitmap(bitmap);

                    String user_id = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);
                    new updateProfile(user_id, file.getAbsolutePath()).execute();
                }

            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                CommonAppCompatActivity.showToast(Edit_profileActivity.this, "User cancelled image capture");
            } else {
                // failed to capture image
                CommonAppCompatActivity.showToast(Edit_profileActivity.this, "Sorry! Failed to capture image");
            }

        } else if ((requestCode == GALLERY_REQUEST_CODE1)) {
            try {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);

                Bitmap b = BitmapFactory.decodeFile(imgDecodableString);
                Bitmap out = Bitmap.createScaledBitmap(b, 1200, 1024, false);

                File file = new File(imgDecodableString);
                FileOutputStream fOut;
                try {
                    fOut = new FileOutputStream(file);
                    out.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
                    fOut.flush();
                    fOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (requestCode == GALLERY_REQUEST_CODE1) {
                    imagefile1 = file;

                    // Set the Image in ImageView after decoding the String
                    iv_profile.setImageBitmap(b);

                    String user_id = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);
                    new updateProfile(user_id, file.getAbsolutePath()).execute();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private class updateProfile extends AsyncTask<String, Integer, Void> {

        JSONParser jsonParser;
        ArrayList<NameValuePair> nameValuePairs;
        boolean response;
        String error_string, success_msg;
        String filePath = "";

        private updateProfile(String user_id, String filepath) {

            nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new NameValuePair("user_id", user_id));

            this.filePath = filepath;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            jsonParser = new JSONParser(Edit_profileActivity.this);
        }

        protected Void doInBackground(String... urls) {

            String json_responce = null;
            try {
                // this JsonParser class method use for post images to server
                json_responce = jsonParser.execMultiPartPostScriptJSON(BaseURL.EDIT_PROFILE_IMG_URL,
                        nameValuePairs, "image/png", filePath, "user_image");
                Log.e(TAG, json_responce + "," + filePath);

                JSONObject jObj = new JSONObject(json_responce);
                if (jObj.getBoolean("responce")) {
                    response = true;
                    success_msg = jObj.getString("data");
                } else {
                    response = false;
                    error_string = jObj.getString("error");
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            if (response) {
                sessionManagement.updateImage(success_msg);
                // display toast message
                CommonAppCompatActivity.showToast(Edit_profileActivity.this, getResources().getString(R.string.profile_pic_updated));
            } else {
                // display toast message
                CommonAppCompatActivity.showToast(Edit_profileActivity.this, error_string);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem cart = menu.findItem(R.id.action_cart);
        MenuItem change_password = menu.findItem(R.id.action_change_password);

        cart.setVisible(false);
        change_password.setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                //onBackPressed();
                this.finish();
                return true;
            case R.id.action_change_password:
                Intent changeIntent = new Intent(this, Change_passwordActivity.class);
                startActivity(changeIntent);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

}
