package com.aura.app.fragments;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aura.app.adapters.PostAdapter;
import com.aura.app.managers.AuthManager;
import com.aura.app.managers.DatabaseManager;
import com.aura.app.models.Post;
import com.aura.app.models.User;
import com.aura.app.utils.U;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Fragment profil utilisateur.
 * Affiche : avatar, stats, bio, grille de publications.
 */
public class ProfileFragment extends Fragment {

    private static final String ARG_UID = "uid";

    private String  userId;
    private boolean isMyProfile;
    private User    profileUser;
    private View    rootView;

    public static ProfileFragment newInstance(String userId) {
        ProfileFragment f = new ProfileFragment();
        Bundle b = new Bundle();
        b.putString(ARG_UID, userId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) userId = getArguments().getString(ARG_UID);
        User me = AuthManager.getInstance().getCurrentUser();
        if (userId == null && me != null) userId = me.getId();
        isMyProfile = me != null && me.getId().equals(userId);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        rootView = buildView();
        loadProfile();
        return rootView;
    }

    private View buildView() {
        ScrollView scroll = new ScrollView(requireContext());
        scroll.setBackgroundColor(U.C_BG);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(U.C_BG);
        int pad = U.dp(requireContext(), 16);
        root.setPadding(pad, U.dp(requireContext(), 24), pad, pad);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Bouton retour
        if (!isMyProfile) {
            TextView back = U.tv(requireContext(), "← Retour", 14, U.C_TEXT2, false);
            back.setPadding(0, 0, 0, U.dp(requireContext(), 12));
            back.setOnClickListener(v -> {
                if (getActivity() != null) getActivity().onBackPressed();
            });
            root.addView(back);
        }

        // ── Ligne avatar + stats ───────────────────────────────────────────────
        LinearLayout topRow = U.hStack(requireContext(), U.C_BG);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setLayoutParams(U.llMatch());

        CircleImageView avatar = new CircleImageView(requireContext());
        avatar.setTag("prof_avatar");
        int avSz = U.dp(requireContext(), 86);
        LinearLayout.LayoutParams avP = new LinearLayout.LayoutParams(avSz, avSz);
        avP.setMargins(0, 0, U.dp(requireContext(),16), 0);
        avatar.setLayoutParams(avP);

        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.OVAL);
        border.setColor(U.C_SURFACE2);
        avatar.setBackground(border);
        topRow.addView(avatar);

        // Stats
        LinearLayout stats = U.hStack(requireContext(), U.C_BG);
        stats.setGravity(Gravity.CENTER);
        stats.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        stats.addView(buildStat("0", "Publications", "stat_posts"));
        stats.addView(buildStat("0", "Abonnés",      "stat_followers"));
        stats.addView(buildStat("0", "Abonnements",  "stat_following"));

        topRow.addView(stats);
        root.addView(topRow);

        // ── Nom + handle + bio ────────────────────────────────────────────────
        TextView tvName = new TextView(requireContext());
        tvName.setTag("prof_name");
        tvName.setTextColor(U.C_TEXT);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvName.setTypeface(tvName.getTypeface(), Typeface.BOLD);
        LinearLayout.LayoutParams np = U.llWrap();
        np.setMargins(0, U.dp(requireContext(),14), 0, U.dp(requireContext(),2));
        tvName.setLayoutParams(np);
        root.addView(tvName);

        TextView tvHandle = new TextView(requireContext());
        tvHandle.setTag("prof_handle");
        tvHandle.setTextColor(U.C_TEXT2);
        tvHandle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        root.addView(tvHandle);

        TextView tvBio = new TextView(requireContext());
        tvBio.setTag("prof_bio");
        tvBio.setTextColor(U.C_TEXT);
        tvBio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvBio.setLineSpacing(0, 1.3f);
        LinearLayout.LayoutParams bp = U.llMatch();
        bp.setMargins(0, U.dp(requireContext(),8), 0, U.dp(requireContext(),14));
        tvBio.setLayoutParams(bp);
        root.addView(tvBio);

        // ── Bouton action ──────────────────────────────────────────────────────
        if (isMyProfile) {
            MaterialButton btnEdit = U.btnOutline(requireContext(), "Modifier le profil");
            btnEdit.setOnClickListener(v -> openEditProfile());
            root.addView(btnEdit);
        } else {
            MaterialButton btnFollow = U.btnPrimary(requireContext(), "Suivre");
            btnFollow.setTag("prof_follow_btn");
            btnFollow.setOnClickListener(v -> toggleFollow(btnFollow));
            root.addView(btnFollow);
        }

        // ── Grille de publications ────────────────────────────────────────────
        root.addView(U.divider(requireContext()));
        TextView tvPosts = U.tv(requireContext(), "Publications", 13, U.C_TEXT2, false);
        LinearLayout.LayoutParams tvP = U.llWrap();
        tvP.setMargins(0, U.dp(requireContext(),4), 0, U.dp(requireContext(),8));
        tvPosts.setLayoutParams(tvP);
        root.addView(tvPosts);

        RecyclerView rvPosts = new RecyclerView(requireContext());
        rvPosts.setTag("prof_rv");
        rvPosts.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        rvPosts.setNestedScrollingEnabled(false);
        rvPosts.setLayoutParams(U.llMatch());
        PostAdapter pa = new PostAdapter(null);
        rvPosts.setAdapter(pa);
        root.addView(rvPosts);

        scroll.addView(root);
        return scroll;
    }

    private LinearLayout buildStat(String count, String label, String tag) {
        LinearLayout col = new LinearLayout(requireContext());
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        col.setLayoutParams(p);

        TextView tvCount = U.tv(requireContext(), count, 18, U.C_TEXT, true);
        tvCount.setGravity(Gravity.CENTER);
        tvCount.setTag(tag);
        col.addView(tvCount);

        TextView tvLabel = U.tv(requireContext(), label, 11, U.C_TEXT2, false);
        tvLabel.setGravity(Gravity.CENTER);
        col.addView(tvLabel);
        return col;
    }

    // ─── Chargement ───────────────────────────────────────────────────────────

    private void loadProfile() {
        DatabaseManager.getInstance().getUserById(userId,
                new DatabaseManager.SingleCb<User>() {
                    @Override public void ok(User user) {
                        profileUser = user;
                        U.ui(() -> bindProfile(user));
                        loadPosts(user);
                    }
                    @Override public void err(String m) {
                        U.ui(() -> { if (rootView != null) U.snackError(rootView, m); });
                    }
                });
    }

    private void bindProfile(User user) {
        if (!isAdded() || rootView == null) return;

        // Avatar
        CircleImageView av = rootView.findViewWithTag("prof_avatar");
        if (av != null && !user.getAvatarUrl().isEmpty())
            Glide.with(this).load(user.getAvatarUrl()).circleCrop().into(av);

        // Stats
        setText("stat_posts",     String.valueOf(user.getPostsCount()));
        setText("stat_followers", String.valueOf(user.getFollowers()));
        setText("stat_following", String.valueOf(user.getFollowing()));

        // Textes
        setText("prof_name",   user.getFullName()
                + (user.isVerified() ? " ✓" : ""));
        setText("prof_handle", "@" + user.getUsername());
        setText("prof_bio",    user.getBio().isEmpty()
                ? "Aucune biographie renseignée." : user.getBio());
    }

    private void loadPosts(User user) {
        DatabaseManager.getInstance().getUserPosts(user.getId(),
                new DatabaseManager.ListCb<Post>() {
                    @Override public void ok(List<Post> posts) {
                        U.ui(() -> {
                            if (!isAdded() || rootView == null) return;
                            RecyclerView rv = rootView.findViewWithTag("prof_rv");
                            if (rv != null && rv.getAdapter() instanceof PostAdapter)
                                ((PostAdapter) rv.getAdapter()).setData(posts);
                        });
                    }
                    @Override public void err(String m) {}
                });
    }

    private void toggleFollow(MaterialButton btn) {
        String myId = AuthManager.getInstance().getCurrentUser() != null
                ? AuthManager.getInstance().getCurrentUser().getId() : "";
        if (myId.isEmpty()) return;

        boolean isFollowing = "Abonné".equals(btn.getText().toString());
        if (isFollowing) {
            DatabaseManager.getInstance().unfollow(myId, userId, new DatabaseManager.ActionCb() {
                @Override public void ok() {
                    U.ui(() -> {
                        btn.setText("Suivre");
                        btn.setBackgroundTintList(ColorStateList.valueOf(U.C_PRIMARY));
                        btn.setTextColor(U.C_WHITE);
                    });
                }
                @Override public void err(String m) {}
            });
        } else {
            DatabaseManager.getInstance().follow(myId, userId, new DatabaseManager.ActionCb() {
                @Override public void ok() {
                    U.ui(() -> {
                        btn.setText("Abonné");
                        btn.setBackgroundTintList(ColorStateList.valueOf(U.C_SURFACE2));
                        btn.setTextColor(U.C_TEXT);
                    });
                }
                @Override public void err(String m) {}
            });
        }
    }

    private void openEditProfile() {
        // Ouvre un BottomSheet d'édition du profil
        EditProfileSheet sheet = new EditProfileSheet();
        sheet.show(getChildFragmentManager(), "edit_profile");
    }

    private void setText(String tag, String text) {
        if (rootView == null) return;
        TextView tv = rootView.findViewWithTag(tag);
        if (tv != null) tv.setText(text);
    }
}
