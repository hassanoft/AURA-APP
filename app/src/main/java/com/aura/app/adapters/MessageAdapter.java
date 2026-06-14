package com.aura.app.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aura.app.models.Message;
import com.aura.app.utils.TimeUtils;
import com.aura.app.utils.U;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptateur messages privés.
 * Bulles à gauche (interlocuteur) / droite (moi).
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MsgVH> {

    private static final int TYPE_ME    = 0;
    private static final int TYPE_OTHER = 1;

    private final List<Message> messages = new ArrayList<>();
    private final String        myUserId;

    public MessageAdapter(String myUserId) {
        this.myUserId = myUserId;
    }

    public void setData(List<Message> list) {
        messages.clear();
        messages.addAll(list);
        notifyDataSetChanged();
    }

    public void addMessage(Message m) {
        messages.add(m);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return myUserId.equals(messages.get(position).getSenderId()) ? TYPE_ME : TYPE_OTHER;
    }

    @NonNull
    @Override
    public MsgVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = buildBubble(parent.getContext(), viewType == TYPE_ME);
        v.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MsgVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MsgVH holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() { return messages.size(); }

    // ─── Construction de la bulle ─────────────────────────────────────────────

    private View buildBubble(Context c, boolean isMe) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(isMe ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams rp = U.llMatch();
        rp.setMargins(U.dp(c, 8), U.dp(c, 4), U.dp(c, 8), U.dp(c, 4));
        row.setLayoutParams(rp);

        LinearLayout bubble = new LinearLayout(c);
        bubble.setOrientation(LinearLayout.VERTICAL);
        int bubblePad = U.dp(c, 10);
        bubble.setPadding(bubblePad, U.dp(c, 8), bubblePad, U.dp(c, 8));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(isMe ? U.C_PRIMARY : U.C_SURFACE2);
        float[] radii;
        if (isMe) {
            radii = new float[]{
                    U.dp(c, 18), U.dp(c, 18),
                    U.dp(c, 4),  U.dp(c, 4),
                    U.dp(c, 18), U.dp(c, 18),
                    U.dp(c, 18), U.dp(c, 18)};
        } else {
            radii = new float[]{
                    U.dp(c, 4),  U.dp(c, 4),
                    U.dp(c, 18), U.dp(c, 18),
                    U.dp(c, 18), U.dp(c, 18),
                    U.dp(c, 18), U.dp(c, 18)};
        }
        bg.setCornerRadii(radii);
        bubble.setBackground(bg);

        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bubble.setLayoutParams(bp);

        // Texte du message
        TextView tvText = new TextView(c);
        tvText.setTag("msg_text");
        tvText.setTextColor(isMe ? U.C_WHITE : U.C_TEXT);
        tvText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvText.setLineSpacing(0, 1.2f);
        bubble.addView(tvText);

        // Heure + statut lu
        TextView tvTime = new TextView(c);
        tvTime.setTag("msg_time");
        tvTime.setTextColor(isMe ? 0xBBFFFFFF : U.C_TEXT2);
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        tvTime.setGravity(Gravity.END);
        LinearLayout.LayoutParams tp = U.llWrap();
        tp.gravity = Gravity.END;
        tp.setMargins(0, U.dp(c, 2), 0, 0);
        tvTime.setLayoutParams(tp);
        bubble.addView(tvTime);

        row.addView(bubble);
        return row;
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    static class MsgVH extends RecyclerView.ViewHolder {
        TextView tvText, tvTime;

        MsgVH(View v) {
            super(v);
            // Chercher dans la hiérarchie
            ViewGroup row    = (ViewGroup) v;
            ViewGroup bubble = (ViewGroup) row.getChildAt(0);
            tvText = bubble.findViewWithTag("msg_text");
            tvTime = bubble.findViewWithTag("msg_time");
        }

        void bind(Message m) {
            tvText.setText(m.getContent());
            String timeStr = TimeUtils.toTime(m.getCreatedAt());
            String status  = m.isRead() ? " · Lu" : "";
            tvTime.setText(timeStr + status);
        }
    }
}
