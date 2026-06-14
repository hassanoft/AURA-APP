package com.aura.app.auth;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aura.app.activities.AuthActivity;
import com.aura.app.managers.AuthManager;
import com.aura.app.models.User;
import com.aura.app.utils.U;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment de connexion AURA.
 * Interface 100% Java, aucun XML.
 */
public class LoginFragment extends Fragment {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private MaterialButton  btnLogin;
    private View            rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        rootView = buildView();
        return rootView;
    }

    private View buildView() {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(U.C_BG);
        root.setPadding(U.dp(requireContext(), 8), 0, U.dp(requireContext(), 8), 0);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Titre
        TextView title = U.tv(requireContext(), "Connexion", 24, U.C_TEXT, true);
        LinearLayout.LayoutParams titleP = U.llWrap();
        titleP.setMargins(0, 0, 0, U.dp(requireContext(), 4));
        title.setLayoutParams(titleP);
        root.addView(title);

        // Sous-titre
        TextView sub = U.tv(requireContext(), "Bienvenue sur AURA", 14, U.C_TEXT2, false);
        LinearLayout.LayoutParams subP = U.llWrap();
        subP.setMargins(0, 0, 0, U.dp(requireContext(), 24));
        sub.setLayoutParams(subP);
        root.addView(sub);

        // Champ e-mail
        tilEmail = U.inputField(requireContext(), "Adresse e-mail", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT);
        root.addView(tilEmail);

        // Champ mot de passe
        tilPassword = U.inputField(requireContext(), "Mot de passe", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        root.addView(tilPassword);

        // Mot de passe oublié
        TextView forgot = U.tv(requireContext(), "Mot de passe oublié ?", 13, U.C_PRIMARY, false);
        LinearLayout.LayoutParams fP = U.llWrap();
        fP.gravity = Gravity.END;
        fP.setMargins(0, 0, 0, U.dp(requireContext(), 16));
        forgot.setLayoutParams(fP);
        forgot.setPadding(0, U.dp(requireContext(), 4), 0, U.dp(requireContext(), 4));
        root.addView(forgot);

        // Bouton connexion
        btnLogin = U.btnPrimary(requireContext(), "Se connecter");
        btnLogin.setOnClickListener(v -> attemptLogin());
        root.addView(btnLogin);

        // Séparateur
        root.addView(buildSeparator());

        // Lien inscription
        LinearLayout row = U.hStack(requireContext(), U.C_BG);
        row.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rowP = U.llMatch();
        rowP.setMargins(0, U.dp(requireContext(), 8), 0, 0);
        row.setLayoutParams(rowP);

        TextView noAccount = U.tv(requireContext(), "Pas encore de compte ? ", 13, U.C_TEXT2, false);
        TextView signUp    = U.tv(requireContext(), "Créer un compte", 13, U.C_PRIMARY, true);
        signUp.setOnClickListener(v -> openRegister());
        row.addView(noAccount);
        row.addView(signUp);
        root.addView(row);

        return root;
    }

    private View buildSeparator() {
        LinearLayout row = U.hStack(requireContext(), U.C_BG);
        LinearLayout.LayoutParams rP = U.llMatch();
        rP.setMargins(0, U.dp(requireContext(), 16), 0, U.dp(requireContext(), 8));
        row.setLayoutParams(rP);
        row.setGravity(Gravity.CENTER_VERTICAL);

        View  line1 = new View(requireContext());
        line1.setBackgroundColor(U.C_DIVIDER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, U.dp(requireContext(), 1), 1f);
        line1.setLayoutParams(lp);

        TextView or = U.tv(requireContext(), "  ou  ", 12, U.C_TEXT2, false);
        or.setLayoutParams(U.llWrap());

        View  line2 = new View(requireContext());
        line2.setBackgroundColor(U.C_DIVIDER);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, U.dp(requireContext(), 1), 1f);
        line2.setLayoutParams(lp2);

        row.addView(line1); row.addView(or); row.addView(line2);
        return row;
    }

    private void attemptLogin() {
        String email = tilEmail.getEditText() != null
                ? tilEmail.getEditText().getText().toString().trim() : "";
        String pwd   = tilPassword.getEditText() != null
                ? tilPassword.getEditText().getText().toString() : "";

        // Validation basique
        if (email.isEmpty()) { tilEmail.setError("E-mail requis"); return; }
        if (pwd.isEmpty())   { tilPassword.setError("Mot de passe requis"); return; }
        tilEmail.setError(null); tilPassword.setError(null);

        btnLogin.setEnabled(false);
        btnLogin.setText("Connexion en cours…");

        AuthManager.getInstance().signIn(email, pwd, new AuthManager.AuthCallback() {
            @Override public void onSuccess(User user) {
                U.ui(() -> {
                    if (getActivity() instanceof AuthActivity) {
                        ((AuthActivity) getActivity()).onAuthSuccess();
                    }
                });
            }
            @Override public void onError(String msg) {
                U.ui(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Se connecter");
                    U.snackError(rootView, msg);
                });
            }
        });
    }

    private void openRegister() {
        if (getActivity() instanceof AuthActivity) {
            ((AuthActivity) getActivity())
                    .showFragment(new RegisterWizardFragment(), true);
        }
    }
}
