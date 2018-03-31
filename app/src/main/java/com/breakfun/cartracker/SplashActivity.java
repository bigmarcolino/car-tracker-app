package com.breakfun.cartracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class SplashActivity extends Activity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        TextView tv = (TextView)findViewById(R.id.splash_app_name);
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/Windlesham Pro Medium.ttf");
        tv.setTypeface(face);

        final int SPLASH_DISPLAY_LENGTH = 500;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent main_intent = new Intent(SplashActivity.this, OptionsActivity.class);
                SplashActivity.this.startActivity(main_intent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}