package com.aura.app.managers;

import com.aura.app.models.Comment;
import com.aura.app.models.Conversation;
import com.aura.app.models.Message;
import com.aura.app.models.Notification;
import com.aura.app.models.Post;
import com.aura.app.models.User;
import com.aura.app.supabase.SupabaseClient;
import com.aura.app.supabase.SupabaseConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/** Toutes les opérations CRUD Supabase PostgREST */
public class DatabaseManager {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static volatile DatabaseManager instance;
    private final OkHttpClient http;
    private final Executor     exec;

    // ─── Callbacks ────────────────────────────────────────────────────────────

    public interface ListCb<T>   { void ok(List<T> list); void err(String msg); }
    public interface SingleCb<T> { void ok(T item);       void err(String msg); }
    public interface ActionCb    { void ok();              void err(String msg); }

    // ─── Singleton ────────────────────────────────────────────────────────────

    private DatabaseManager() {
        http = SupabaseClient.getInstance().http();
        exec = Executors.newCachedThreadPool();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) synchronized (DatabaseManager.class) {
            if (instance == null) instance = new DatabaseManager();
        }
        return instance;
    }

    // ─── FEED ─────────────────────────────────────────────────────────────────

    public void getFeed(int page, ListCb<Post> cb) {
        exec.execute(() -> {
            try {
                int offset = page * 20;
                Request r = get(SupabaseConfig.TABLE_POSTS
                        + "?select=*,users!inner(id,username,firstname,lastname,avatar_url,verified)"
                        + "&order=created_at.desc&limit=20&offset=" + offset);
                Response resp = http.newCall(r).execute();
                String   body = resp.body() != null ? resp.body().string() : "[]";
                cb.ok(parsePosts(body));
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    public void createPost(Post p, ActionCb cb) {
        exec.execute(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("user_id",    p.getUserId());
                body.addProperty("content",    p.getContent());
                body.addProperty("media_url",  p.getMediaUrl());
                body.addProperty("media_type", p.getMediaType());
                Request r = new Request.Builder()
                        .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_POSTS)
                        .post(RequestBody.create(body.toString(), JSON))
                        .build();
                Response resp = http.newCall(r).execute();
                if (resp.isSuccessful()) cb.ok(); else cb.err("Création échouée.");
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    // ─── LIKES ────────────────────────────────────────────────────────────────

    public void toggleLike(String userId, String postId, boolean like, ActionCb cb) {
        exec.execute(() -> {
            try {
                Request r;
                if (like) {
                    JsonObject b = new JsonObject();
                    b.addProperty("user_id", userId);
                    b.addProperty("post_id", postId);
                    r = new Request.Builder()
                            .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_LIKES)
                            .post(RequestBody.create(b.toString(), JSON))
                            .build();
                } else {
                    r = new Request.Builder()
                            .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_LIKES
                                    + "?user_id=eq." + userId + "&post_id=eq." + postId)
                            .delete()
                            .build();
                }
                http.newCall(r).execute();
                cb.ok();
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    // ─── COMMENTAIRES ─────────────────────────────────────────────────────────

    public void getComments(String postId, ListCb<Comment> cb) {
        exec.execute(() -> {
            try {
                Request r = get(SupabaseConfig.TABLE_COMMENTS
                        + "?post_id=eq." + postId
                        + "&select=*,users!inner(username,avatar_url)"
                        + "&order=created_at.asc");
                Response resp = http.newCall(r).execute();
                String   body = resp.body() != null ? resp.body().string() : "[]";
                cb.ok(parseComments(body));
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    public void addComment(String userId, String postId, String content, ActionCb cb) {
        exec.execute(() -> {
            try {
                JsonObject b = new JsonObject();
                b.addProperty("user_id", userId);
                b.addProperty("post_id", postId);
                b.addProperty("content", content);
                Request r = new Request.Builder()
                        .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_COMMENTS)
                        .post(RequestBody.create(b.toString(), JSON))
                        .build();
                Response resp = http.newCall(r).execute();
                if (resp.isSuccessful()) cb.ok(); else cb.err("Impossible d'ajouter.");
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    // ─── FOLLOW ───────────────────────────────────────────────────────────────

    public void follow(String followerId, String followedId, ActionCb cb) {
        exec.execute(() -> {
            try {
                JsonObject b = new JsonObject();
                b.addProperty("follower_id", followerId);
                b.addProperty("followed_id", followedId);
                Request r = new Request.Builder()
                        .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_FOLLOWS)
                        .post(RequestBody.create(b.toString(), JSON))
                        .build();
                Response resp = http.newCall(r).execute();
                if (resp.isSuccessful()) cb.ok(); else cb.err("Follow échoué.");
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    public void unfollow(String followerId, String followedId, ActionCb cb) {
        exec.execute(() -> {
            try {
                Request r = new Request.Builder()
                        .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_FOLLOWS
                                + "?follower_id=eq." + followerId
                                + "&followed_id=eq." + followedId)
                        .delete()
                        .build();
                http.newCall(r).execute();
                cb.ok();
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    // ─── RECHERCHE ────────────────────────────────────────────────────────────

    public void searchUsers(String q, ListCb<User> cb) {
        exec.execute(() -> {
            try {
                String enc = q.toLowerCase().trim();
                Request r = get(SupabaseConfig.TABLE_USERS
                        + "?or=(username.ilike.*" + enc + "*,firstname.ilike.*"
                        + enc + "*,lastname.ilike.*" + enc + "*)"
                        + "&select=id,username,firstname,lastname,avatar_url,verified,followers"
                        + "&limit=30");
                Response resp = http.newCall(r).execute();
                String   body = resp.body() != null ? resp.body().string() : "[]";
                cb.ok(parseUsers(body));
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    public void searchPosts(String q, ListCb<Post> cb) {
        exec.execute(() -> {
            try {
                String enc = q.trim();
                Request r = get(SupabaseConfig.TABLE_POSTS
                        + "?content=ilike.*" + enc + "*"
                        + "&select=*,users!inner(username,avatar_url,verified)"
                        + "&order=created_at.desc&limit=30");
                Response resp = http.newCall(r).execute();
                String   body = resp.body() != null ? resp.body().string() : "[]";
                cb.ok(parsePosts(body));
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    // ─── PROFIL ───────────────────────────────────────────────────────────────

    public void getUserById(String userId, SingleCb<User> cb) {
        exec.execute(() -> {
            try {
                Request r = get(SupabaseConfig.TABLE_USERS
                        + "?id=eq." + userId + "&select=*");
                Response resp = http.newCall(r).execute();
                String   body = resp.body() != null ? resp.body().string() : "[]";
                List<User> users = parseUsers(body);
                if (!users.isEmpty()) cb.ok(users.get(0));
                else cb.err("Utilisateur introuvable.");
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    public void getUserPosts(String userId, ListCb<Post> cb) {
        exec.execute(() -> {
            try {
                Request r = get(SupabaseConfig.TABLE_POSTS
                        + "?user_id=eq." + userId
                        + "&select=*,users!inner(username,avatar_url,verified)"
                        + "&order=created_at.desc");
                Response resp = http.newCall(r).execute();
                String   body = resp.body() != null ? resp.body().string() : "[]";
                cb.ok(parsePosts(body));
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    public void updateProfile(String userId, String bio, String avatarUrl, ActionCb cb) {
        exec.execute(() -> {
            try {
                JsonObject b = new JsonObject();
                if (bio != null)       b.addProperty("bio",        bio);
                if (avatarUrl != null) b.addProperty("avatar_url", avatarUrl);
                Request r = new Request.Builder()
                        .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_USERS
                                + "?id=eq." + userId)
                        .patch(RequestBody.create(b.toString(), JSON))
                        .build();
                Response resp = http.newCall(r).execute();
                if (resp.isSuccessful()) cb.ok(); else cb.err("Mise à jour échouée.");
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    // ─── NOTIFICATIONS ────────────────────────────────────────────────────────

    public void getNotifications(String userId, ListCb<Notification> cb) {
        exec.execute(() -> {
            try {
                Request r = get(SupabaseConfig.TABLE_NOTIFICATIONS
                        + "?user_id=eq." + userId
                        + "&select=*,sender:users!sender_id(username,avatar_url)"
                        + "&order=created_at.desc&limit=50");
                Response resp = http.newCall(r).execute();
                String   body = resp.body() != null ? resp.body().string() : "[]";
                cb.ok(parseNotifications(body));
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    public void markNotifRead(String notifId, ActionCb cb) {
        exec.execute(() -> {
            try {
                JsonObject b = new JsonObject();
                b.addProperty("is_read", true);
                Request r = new Request.Builder()
                        .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_NOTIFICATIONS
                                + "?id=eq." + notifId)
                        .patch(RequestBody.create(b.toString(), JSON))
                        .build();
                http.newCall(r).execute();
                cb.ok();
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    // ─── MESSAGES ─────────────────────────────────────────────────────────────

    public void getConversations(String userId, ListCb<Conversation> cb) {
        exec.execute(() -> {
            try {
                Request r = get(SupabaseConfig.TABLE_CONVERSATIONS
                        + "?or=(user1_id.eq." + userId + ",user2_id.eq." + userId + ")"
                        + "&select=*&order=updated_at.desc");
                Response resp = http.newCall(r).execute();
                String   body = resp.body() != null ? resp.body().string() : "[]";
                cb.ok(parseConversations(body));
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    public void getMessages(String convId, ListCb<Message> cb) {
        exec.execute(() -> {
            try {
                Request r = get(SupabaseConfig.TABLE_MESSAGES
                        + "?conversation_id=eq." + convId
                        + "&select=*,sender:users!sender_id(username,avatar_url)"
                        + "&order=created_at.asc");
                Response resp = http.newCall(r).execute();
                String   body = resp.body() != null ? resp.body().string() : "[]";
                cb.ok(parseMessages(body));
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    public void sendMessage(String convId, String senderId, String content, ActionCb cb) {
        exec.execute(() -> {
            try {
                JsonObject b = new JsonObject();
                b.addProperty("conversation_id", convId);
                b.addProperty("sender_id",       senderId);
                b.addProperty("content",         content);
                b.addProperty("is_read",         false);
                Request r = new Request.Builder()
                        .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_MESSAGES)
                        .post(RequestBody.create(b.toString(), JSON))
                        .build();
                Response resp = http.newCall(r).execute();
                if (resp.isSuccessful()) cb.ok(); else cb.err("Envoi échoué.");
            } catch (IOException e) { cb.err(e.getMessage()); }
        });
    }

    // ─── Parsers ──────────────────────────────────────────────────────────────

    private List<Post> parsePosts(String json) {
        List<Post> list = new ArrayList<>();
        try {
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();
                Post p = new Post();
                p.setId(s(o, "id"));
                p.setUserId(s(o, "user_id"));
                p.setContent(s(o, "content"));
                p.setMediaUrl(s(o, "media_url"));
                p.setMediaType(s(o, "media_type"));
                p.setLikesCount(n(o, "likes_count"));
                p.setCommentsCount(n(o, "comments_count"));
                p.setSharesCount(n(o, "shares_count"));
                p.setCreatedAt(s(o, "created_at"));
                if (o.has("users") && !o.get("users").isJsonNull()) {
                    JsonObject u = o.getAsJsonObject("users");
                    p.setAuthorUsername(s(u, "username"));
                    p.setAuthorAvatar(s(u, "avatar_url"));
                    p.setAuthorFirstName(s(u, "firstname"));
                    p.setAuthorVerified(u.has("verified") && !u.get("verified").isJsonNull()
                            && u.get("verified").getAsBoolean());
                }
                list.add(p);
            }
        } catch (Exception ignored) {}
        return list;
    }

    private List<User> parseUsers(String json) {
        List<User> list = new ArrayList<>();
        try {
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();
                User u = new User(s(o,"id"), s(o,"firstname"), s(o,"lastname"),
                        s(o,"email"), s(o,"username"), s(o,"birthdate"),
                        s(o,"avatar_url"), s(o,"bio"),
                        n(o,"followers"), n(o,"following"), n(o,"posts"),
                        o.has("verified") && !o.get("verified").isJsonNull()
                                && o.get("verified").getAsBoolean());
                list.add(u);
            }
        } catch (Exception ignored) {}
        return list;
    }

    private List<Comment> parseComments(String json) {
        List<Comment> list = new ArrayList<>();
        try {
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();
                Comment c = new Comment();
                c.setId(s(o,"id")); c.setPostId(s(o,"post_id"));
                c.setUserId(s(o,"user_id")); c.setContent(s(o,"content"));
                c.setCreatedAt(s(o,"created_at"));
                if (o.has("users") && !o.get("users").isJsonNull()) {
                    JsonObject u = o.getAsJsonObject("users");
                    c.setAuthorUsername(s(u,"username"));
                    c.setAuthorAvatar(s(u,"avatar_url"));
                }
                list.add(c);
            }
        } catch (Exception ignored) {}
        return list;
    }

    private List<Message> parseMessages(String json) {
        List<Message> list = new ArrayList<>();
        try {
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();
                Message m = new Message();
                m.setId(s(o,"id")); m.setConversationId(s(o,"conversation_id"));
                m.setSenderId(s(o,"sender_id")); m.setContent(s(o,"content"));
                m.setMediaUrl(s(o,"media_url")); m.setCreatedAt(s(o,"created_at"));
                m.setRead(o.has("is_read") && !o.get("is_read").isJsonNull()
                        && o.get("is_read").getAsBoolean());
                if (o.has("sender") && !o.get("sender").isJsonNull()) {
                    JsonObject u = o.getAsJsonObject("sender");
                    m.setSenderUsername(s(u,"username"));
                    m.setSenderAvatar(s(u,"avatar_url"));
                }
                list.add(m);
            }
        } catch (Exception ignored) {}
        return list;
    }

    private List<Notification> parseNotifications(String json) {
        List<Notification> list = new ArrayList<>();
        try {
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();
                Notification n = new Notification();
                n.setId(s(o,"id")); n.setUserId(s(o,"user_id"));
                n.setSenderId(s(o,"sender_id")); n.setType(s(o,"type"));
                n.setPostId(s(o,"post_id")); n.setCreatedAt(s(o,"created_at"));
                n.setRead(o.has("is_read") && !o.get("is_read").isJsonNull()
                        && o.get("is_read").getAsBoolean());
                if (o.has("sender") && !o.get("sender").isJsonNull()) {
                    JsonObject u = o.getAsJsonObject("sender");
                    n.setSenderUsername(s(u,"username"));
                    n.setSenderAvatar(s(u,"avatar_url"));
                }
                list.add(n);
            }
        } catch (Exception ignored) {}
        return list;
    }

    private List<Conversation> parseConversations(String json) {
        List<Conversation> list = new ArrayList<>();
        try {
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();
                Conversation c = new Conversation();
                c.setId(s(o,"id")); c.setUser1Id(s(o,"user1_id"));
                c.setUser2Id(s(o,"user2_id")); c.setLastMessage(s(o,"last_message"));
                c.setUpdatedAt(s(o,"updated_at")); c.setUnreadCount(n(o,"unread_count"));
                list.add(c);
            }
        } catch (Exception ignored) {}
        return list;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Request get(String endpoint) {
        return new Request.Builder()
                .url(SupabaseConfig.REST_URL + endpoint)
                .get()
                .build();
    }

    private String s(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }

    private int n(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : 0;
    }
}
