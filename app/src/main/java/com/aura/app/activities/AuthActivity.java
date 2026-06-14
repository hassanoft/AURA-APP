package com.aura.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.aura.app.auth.LoginFragment;
import com.aura.app.utils.U;

/**
 * Activité hôte pour l'authentification (Login + Register Wizard).
 * Contient un FrameLayout scrollable qui accueille les fragments.
 */
public class AuthActivity extends AppCompatActivity {

    public static final int CONTAINER_ID = 0x7F_AA_01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ScrollView racine
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(U.C_BG);
        scroll.setFillViewport(true);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Padding haut (logo dans AuthActivity) géré dans chaque fragment
        FrameLayout container = new FrameLayout(this);
        container.setId(CONTAINER_ID);
        int pad = U.dp(this, 24);
        container.setPadding(pad, U.dp(this, 72), pad, U.dp(this, 32));
        container.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        scroll.addView(container);
        setContentView(scroll);

        if (savedInstanceState == null) {
            showFragment(new LoginFragment(), false);
        }
    }

    /** Affiche un fragment dans le conteneur central */
    public void showFragment(Fragment f, boolean backStack) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        tx.replace(CONTAINER_ID, f);
        if (backStack) tx.addToBackStack(null);
        tx.commit();
    }

    /** Appelé après connexion/inscription réussie */
    public void onAuthSuccess() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
