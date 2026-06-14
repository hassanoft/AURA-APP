package com.aura.app.chat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
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

import com.aura.app.adapters.MessageAdapter;
import com.aura.app.managers.AuthManager;
import com.aura.app.managers.DatabaseManager;
import com.aura.app.models.Message;
import com.aura.app.services.RealtimeService;
import com.aura.app.utils.U;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Fragment de conversation privée AURA.
 * Affiche l'historique des messages et permet l'envoi en temps réel
 * via Supabase Realtime (WebSocket).
 */
public class ChatFragment extends Fragment {

    private static final String ARG_CONV_ID  = "conv_id";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_AVATAR   = "avatar";

    private String conversationId;
    private String otherUsername;
    private String otherAvatar;
    private String myUserId = "";

    private RecyclerView    rv;
    private MessageAdapter   adapter;
    private TextInputLayout  tilInput;
    private View             rootView;

    // ─── Service Realtime ─────────────────────────────────────────────────────
    private RealtimeService realtimeService;
    private boolean         bound;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName n, IBinder b) {
            realtimeService = ((RealtimeService.LocalBinder) b).getService();
            bound = true;
            realtimeService.addListener(realtimeCb);
        }
        @Override public void onServiceDisconnected(ComponentName n) { bound = false; }
    };

    private final RealtimeService.RealtimeCb realtimeCb = new RealtimeService.RealtimeCb() {
        @Override public void onMessage(String convId, String payload) {
            // Sur réception d'un nouveau message, on rafraîchit la conversation
            U.ui(ChatFragment.this::loadMessages);
        }
        @Override public void onNotification(String payload) {}
    };

    // ─── Instanciation ────────────────────────────────────────────────────────

    public static ChatFragment newInstance(String convId, String username, String avatarUrl) {
        ChatFragment f = new ChatFragment();
        Bundle b = new Bundle();
        b.putString(ARG_CONV_ID,  convId);
        b.putString(ARG_USERNAME, username);
        b.putString(ARG_AVATAR,   avatarUrl);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            conversationId = getArguments().getString(ARG_CONV_ID);
            otherUsername  = getArguments().getString(ARG_USERNAME);
            otherAvatar    = getArguments().getString(ARG_AVATAR);
        }
        com.aura.app.models.User me = AuthManager.getInstance().getCurrentUser();
        if (me != null) myUserId = me.getId();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        rootView = buildView();
        loadMessages();

        // Liaison au service Realtime
        Intent svc = new Intent(requireContext(), RealtimeService.class);
        requireActivity().bindService(svc, connection, Context.BIND_AUTO_CREATE);

        return rootView;
    }

    // ─── Construction de l'UI ─────────────────────────────────────────────────

    private View buildView() {
        LinearLayout root = U.vStack(requireContext(), U.C_BG);

        // ── Header avec avatar + nom + statut ──────────────────────────────────
        LinearLayout header = U.hStack(requireContext(), U.C_SURFACE);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(U.dp(requireContext(),12), U.dp(requireContext(),14),
                U.dp(requireContext(),12), U.dp(requireContext(),14));
        header.setElevation(U.dp(requireContext(), 4));
        header.setLayoutParams(U.llMatch());

        TextView back = U.tv(requireContext(), "←", 22, U.C_TEXT, false);
        back.setPadding(0, 0, U.dp(requireContext(),12), 0);
        back.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        header.addView(back);

        CircleImageView avatar = new CircleImageView(requireContext());
        int avSz = U.dp(requireContext(), 40);
        LinearLayout.LayoutParams avP = new LinearLayout.LayoutParams(avSz, avSz);
        avP.setMargins(0, 0, U.dp(requireContext(),10), 0);
        avatar.setLayoutParams(avP);
        if (otherAvatar != null && !otherAvatar.isEmpty())
            Glide.with(this).load(otherAvatar).circleCrop().into(avatar);
        header.addView(avatar);

        LinearLayout col = new LinearLayout(requireContext());
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvName = U.tv(requireContext(), "@" + (otherUsername != null ? otherUsername : ""),
                15, U.C_TEXT, true);
        col.addView(tvName);

        TextView tvStatus = U.tv(requireContext(), "En ligne", 11, U.C_PRIMARY, false);
        col.addView(tvStatus);

        header.addView(col);
        root.addView(header);

        // ── Liste de messages ──────────────────────────────────────────────────
        rv = new RecyclerView(requireContext());
        rv.setBackgroundColor(U.C_BG);
        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        llm.setStackFromEnd(true);
        rv.setLayoutManager(llm);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        rv.setLayoutParams(rp);
        adapter = new MessageAdapter(myUserId);
        rv.setAdapter(adapter);
        root.addView(rv);

        // ── Barre de saisie ─────────────────────────────────────────────────────
        LinearLayout inputRow = U.hStack(requireContext(), U.C_SURFACE);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        int pad = U.dp(requireContext(), 12);
        inputRow.setPadding(pad, U.dp(requireContext(),8), pad, U.dp(requireContext(),8));
        inputRow.setLayoutParams(U.llMatch());

        // Bouton image
        TextView btnImg = U.tv(requireContext(), "📷", 20, U.C_TEXT2, false);
        btnImg.setPadding(U.dp(requireContext(),4), 0, U.dp(requireContext(),8), 0);
        inputRow.addView(btnImg);

        tilInput = U.inputField(requireContext(), "Votre message…", InputType.TYPE_CLASS_TEXT);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tilInput.setLayoutParams(tp);
        inputRow.addView(tilInput);

        MaterialButton btnSend = new MaterialButton(requireContext());
        btnSend.setText("➤");
        btnSend.setTextColor(U.C_WHITE);
        btnSend.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(U.C_PRIMARY));
        btnSend.setCornerRadius(U.dp(requireContext(), 20));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                U.dp(requireContext(), 44), U.dp(requireContext(), 44));
        sp.setMargins(U.dp(requireContext(),8), 0, 0, 0);
        btnSend.setPadding(0, 0, 0, 0);
        btnSend.setLayoutParams(sp);
        btnSend.setOnClickListener(v -> sendMessage());
        inputRow.addView(btnSend);

        root.addView(inputRow);
        return root;
    }

    // ─── Chargement / envoi ────────────────────────────────────────────────────

    private void loadMessages() {
        if (conversationId == null) return;
        DatabaseManager.getInstance().getMessages(conversationId,
                new DatabaseManager.ListCb<Message>() {
                    @Override public void ok(List<Message> list) {
                        U.ui(() -> {
                            adapter.setData(list);
                            if (rv != null && list.size() > 0)
                                rv.scrollToPosition(list.size() - 1);
                        });
                    }
                    @Override public void err(String m) {
                        U.ui(() -> { if (rootView != null) U.snackError(rootView, m); });
                    }
                });
    }

    private void sendMessage() {
        if (tilInput.getEditText() == null) return;
        String text = tilInput.getEditText().getText().toString().trim();
        if (text.isEmpty() || conversationId == null) return;

        tilInput.getEditText().setText("");

        DatabaseManager.getInstance().sendMessage(conversationId, myUserId, text,
                new DatabaseManager.ActionCb() {
                    @Override public void ok() {
                        U.ui(ChatFragment.this::loadMessages);
                    }
                    @Override public void err(String m) {
                        U.ui(() -> { if (rootView != null) U.snackError(rootView, m); });
                    }
                });
    }

    // ─── Cycle de vie ──────────────────────────────────────────────────────────

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bound) {
            if (realtimeService != null) realtimeService.removeListener(realtimeCb);
            requireActivity().unbindService(connection);
            bound = false;
        }
    }
}
