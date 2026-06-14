package com.aura.app.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aura.app.models.Comment;
import com.aura.app.utils.TimeUtils;
import com.aura.app.utils.U;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/** Adaptateur pour la liste des commentaires d'un post */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CVH> {

    private final List<Comment> comments = new ArrayList<>();

    public void setData(List<Comment> list) {
        comments.clear();
        comments.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public CVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = buildRow(parent.getContext());
        v.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new CVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CVH holder, int position) {
        holder.bind(comments.get(position));
    }

    @Override
    public int getItemCount() { return comments.size(); }

    private View buildRow(Context c) {
        LinearLayout row = U.hStack(c, U.C_BG);
        row.setLayoutParams(U.llMatch());
        int pad = U.dp(c, 12);
        row.setPadding(pad, U.dp(c, 8), pad, U.dp(c, 8));

        CircleImageView avatar = new CircleImageView(c);
        avatar.setTag("c_avatar");
        int sz = U.dp(c, 36);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(sz, sz);
        ap.setMargins(0, 0, U.dp(c, 10), 0);
        avatar.setLayoutParams(ap);
        row.addView(avatar);

        LinearLayout col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout headerRow = U.hStack(c, U.C_BG);
        headerRow.setLayoutParams(U.llMatch());
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvUser = new TextView(c);
        tvUser.setTag("c_user");
        tvUser.setTextColor(U.C_TEXT);
        tvUser.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvUser.setTypeface(tvUser.getTypeface(), Typeface.BOLD);
        headerRow.addView(tvUser);

        TextView tvTime = new TextView(c);
        tvTime.setTag("c_time");
        tvTime.setTextColor(U.C_TEXT2);
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        LinearLayout.LayoutParams timeP = U.llWrap();
        timeP.setMargins(U.dp(c, 8), 0, 0, 0);
        tvTime.setLayoutParams(timeP);
        headerRow.addView(tvTime);

        col.addView(headerRow);

        TextView tvContent = new TextView(c);
        tvContent.setTag("c_content");
        tvContent.setTextColor(U.C_TEXT);
        tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvContent.setLineSpacing(0, 1.2f);
        LinearLayout.LayoutParams cp = U.llMatch();
        cp.setMargins(0, U.dp(c, 2), 0, 0);
        tvContent.setLayoutParams(cp);
        col.addView(tvContent);

        row.addView(col);
        return row;
    }

    static class CVH extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView tvUser, tvTime, tvContent;

        CVH(View v) {
            super(v);
            avatar    = v.findViewWithTag("c_avatar");
            tvUser    = v.findViewWithTag("c_user");
            tvTime    = v.findViewWithTag("c_time");
            tvContent = v.findViewWithTag("c_content");
        }

        void bind(Comment c) {
            Context ctx = itemView.getContext();
            tvUser.setText("@" + c.getAuthorUsername());
            tvTime.setText(TimeUtils.relative(c.getCreatedAt()));
            tvContent.setText(c.getContent());
            if (!c.getAuthorAvatar().isEmpty()) {
                Glide.with(ctx).load(c.getAuthorAvatar()).circleCrop().into(avatar);
            }
        }
    }
}
