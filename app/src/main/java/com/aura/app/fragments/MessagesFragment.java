package com.aura.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aura.app.activities.ChatActivity;
import com.aura.app.managers.AuthManager;
import com.aura.app.managers.DatabaseManager;
import com.aura.app.models.Conversation;
import com.aura.app.models.User;
import com.aura.app.utils.TimeUtils;
import com.aura.app.utils.U;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/** Fragment liste des conversations privées */
public class MessagesFragment extends Fragment {

    private SwipeRefreshLayout swipe;
    private ConvAdapter        adapter;
    private View               rootView;
    private String             myUserId = "";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        User me = AuthManager.getInstance().getCurrentUser();
        if (me != null) myUserId = me.getId();
        rootView = buildView();
        load();
        return rootView;
    }

    private View buildView() {
        LinearLayout root = U.vStack(requireContext(), U.C_BG);

        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(U.dp(requireContext(),16), U.dp(requireContext(),16),
                U.dp(requireContext(),16), U.dp(requireContext(),8));
        header.setLayoutParams(U.llMatch());
        header.addView(U.tv(requireContext(), "Messages", 22, U.C_TEXT, true));
        root.addView(header);

        swipe = new SwipeRefreshLayout(requireContext());
        swipe.setColorSchemeColors(U.C_PRIMARY);
        swipe.setProgressBackgroundColorSchemeColor(U.C_SURFACE);
        swipe.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        swipe.setOnRefreshListener(this::load);

        RecyclerView rv = new RecyclerView(requireContext());
        rv.setBackgroundColor(U.C_BG);
        rv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ConvAdapter(conv -> {
            Intent i = new Intent(requireContext(), ChatActivity.class);
            i.putExtra(ChatActivity.EXTRA_CONV_ID,  conv.getId());
            i.putExtra(ChatActivity.EXTRA_USERNAME,  conv.getOtherUsername());
            i.putExtra(ChatActivity.EXTRA_AVATAR,    conv.getOtherAvatar());
            startActivity(i);
        });
        rv.setAdapter(adapter);
        swipe.addView(rv);
        root.addView(swipe);
        return root;
    }

    private void load() {
        if (myUserId.isEmpty()) { swipe.setRefreshing(false); return; }
        swipe.setRefreshing(true);
        DatabaseManager.getInstance().getConversations(myUserId,
                new DatabaseManager.ListCb<Conversation>() {
                    @Override public void ok(List<Conversation> l) {
                        U.ui(() -> { swipe.setRefreshing(false); adapter.setData(l); });
                    }
                    @Override public void err(String m) {
                        U.ui(() -> { swipe.setRefreshing(false);
                            if (rootView != null) U.snackError(rootView, m); });
                    }
                });
    }

    // ─── Adaptateur interne ───────────────────────────────────────────────────

    interface OnConvClick { void onClick(Conversation c); }

    static class ConvAdapter extends RecyclerView.Adapter<ConvAdapter.CVH> {
        private final List<Conversation> list = new ArrayList<>();
        private final OnConvClick        cb;
        ConvAdapter(OnConvClick cb) { this.cb = cb; }

        void setData(List<Conversation> l) { list.clear(); list.addAll(l); notifyDataSetChanged(); }

        @NonNull @Override
        public CVH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            View v = buildRow(p.getContext());
            v.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new CVH(v);
        }
        @Override public void onBindViewHolder(@NonNull CVH h, int pos) { h.bind(list.get(pos)); }
        @Override public int getItemCount() { return list.size(); }

        static View buildRow(Context c) {
            LinearLayout row = U.hStack(c, U.C_BG);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rp = U.llMatch();
            rp.setMargins(U.dp(c,12), U.dp(c,4), U.dp(c,12), U.dp(c,4));
            row.setLayoutParams(rp);
            row.setPadding(U.dp(c,12), U.dp(c,12), U.dp(c,12), U.dp(c,12));

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(U.C_SURFACE);
            bg.setCornerRadius(U.dp(c, 14));
            row.setBackground(bg);

            // Conteneur avatar + point en ligne
            FrameWithDot aFrame = new FrameWithDot(c);
            int sz = U.dp(c, 50);
            LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(sz, sz);
            fp.setMargins(0, 0, U.dp(c,12), 0);
            aFrame.setLayoutParams(fp);
            row.addView(aFrame);

            LinearLayout col = new LinearLayout(c);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvName = new TextView(c);
            tvName.setTag("conv_name");
            tvName.setTextColor(U.C_TEXT);
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvName.setTypeface(tvName.getTypeface(), android.graphics.Typeface.BOLD);
            col.addView(tvName);

            TextView tvMsg = new TextView(c);
            tvMsg.setTag("conv_last");
            tvMsg.setTextColor(U.C_TEXT2);
            tvMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvMsg.setMaxLines(1);
            tvMsg.setEllipsize(android.text.TextUtils.TruncateAt.END);
            col.addView(tvMsg);

            row.addView(col);

            TextView tvTime = new TextView(c);
            tvTime.setTag("conv_time");
            tvTime.setTextColor(U.C_TEXT2);
            tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            row.addView(tvTime);

            return row;
        }

        static class CVH extends RecyclerView.ViewHolder {
            TextView tvName, tvMsg, tvTime;
            CVH(View v) {
                super(v);
                tvName = v.findViewWithTag("conv_name");
                tvMsg  = v.findViewWithTag("conv_last");
                tvTime = v.findViewWithTag("conv_time");
            }
            void bind(Conversation conv) {
                tvName.setText("@" + conv.getOtherUsername());
                tvMsg.setText(conv.getLastMessage().isEmpty()
                        ? "Démarrer la conversation" : conv.getLastMessage());
                tvTime.setText(TimeUtils.relative(conv.getUpdatedAt()));
                itemView.setOnClickListener(v -> {
                    if (((ConvAdapter) getBindingAdapter()).cb != null)
                        ((ConvAdapter) getBindingAdapter()).cb.onClick(conv);
                });
            }
        }
    }

    // FrameLayout simple pour le point de présence en ligne
    static class FrameWithDot extends android.widget.FrameLayout {
        FrameWithDot(Context c) {
            super(c);
            CircleImageView av = new CircleImageView(c);
            av.setTag("conv_avatar");
            LayoutParams lp = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            av.setLayoutParams(lp);
            addView(av);

            View dot = new View(c);
            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            d.setColor(U.C_PRIMARY);
            d.setStroke(U.dp(c,2), U.C_BG);
            dot.setBackground(d);
            int ds = U.dp(c, 12);
            LayoutParams dp = new LayoutParams(ds, ds, Gravity.BOTTOM | Gravity.END);
            dot.setLayoutParams(dp);
            dot.setTag("online_dot");
            addView(dot);
        }
    }
}
