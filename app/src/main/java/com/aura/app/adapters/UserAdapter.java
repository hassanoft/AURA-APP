package com.aura.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aura.app.activities.ProfileActivity;
import com.aura.app.models.User;
import com.aura.app.utils.U;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/** Adaptateur liste d'utilisateurs pour la recherche */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserVH> {

    public interface FollowAction { void onFollow(User user, boolean follow, int pos); }

    private final List<User>   users      = new ArrayList<>();
    private final String       myUserId;
    private final FollowAction followAction;

    public UserAdapter(String myUserId, FollowAction action) {
        this.myUserId    = myUserId;
        this.followAction = action;
    }

    public void setData(List<User> list) {
        users.clear(); users.addAll(list); notifyDataSetChanged();
    }

    @NonNull @Override
    public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int vt) {
        View v = buildRow(parent.getContext());
        v.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new UserVH(v);
    }

    @Override public void onBindViewHolder(@NonNull UserVH h, int pos) { h.bind(users.get(pos), pos); }
    @Override public int getItemCount() { return users.size(); }

    private View buildRow(Context c) {
        LinearLayout row = U.hStack(c, U.C_BG);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rp = U.llMatch();
        rp.setMargins(U.dp(c,12), U.dp(c,4), U.dp(c,12), U.dp(c,4));
        row.setLayoutParams(rp);
        int pad = U.dp(c, 12);
        row.setPadding(pad, pad, pad, pad);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(U.C_SURFACE);
        bg.setCornerRadius(U.dp(c, 14));
        row.setBackground(bg);

        CircleImageView avatar = new CircleImageView(c);
        avatar.setTag("u_avatar");
        int sz = U.dp(c, 46);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(sz, sz);
        ap.setMargins(0, 0, U.dp(c,12), 0);
        avatar.setLayoutParams(ap);
        row.addView(avatar);

        LinearLayout col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvName = new TextView(c);
        tvName.setTag("u_name");
        tvName.setTextColor(U.C_TEXT);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvName.setTypeface(tvName.getTypeface(), Typeface.BOLD);
        col.addView(tvName);

        TextView tvHandle = new TextView(c);
        tvHandle.setTag("u_handle");
        tvHandle.setTextColor(U.C_TEXT2);
        tvHandle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        col.addView(tvHandle);

        row.addView(col);

        MaterialButton btnFollow = new MaterialButton(c);
        btnFollow.setTag("u_follow_btn");
        btnFollow.setText("Suivre");
        btnFollow.setTextColor(U.C_WHITE);
        btnFollow.setBackgroundTintList(ColorStateList.valueOf(U.C_PRIMARY));
        btnFollow.setCornerRadius(U.dp(c, 20));
        btnFollow.setPadding(U.dp(c,12), 0, U.dp(c,12), 0);
        btnFollow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, U.dp(c, 34));
        bp.setMargins(U.dp(c,8), 0, 0, 0);
        btnFollow.setLayoutParams(bp);
        row.addView(btnFollow);

        return row;
    }

    class UserVH extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView tvName, tvHandle;
        MaterialButton btnFollow;
        boolean following = false;

        UserVH(View v) {
            super(v);
            avatar    = v.findViewWithTag("u_avatar");
            tvName    = v.findViewWithTag("u_name");
            tvHandle  = v.findViewWithTag("u_handle");
            btnFollow = v.findViewWithTag("u_follow_btn");
        }

        void bind(User user, int pos) {
            Context c = itemView.getContext();
            tvName.setText(user.getFullName()
                    + (user.isVerified() ? " ✓" : ""));
            tvHandle.setText("@" + user.getUsername()
                    + " · " + user.getFollowers() + " abonnés");

            if (!user.getAvatarUrl().isEmpty())
                Glide.with(c).load(user.getAvatarUrl()).circleCrop().into(avatar);

            // Masquer le bouton pour son propre profil
            if (user.getId().equals(myUserId)) {
                btnFollow.setVisibility(View.GONE);
            } else {
                btnFollow.setVisibility(View.VISIBLE);
                btnFollow.setOnClickListener(v -> {
                    following = !following;
                    if (followAction != null) followAction.onFollow(user, following, pos);
                    updateFollowBtn(c);
                });
            }

            itemView.setOnClickListener(v -> {
                Intent i = new Intent(c, ProfileActivity.class);
                i.putExtra(ProfileActivity.EXTRA_USER_ID, user.getId());
                c.startActivity(i);
            });
        }

        private void updateFollowBtn(Context c) {
            if (following) {
                btnFollow.setText("Abonné");
                btnFollow.setBackgroundTintList(ColorStateList.valueOf(U.C_SURFACE2));
                btnFollow.setTextColor(U.C_TEXT);
            } else {
                btnFollow.setText("Suivre");
                btnFollow.setBackgroundTintList(ColorStateList.valueOf(U.C_PRIMARY));
                btnFollow.setTextColor(U.C_WHITE);
            }
        }
    }
}
