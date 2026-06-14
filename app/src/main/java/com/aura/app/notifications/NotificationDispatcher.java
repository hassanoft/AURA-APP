package com.aura.app.notifications;

import com.aura.app.supabase.SupabaseClient;
import com.aura.app.supabase.SupabaseConfig;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Crée des entrées dans la table {@code notifications} de Supabase
 * lorsqu'un utilisateur effectue une action sociale (like, commentaire,
 * abonnement, mention).
 */
public final class NotificationDispatcher {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Executor  EXEC = Executors.newCachedThreadPool();

    private NotificationDispatcher() {}

    /** Crée une notification de type "like". */
    public static void notifyLike(String receiverId, String senderId, String postId) {
        dispatch(receiverId, senderId, "like", postId);
    }

    /** Crée une notification de type "comment". */
    public static void notifyComment(String receiverId, String senderId, String postId) {
        dispatch(receiverId, senderId, "comment", postId);
    }

    /** Crée une notification de type "follow". */
    public static void notifyFollow(String receiverId, String senderId) {
        dispatch(receiverId, senderId, "follow", "");
    }

    /** Crée une notification de type "mention". */
    public static void notifyMention(String receiverId, String senderId, String postId) {
        dispatch(receiverId, senderId, "mention", postId);
    }

    // ─── Implémentation ───────────────────────────────────────────────────────

    private static void dispatch(String receiverId, String senderId,
                                  String type, String postId) {
        // Pas de notification si l'utilisateur interagit avec son propre contenu
        if (receiverId == null || receiverId.equals(senderId)) return;

        EXEC.execute(() -> {
            try {
                OkHttpClient http = SupabaseClient.getInstance().http();

                JsonObject body = new JsonObject();
                body.addProperty("user_id",   receiverId);
                body.addProperty("sender_id", senderId);
                body.addProperty("type",      type);
                body.addProperty("post_id",   postId);
                body.addProperty("is_read",   false);

                Request request = new Request.Builder()
                        .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_NOTIFICATIONS)
                        .post(RequestBody.create(body.toString(), JSON))
                        .build();

                http.newCall(request).execute().close();

            } catch (IOException ignored) {
                // Échec silencieux — la notification n'est pas critique
            }
        });
    }
}
