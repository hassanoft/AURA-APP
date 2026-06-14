package com.aura.app.fragments;

import android.content.Intent;
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

import com.aura.app.adapters.PostAdapter;
import com.aura.app.managers.AuthManager;
import com.aura.app.managers.DatabaseManager;
import com.aura.app.models.Post;
import com.aura.app.models.User;
import com.aura.app.utils.U;

import java.util.List;

/**
 * Fragment du fil d'actualité AURA (style TikTok + Instagram + X).
 * RecyclerView vertical avec pagination, pull-to-refresh.
 */
public class FeedFragment extends Fragment implements PostAdapter.PostInteraction {

    private SwipeRefreshLayout swipe;
    private RecyclerView       rv;
    private PostAdapter        adapter;
    private View               rootView;

    private int     currentPage = 0;
    private boolean loading     = false;
    private String  myUserId    = "";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        rootView = buildView();

        User me = AuthManager.getInstance().getCurrentUser();
        if (me != null) myUserId = me.getId();

        loadFeed(true);
        return rootView;
    }

    private View buildView() {
        LinearLayout root = U.vStack(requireContext(), U.C_BG);

        // Header "Accueil"
        View header = buildHeader();
        root.addView(header);

        // Pull-to-refresh
        swipe = new SwipeRefreshLayout(requireContext());
        swipe.setColorSchemeColors(U.C_PRIMARY);
        swipe.setProgressBackgroundColorSchemeColor(U.C_SURFACE);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        swipe.setLayoutParams(sp);
        swipe.setOnRefreshListener(() -> {
            currentPage = 0;
            loadFeed(true);
        });

        // RecyclerView
        rv = new RecyclerView(requireContext());
        rv.setBackgroundColor(U.C_BG);
        rv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        rv.setLayoutManager(llm);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override public void getItemOffsets(@NonNull android.graphics.Rect out,
                                                  @NonNull View v, @NonNull RecyclerView parent,
                                                  @NonNull RecyclerView.State state) {
                out.top = U.dp(requireContext(), 4);
            }
        });

        // Pagination infinie
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(@NonNull RecyclerView r, int dx, int dy) {
                if (!loading && llm.findLastVisibleItemPosition()
                        >= adapter.getItemCount() - 5) {
                    currentPage++;
                    loadFeed(false);
                }
            }
        });

        adapter = new PostAdapter(this);
        rv.setAdapter(adapter);

        swipe.addView(rv);
        root.addView(swipe);
        return root;
    }

    private View buildHeader() {
        LinearLayout h = U.hStack(requireContext(), U.C_BG);
        h.setPadding(U.dp(requireContext(), 16), U.dp(requireContext(), 16),
                U.dp(requireContext(), 16), U.dp(requireContext(), 12));
        h.setLayoutParams(U.llMatch());

        android.widget.TextView logo = U.tv(requireContext(), "AURA", 22,
                U.C_PRIMARY, true);
        logo.setLetterSpacing(0.1f);
        logo.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        h.addView(logo);

        android.widget.TextView msgIcon = U.tv(requireContext(), "✉", 22,
                U.C_TEXT2, false);
        msgIcon.setPadding(U.dp(requireContext(), 8), 0, 0, 0);
        msgIcon.setOnClickListener(v -> {
            // Naviguer vers la messagerie
            if (getActivity() instanceof com.aura.app.activities.MainActivity) {
                ((com.aura.app.activities.MainActivity) getActivity())
                        .getSupportFragmentManager().beginTransaction()
                        .replace(com.aura.app.activities.MainActivity.CONTAINER_ID,
                                new MessagesFragment())
                        .commit();
            }
        });
        h.addView(msgIcon);
        return h;
    }

    // ─── Chargement du feed ───────────────────────────────────────────────────

    private void loadFeed(boolean refresh) {
        loading = true;
        if (refresh) swipe.setRefreshing(true);

        DatabaseManager.getInstance().getFeed(currentPage,
                new DatabaseManager.ListCb<Post>() {
                    @Override public void ok(List<Post> list) {
                        U.ui(() -> {
                            loading = false;
                            swipe.setRefreshing(false);
                            if (refresh) adapter.setData(list);
                            else adapter.appendData(list);
                        });
                    }
                    @Override public void err(String msg) {
                        U.ui(() -> {
                            loading = false;
                            swipe.setRefreshing(false);
                            if (rootView != null) U.snackError(rootView, msg);
                        });
                    }
                });
    }

    // ─── PostInteraction ──────────────────────────────────────────────────────

    @Override
    public void onLike(Post post, int position) {
        boolean liked = !post.isLikedByMe();
        post.setLikedByMe(liked);
        post.setLikesCount(post.getLikesCount() + (liked ? 1 : -1));
        adapter.updateItem(position, post);

        DatabaseManager.getInstance().toggleLike(myUserId, post.getId(), liked,
                new DatabaseManager.ActionCb() {
                    @Override public void ok() {
                        if (liked) {
                            com.aura.app.notifications.NotificationDispatcher
                                    .notifyLike(post.getUserId(), myUserId, post.getId());
                        }
                    }
                    @Override public void err(String msg) {
                        // Rollback UI
                        U.ui(() -> {
                            post.setLikedByMe(!liked);
                            post.setLikesCount(post.getLikesCount() + (liked ? -1 : 1));
                            adapter.updateItem(position, post);
                        });
                    }
                });
    }

    @Override
    public void onComment(Post post) {
        Intent i = new Intent(requireContext(),
                com.aura.app.activities.PostDetailActivity.class);
        i.putExtra(com.aura.app.activities.PostDetailActivity.EXTRA_POST_ID, post.getId());
        startActivity(i);
    }

    @Override
    public void onShare(Post post) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT,
                "Découvrez ce post sur AURA : " + post.getContent());
        startActivity(Intent.createChooser(share, "Partager via"));
    }

    @Override
    public void onSave(Post post, int position) {
        boolean saved = !post.isSavedByMe();
        post.setSavedByMe(saved);
        adapter.updateItem(position, post);
        if (rootView != null)
            U.snackOk(rootView, saved ? "Publication sauvegardée" : "Sauvegarde retirée");
    }
}
