package com.aura.app.auth;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aura.app.activities.AuthActivity;
import com.aura.app.managers.AuthManager;
import com.aura.app.models.User;
import com.aura.app.services.EmailService;
import com.aura.app.utils.AgeValidator;
import com.aura.app.utils.CodeGenerator;
import com.aura.app.utils.PasswordUtils;
import com.aura.app.utils.U;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

/**
 * Wizard d'inscription AURA en 5 étapes — 100% Java, aucun XML.
 *
 * Étape 1 : Prénom / Nom
 * Étape 2 : E-mail
 * Étape 3 : Date de naissance (roues style iOS)
 * Étape 4 : Mot de passe
 * Étape 5 : Code de confirmation e-mail
 */
public class RegisterWizardFragment extends Fragment {

    // ─── État du wizard ───────────────────────────────────────────────────────
    private int    currentStep = 1;
    private static final int TOTAL_STEPS = 5;

    // Données collectées
    private String firstName, lastName, email, password, birthDateIso;
    private String confirmationCode;

    // ─── Vues ─────────────────────────────────────────────────────────────────
    private LinearLayout  root;
    private FrameLayout   stepContainer;
    private TextView      tvStepTitle;
    private TextView      tvStepSub;
    private LinearLayout  progressBar;
    private MaterialButton btnNext, btnBack;
    private View          rootView;

    // ─── Champs par étape ─────────────────────────────────────────────────────
    // Étape 1
    private TextInputLayout tilFirstName, tilLastName;
    // Étape 2
    private TextInputLayout tilEmail;
    // Étape 3
    private NumberPicker    pickerDay, pickerMonth, pickerYear;
    // Étape 4
    private TextInputLayout tilPassword, tilPasswordConfirm;
    private TextView        tvStrength;
    // Étape 5
    private TextInputLayout tilCode;
    private TextView        tvCodeInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        rootView = buildShell();
        renderStep(currentStep);
        return rootView;
    }

    // ─── Shell du wizard ──────────────────────────────────────────────────────

    private View buildShell() {
        root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(U.C_BG);
        int pad = U.dp(requireContext(), 8);
        root.setPadding(pad, 0, pad, 0);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Barre de progression
        progressBar = buildProgressBar();
        root.addView(progressBar);

        // Titre étape
        tvStepTitle = U.tv(requireContext(), "", 22, U.C_TEXT, true);
        LinearLayout.LayoutParams tP = U.llWrap();
        tP.setMargins(0, U.dp(requireContext(), 16), 0, U.dp(requireContext(), 4));
        tvStepTitle.setLayoutParams(tP);
        root.addView(tvStepTitle);

        // Sous-titre
        tvStepSub = U.tv(requireContext(), "", 13, U.C_TEXT2, false);
        LinearLayout.LayoutParams sP = U.llWrap();
        sP.setMargins(0, 0, 0, U.dp(requireContext(), 20));
        tvStepSub.setLayoutParams(sP);
        root.addView(tvStepSub);

        // Conteneur contenu de l'étape
        stepContainer = new FrameLayout(requireContext());
        stepContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(stepContainer);

        // Boutons navigation
        root.addView(buildNavButtons());

        return root;
    }

    private LinearLayout buildProgressBar() {
        LinearLayout bar = U.hStack(requireContext(), U.C_BG);
        LinearLayout.LayoutParams p = U.llMatch();
        p.setMargins(0, 0, 0, U.dp(requireContext(), 8));
        bar.setLayoutParams(p);
        bar.setWeightSum(TOTAL_STEPS);
        return bar;
    }

    private LinearLayout buildNavButtons() {
        LinearLayout row = U.hStack(requireContext(), U.C_BG);
        LinearLayout.LayoutParams rP = U.llMatch();
        rP.setMargins(0, U.dp(requireContext(), 16), 0, 0);
        row.setLayoutParams(rP);
        row.setWeightSum(2f);

        btnBack = new MaterialButton(requireContext(), null,
                com.google.android.material.R.attr.borderlessButtonStyle);
        btnBack.setText("← Retour");
        btnBack.setTextColor(U.C_TEXT2);
        btnBack.setBackgroundTintList(ColorStateList.valueOf(U.C_TRANS));
        LinearLayout.LayoutParams bP = new LinearLayout.LayoutParams(0, U.dp(requireContext(), 48), 1f);
        bP.setMargins(0, 0, U.dp(requireContext(), 8), 0);
        btnBack.setLayoutParams(bP);
        btnBack.setOnClickListener(v -> goBack());
        row.addView(btnBack);

        btnNext = new MaterialButton(requireContext());
        btnNext.setText("Suivant");
        btnNext.setTextColor(U.C_WHITE);
        btnNext.setBackgroundTintList(ColorStateList.valueOf(U.C_PRIMARY));
        btnNext.setCornerRadius(U.dp(requireContext(), 12));
        LinearLayout.LayoutParams nP = new LinearLayout.LayoutParams(0, U.dp(requireContext(), 48), 1f);
        btnNext.setLayoutParams(nP);
        btnNext.setOnClickListener(v -> goNext());
        row.addView(btnNext);

        return row;
    }

    // ─── Rendu d'une étape ────────────────────────────────────────────────────

    private void renderStep(int step) {
        updateProgress(step);
        stepContainer.removeAllViews();

        switch (step) {
            case 1: renderStep1(); break;
            case 2: renderStep2(); break;
            case 3: renderStep3(); break;
            case 4: renderStep4(); break;
            case 5: renderStep5(); break;
        }

        btnBack.setVisibility(step > 1 ? View.VISIBLE : View.GONE);
        btnNext.setText(step == TOTAL_STEPS ? "Terminer ✓" : "Suivant");
    }

    private void updateProgress(int step) {
        progressBar.removeAllViews();
        int gap = U.dp(requireContext(), 4);
        for (int i = 1; i <= TOTAL_STEPS; i++) {
            View seg = new View(requireContext());
            seg.setBackgroundColor(i <= step ? U.C_PRIMARY : U.C_DIVIDER);
            // Coins arrondis
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setColor(i <= step ? U.C_PRIMARY : U.C_DIVIDER);
            gd.setCornerRadius(U.dp(requireContext(), 4));
            seg.setBackground(gd);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, U.dp(requireContext(), 5), 1f);
            p.setMargins(i > 1 ? gap : 0, 0, 0, 0);
            seg.setLayoutParams(p);
            progressBar.addView(seg);
        }
    }

    // ── Étape 1 : Prénom / Nom ────────────────────────────────────────────────
    private void renderStep1() {
        tvStepTitle.setText("Votre identité");
        tvStepSub.setText("Comment vous appelez-vous ?");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tilFirstName = U.inputField(requireContext(), "Prénom",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        if (firstName != null && tilFirstName.getEditText() != null)
            tilFirstName.getEditText().setText(firstName);
        layout.addView(tilFirstName);

        tilLastName = U.inputField(requireContext(), "Nom",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        if (lastName != null && tilLastName.getEditText() != null)
            tilLastName.getEditText().setText(lastName);
        layout.addView(tilLastName);

        stepContainer.addView(layout);
    }

    private boolean validateStep1() {
        String fn = tilFirstName.getEditText() != null
                ? tilFirstName.getEditText().getText().toString().trim() : "";
        String ln = tilLastName.getEditText() != null
                ? tilLastName.getEditText().getText().toString().trim() : "";

        tilFirstName.setError(null); tilLastName.setError(null);

        if (fn.length() < 2) { tilFirstName.setError("Minimum 2 caractères"); return false; }
        if (fn.length() > 25){ tilFirstName.setError("Maximum 25 caractères"); return false; }
        if (ln.length() < 2) { tilLastName.setError("Minimum 2 caractères"); return false; }
        if (ln.length() > 25){ tilLastName.setError("Maximum 25 caractères"); return false; }

        firstName = fn; lastName = ln;
        return true;
    }

    // ── Étape 2 : E-mail ──────────────────────────────────────────────────────
    private void renderStep2() {
        tvStepTitle.setText("Votre e-mail");
        tvStepSub.setText("Un code de confirmation vous sera envoyé.");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tilEmail = U.inputField(requireContext(), "Adresse e-mail",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        if (email != null && tilEmail.getEditText() != null)
            tilEmail.getEditText().setText(email);
        layout.addView(tilEmail);

        stepContainer.addView(layout);
    }

    private void validateStep2(Runnable onOk) {
        String em = tilEmail.getEditText() != null
                ? tilEmail.getEditText().getText().toString().trim() : "";
        tilEmail.setError(null);

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
            tilEmail.setError("Format d'e-mail invalide");
            return;
        }

        btnNext.setEnabled(false);
        btnNext.setText("Vérification…");

        AuthManager.getInstance().isEmailAvailable(em, available -> U.ui(() -> {
            btnNext.setEnabled(true);
            btnNext.setText("Suivant");
            if (!available) {
                tilEmail.setError("Cette adresse est déjà utilisée.");
            } else {
                email = em;
                onOk.run();
            }
        }));
    }

    // ── Étape 3 : Date de naissance ───────────────────────────────────────────
    private void renderStep3() {
        tvStepTitle.setText("Date de naissance");
        tvStepSub.setText("Vous devez avoir au moins 15 ans.");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Rangée de roues (JJ | MM | AAAA)
        LinearLayout pickers = U.hStack(requireContext(), U.C_BG);
        pickers.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams pP = U.llMatch();
        pP.setMargins(0, U.dp(requireContext(), 8), 0, U.dp(requireContext(), 8));
        pickers.setLayoutParams(pP);

        // Jour
        pickerDay = makeNumberPicker(1, 31, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        pickers.addView(wrapPicker(pickerDay, "Jour"));

        TextView sep1 = U.tv(requireContext(), " / ", 20, U.C_TEXT2, false);
        pickers.addView(sep1);

        // Mois
        pickerMonth = makeNumberPicker(1, 12, Calendar.getInstance().get(Calendar.MONTH) + 1);
        pickers.addView(wrapPicker(pickerMonth, "Mois"));

        TextView sep2 = U.tv(requireContext(), " / ", 20, U.C_TEXT2, false);
        pickers.addView(sep2);

        // Année (age 15-100 ans depuis aujourd'hui)
        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        pickerYear = makeNumberPicker(curYear - 100, curYear - 15, curYear - 20);
        pickers.addView(wrapPicker(pickerYear, "Année"));

        layout.addView(pickers);

        // Message d'erreur âge
        TextView tvAgeError = new TextView(requireContext());
        tvAgeError.setId(View.generateViewId());
        tvAgeError.setTextColor(U.C_ERROR);
        tvAgeError.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvAgeError.setGravity(Gravity.CENTER);
        tvAgeError.setTag("age_error");
        layout.addView(tvAgeError);

        stepContainer.addView(layout);
    }

    private NumberPicker makeNumberPicker(int min, int max, int value) {
        NumberPicker np = new NumberPicker(requireContext());
        np.setMinValue(min);
        np.setMaxValue(max);
        np.setValue(value);
        // Style iOS-like : supprimer le diviseur de sélection
        try {
            java.lang.reflect.Field f = NumberPicker.class.getDeclaredField("mSelectionDivider");
            f.setAccessible(true);
            f.set(np, null);
        } catch (Exception ignored) {}
        // Forcer la couleur du texte des EditText internes (API publique indisponible)
        try {
            for (int i = 0; i < np.getChildCount(); i++) {
                View child = np.getChildAt(i);
                if (child instanceof android.widget.EditText) {
                    ((android.widget.EditText) child).setTextColor(U.C_TEXT);
                }
            }
        } catch (Exception ignored) {}
        return np;
    }

    private LinearLayout wrapPicker(NumberPicker np, String label) {
        LinearLayout col = new LinearLayout(requireContext());
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams cp = U.llWrap();
        cp.setMargins(U.dp(requireContext(), 8), 0, U.dp(requireContext(), 8), 0);
        col.setLayoutParams(cp);

        TextView lbl = U.tv(requireContext(), label, 11, U.C_TEXT2, false);
        lbl.setGravity(Gravity.CENTER);
        col.addView(lbl);
        col.addView(np);
        return col;
    }

    private boolean validateStep3() {
        int day   = pickerDay.getValue();
        int month = pickerMonth.getValue();
        int year  = pickerYear.getValue();

        // Cherche le TextView d'erreur
        TextView tvErr = stepContainer.findViewWithTag("age_error");

        AgeValidator.Result res = AgeValidator.validate(day, month, year);
        switch (res) {
            case VALID:
                birthDateIso = AgeValidator.toIso(day, month, year);
                if (tvErr != null) tvErr.setText("");
                return true;
            case TOO_YOUNG:
                if (tvErr != null) tvErr.setText("Vous devez avoir au moins 15 ans.");
                return false;
            case TOO_OLD:
                if (tvErr != null) tvErr.setText("Âge invalide (maximum 100 ans).");
                return false;
            default:
                if (tvErr != null) tvErr.setText("Date invalide.");
                return false;
        }
    }

    // ── Étape 4 : Mot de passe ────────────────────────────────────────────────
    private void renderStep4() {
        tvStepTitle.setText("Mot de passe");
        tvStepSub.setText("8 caractères min. · majuscule · minuscule · chiffre");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tilPassword = U.inputField(requireContext(), "Mot de passe",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        layout.addView(tilPassword);

        tilPasswordConfirm = U.inputField(requireContext(), "Confirmer le mot de passe",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilPasswordConfirm.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        layout.addView(tilPasswordConfirm);

        // Barre de force
        tvStrength = U.tv(requireContext(), "", 12, U.C_TEXT2, false);
        LinearLayout.LayoutParams sP = U.llMatch();
        sP.setMargins(0, 0, 0, U.dp(requireContext(), 8));
        tvStrength.setLayoutParams(sP);
        layout.addView(tvStrength);

        // Mise à jour dynamique de la force
        if (tilPassword.getEditText() != null) {
            tilPassword.getEditText().addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                    updateStrength(s.toString());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        stepContainer.addView(layout);
    }

    private void updateStrength(String pwd) {
        int score = PasswordUtils.strength(pwd);
        String[] labels = {"", "Très faible", "Faible", "Moyen", "Fort"};
        int[]    colors = {U.C_TEXT2, U.C_ERROR, 0xFFFF8C00, U.C_PRIMARY, U.C_P_LIGHT};
        if (tvStrength != null && score > 0 && score < labels.length) {
            tvStrength.setText("Force : " + labels[score]);
            tvStrength.setTextColor(colors[score]);
        }
    }

    private boolean validateStep4() {
        String pwd  = tilPassword.getEditText() != null
                ? tilPassword.getEditText().getText().toString() : "";
        String pwd2 = tilPasswordConfirm.getEditText() != null
                ? tilPasswordConfirm.getEditText().getText().toString() : "";

        tilPassword.setError(null); tilPasswordConfirm.setError(null);

        String err = PasswordUtils.validate(pwd);
        if (err != null) { tilPassword.setError(err); return false; }

        if (!pwd.equals(pwd2)) {
            tilPasswordConfirm.setError("Les mots de passe ne correspondent pas.");
            return false;
        }
        password = pwd;
        return true;
    }

    // ── Étape 5 : Code de confirmation ────────────────────────────────────────
    private void renderStep5() {
        tvStepTitle.setText("Vérification e-mail");
        tvStepSub.setText("Un code a été envoyé à " + email);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tilCode = U.inputField(requireContext(), "Code à 6 caractères",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        layout.addView(tilCode);

        tvCodeInfo = U.tv(requireContext(), "Pas reçu ? ", 13, U.C_TEXT2, false);
        LinearLayout.LayoutParams cP = U.llWrap();
        cP.setMargins(0, U.dp(requireContext(), 8), 0, 0);
        tvCodeInfo.setLayoutParams(cP);

        // "Renvoyer" cliquable
        android.text.SpannableString ss = new android.text.SpannableString("Pas reçu ? Renvoyer le code");
        ss.setSpan(new android.text.style.ForegroundColorSpan(U.C_PRIMARY),
                11, 27, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new android.text.style.UnderlineSpan(),
                11, 27, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvCodeInfo.setText(ss);
        tvCodeInfo.setOnClickListener(v -> sendConfirmationCode());
        layout.addView(tvCodeInfo);

        stepContainer.addView(layout);

        // Envoi automatique du code
        sendConfirmationCode();
    }

    private void sendConfirmationCode() {
        confirmationCode = CodeGenerator.generate();
        android.util.Log.d("AURA_DEV", "Code de confirmation : " + confirmationCode);
        EmailService.getInstance().sendCode(email, confirmationCode,
                new EmailService.EmailCb() {
                    @Override public void ok() {
                        U.ui(() -> { if (tvCodeInfo != null)
                            tvCodeInfo.setText("Code envoyé à " + email); });
                    }
                    @Override public void err(String m) {
                        U.ui(() -> { if (rootView != null)
                            U.snackError(rootView, "Envoi échoué : " + m); });
                    }
                });
    }

    private boolean validateStep5() {
        String entered = tilCode.getEditText() != null
                ? tilCode.getEditText().getText().toString().trim() : "";
        tilCode.setError(null);

        if (entered.isEmpty()) { tilCode.setError("Code requis"); return false; }
        if (!entered.equals(confirmationCode)) {
            tilCode.setError("Code invalide. Veuillez réessayer.");
            return false;
        }
        return true;
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    private void goNext() {
        switch (currentStep) {
            case 1:
                if (!validateStep1()) return;
                advance();
                break;
            case 2:
                validateStep2(() -> advance());
                break;
            case 3:
                if (!validateStep3()) return;
                advance();
                break;
            case 4:
                if (!validateStep4()) return;
                advance();
                break;
            case 5:
                if (!validateStep5()) return;
                createAccount();
                break;
        }
    }

    private void goBack() {
        if (currentStep > 1) {
            currentStep--;
            renderStep(currentStep);
        } else {
            if (getActivity() != null) getActivity().onBackPressed();
        }
    }

    private void advance() {
        currentStep++;
        renderStep(currentStep);
    }

    // ─── Création du compte ───────────────────────────────────────────────────

    private void createAccount() {
        btnNext.setEnabled(false);
        btnNext.setText("Création en cours…");

        AuthManager.getInstance().signUp(
                firstName, lastName, email, password, birthDateIso,
                new AuthManager.AuthCallback() {
                    @Override public void onSuccess(User user) {
                        U.ui(() -> {
                            if (getActivity() instanceof AuthActivity) {
                                ((AuthActivity) getActivity()).onAuthSuccess();
                            }
                        });
                    }
                    @Override public void onError(String msg) {
                        U.ui(() -> {
                            btnNext.setEnabled(true);
                            btnNext.setText("Terminer ✓");
                            U.snackError(rootView, msg);
                        });
                    }
                }
        );
    }
}
