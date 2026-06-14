package com.aura.app.supabase;

import com.aura.app.BuildConfig;

/**
 * Configuration centrale Supabase.
 * Les credentials sont injectés depuis app/build.gradle via BuildConfig.
 *
 * Pour configurer : modifier les buildConfigField dans app/build.gradle
 */
public final class SupabaseConfig {

    // ── Credentials (depuis BuildConfig → app/build.gradle) ──────────────────
    public static final String SUPABASE_URL      = BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;

    // ── Endpoints ─────────────────────────────────────────────────────────────
    public static final String REST_URL     = SUPABASE_URL + "/rest/v1/";
    public static final String AUTH_URL     = SUPABASE_URL + "/auth/v1/";
    public static final String STORAGE_URL  = SUPABASE_URL + "/storage/v1/";
    public static final String REALTIME_URL = SUPABASE_URL
            .replace("https://", "wss://") + "/realtime/v1/websocket";

    // ── Tables ────────────────────────────────────────────────────────────────
    public static final String TABLE_USERS         = "users";
    public static final String TABLE_POSTS         = "posts";
    public static final String TABLE_COMMENTS      = "comments";
    public static final String TABLE_LIKES         = "likes";
    public static final String TABLE_FOLLOWS       = "follows";
    public static final String TABLE_MESSAGES      = "messages";
    public static final String TABLE_CONVERSATIONS = "conversations";
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String TABLE_SAVES         = "saves";

    // ── Storage Buckets ───────────────────────────────────────────────────────
    public static final String BUCKET_AVATARS  = "avatars";
    public static final String BUCKET_POSTS    = "posts";
    public static final String BUCKET_MESSAGES = "messages";

    // ── Timeouts (secondes) ───────────────────────────────────────────────────
    public static final int TIMEOUT_CONNECT = 15;
    public static final int TIMEOUT_READ    = 30;
    public static final int TIMEOUT_WRITE   = 60;

    private SupabaseConfig() {}
}
