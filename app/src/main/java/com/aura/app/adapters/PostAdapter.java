package com.aura.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aura.app.activities.PostDetailActivity;
import com.aura.app.activities.ProfileActivity;
import com.aura.app.models.Post;
import com.aura.app.utils.TimeUtils;
import com.aura.app.utils.U;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adaptateur RecyclerView pour le fil de publications AURA.
 * Chaque ViewHolder est construit programmatiquement en Java.
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostVH> {

    // ─── Interface callback ───────────────────────────────────────────────────

    public interface PostInteraction {
        void onLike(Post post, int position);
        void onComment(Post post);
        void onShare(Post post);
        void onSave(Post post, int position);
    }

    private final List<Post>        posts       = new ArrayList<>();
    private final PostInteraction   interaction;

    public PostAdapter(PostInteraction interaction) {
        this.interaction = interaction;
    }

    // ─── Données ──────────────────────────────────────────────────────────────

    public void setData(List<Post> newPosts) {
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    public void appendData(List<Post> more) {
        int start = posts.size();
        posts.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    public void updateItem(int position, Post updated) {
        if (position >= 0 && position < posts.size()) {
            posts.set(position, updated);
            notifyItemChanged(position);
        }
    }

    // ─── RecyclerView ─────────────────────────────────────────────────────────

    @NonNull
    @Override
    public PostVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = buildPostView(parent.getContext());
        view.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return new PostVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostVH holder, int position) {
        holder.bind(posts.get(position), position);
    }

    @Override
    public int getItemCount() { return posts.size(); }

    // ─── Construction d'une carte post ────────────────────────────────────────

    private View buildPostView(Context c) {
        LinearLayout card = new LinearLayout(c);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(U.C_SURFACE);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(U.C_SURFACE);
        bg.setCornerRadius(U.dp(c, 16));
        card.setBackground(bg);
        card.setElevation(U.dp(c, 2));

        int pad = U.dp(c, 14);
        card.setPadding(pad, pad, pad, U.dp(c, 8));

        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cp.setMargins(U.dp(c, 12), U.dp(c, 6), U.dp(c, 12), U.dp(c, 6));
        card.setLayoutParams(cp);

        // ── Ligne header : avatar + nom + date ────────────────────────────────
        LinearLayout header = U.hStack(c, U.C_SURFACE);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams hp = U.llMatch();
        hp.setMargins(0, 0, 0, U.dp(c, 10));
        header.setLayoutParams(hp);

        CircleImageView avatar = new CircleImageView(c);
        avatar.setTag("avatar");
        int avSize = U.dp(c, 40);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(avSize, avSize);
        ap.setMargins(0, 0, U.dp(c, 10), 0);
        avatar.setLayoutParams(ap);
        avatar.setBorderColor(U.C_DIVIDER);
        avatar.setBorderWidth(U.dp(c, 1));
        header.addView(avatar);

        LinearLayout nameCol = new LinearLayout(c);
        nameCol.setOrientation(LinearLayout.VERTICAL);
        nameCol.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvName = new TextView(c);
        tvName.setTag("author");
        tvName.setTextColor(U.C_TEXT);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvName.setTypeface(tvName.getTypeface(), Typeface.BOLD);
        nameCol.addView(tvName);

        TextView tvTime = new TextView(c);
        tvTime.setTag("time");
        tvTime.setTextColor(U.C_TEXT2);
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        nameCol.addView(tvTime);

        header.addView(nameCol);
        card.addView(header);

        // ── Contenu texte ────────────────────────────────────────────────────
        TextView tvContent = new TextView(c);
        tvContent.setTag("content");
        tvContent.setTextColor(U.C_TEXT);
        tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvContent.setLineSpacing(U.dp(c, 2), 1f);
        LinearLayout.LayoutParams txP = U.llMatch();
        txP.setMargins(0, 0, 0, U.dp(c, 10));
        tvContent.setLayoutParams(txP);
        card.addView(tvContent);

        // ── Image du post ────────────────────────────────────────────────────
        ImageView postImage = new ImageView(c);
        postImage.setTag("post_image");
        postImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable imgBg = new GradientDrawable();
        imgBg.setColor(U.C_SURFACE2);
        imgBg.setCornerRadius(U.dp(c, 12));
        postImage.setBackground(imgBg);
        postImage.setClipToOutline(true);
        LinearLayout.LayoutParams imgP = U.llMatchH(c, 260);
        imgP.setMargins(0, 0, 0, U.dp(c, 10));
        postImage.setLayoutParams(imgP);
        postImage.setVisibility(View.GONE);
        card.addView(postImage);

        // ── Ligne actions : Like · Commenter · Partager · Sauvegarder ────────
        LinearLayout actions = U.hStack(c, U.C_SURFACE);
        actions.setLayoutParams(U.llMatch());
        actions.setGravity(Gravity.CENTER_VERTICAL);

        card.addView(U.divider(c));

        TextView btnLike    = buildActionBtn(c, "♥", "0", "like_btn");
        TextView btnComment = buildActionBtn(c, "✎", "0", "comment_btn");
        TextView btnShare   = buildActionBtn(c, "↗", "0", "share_btn");
        TextView btnSave    = buildActionBtn(c, "⊕", "",  "save_btn");

        actions.addView(btnLike);
        actions.addView(btnComment);
        actions.addView(btnShare);

        // Espaceur
        View spacer = new View(c);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        actions.addView(spacer);
        actions.addView(btnSave);

        card.addView(actions);

        return card;
    }

    private TextView buildActionBtn(Context c, String icon, String count, String tag) {
        TextView tv = new TextView(c);
        tv.setTag(tag);
        tv.setText(icon + (count.isEmpty() ? "" : " " + count));
        tv.setTextColor(U.C_TEXT2);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tv.setPadding(U.dp(c, 8), U.dp(c, 8), U.dp(c, 8), U.dp(c, 8));
        tv.setGravity(Gravity.CENTER_VERTICAL);
        return tv;
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    class PostVH extends RecyclerView.ViewHolder {

        private final CircleImageView avatar;
        private final TextView        tvAuthor, tvTime, tvContent;
        private final ImageView       postImage;
        private final TextView        btnLike, btnComment, btnShare, btnSave;

        PostVH(View v) {
            super(v);
            avatar     = v.findViewWithTag("avatar");
            tvAuthor   = v.findViewWithTag("author");
            tvTime     = v.findViewWithTag("time");
            tvContent  = v.findViewWithTag("content");
            postImage  = v.findViewWithTag("post_image");
            btnLike    = v.findViewWithTag("like_btn");
            btnComment = v.findViewWithTag("comment_btn");
            btnShare   = v.findViewWithTag("share_btn");
            btnSave    = v.findViewWithTag("save_btn");
        }

        void bind(Post post, int position) {
            Context c = itemView.getContext();

            // Avatar
            if (!post.getAuthorAvatar().isEmpty()) {
                Glide.with(c).load(post.getAuthorAvatar())
                        .placeholder(buildCirclePlaceholder(c))
                        .into(avatar);
            } else {
                avatar.setImageDrawable(buildCirclePlaceholder(c));
            }

            // Auteur + badge vérifié
            String authorText = "@" + post.getAuthorUsername()
                    + (post.isAuthorVerified() ? " ✓" : "");
            tvAuthor.setText(authorText);
            if (post.isAuthorVerified()) tvAuthor.setTextColor(U.C_PRIMARY);
            else tvAuthor.setTextColor(U.C_TEXT);

            tvTime.setText(TimeUtils.relative(post.getCreatedAt()));
            tvContent.setText(post.getContent());

            // Image
            if (post.isImage() && !post.getMediaUrl().isEmpty()) {
                postImage.setVisibility(View.VISIBLE);
                Glide.with(c).load(post.getMediaUrl())
                        .centerCrop()
                        .into(postImage);
            } else {
                postImage.setVisibility(View.GONE);
            }

            // Bouton Like
            String likeTxt = (post.isLikedByMe() ? "♥ " : "♡ ") + post.getLikesCount();
            btnLike.setText(likeTxt);
            btnLike.setTextColor(post.isLikedByMe() ? U.C_PRIMARY : U.C_TEXT2);
            btnLike.setOnClickListener(v -> {
                if (interaction != null) interaction.onLike(post, position);
            });

            // Bouton Commentaire
            btnComment.setText("✎ " + post.getCommentsCount());
            btnComment.setOnClickListener(v -> {
                Intent i = new Intent(c, PostDetailActivity.class);
                i.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getId());
                c.startActivity(i);
            });

            // Bouton Partager
            btnShare.setText("↗ " + post.getSharesCount());
            btnShare.setOnClickListener(v -> {
                if (interaction != null) interaction.onShare(post);
            });

            // Bouton Sauvegarder
            btnSave.setText(post.isSavedByMe() ? "★" : "☆");
            btnSave.setTextColor(post.isSavedByMe() ? U.C_PRIMARY : U.C_TEXT2);
            btnSave.setOnClickListener(v -> {
                if (interaction != null) interaction.onSave(post, position);
            });

            // Clic sur l'auteur → profil
            avatar.setOnClickListener(v -> {
                Intent i = new Intent(c, ProfileActivity.class);
                i.putExtra(ProfileActivity.EXTRA_USER_ID, post.getUserId());
                c.startActivity(i);
            });
            tvAuthor.setOnClickListener(v -> {
                Intent i = new Intent(c, ProfileActivity.class);
                i.putExtra(ProfileActivity.EXTRA_USER_ID, post.getUserId());
                c.startActivity(i);
            });
        }

        private android.graphics.drawable.Drawable buildCirclePlaceholder(Context c) {
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(U.C_SURFACE2);
            return gd;
        }
    }
}
