package com.aura.app.profile;

import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.aura.app.managers.DatabaseManager;
import com.aura.app.managers.StorageManager;
import com.aura.app.utils.U;
import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Gestionnaire de sélection et d'upload de la photo de profil AURA.
 *
 * <p>Encapsule le sélecteur d'image, l'upload vers Supabase Storage
 * et la mise à jour du champ {@code avatar_url} dans la table {@code users}.
 *
 * <p>Utilisation dans un Fragment :
 * <pre>
 *   AvatarPicker picker = new AvatarPicker(this, userId, avatarImageView);
 *   avatarImageView.setOnClickListener(v -> picker.pick());
 * </pre>
 */
public class AvatarPicker {

    private final ActivityResultLauncher<String> launcher;
    private final String           userId;
    private final CircleImageView  avatarView;
    private final android.view.View rootView;

    /**
     * @param fragment   Fragment hôte (pour enregistrer le launcher)
     * @param userId     ID de l'utilisateur courant
     * @param avatarView Vue circulaire affichant l'avatar
     */
    public AvatarPicker(Fragment fragment, String userId, CircleImageView avatarView) {
        this.userId     = userId;
        this.avatarView = avatarView;
        this.rootView   = fragment.getView();

        this.launcher = fragment.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::onImageSelected);
    }

    /** Ouvre le sélecteur d'images de la galerie. */
    public void pick() {
        launcher.launch("image/*");
    }

    // ─── Traitement de l'image sélectionnée ──────────────────────────────────

    private void onImageSelected(Uri uri) {
        if (uri == null) return;

        // Aperçu immédiat
        if (avatarView != null) {
            Glide.with(avatarView.getContext()).load(uri).into(avatarView);
        }

        // Upload vers Supabase Storage
        StorageManager.getInstance().uploadAvatar(userId, uri, new StorageManager.UploadCb() {
            @Override public void ok(String publicUrl) {
                DatabaseManager.getInstance().updateProfile(userId, null, publicUrl,
                        new DatabaseManager.ActionCb() {
                            @Override public void ok() {
                                U.ui(() -> {
                                    if (rootView != null)
                                        U.snackOk(rootView, "Photo de profil mise à jour !");
                                });
                            }
                            @Override public void err(String m) {
                                U.ui(() -> {
                                    if (rootView != null) U.snackError(rootView, m);
                                });
                            }
                        });
            }

            @Override public void err(String m) {
                U.ui(() -> {
                    if (rootView != null) U.snackError(rootView, "Upload échoué : " + m);
                });
            }
        });
    }
}
