package com.aura.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.aura.app.fragments.ProfileFragment;
import com.aura.app.utils.U;

/**
 * Activité profil utilisateur.
 * Reçoit un userId en extra pour afficher n'importe quel profil.
 */
public class ProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "user_id";
    public static final int    CONTAINER_ID  = 0x7F_DD_01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setId(CONTAINER_ID);
        root.setBackgroundColor(U.C_BG);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        if (savedInstanceState == null) {
            String userId = getIntent().getStringExtra(EXTRA_USER_ID);
            ProfileFragment frag = ProfileFragment.newInstance(userId);
            getSupportFragmentManager().beginTransaction()
                    .replace(CONTAINER_ID, frag)
                    .commit();
        }
    }
}
