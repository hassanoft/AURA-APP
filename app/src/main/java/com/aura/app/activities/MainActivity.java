package com.aura.app.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.aura.app.R;
import com.aura.app.fragments.FeedFragment;
import com.aura.app.fragments.MessagesFragment;
import com.aura.app.fragments.NotificationsFragment;
import com.aura.app.fragments.ProfileFragment;
import com.aura.app.fragments.SearchFragment;
import com.aura.app.services.RealtimeService;
import com.aura.app.utils.U;

/**
 * Activité principale AURA.
 * Navigation bottom bar 5 onglets : Accueil, Recherche, Créer, Notifications, Profil.
 * Interface 100% Java, aucun XML.
 */
public class MainActivity extends AppCompatActivity {

    public static final int CONTAINER_ID = 0x7F_BB_01;

    // ─── Bottom nav ───────────────────────────────────────────────────────────
    private static final int[] NAV_IDS = {
            0x7F_C0_01, 0x7F_C0_02, 0x7F_C0_03, 0x7F_C0_04, 0x7F_C0_05
    };
    private static final String[] NAV_LABELS = {
            "Accueil", "Recherche", "Créer", "Notifications", "Messages"
    };
    // Icônes Unicode Material-like (fallback sans resources SVG)
    private static final String[] NAV_ICONS = {
            "⌂", "⌕", "＋", "♔", "✉"
    };

    private TextView[]  navItems;
    private int         selectedTab = 0;

    // ─── Service Realtime ─────────────────────────────────────────────────────
    private RealtimeService realtimeService;
    private boolean         serviceBound;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName n, IBinder b) {
            realtimeService = ((RealtimeService.LocalBinder) b).getService();
            serviceBound    = true;
        }
        @Override public void onServiceDisconnected(ComponentName n) {
            serviceBound = false;
        }
    };

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildLayout());

        // Démarre le service Realtime
        Intent svcIntent = new Intent(this, RealtimeService.class);
        startService(svcIntent);
        bindService(svcIntent, connection, Context.BIND_AUTO_CREATE);

        // Affiche l'onglet Accueil par défaut
        if (savedInstanceState == null) selectTab(0);
    }

    // ─── Construction de l'UI ─────────────────────────────────────────────────

    private View buildLayout() {
        // Conteneur racine
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(U.C_BG);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Zone de contenu des fragments
        FrameLayout content = new FrameLayout(this);
        content.setId(CONTAINER_ID);
        content.setBackgroundColor(U.C_BG);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        content.setLayoutParams(cp);
        root.addView(content);

        // Barre de navigation en bas
        root.addView(buildBottomNav());

        return root;
    }

    private View buildBottomNav() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackgroundColor(U.C_SURFACE);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setWeightSum(NAV_LABELS.length);

        // Elevation via shadow
        bar.setElevation(U.dp(this, 8));

        LinearLayout.LayoutParams barP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, U.dp(this, 64));
        bar.setLayoutParams(barP);

        navItems = new TextView[NAV_LABELS.length];

        for (int i = 0; i < NAV_LABELS.length; i++) {
            final int idx = i;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            item.setLayoutParams(ip);
            item.setClickable(true);
            item.setFocusable(true);

            // Icône
            TextView icon = new TextView(this);
            icon.setText(NAV_ICONS[i]);
            icon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            icon.setGravity(Gravity.CENTER);
            icon.setTextColor(U.C_TEXT2);
            navItems[i] = icon;

            // Label
            TextView label = new TextView(this);
            label.setText(NAV_LABELS[i]);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            label.setGravity(Gravity.CENTER);
            label.setTextColor(U.C_TEXT2);

            item.addView(icon);
            item.addView(label);

            item.setOnClickListener(v -> selectTab(idx));
            bar.addView(item);
        }

        return bar;
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    private void selectTab(int idx) {
        // Mise à jour visuelle
        for (int i = 0; i < navItems.length; i++) {
            navItems[i].setTextColor(i == idx ? U.C_PRIMARY : U.C_TEXT2);
        }

        // Fragment à afficher
        Fragment fragment;
        switch (idx) {
            case 0:  fragment = new FeedFragment();          break;
            case 1:  fragment = new SearchFragment();        break;
            case 2:  openCreatePost(); return;
            case 3:  fragment = new NotificationsFragment(); break;
            case 4:  fragment = new MessagesFragment();      break;
            default: fragment = new FeedFragment();
        }

        selectedTab = idx;

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        tx.replace(CONTAINER_ID, fragment);
        tx.commit();
    }

    private void openCreatePost() {
        // Ouvre le fragment de création en modal bottom sheet
        com.aura.app.fragments.CreatePostFragment dialog =
                new com.aura.app.fragments.CreatePostFragment();
        dialog.show(getSupportFragmentManager(), "create_post");
    }

    // ─── Cycle de vie ─────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(connection);
            serviceBound = false;
        }
    }
}
