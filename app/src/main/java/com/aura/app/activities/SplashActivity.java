package com.aura.app.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aura.app.R;
import com.aura.app.supabase.SupabaseClient;
import com.aura.app.utils.U;

/**
 * Écran de démarrage AURA.
 * Affiche le logo avec animation, puis redirige selon la session.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long DURATION = 2500L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWindow();
        View root = buildView();
        setContentView(root);
        animateIn(root);
        new Handler(Looper.getMainLooper()).postDelayed(this::navigate, DURATION);
    }

    private void setupWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(U.C_BG);
    }

    private View buildView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(U.C_BG);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Logo
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.aura_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int size = U.dp(this, 120);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMargins(0, 0, 0, U.dp(this, 28));
        logo.setLayoutParams(lp);
        logo.setAlpha(0f);
        logo.setTranslationY(U.dp(this, 30));
        root.addView(logo);

        // Nom
        TextView name = new TextView(this);
        name.setText("AURA");
        name.setTextColor(U.C_TEXT);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44);
        name.setTypeface(name.getTypeface(), Typeface.BOLD);
        name.setLetterSpacing(0.18f);
        name.setGravity(Gravity.CENTER);
        name.setAlpha(0f);
        root.addView(name);

        // Slogan
        TextView tag = new TextView(this);
        tag.setText("Exprimez votre aura.");
        tag.setTextColor(U.C_TEXT2);
        tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tag.setLetterSpacing(0.04f);
        tag.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.setMargins(0, U.dp(this, 6), 0, 0);
        tag.setLayoutParams(tp);
        tag.setAlpha(0f);
        root.addView(tag);

        return root;
    }

    private void animateIn(View root) {
        LinearLayout ll = (LinearLayout) root;
        View logo = ll.getChildAt(0);
        View name = ll.getChildAt(1);
        View tag  = ll.getChildAt(2);

        // Logo monte et apparaît
        ObjectAnimator a1 = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f).setDuration(600);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(logo, "translationY",
                U.dp(this, 30), 0f).setDuration(600);

        // Nom apparaît décalé
        ObjectAnimator a3 = ObjectAnimator.ofFloat(name, "alpha", 0f, 1f).setDuration(500);
        a3.setStartDelay(300);

        // Slogan encore plus décalé
        ObjectAnimator a4 = ObjectAnimator.ofFloat(tag, "alpha", 0f, 1f).setDuration(400);
        a4.setStartDelay(550);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(a1, a2, a3, a4);
        set.start();
    }

    private void navigate() {
        Intent i = SupabaseClient.isLoggedIn()
                ? new Intent(this, MainActivity.class)
                : new Intent(this, AuthActivity.class);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
