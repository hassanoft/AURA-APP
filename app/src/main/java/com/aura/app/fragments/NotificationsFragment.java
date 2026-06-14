package com.aura.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aura.app.adapters.NotificationAdapter;
import com.aura.app.managers.AuthManager;
import com.aura.app.managers.DatabaseManager;
import com.aura.app.models.Notification;
import com.aura.app.models.User;
import com.aura.app.utils.U;

import java.util.List;

/** Fragment des notifications : likes, commentaires, abonnements, mentions */
public class NotificationsFragment extends Fragment {

    private SwipeRefreshLayout    swipe;
    private NotificationAdapter   adapter;
    private View                  rootView;
    private String                myUserId = "";

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

        // En-tête
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(U.dp(requireContext(),16), U.dp(requireContext(),16),
                U.dp(requireContext(),16), U.dp(requireContext(),8));
        header.setLayoutParams(U.llMatch());
        header.addView(U.tv(requireContext(), "Notifications", 22, U.C_TEXT, true));
        root.addView(header);

        // SwipeRefresh
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

        adapter = new NotificationAdapter(notif -> {
            // Marquer comme lu + naviguer vers le post si applicable
            DatabaseManager.getInstance().markNotifRead(notif.getId(),
                    new DatabaseManager.ActionCb() {
                        @Override public void ok() {}
                        @Override public void err(String m) {}
                    });
        });
        rv.setAdapter(adapter);
        swipe.addView(rv);
        root.addView(swipe);
        return root;
    }

    private void load() {
        if (myUserId.isEmpty()) { swipe.setRefreshing(false); return; }
        swipe.setRefreshing(true);
        DatabaseManager.getInstance().getNotifications(myUserId,
                new DatabaseManager.ListCb<Notification>() {
                    @Override public void ok(List<Notification> l) {
                        U.ui(() -> { swipe.setRefreshing(false); adapter.setData(l); });
                    }
                    @Override public void err(String m) {
                        U.ui(() -> { swipe.setRefreshing(false);
                            if (rootView != null) U.snackError(rootView, m); });
                    }
                });
    }
}
