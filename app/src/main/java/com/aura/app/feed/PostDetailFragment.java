package com.aura.app.feed;

import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aura.app.adapters.CommentAdapter;
import com.aura.app.managers.AuthManager;
import com.aura.app.managers.DatabaseManager;
import com.aura.app.models.Comment;
import com.aura.app.models.User;
import com.aura.app.utils.U;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

/** Détail d'un post avec la liste des commentaires */
public class PostDetailFragment extends Fragment {

    private static final String ARG_POST = "post_id";
    private String postId;
    private String myUserId = "";
    private CommentAdapter adapter;
    private TextInputLayout tilComment;
    private View rootView;

    public static PostDetailFragment newInstance(String postId) {
        PostDetailFragment f = new PostDetailFragment();
        Bundle b = new Bundle();
        b.putString(ARG_POST, postId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) postId = getArguments().getString(ARG_POST);
        User me = AuthManager.getInstance().getCurrentUser();
        if (me != null) myUserId = me.getId();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        rootView = buildView();
        loadComments();
        return rootView;
    }

    private View buildView() {
        LinearLayout root = U.vStack(requireContext(), U.C_BG);
        int pad = U.dp(requireContext(), 16);
        root.setPadding(pad, U.dp(requireContext(), 24), pad, 0);

        // Header
        LinearLayout header = U.hStack(requireContext(), U.C_BG);
        header.setLayoutParams(U.llMatch());
        View back = U.tv(requireContext(), "←", 22, U.C_TEXT2, false);
        back.setPadding(0, 0, U.dp(requireContext(),12), 0);
        back.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        header.addView(back);
        header.addView(U.tv(requireContext(), "Commentaires", 18, U.C_TEXT, true));
        LinearLayout.LayoutParams hp = U.llMatch();
        hp.setMargins(0, 0, 0, U.dp(requireContext(), 12));
        header.setLayoutParams(hp);
        root.addView(header);

        // Liste commentaires
        RecyclerView rv = new RecyclerView(requireContext());
        rv.setBackgroundColor(U.C_BG);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        rv.setLayoutParams(rp);
        adapter = new CommentAdapter();
        rv.setAdapter(adapter);
        root.addView(rv);

        // Saisie d'un commentaire
        LinearLayout inputRow = U.hStack(requireContext(), U.C_SURFACE);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        inputRow.setPadding(pad, U.dp(requireContext(),8), pad, U.dp(requireContext(),8));
        inputRow.setLayoutParams(U.llMatch());

        tilComment = U.inputField(requireContext(), "Ajouter un commentaire…",
                InputType.TYPE_CLASS_TEXT);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tilComment.setLayoutParams(tp);
        inputRow.addView(tilComment);

        MaterialButton btnSend = new MaterialButton(requireContext());
        btnSend.setText("↑");
        btnSend.setTextColor(U.C_WHITE);
        btnSend.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(U.C_PRIMARY));
        btnSend.setCornerRadius(U.dp(requireContext(), 20));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                U.dp(requireContext(), 44), U.dp(requireContext(), 44));
        sp.setMargins(U.dp(requireContext(),8), 0, 0, 0);
        btnSend.setLayoutParams(sp);
        btnSend.setPadding(0, 0, 0, 0);
        btnSend.setOnClickListener(v -> sendComment());
        inputRow.addView(btnSend);

        root.addView(inputRow);
        return root;
    }

    private void loadComments() {
        DatabaseManager.getInstance().getComments(postId,
                new DatabaseManager.ListCb<Comment>() {
                    @Override public void ok(List<Comment> list) {
                        U.ui(() -> adapter.setData(list));
                    }
                    @Override public void err(String m) {
                        U.ui(() -> { if (rootView != null) U.snackError(rootView, m); });
                    }
                });
    }

    private void sendComment() {
        String text = tilComment.getEditText() != null
                ? tilComment.getEditText().getText().toString().trim() : "";
        if (text.isEmpty()) return;

        DatabaseManager.getInstance().addComment(myUserId, postId, text,
                new DatabaseManager.ActionCb() {
                    @Override public void ok() {
                        U.ui(() -> {
                            if (tilComment.getEditText() != null)
                                tilComment.getEditText().setText("");
                            loadComments();
                        });
                    }
                    @Override public void err(String m) {
                        U.ui(() -> { if (rootView != null) U.snackError(rootView, m); });
                    }
                });
    }
}
