package com.aura.app.adapters;

import android.content.Context;
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

import com.aura.app.models.Notification;
import com.aura.app.utils.TimeUtils;
import com.aura.app.utils.U;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/** Adaptateur liste des notifications */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifVH> {

    public interface OnNotifClick { void onClick(Notification n); }

    private final List<Notification> list    = new ArrayList<>();
    private final OnNotifClick       onClick;

    public NotificationAdapter(OnNotifClick onClick) {
        this.onClick = onClick;
    }

    public void setData(List<Notification> data) {
        list.clear(); list.addAll(data); notifyDataSetChanged();
    }

    @NonNull @Override
    public NotifVH onCreateViewHolder(@NonNull ViewGroup parent, int vt) {
        View v = buildRow(parent.getContext());
        v.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new NotifVH(v);
    }

    @Override public void onBindViewHolder(@NonNull NotifVH h, int pos) { h.bind(list.get(pos)); }
    @Override public int getItemCount() { return list.size(); }

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
        avatar.setTag("notif_avatar");
        int sz = U.dp(c, 44);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(sz, sz);
        ap.setMargins(0, 0, U.dp(c,12), 0);
        avatar.setLayoutParams(ap);
        row.addView(avatar);

        LinearLayout col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvMsg = new TextView(c);
        tvMsg.setTag("notif_msg");
        tvMsg.setTextColor(U.C_TEXT);
        tvMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        col.addView(tvMsg);

        TextView tvTime = new TextView(c);
        tvTime.setTag("notif_time");
        tvTime.setTextColor(U.C_TEXT2);
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        col.addView(tvTime);

        row.addView(col);

        // Point non-lu
        View dot = new View(c);
        dot.setTag("notif_dot");
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        dotBg.setColor(U.C_PRIMARY);
        dot.setBackground(dotBg);
        int dotSz = U.dp(c, 8);
        LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(dotSz, dotSz);
        dp2.setMargins(U.dp(c,8), 0, 0, 0);
        dot.setLayoutParams(dp2);
        row.addView(dot);

        return row;
    }

    class NotifVH extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView tvMsg, tvTime;
        View dot;

        NotifVH(View v) {
            super(v);
            avatar = v.findViewWithTag("notif_avatar");
            tvMsg  = v.findViewWithTag("notif_msg");
            tvTime = v.findViewWithTag("notif_time");
            dot    = v.findViewWithTag("notif_dot");
        }

        void bind(Notification n) {
            Context c = itemView.getContext();
            tvMsg.setText(n.getDisplayText());
            tvTime.setText(TimeUtils.relative(n.getCreatedAt()));
            dot.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);
            if (!n.isRead()) tvMsg.setTypeface(tvMsg.getTypeface(), Typeface.BOLD);
            else tvMsg.setTypeface(Typeface.DEFAULT);

            if (!n.getSenderAvatar().isEmpty()) {
                Glide.with(c).load(n.getSenderAvatar()).circleCrop().into(avatar);
            }
            itemView.setOnClickListener(v -> { if (onClick != null) onClick.onClick(n); });
        }
    }
}
