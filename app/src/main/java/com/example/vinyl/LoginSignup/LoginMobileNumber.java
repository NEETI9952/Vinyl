package com.example.vinyl.LoginSignup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vinyl.Home.HomeScreenActivity;
import com.example.vinyl.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

public class LoginMobileNumber extends AppCompatActivity {

    Button getOtpButton;
    TextView titleText;
    TextInputLayout phoneNumber;
    CountryCodePicker countryCodePicker;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_mobile_number);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            login();
        }

        getOtpButton=findViewById(R.id.loginGetOtpButton);
        titleText=findViewById(R.id.loginTextView);
        phoneNumber=findViewById(R.id.loginPhoneNumber);
        countryCodePicker=findViewById(R.id.loginCcp);
    }

    public void login() {
        //move to next activity
        Intent intent = new Intent(this, HomeScreenActivity.class);
        startActivity(intent);
    }

    public void getOtp(View view){

        if(!validateFields()){
            return;
        }
        String phoneNumberText=phoneNumber.getEditText().getText().toString().trim();
        String ccp = countryCodePicker.getSelectedCountryCode();
        String verificationPhoneNumber="+"+ccp+phoneNumberText;

        Intent intent = new Intent(getApplicationContext(), OtpVerification.class);
        intent.putExtra("PhoneNumber",verificationPhoneNumber);
        startActivity(intent);
    }

    private boolean validateFields() {
        String val =phoneNumber.getEditText().getText().toString().trim();


        if (val.isEmpty()) {
            phoneNumber.setError("Field cannot be empty");
            return false;
        }else {
            return true;
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
