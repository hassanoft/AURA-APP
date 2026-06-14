package com.aura.app.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aura.app.managers.AuthManager;
import com.aura.app.managers.DatabaseManager;
import com.aura.app.models.User;
import com.aura.app.utils.U;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

/** BottomSheet pour modifier la bio */
public class EditProfileSheet extends BottomSheetDialogFragment {

    private TextInputLayout tilBio;
    private View            rootView;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(U.C_SURFACE);
        int pad = U.dp(requireContext(), 20);
        root.setPadding(pad, pad, pad, U.dp(requireContext(), 40));

        root.addView(U.tv(requireContext(), "Modifier le profil",
                18, U.C_TEXT, true));

        tilBio = U.inputField(requireContext(), "Biographie",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        tilBio.setCounterEnabled(true);
        tilBio.setCounterMaxLength(160);
        LinearLayout.LayoutParams bp = U.llMatch();
        bp.setMargins(0, U.dp(requireContext(),16), 0, U.dp(requireContext(),16));
        tilBio.setLayoutParams(bp);

        // Pré-remplir avec la bio actuelle
        User me = AuthManager.getInstance().getCurrentUser();
        if (me != null && tilBio.getEditText() != null)
            tilBio.getEditText().setText(me.getBio());

        root.addView(tilBio);

        root.addView(U.btnPrimary(requireContext(), "Enregistrer"));
        ((com.google.android.material.button.MaterialButton)
                root.getChildAt(root.getChildCount() - 1))
                .setOnClickListener(v -> save());

        rootView = root;
        return root;
    }

    private void save() {
        User me = AuthManager.getInstance().getCurrentUser();
        if (me == null) return;

        String bio = tilBio.getEditText() != null
                ? tilBio.getEditText().getText().toString().trim() : "";

        DatabaseManager.getInstance().updateProfile(me.getId(), bio, null,
                new DatabaseManager.ActionCb() {
                    @Override public void ok() {
                        U.ui(() -> {
                            if (rootView != null) U.snackOk(rootView, "Profil mis à jour !");
                            dismiss();
                        });
                    }
                    @Override public void err(String m) {
                        U.ui(() -> { if (rootView != null) U.snackError(rootView, m); });
                    }
                });
    }
}
