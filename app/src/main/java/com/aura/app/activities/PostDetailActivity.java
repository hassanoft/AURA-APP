package com.aura.app.activities;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.aura.app.feed.PostDetailFragment;
import com.aura.app.utils.U;

/** Activité détail d'un post avec ses commentaires */
public class PostDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "post_id";
    public static final int    CONTAINER_ID  = 0x7F_FF_01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setId(CONTAINER_ID);
        root.setBackgroundColor(U.C_BG);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        if (savedInstanceState == null) {
            String postId = getIntent().getStringExtra(EXTRA_POST_ID);
            PostDetailFragment frag = PostDetailFragment.newInstance(postId);
            getSupportFragmentManager().beginTransaction()
                    .replace(CONTAINER_ID, frag)
                    .commit();
        }
    }
}
