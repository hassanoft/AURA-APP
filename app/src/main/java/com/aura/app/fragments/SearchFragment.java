package com.aura.app.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aura.app.adapters.PostAdapter;
import com.aura.app.adapters.UserAdapter;
import com.aura.app.managers.AuthManager;
import com.aura.app.managers.DatabaseManager;
import com.aura.app.models.Post;
import com.aura.app.models.User;
import com.aura.app.utils.U;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

/** Fragment de recherche — utilisateurs + publications */
public class SearchFragment extends Fragment {

    private TextInputLayout tilSearch;
    private RecyclerView    rvUsers, rvPosts;
    private UserAdapter     userAdapter;
    private PostAdapter     postAdapter;
    private View            rootView;
    private String          myUserId = "";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        User me = AuthManager.getInstance().getCurrentUser();
        if (me != null) myUserId = me.getId();
        rootView = buildView();
        return rootView;
    }

    private View buildView() {
        LinearLayout root = U.vStack(requireContext(), U.C_BG);
        root.setPadding(U.dp(requireContext(),16), U.dp(requireContext(),16),
                U.dp(requireContext(),16), 0);

        // Titre
        root.addView(U.tv(requireContext(), "Recherche", 22, U.C_TEXT, true));

        // Champ de recherche
        tilSearch = U.inputField(requireContext(), "Rechercher…",
                InputType.TYPE_CLASS_TEXT);
        LinearLayout.LayoutParams sp = U.llMatch();
        sp.setMargins(0, U.dp(requireContext(),12), 0, U.dp(requireContext(),8));
        tilSearch.setLayoutParams(sp);
        root.addView(tilSearch);

        // Label utilisateurs
        root.addView(U.tv(requireContext(), "Utilisateurs",
                13, U.C_TEXT2, false));

        // RecyclerView utilisateurs
        rvUsers = new RecyclerView(requireContext());
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        LinearLayout.LayoutParams up = U.llMatch();
        up.setMargins(0, U.dp(requireContext(),4), 0, U.dp(requireContext(),12));
        rvUsers.setLayoutParams(up);
        userAdapter = new UserAdapter(myUserId, (user, follow, pos) ->
            DatabaseManager.getInstance().follow(myUserId, user.getId(),
                    new DatabaseManager.ActionCb() {
                        @Override public void ok() {}
                        @Override public void err(String m) {}
                    })
        );
        rvUsers.setAdapter(userAdapter);
        root.addView(rvUsers);

        // Label publications
        root.addView(U.tv(requireContext(), "Publications",
                13, U.C_TEXT2, false));

        // RecyclerView posts
        rvPosts = new RecyclerView(requireContext());
        rvPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        pp.setMargins(0, U.dp(requireContext(),4), 0, 0);
        rvPosts.setLayoutParams(pp);
        postAdapter = new PostAdapter(null);
        rvPosts.setAdapter(postAdapter);
        root.addView(rvPosts);

        // Listener de saisie avec délai anti-spam
        if (tilSearch.getEditText() != null) {
            tilSearch.getEditText().addTextChangedListener(new TextWatcher() {
                private final android.os.Handler h = new android.os.Handler();
                private Runnable r;
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                    if (r != null) h.removeCallbacks(r);
                    r = () -> search(s.toString().trim());
                    h.postDelayed(r, 400);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        return root;
    }

    private void search(String q) {
        if (q.isEmpty()) {
            userAdapter.setData(java.util.Collections.emptyList());
            postAdapter.setData(java.util.Collections.emptyList());
            return;
        }

        DatabaseManager.getInstance().searchUsers(q,
                new DatabaseManager.ListCb<User>() {
                    @Override public void ok(List<User> l) {
                        U.ui(() -> userAdapter.setData(l));
                    }
                    @Override public void err(String m) {}
                });

        DatabaseManager.getInstance().searchPosts(q,
                new DatabaseManager.ListCb<Post>() {
                    @Override public void ok(List<Post> l) {
                        U.ui(() -> postAdapter.setData(l));
                    }
                    @Override public void err(String m) {}
                });
    }
}
