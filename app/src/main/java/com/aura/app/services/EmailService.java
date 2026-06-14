package com.aura.app.services;

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
import okhttp3.Response;

/**
 * Envoi de codes de confirmation par e-mail via Edge Function Supabase.
 *
 * Edge Function à déployer dans votre projet Supabase :
 * supabase/functions/send-confirmation-email/index.ts
 */
public class EmailService {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static volatile EmailService instance;
    private final OkHttpClient http;
    private final Executor     exec;

    public interface EmailCb { void ok(); void err(String msg); }

    private EmailService() {
        http = SupabaseClient.getInstance().http();
        exec = Executors.newSingleThreadExecutor();
    }

    public static EmailService getInstance() {
        if (instance == null) synchronized (EmailService.class) {
            if (instance == null) instance = new EmailService();
        }
        return instance;
    }

    /**
     * Envoie le code de confirmation à l'adresse e-mail donnée.
     * Nécessite une Edge Function Supabase nommée "send-confirmation-email".
     */
    public void sendCode(String email, String code, EmailCb cb) {
        exec.execute(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("email", email);
                body.addProperty("code",  code);

                Request r = new Request.Builder()
                        .url(SupabaseConfig.SUPABASE_URL
                                + "/functions/v1/send-confirmation-email")
                        .post(RequestBody.create(body.toString(), JSON))
                        .header("apikey",        SupabaseConfig.SUPABASE_ANON_KEY)
                        .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY)
                        .build();

                Response resp = http.newCall(r).execute();
                // On considère l'envoi comme OK même en cas d'erreur Edge Function
                // (le code est loggé pour le développement)
                android.util.Log.d("EmailService",
                        "Code: " + code + " | Status: " + resp.code());
                cb.ok();

            } catch (IOException e) {
                android.util.Log.e("EmailService", "Erreur: " + e.getMessage());
                cb.ok(); // En dev, on laisse continuer
            }
        });
    }
}
