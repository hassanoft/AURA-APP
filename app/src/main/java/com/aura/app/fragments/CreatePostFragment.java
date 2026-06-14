package com.aura.app.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aura.app.managers.AuthManager;
import com.aura.app.managers.DatabaseManager;
import com.aura.app.managers.StorageManager;
import com.aura.app.models.Post;
import com.aura.app.models.User;
import com.aura.app.utils.U;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

/** BottomSheet de création d'une publication */
public class CreatePostFragment extends BottomSheetDialogFragment {

    private TextInputLayout   tilContent;
    private MaterialButton    btnMedia, btnPost;
    private View              rootView;
    private Uri               selectedMediaUri;
    private String            myUserId = "";

    private final ActivityResultLauncher<String> mediaPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedMediaUri = uri;
                    if (btnMedia != null)
                        btnMedia.setText("Média sélectionné ✓");
                }
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        User me = AuthManager.getInstance().getCurrentUser();
        if (me != null) myUserId = me.getId();
        rootView = buildView();
        return rootView;
    }

    private View buildView() {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(U.C_SURFACE);
        int pad = U.dp(requireContext(), 20);
        root.setPadding(pad, pad, pad, U.dp(requireContext(), 40));

        root.addView(U.tv(requireContext(), "Nouvelle publication",
                18, U.C_TEXT, true));

        tilContent = U.inputField(requireContext(), "Quoi de neuf ?",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        tilContent.setCounterEnabled(true);
        tilContent.setCounterMaxLength(500);
        LinearLayout.LayoutParams cp = U.llMatch();
        cp.setMargins(0, U.dp(requireContext(),16), 0, U.dp(requireContext(),12));
        tilContent.setLayoutParams(cp);
        root.addView(tilContent);

        // Bouton ajouter un média
        btnMedia = U.btnOutline(requireContext(), "📷  Ajouter une photo / vidéo");
        btnMedia.setOnClickListener(v -> mediaPicker.launch("image/*"));
        root.addView(btnMedia);

        // Bouton publier
        btnPost = U.btnPrimary(requireContext(), "Publier");
        btnPost.setOnClickListener(v -> publish());
        root.addView(btnPost);

        return root;
    }

    private void publish() {
        String text = tilContent.getEditText() != null
                ? tilContent.getEditText().getText().toString().trim() : "";

        if (text.isEmpty() && selectedMediaUri == null) {
            tilContent.setError("Ajoutez du texte ou un média.");
            return;
        }

        btnPost.setEnabled(false);
        btnPost.setText("Publication en cours…");

        if (selectedMediaUri != null) {
            StorageManager.getInstance().uploadPostMedia(myUserId, selectedMediaUri,
                    new StorageManager.UploadCb() {
                        @Override public void ok(String url) {
                            createPost(text, url, url.endsWith(".mp4") ? "video" : "image");
                        }
                        @Override public void err(String m) {
                            U.ui(() -> {
                                btnPost.setEnabled(true);
                                btnPost.setText("Publier");
                                U.snackError(rootView, "Upload échoué : " + m);
                            });
                        }
                    });
        } else {
            createPost(text, "", "");
        }
    }

    private void createPost(String text, String mediaUrl, String mediaType) {
        Post post = new Post();
        post.setUserId(myUserId);
        post.setContent(text);
        post.setMediaUrl(mediaUrl);
        post.setMediaType(mediaType);

        DatabaseManager.getInstance().createPost(post, new DatabaseManager.ActionCb() {
            @Override public void ok() {
                U.ui(() -> {
                    dismiss();
                    U.snackOk(rootView, "Publication créée !");
                });
            }
            @Override public void err(String m) {
                U.ui(() -> {
                    btnPost.setEnabled(true);
                    btnPost.setText("Publier");
                    U.snackError(rootView, m);
                });
            }
        });
    }
}
