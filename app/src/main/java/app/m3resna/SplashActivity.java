package app.m3resna;

import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.Fade;
import androidx.transition.Visibility;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import app.m3resna.classes.GlobalFunctions;
import app.m3resna.classes.LocaleHelper;
import app.m3resna.classes.SessionManager;
import butterknife.BindView;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 5000;
    ImageView logo;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(LocaleHelper.onAttach(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //make the screen without statusBar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        GlobalFunctions.setDefaultLanguage(this);
        GlobalFunctions.setUpFont(this);
        logo = (ImageView) findViewById(R.id.activity_splash_iv_logo);

        //get the location of view(imageView,textView,...etc) on screen
//        int[] headLocation = new int[2];
//        head.getLocationOnScreen(headLocation);
//        int headX = headLocation[0];
//        int headY = headLocation[1];

//        int[] logoLocation = new int[2];
//        head.getLocationOnScreen(logoLocation);
//        int logoX = logoLocation[0];
//        int logoY = logoLocation[1];

//        TranslateAnimation headAnimation = new TranslateAnimation(0, headX, -700, headY);
//        headAnimation.setDuration(3000);
//        headAnimation.setFillAfter(true);

//        TranslateAnimation logoAnimation = new TranslateAnimation(0, 0, 700, 0);
//        logoAnimation.setDuration(2500);
//        logoAnimation.setFillAfter(true);

//        Animation fadeIn = new AlphaAnimation(0, 1);
//        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
//        fadeIn.setDuration(3000);
//
//
//        AnimationSet animation = new AnimationSet(false); //change to false
//        animation.addAnimation(fadeIn);
//        logo.startAnimation(animation);


        //this.setAnimation(animation);
       // head.startAnimation(headAnimation);
        //logo.startAnimation(logoAnimation);

        ScaleAnimation scale =new ScaleAnimation(0, 1f, 0, 1f, Animation.RELATIVE_TO_SELF, (float)0.5,Animation.RELATIVE_TO_SELF, (float)0.5);
        scale.setDuration(1500);
        scale.setFillAfter(true);
        logo.startAnimation(scale);

        //after splash screen >> the main activity will be opened and the main fragment will be displayed(MagalesTypesFragment)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
