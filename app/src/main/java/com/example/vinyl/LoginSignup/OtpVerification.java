package com.example.vinyl.LoginSignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.example.vinyl.Home.HomeScreenActivity;
import com.example.vinyl.Profile;
import com.example.vinyl.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OtpVerification extends AppCompatActivity {
    PinView pinView;
    String systemOtp;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    Button verify;
    TextView textViewNumber;
    public FirebaseFirestore db= FirebaseFirestore.getInstance();
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        pinView=findViewById(R.id.pinView);
        progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        verify=findViewById(R.id.verifyOtp);
        mAuth=FirebaseAuth.getInstance();
        textViewNumber=findViewById(R.id.textViewNumber);

        phoneNumber=getIntent().getStringExtra("PhoneNumber");

        textViewNumber.setText("Enter One Time Password sent on \n   "+phoneNumber);

        sendVerificationCodeToUser(phoneNumber);
    }

    private void sendVerificationCodeToUser(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks=
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    String code=phoneAuthCredential.getSmsCode();
                    if(code!=null) {
                        pinView.setText(code);
                        progressBar.setVisibility(View.VISIBLE);
                        verifyCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(OtpVerification.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    systemOtp=s;
                }
            };

    private void verifyCode(String codeByUser) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(systemOtp,codeByUser);
        SignInUsingCredential(credential);
    }

    private void SignInUsingCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(OtpVerification.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Otp", "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            String uid = user.getUid();

                            long creationTimestamp = user.getMetadata().getCreationTimestamp();
                            long lastSignInTimestamp = user.getMetadata().getLastSignInTimestamp();

                            if (creationTimestamp==lastSignInTimestamp) {
                                DocumentReference user_profileReference = db.collection("Users").document(uid);

                                Map<String,Object> profile = new HashMap<>();
                                profile.put("Phone Number",phoneNumber);
                                profile.put("Username","");
                                profile.put("Status","");
                                profile.put("profileImage","");

                                user_profileReference.set(profile).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(OtpVerification.this,"Account Created",Toast.LENGTH_SHORT).show();
                                        DocumentReference FriendReference = db.collection(uid).document("Friends");
                                        DocumentReference requestReference = db.collection(uid).document("Request");

                                        Intent intent= new Intent(getApplicationContext(), Profile.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                          intent.putExtra("uid",uid);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(OtpVerification.this,"Something went wrong /n Try again",Toast.LENGTH_SHORT).show();
                                        Intent intent= new Intent(getApplicationContext(),LoginMobileNumber.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                });
                            }else{
                                Intent intent= new Intent(getApplicationContext(), HomeScreenActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                          intent.putExtra("uid",uid);
                                startActivity(intent);
                            }

                        } else {
                            Toast.makeText(OtpVerification.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            // Sign in failed, display a message and update the UI
                            Log.w("Otp", "signInWithCredential:failure", task.getException());


                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(OtpVerification.this, "The Otp entered was invalid", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    public void verifyOtp(View view){
        String code=pinView.getText().toString();

        if(code.isEmpty()){
            Toast.makeText(OtpVerification.this, "Otp cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!code.isEmpty()){
            progressBar.setVisibility(View.VISIBLE);
            verifyCode(code);
        }
    }

    public void stopOtp(View view){
        Intent intent= new Intent(getApplicationContext(),LoginMobileNumber.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(OtpVerification.this,"To go back click on the close button",Toast.LENGTH_SHORT).show();
    }
}