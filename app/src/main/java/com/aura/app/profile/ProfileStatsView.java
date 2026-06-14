package com.aura.app.profile;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aura.app.utils.U;

/**
 * Composant réutilisable affichant les statistiques d'un profil AURA :
 * Publications · Abonnés · Abonnements.
 *
 * <p>Construit entièrement en Java (aucun XML), peut être inséré
 * dans n'importe quel écran de profil.
 */
public class ProfileStatsView extends LinearLayout {

    private TextView tvPosts, tvFollowers, tvFollowing;

    public ProfileStatsView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context c) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tvPosts     = addStatColumn(c, "Publications");
        tvFollowers = addStatColumn(c, "Abonnés");
        tvFollowing = addStatColumn(c, "Abonnements");
    }

    private TextView addStatColumn(Context c, String label) {
        LinearLayout col = new LinearLayout(c);
        col.setOrientation(VERTICAL);
        col.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        col.setLayoutParams(p);

        TextView count = new TextView(c);
        count.setTextColor(U.C_TEXT);
        count.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        count.setTypeface(count.getTypeface(), Typeface.BOLD);
        count.setText("0");
        count.setGravity(Gravity.CENTER);
        col.addView(count);

        TextView lbl = new TextView(c);
        lbl.setTextColor(U.C_TEXT2);
        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        lbl.setText(label);
        lbl.setGravity(Gravity.CENTER);
        col.addView(lbl);

        addView(col);
        return count;
    }

    /**
     * Met à jour les trois statistiques affichées.
     *
     * @param posts     Nombre de publications
     * @param followers Nombre d'abonnés
     * @param following Nombre d'abonnements
     */
    public void setStats(int posts, int followers, int following) {
        tvPosts.setText(String.valueOf(posts));
        tvFollowers.setText(String.valueOf(followers));
        tvFollowing.setText(String.valueOf(following));
    }
}
