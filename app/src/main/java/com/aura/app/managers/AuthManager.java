package com.aura.app.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.aura.app.AuraApplication;
import com.aura.app.models.User;
import com.aura.app.supabase.SupabaseClient;
import com.aura.app.supabase.SupabaseConfig;
import com.aura.app.utils.PasswordUtils;
import com.aura.app.utils.UsernameGenerator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Gestionnaire d'authentification AURA via Supabase Auth REST API.
 * Gère : inscription wizard, connexion, déconnexion, vérification email.
 */
public class AuthManager {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String PREFS      = "aura_session";
    private static final String KEY_USER   = "current_user_json";

    private static volatile AuthManager instance;
    private final OkHttpClient http;
    private final Gson         gson;
    private final Executor     exec;

    // ─── Callbacks ────────────────────────────────────────────────────────────

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String msg);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String msg);
    }

    public interface BoolCallback {
        void onResult(boolean value);
    }

    // ─── Singleton ────────────────────────────────────────────────────────────

    private AuthManager() {
        http = SupabaseClient.getInstance().http();
        gson = new Gson();
        exec = Executors.newCachedThreadPool();
    }

    public static AuthManager getInstance() {
        if (instance == null) synchronized (AuthManager.class) {
            if (instance == null) instance = new AuthManager();
        }
        return instance;
    }

    // ─── Inscription ──────────────────────────────────────────────────────────

    /**
     * Inscrit un nouvel utilisateur.
     * 1. Crée le compte Auth Supabase
     * 2. Insère le profil dans la table users
     */
    public void signUp(String firstName, String lastName, String email,
                       String password, String birthDate, AuthCallback cb) {
        exec.execute(() -> {
            try {
                // Étape 1 — Auth signup
                JsonObject meta = new JsonObject();
                meta.addProperty("firstname", firstName);
                meta.addProperty("lastname",  lastName);
                meta.addProperty("birthdate", birthDate);

                JsonObject body = new JsonObject();
                body.addProperty("email",    email);
                body.addProperty("password", password);
                body.add("data", meta);

                Request req = new Request.Builder()
                        .url(SupabaseConfig.AUTH_URL + "signup")
                        .post(RequestBody.create(body.toString(), JSON))
                        .header("apikey",       SupabaseConfig.SUPABASE_ANON_KEY)
                        .header("Content-Type", "application/json")
                        .build();

                Response resp = http.newCall(req).execute();
                String   raw  = resp.body() != null ? resp.body().string() : "";

                if (!resp.isSuccessful()) { cb.onError(extractError(raw)); return; }

                JsonObject authObj = JsonParser.parseString(raw).getAsJsonObject();
                String userId = authObj.get("id").getAsString();
                String token  = authObj.has("access_token")
                        ? authObj.get("access_token").getAsString() : null;

                if (token != null) SupabaseClient.saveSession(token, userId);

                // Étape 2 — Génération username unique
                String username = ensureUnique(UsernameGenerator.generate(firstName, lastName));

                // Étape 3 — Hash BCrypt
                String pwdHash = PasswordUtils.hash(password);

                // Étape 4 — Insertion profil users
                JsonObject profile = new JsonObject();
                profile.addProperty("id",            userId);
                profile.addProperty("firstname",     firstName);
                profile.addProperty("lastname",      lastName);
                profile.addProperty("email",         email);
                profile.addProperty("birthdate",     birthDate);
                profile.addProperty("password_hash", pwdHash);
                profile.addProperty("username",      username);
                profile.addProperty("avatar_url",    "");
                profile.addProperty("bio",           "");
                profile.addProperty("followers",     0);
                profile.addProperty("following",     0);
                profile.addProperty("posts",         0);
                profile.addProperty("verified",      false);

                String authHeader = token != null
                        ? "Bearer " + token
                        : "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY;

                Request ins = new Request.Builder()
                        .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_USERS)
                        .post(RequestBody.create(profile.toString(), JSON))
                        .header("apikey",        SupabaseConfig.SUPABASE_ANON_KEY)
                        .header("Authorization", authHeader)
                        .header("Content-Type",  "application/json")
                        .header("Prefer",        "return=representation")
                        .build();

                Response insResp = http.newCall(ins).execute();
                if (!insResp.isSuccessful()) {
                    String e = insResp.body() != null ? insResp.body().string() : "";
                    cb.onError(extractError(e));
                    return;
                }

                User user = new User(userId, firstName, lastName, email,
                        username, birthDate, "", "", 0, 0, 0, false);
                saveUser(user);
                cb.onSuccess(user);

            } catch (IOException e) {
                cb.onError("Erreur réseau : " + e.getMessage());
            }
        });
    }

    // ─── Connexion ────────────────────────────────────────────────────────────

    public void signIn(String email, String password, AuthCallback cb) {
        exec.execute(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("email",    email);
                body.addProperty("password", password);

                Request req = new Request.Builder()
                        .url(SupabaseConfig.AUTH_URL + "token?grant_type=password")
                        .post(RequestBody.create(body.toString(), JSON))
                        .header("apikey",       SupabaseConfig.SUPABASE_ANON_KEY)
                        .header("Content-Type", "application/json")
                        .build();

                Response resp = http.newCall(req).execute();
                String   raw  = resp.body() != null ? resp.body().string() : "";

                if (!resp.isSuccessful()) { cb.onError("E-mail ou mot de passe incorrect."); return; }

                JsonObject obj    = JsonParser.parseString(raw).getAsJsonObject();
                String     token  = obj.get("access_token").getAsString();
                String     userId = obj.getAsJsonObject("user").get("id").getAsString();

                SupabaseClient.saveSession(token, userId);
                fetchProfile(userId, token, cb);

            } catch (IOException e) {
                cb.onError("Erreur réseau : " + e.getMessage());
            }
        });
    }

    // ─── Déconnexion ──────────────────────────────────────────────────────────

    public void signOut(SimpleCallback cb) {
        exec.execute(() -> {
            try {
                Request req = new Request.Builder()
                        .url(SupabaseConfig.AUTH_URL + "logout")
                        .post(RequestBody.create("{}", JSON))
                        .build();
                http.newCall(req).execute();
            } catch (IOException ignored) {}
            SupabaseClient.clearSession();
            clearUser();
            cb.onSuccess();
        });
    }

    // ─── Vérification email unique ────────────────────────────────────────────

    public void isEmailAvailable(String email, BoolCallback cb) {
        exec.execute(() -> {
            try {
                Request r = new Request.Builder()
                        .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_USERS
                                + "?email=eq." + email + "&select=id")
                        .get()
                        .header("apikey",        SupabaseConfig.SUPABASE_ANON_KEY)
                        .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY)
                        .build();
                Response resp = http.newCall(r).execute();
                String   body = resp.body() != null ? resp.body().string() : "[]";
                cb.onResult(body.trim().equals("[]"));
            } catch (IOException e) {
                cb.onResult(true);
            }
        });
    }

    // ─── Profil courant ───────────────────────────────────────────────────────

    public User getCurrentUser() {
        String json = AuraApplication.getAppContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_USER, null);
        return json != null ? gson.fromJson(json, User.class) : null;
    }

    public void refreshCurrentUser(AuthCallback cb) {
        String uid   = SupabaseClient.getSavedUserId();
        String token = SupabaseClient.getToken();
        if (uid == null) { cb.onError("Non connecté"); return; }
        exec.execute(() -> {
            try { fetchProfile(uid, token, cb); }
            catch (IOException e) { cb.onError(e.getMessage()); }
        });
    }

    // ─── Privé ────────────────────────────────────────────────────────────────

    private void fetchProfile(String uid, String token, AuthCallback cb) throws IOException {
        Request r = new Request.Builder()
                .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_USERS
                        + "?id=eq." + uid + "&select=*")
                .get()
                .header("apikey",        SupabaseConfig.SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + token)
                .build();

        Response resp = http.newCall(r).execute();
        String   body = resp.body() != null ? resp.body().string() : "[]";

        if (!resp.isSuccessful() || body.trim().equals("[]")) {
            cb.onError("Profil introuvable.");
            return;
        }

        JsonArray arr = JsonParser.parseString(body).getAsJsonArray();
        JsonObject o  = arr.get(0).getAsJsonObject();

        User user = new User(
                s(o, "id"), s(o, "firstname"), s(o, "lastname"),
                s(o, "email"), s(o, "username"), s(o, "birthdate"),
                s(o, "avatar_url"), s(o, "bio"),
                i(o, "followers"), i(o, "following"), i(o, "posts"),
                o.has("verified") && !o.get("verified").isJsonNull()
                        && o.get("verified").getAsBoolean()
        );
        saveUser(user);
        cb.onSuccess(user);
    }

    private String ensureUnique(String base) throws IOException {
        String candidate = base;
        int    suffix    = 1;
        while (true) {
            Request r = new Request.Builder()
                    .url(SupabaseConfig.REST_URL + SupabaseConfig.TABLE_USERS
                            + "?username=eq." + candidate + "&select=id")
                    .get()
                    .header("apikey",        SupabaseConfig.SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY)
                    .build();
            Response resp = http.newCall(r).execute();
            String   body = resp.body() != null ? resp.body().string() : "[]";
            if (body.trim().equals("[]")) return candidate;
            candidate = base + suffix++;
            if (suffix > 9999) return base + System.currentTimeMillis() % 10000;
        }
    }

    private void saveUser(User u) {
        AuraApplication.getAppContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_USER, gson.toJson(u)).apply();
    }

    private void clearUser() {
        AuraApplication.getAppContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().remove(KEY_USER).apply();
    }

    private String extractError(String json) {
        try {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            if (o.has("msg"))     return o.get("msg").getAsString();
            if (o.has("message")) return o.get("message").getAsString();
            if (o.has("error"))   return o.get("error").getAsString();
        } catch (Exception ignored) {}
        return "Erreur inconnue.";
    }

    private String s(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }

    private int i(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : 0;
    }
}
