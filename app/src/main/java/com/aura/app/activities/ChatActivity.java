package com.aura.app.activities;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.aura.app.chat.ChatFragment;
import com.aura.app.utils.U;

/**
 * Activité de messagerie privée.
 * Reçoit conversationId + otherUsername en extra.
 */
public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CONV_ID  = "conversation_id";
    public static final String EXTRA_USERNAME = "other_username";
    public static final String EXTRA_AVATAR   = "other_avatar";
    public static final int    CONTAINER_ID   = 0x7F_EE_01;

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
            String convId    = getIntent().getStringExtra(EXTRA_CONV_ID);
            String username  = getIntent().getStringExtra(EXTRA_USERNAME);
            String avatarUrl = getIntent().getStringExtra(EXTRA_AVATAR);

            ChatFragment frag = ChatFragment.newInstance(convId, username, avatarUrl);
            getSupportFragmentManager().beginTransaction()
                    .replace(CONTAINER_ID, frag)
                    .commit();
        }
    }
}
