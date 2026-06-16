package com.aura.app.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Utilitaires UI — création programmatique de vues Material Design 3.
 * Toutes les vues AURA sont créées ici, sans XML.
 */
public final class U {

    // ─── Palette AURA ─────────────────────────────────────────────────────────
    public static final int C_PRIMARY   = 0xFF18B05A;
    public static final int C_P_DARK    = 0xFF0F8040;
    public static final int C_P_LIGHT   = 0xFF4FD18A;
    public static final int C_BG        = 0xFF121212;
    public static final int C_SURFACE   = 0xFF1E1E1E;
    public static final int C_SURFACE2  = 0xFF2A2A2A;
    public static final int C_TEXT      = 0xFFE8E8E8;
    public static final int C_TEXT2     = 0xFF9E9E9E;
    public static final int C_DIVIDER   = 0xFF2C2C2C;
    public static final int C_ERROR     = 0xFFCF6679;
    public static final int C_WHITE     = 0xFFFFFFFF;
    public static final int C_TRANS     = 0x00000000;

    private U() {}

    // ─── Conversions ──────────────────────────────────────────────────────────

    /** dp → px */
    public static int dp(Context c, float v) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics()));
    }

    /** sp → px */
    public static int sp(Context c, float v) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, v, c.getResources().getDisplayMetrics()));
    }

    // ─── LinearLayout params ──────────────────────────────────────────────────

    public static LinearLayout.LayoutParams llWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static LinearLayout.LayoutParams llMatch() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static LinearLayout.LayoutParams llMatchH(Context c, int heightDp) {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(c, heightDp));
    }

    // ─── Vues de base ─────────────────────────────────────────────────────────

    /** LinearLayout vertical centré, fond couleur donnée */
    public static LinearLayout vStack(Context c, int bgColor) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(bgColor);
        l.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return l;
    }

    /** LinearLayout horizontal */
    public static LinearLayout hStack(Context c, int bgColor) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setBackgroundColor(bgColor);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setLayoutParams(llMatch());
        return l;
    }

    /** TextView standard */
    public static TextView tv(Context c, String text, float spSize, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, spSize);
        t.setTextColor(color);
        if (bold) t.setTypeface(t.getTypeface(), Typeface.BOLD);
        return t;
    }

    /** Séparateur horizontal */
    public static View divider(Context c) {
        View v = new View(c);
        v.setBackgroundColor(C_DIVIDER);
        LinearLayout.LayoutParams p = llMatchH(c, 1);
        p.setMargins(0, dp(c, 4), 0, dp(c, 4));
        v.setLayoutParams(p);
        return v;
    }

    // ─── Champ de saisie Material Outlined ───────────────────────────────────

    /**
     * Crée un TextInputLayout avec style Outlined Material Design 3.
     *
     * @param c         Contexte
     * @param hint      Placeholder
     * @param inputType android.text.InputType.*
     * @return TextInputLayout prêt à l'emploi
     */
    public static TextInputLayout inputField(Context c, String hint, int inputType) {
        // Style outlined Material3 (utilise textInputStyle, puis force outline via setBoxBackgroundMode)
        TextInputLayout til = new TextInputLayout(c, null,
                com.google.android.material.R.attr.textInputStyle);
        til.setHint(hint);
        til.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        til.setBoxCornerRadii(dp(c, 12), dp(c, 12), dp(c, 12), dp(c, 12));
        til.setBoxStrokeColorStateList(ColorStateList.valueOf(C_PRIMARY));
        til.setHintTextColor(ColorStateList.valueOf(C_PRIMARY));
        til.setBoxBackgroundColor(C_SURFACE);

        LinearLayout.LayoutParams p = llMatch();
        p.setMargins(0, dp(c, 8), 0, dp(c, 8));
        til.setLayoutParams(p);

        TextInputEditText et = new TextInputEditText(til.getContext());
        et.setInputType(inputType);
        et.setTextColor(C_TEXT);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        et.setBackground(null);
        til.addView(et);

        return til;
    }

    /** Récupère le TextInputEditText enfant d'un TextInputLayout */
    public static TextInputEditText editOf(TextInputLayout til) {
        return (TextInputEditText) til.getEditText();
    }

    // ─── Boutons ──────────────────────────────────────────────────────────────

    /** Bouton principal vert plein */
    public static MaterialButton btnPrimary(Context c, String text) {
        MaterialButton b = new MaterialButton(c);
        b.setText(text);
        b.setTextColor(C_WHITE);
        b.setBackgroundTintList(ColorStateList.valueOf(C_PRIMARY));
        b.setCornerRadius(dp(c, 12));
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        b.setTypeface(b.getTypeface(), Typeface.BOLD);
        b.setLetterSpacing(0.04f);
        LinearLayout.LayoutParams p = llMatchH(c, 52);
        p.setMargins(0, dp(c, 8), 0, dp(c, 8));
        b.setLayoutParams(p);
        return b;
    }

    /** Bouton contour vert */
    public static MaterialButton btnOutline(Context c, String text) {
        MaterialButton b = new MaterialButton(c, null,
                com.google.android.material.R.attr.borderlessButtonStyle);
        b.setText(text);
        b.setTextColor(C_PRIMARY);
        b.setStrokeColor(ColorStateList.valueOf(C_PRIMARY));
        b.setStrokeWidth(dp(c, 1));
        b.setCornerRadius(dp(c, 12));
        b.setBackgroundTintList(ColorStateList.valueOf(C_TRANS));
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        LinearLayout.LayoutParams p = llMatchH(c, 52);
        p.setMargins(0, dp(c, 4), 0, dp(c, 4));
        b.setLayoutParams(p);
        return b;
    }

    // ─── Snackbar ─────────────────────────────────────────────────────────────

    public static void snackError(View root, String msg) {
        Snackbar s = Snackbar.make(root, msg, Snackbar.LENGTH_LONG);
        s.setBackgroundTint(C_ERROR);
        s.setTextColor(C_WHITE);
        s.show();
    }

    public static void snackOk(View root, String msg) {
        Snackbar s = Snackbar.make(root, msg, Snackbar.LENGTH_SHORT);
        s.setBackgroundTint(C_PRIMARY);
        s.setTextColor(C_WHITE);
        s.show();
    }

    // ─── Thread UI ────────────────────────────────────────────────────────────

    public static void ui(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }
}