package com.aura.app.supabase;

import android.content.Context;
import android.content.SharedPreferences;

import com.aura.app.AuraApplication;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Client HTTP singleton pour toutes les requêtes REST Supabase.
 * Injecte automatiquement les en-têtes d'authentification sur chaque requête.
 */
public final class SupabaseClient {

    private static final String PREFS     = "aura_session";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_UID   = "user_id";

    private static volatile SupabaseClient instance;
    private final OkHttpClient httpClient;

    // ─────────────────────────────────────────────────────────────────────────

    private SupabaseClient() {
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(SupabaseConfig.TIMEOUT_CONNECT, TimeUnit.SECONDS)
                .readTimeout(SupabaseConfig.TIMEOUT_READ,    TimeUnit.SECONDS)
                .writeTimeout(SupabaseConfig.TIMEOUT_WRITE,  TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    String token = getToken();
                    String bearer = (token != null && !token.isEmpty())
                            ? token : SupabaseConfig.SUPABASE_ANON_KEY;

                    Request req = chain.request().newBuilder()
                            .header("apikey",        SupabaseConfig.SUPABASE_ANON_KEY)
                            .header("Authorization", "Bearer " + bearer)
                            .header("Content-Type",  "application/json")
                            .header("Prefer",        "return=representation")
                            .build();
                    return chain.proceed(req);
                })
                .addInterceptor(logger)
                .build();
    }

    public static SupabaseClient getInstance() {
        if (instance == null) {
            synchronized (SupabaseClient.class) {
                if (instance == null) instance = new SupabaseClient();
            }
        }
        return instance;
    }

    public OkHttpClient http() { return httpClient; }

    // ─── Session ─────────────────────────────────────────────────────────────

    public static void saveSession(String token, String userId) {
        prefs().edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_UID,   userId)
                .apply();
    }

    public static void clearSession() {
        prefs().edit().clear().apply();
    }

    public static boolean isLoggedIn() {
        String t = prefs().getString(KEY_TOKEN, null);
        return t != null && !t.isEmpty();
    }

    public static String getToken() {
        return prefs().getString(KEY_TOKEN, null);
    }

    public static String getSavedUserId() {
        return prefs().getString(KEY_UID, null);
    }

    private static SharedPreferences prefs() {
        return AuraApplication.getAppContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
