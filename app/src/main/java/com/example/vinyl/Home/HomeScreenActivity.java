package com.example.vinyl.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.vinyl.LoginSignup.LoginMobileNumber;
import com.example.vinyl.Profile;
import com.example.vinyl.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeScreenActivity extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;

    public static FirebaseFirestore db= FirebaseFirestore.getInstance();
    public static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public static String uid = user.getUid();
    public static DocumentReference currentUserDocumentReference = db.collection("Users").document(uid);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        int Permission_All=1;
        String[] Permissions = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE };
        if(!hasPermissions(this, Permissions)){
            ActivityCompat.requestPermissions(HomeScreenActivity.this, Permissions, Permission_All);
        }

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        Log.i("testingpermission", "reached");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                Log.i("testingpermission", "reached3");
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {

                    Log.i("testingpermission", "reached5");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater= getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.menu_settings:

                return true;

            case R.id.menu_profile:
                Intent intent= new Intent(getApplicationContext(),Profile.class);
                startActivity(intent);
                return true;

            case R.id.menu_logout:
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Confirm Log out")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(HomeScreenActivity.this," Successfully Logged Out",Toast.LENGTH_SHORT).show();
                                FirebaseAuth.getInstance().signOut();
                                Intent intent= new Intent(HomeScreenActivity.this, LoginMobileNumber.class);
                                Log.i("Item selected","Log out");

                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                finish();
                                user=null;
                                uid=null;
                                currentUserDocumentReference=null;
                                startActivity(intent);

//                                finishAffinity();
//                                onDestroy();
                            }
                        })
                        .setNegativeButton("No",null)
                        .show();

                return true;

            case R.id.menu_email_us:

                Intent emailIntent= new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                //mail to protocol that lets you mail using client installed on your device
                String[] to={"neetibisht919@gmail.com"};
                emailIntent.putExtra(Intent.EXTRA_EMAIL,to);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Vinyl");
                emailIntent.setType("message/rfc822");
                //specification for email

                Intent chooser=Intent.createChooser(emailIntent,"Send Email");
                startActivity(chooser);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean doubleBackToExitPressedOnce;
    private Handler mHandler = new Handler();

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) { mHandler.removeCallbacks(mRunnable); }
    }

    @Override
    public void onBackPressed(){
        if (doubleBackToExitPressedOnce) {
            this.finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please press back again to exit", Toast.LENGTH_SHORT).show();

        mHandler.postDelayed(mRunnable, 2000);
    }
}