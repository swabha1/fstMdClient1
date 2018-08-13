package pharmacy.fastmeds;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import Config.BaseURL;
import fcm.MyFirebaseRegister;
import fragment.Home_fragment;
import util.ConnectivityReceiver;
import util.DatabaseHandler;
import util.JSONParser;
import util.NameValuePair;
import util.Session_management;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static String TAG = MainActivity.class.getSimpleName();

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE1 = 101;
    private static final int GALLERY_REQUEST_CODE1 = 201;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_GALLERY = 2;

    private Uri fileUri;
    File imagefile1 = null;

    private ImageView iv_header_img, iv_header_edit, iv_header_home;
    private TextView tv_header_name, tv_header_email;
    private Menu nav_menu;

    private Session_management sessionManagement;

    private TextView totalBudgetCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // intialize Session_management class
        sessionManagement = new Session_management(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING) {
                    updateSidemenu();
                    invalidateOptionsMenu();
                }
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        nav_menu = navigationView.getMenu();

        // getting side navigation header view for set values in side menu controlls
        View header = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);

        iv_header_img = (ImageView) header.findViewById(R.id.iv_header_img);
        iv_header_home = (ImageView) header.findViewById(R.id.iv_header_home);
        iv_header_edit = (ImageView) header.findViewById(R.id.iv_header_edit);
        tv_header_name = (TextView) header.findViewById(R.id.tv_header_name);
        tv_header_email = (TextView) header.findViewById(R.id.tv_header_email);

        iv_header_img.setOnClickListener(this);
        iv_header_home.setOnClickListener(this);
        iv_header_edit.setOnClickListener(this);
        tv_header_name.setOnClickListener(this);

        // savedinstancestate not null then adding fragment in activity
        if (savedInstanceState == null) {
            Fragment fm = new Home_fragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.contentPanel, fm, "Home_fragment")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }

        if (getIntent().getStringExtra("isorder") != null) {
            Intent i = new Intent(MainActivity.this, My_orderActivity.class);
            startActivity(i);
        }

        if (sessionManagement.isLoggedIn()) {
            MyFirebaseRegister firebaseRegister = new MyFirebaseRegister(MainActivity.this);
            String user_id = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);
            firebaseRegister.RegisterUser(user_id);
        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.iv_header_home) {
            Fragment fm = new Home_fragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.contentPanel, fm, "Home_fragment")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.iv_header_img) {
            if (sessionManagement.isLoggedIn()) {
                showImageChooser();
            } else {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                loginIntent.putExtra("setfinish", "true");
                startActivity(loginIntent);

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        } else if (id == R.id.iv_header_edit) {
            if (sessionManagement.isLoggedIn()) {
                Intent i = new Intent(MainActivity.this, Edit_profileActivity.class);
                startActivity(i);
            } else {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                loginIntent.putExtra("setfinish", "true");
                startActivity(loginIntent);

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        }
    }

    // check user is login or not if login then show user data
    private void updateSidemenu() {
        if (sessionManagement.isLoggedIn()) {
            String getname = sessionManagement.getUserDetails().get(BaseURL.KEY_NAME);
            String getimage = sessionManagement.getUserDetails().get(BaseURL.KEY_IMAGE);
            String getemail = sessionManagement.getUserDetails().get(BaseURL.KEY_EMAIL);

            Picasso.with(this)
                    .load(BaseURL.IMG_PROFILE_URL + getimage)
                    .placeholder(R.drawable.ic_loading)
                    .into(iv_header_img);

            tv_header_name.setText(getname);
            tv_header_email.setText(getemail);
            nav_menu.findItem(R.id.menu_user).setVisible(true);
            nav_menu.findItem(R.id.nav_logout).setVisible(true);

        } else {
            tv_header_name.setText(getResources().getString(R.string.login));
            tv_header_email.setText("");
            nav_menu.findItem(R.id.menu_user).setVisible(false);
            nav_menu.findItem(R.id.nav_logout).setVisible(false);

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        final MenuItem cart = menu.findItem(R.id.action_cart);

        View count = cart.getActionView();
        count.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // adding click event in custom actionbar item
                menu.performIdentifierAction(cart.getItemId(), 0);
            }
        });

        totalBudgetCount = (TextView) count.findViewById(R.id.tv_action_cart);

        updateCounter();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_cart) {
            DatabaseHandler dbcart = new DatabaseHandler(MainActivity.this);
            if (dbcart.getCartCount() > 0) {
                Intent cartIntent = new Intent(this, CartActivity.class);
                startActivity(cartIntent);
            } else {
                CommonAppCompatActivity.showToast(this, getResources().getString(R.string.cart_empty));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateCounter() {
        if (totalBudgetCount != null) {
            DatabaseHandler dbcart = new DatabaseHandler(MainActivity.this);
            totalBudgetCount.setText("" + dbcart.getCartCount());
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Intent i = null;

        if (id == R.id.nav_profile) {
            i = new Intent(MainActivity.this, Edit_profileActivity.class);
        } else if (id == R.id.nav_order) {
            i = new Intent(MainActivity.this, My_orderActivity.class);
        } else if (id == R.id.nav_upload) {
            i = new Intent(MainActivity.this, My_prescriptionActivity.class);
        } else if (id == R.id.nav_offer) {
            i = new Intent(MainActivity.this, OffersActivity.class);
        } else if (id == R.id.nav_prescription) {
            i = new Intent(MainActivity.this, Prescription_listActivity.class);
        } else if (id == R.id.nav_medical) {
            i = new Intent(MainActivity.this, Medical_productActivity.class);
        } else if (id == R.id.nav_address) {
            i = new Intent(MainActivity.this, Delivery_addressActivity.class);
        } else if (id == R.id.nav_notification) {
            i = new Intent(MainActivity.this, NotificationActivity.class);
        } else if (id == R.id.nav_rate) {
            reviewOnApp();
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_logout) {
            sessionManagement.logoutSession();
            finish();
        }

        if (i != null) {
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // share intent for share app url from another app
    public void shareApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hi friends i am using ." + " http://play.google.com/store/apps/details?id=" + getPackageName() + " APP");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    // redirect to play store in particular app
    public void reviewOnApp() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // update cart counter every time on app resume
        updateCounter();
    }

    // show image choosing dialog
    private void showImageChooser() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        // ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_profile, null);
        dialogBuilder.setView(dialogView);

        //TextView tv_camera = (TextView) dialogView.findViewById(R.id.tv_camera);
        TextView tv_gallery = (TextView) dialogView.findViewById(R.id.tv_gallery);
        TextView tv_cancle = (TextView) dialogView.findViewById(R.id.tv_cancle);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

//        tv_camera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                alertDialog.dismiss();
//
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
//
//                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
//
//                // start the image capture Intent
//                startActivityForResult(cameraIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE1);
//            }
//        });

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
                    iv_header_img.setImageBitmap(bitmap);

                    String user_id = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);
                    new updateProfile(user_id, file.getAbsolutePath()).execute();
                }

            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled Image capture
                Toast.makeText(MainActivity.this,
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();

            } else {
                // failed to capture image
                Toast.makeText(MainActivity.this,
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
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
                    iv_header_img.setImageBitmap(b);

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
            jsonParser = new JSONParser(MainActivity.this);
        }

        protected Void doInBackground(String... urls) {

            String json_responce = null;
            try {
                // call json parser class for make json request response
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
                Toast.makeText(MainActivity.this, getResources().getString(R.string.profile_pic_updated), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "" + error_string, Toast.LENGTH_SHORT).show();
            }
        }
    }


}
