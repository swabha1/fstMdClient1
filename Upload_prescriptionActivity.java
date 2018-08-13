package pharmacy.fastmeds;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Config.BaseURL;
import util.ConnectivityReceiver;
import util.JSONParser;
import util.NameValuePair;
import util.Session_management;

public class Upload_prescriptionActivity extends CommonAppCompatActivity implements View.OnClickListener {

    private static String TAG = Upload_prescriptionActivity.class.getSimpleName();

    private List<Map<String, String>> image_list = new ArrayList<>();

    private ImageView iv_img_1, iv_img_2, iv_img_3;
    private TextView tv_name, tv_email, tv_add_address, tv_address_detail, tv_bill_name, tv_bill_email, tv_bill_detail, tv_add_billing;
    private Button btn_upload;

    private String filePath1 = "";
    private String filePath2 = "";
    private String filePath3 = "";
    private static final int GALLERY_REQUEST_CODE1 = 201;
    private static final int GALLERY_REQUEST_CODE2 = 202;
    private static final int GALLERY_REQUEST_CODE3 = 203;
    private Bitmap bitmap;
    private Uri imageuri;

    private SharedPreferences prefs_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_prescription);

        prefs_address = getSharedPreferences("Delivery_address", 0);

        iv_img_1 = (ImageView) findViewById(R.id.iv_upload_pres_img_1);
        iv_img_2 = (ImageView) findViewById(R.id.iv_upload_pres_img_2);
        iv_img_3 = (ImageView) findViewById(R.id.iv_upload_pres_img_3);

        tv_name = (TextView) findViewById(R.id.tv_upload_pres_name);
        tv_email = (TextView) findViewById(R.id.tv_upload_pres_email);
        tv_add_address = (TextView) findViewById(R.id.tv_confirm_address_edit);
        tv_address_detail = (TextView) findViewById(R.id.tv_confirm_address_detail);
        tv_bill_name = (TextView) findViewById(R.id.tv_confirm_address_fname);
        tv_bill_email = (TextView) findViewById(R.id.tv_confirm_address_email);
        tv_bill_detail = (TextView) findViewById(R.id.tv_confirm_address_bill_detail);
        tv_add_billing = (TextView) findViewById(R.id.tv_confirm_billing_edit);

        btn_upload = (Button) findViewById(R.id.btn_upload);

        iv_img_1.setOnClickListener(this);
        iv_img_2.setOnClickListener(this);
        iv_img_3.setOnClickListener(this);
        tv_add_address.setOnClickListener(this);
        tv_add_billing.setOnClickListener(this);
        btn_upload.setOnClickListener(this);

        upadateUI();

    }

    private void upadateUI() {
        Session_management sessionManagement = new Session_management(this);

        String getname = sessionManagement.getUserDetails().get(BaseURL.KEY_NAME);
        String getemail = sessionManagement.getUserDetails().get(BaseURL.KEY_EMAIL);
        String getcity = sessionManagement.getUserDetails().get(BaseURL.KEY_CITY);
        String getaddress = sessionManagement.getUserDetails().get(BaseURL.KEY_ADDRESS);
        String getmobile = sessionManagement.getUserDetails().get(BaseURL.KEY_MOBILE);

        StringBuilder sb = new StringBuilder();
        sb.append(getmobile + "\n");
        if (getaddress.isEmpty()) {
            tv_add_billing.setText(getResources().getString(R.string.add));
            sb.append(getResources().getString(R.string.no_address_found));
        } else {
            tv_add_billing.setText(getResources().getString(R.string.edit));
            sb.append(getcity + "\n");
            sb.append(getaddress);
        }

        tv_name.setText(getname);
        tv_email.setText(getemail);

        tv_bill_name.setText(getname);
        tv_bill_email.setText(getemail);
        tv_bill_detail.setText(sb.toString());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        Intent i = null;

        if (id == R.id.iv_upload_pres_img_1) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Start the Intent
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE1);
        } else if (id == R.id.iv_upload_pres_img_2) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Start the Intent
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE2);
        } else if (id == R.id.iv_upload_pres_img_3) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Start the Intent
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE3);
        } else if (id == R.id.tv_confirm_address_edit) {
            i = new Intent(Upload_prescriptionActivity.this, Delivery_addressActivity.class);
            i.putExtra("select", "true");
        } else if (id == R.id.tv_confirm_billing_edit) {
            i = new Intent(Upload_prescriptionActivity.this, Edit_profileActivity.class);
        } else if (id == R.id.btn_upload) {
            // check internet connection available or not
            if (ConnectivityReceiver.isConnected()) {
                if (prefs_address.getString("delivery_id", null) != null) {
                    String delivery_id = prefs_address.getString("delivery_id", null);
                    Session_management sessionManagement = new Session_management(this);
                    String user_id = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);
                    new uploadPrescription(user_id, delivery_id).execute();
                } else {
                    CommonAppCompatActivity.showToast(this, "Please select delivery address");
                }
            } else {
                // display snackbar
                ConnectivityReceiver.showSnackbar(this);
            }
        }

        if (i != null) {
            startActivity(i);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (resultCode == RESULT_OK) {
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
                cursor.close();

                //filePath = imgDecodableString;

                Bitmap b = BitmapFactory.decodeFile(imgDecodableString);
                Bitmap out = Bitmap.createScaledBitmap(b, 1200, 1024, false);

                //getting image from gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);


                File file = new File(imgDecodableString);
                //filePath = file.getAbsolutePath();
                FileOutputStream fOut;
                try {
                    fOut = new FileOutputStream(file);
                    out.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
                    fOut.flush();
                    fOut.close();
                    //b.recycle();
                    //out.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (requestCode == GALLERY_REQUEST_CODE1) {
                    // Set the Image in ImageView after decoding the String
                    iv_img_1.setImageBitmap(bitmap);
                    filePath1 = file.getAbsolutePath();

                    Map<String, String> map = new HashMap<>();
                    map.put("file_type", "image/png");
                    map.put("filepath", filePath1);
                    map.put("imagename", "prescription_img1");
                    image_list.add(map);
                }
                if (requestCode == GALLERY_REQUEST_CODE2) {
                    // Set the Image in ImageView after decoding the String
                    iv_img_2.setImageBitmap(bitmap);
                    filePath2 = file.getAbsolutePath();

                    Map<String, String> map = new HashMap<>();
                    map.put("file_type", "image/png");
                    map.put("filepath", filePath2);
                    map.put("imagename", "prescription_img2");
                    image_list.add(map);
                }
                if (requestCode == GALLERY_REQUEST_CODE3) {
                    // Set the Image in ImageView after decoding the String
                    iv_img_3.setImageBitmap(bitmap);
                    filePath3 = file.getAbsolutePath();

                    Map<String, String> map = new HashMap<>();
                    map.put("file_type", "image/png");
                    map.put("filepath", filePath3);
                    map.put("imagename", "prescription_img3");
                    image_list.add(map);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class uploadPrescription extends AsyncTask<String, Integer, Void> {

        private ProgressDialog progressDialog;
        JSONParser jsonParser;
        ArrayList<NameValuePair> nameValuePairs;
        boolean response;
        String error_string, success_msg;
        String filePath = "";

        private uploadPrescription(String user_id, String delivery_id) {

            nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new NameValuePair("user_id", user_id));
            nameValuePairs.add(new NameValuePair("delivery_id", delivery_id));

        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(Upload_prescriptionActivity.this, R.style.AppCompatAlertDialogStyle);
            progressDialog.setMessage("Process with data..");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            jsonParser = new JSONParser(Upload_prescriptionActivity.this);
        }

        protected Void doInBackground(String... urls) {

            String json_responce = null;
            try {
                // upload multiple images to server
                json_responce = jsonParser.execMultiPartPostScriptJSON(BaseURL.SEND_ORDER_PRESCRIPTION_URL,
                        nameValuePairs, image_list);
                Log.e(TAG, json_responce + "," + filePath);

                JSONObject jObj = new JSONObject(json_responce);
                if (jObj.getBoolean("responce")) {
                    response = true;
                    success_msg = jObj.getString("data");
                } else {
                    response = false;
                    error_string = jObj.getString("message");
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
            if ((this.progressDialog != null) && this.progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }

            if (response) {
                // display toast message
                CommonAppCompatActivity.showToast(Upload_prescriptionActivity.this, success_msg);
                finish();
            } else {
                // display toast message
                CommonAppCompatActivity.showToast(Upload_prescriptionActivity.this, error_string);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        upadateUI();

        if (prefs_address.getString("delivery_id", null) != null) {

            StringBuilder sb = new StringBuilder();

            sb.append(prefs_address.getString("delivery_fullname", null) + "\n");
            sb.append(prefs_address.getString("delivery_city", null) + "\n");
            sb.append(prefs_address.getString("delivery_landmark", null) + "\n");
            sb.append(prefs_address.getString("delivery_zipcode", null) + "\n");
            sb.append(prefs_address.getString("delivery_address", null) + "\n");
            sb.append(prefs_address.getString("delivery_mobilenumber", null) + "\n");

            tv_address_detail.setText(sb);
            tv_add_address.setText(getResources().getString(R.string.edit));
        }

    }

}
