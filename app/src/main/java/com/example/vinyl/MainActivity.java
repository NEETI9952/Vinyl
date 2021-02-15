package com.example.vinyl;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vinyl.LoginSignup.LoginMobileNumber;

public class MainActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN=3000;
    Animation topAnim,bottomAnim;
    ImageView image;
    TextView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image=findViewById(R.id.imageViewlogo);
        logo=findViewById(R.id.textViewLogo);
        topAnim= AnimationUtils.loadAnimation(this,R.anim.top_animatiom);
        bottomAnim= AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        topAnim.setDuration(2000);
        bottomAnim.setDuration(2000);

        image.setAnimation(topAnim);
        logo.setAnimation(bottomAnim);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent= new Intent(MainActivity.this, LoginMobileNumber.class);
                Pair[] pairs= new Pair[2];
                pairs[0]= new Pair<View,String>(image,"logo_image");
                pairs[1]= new Pair<View,String>(logo,"logo_text");

                ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,pairs);
                startActivity(intent,options.toBundle());
            }
        },SPLASH_SCREEN);
    }
}