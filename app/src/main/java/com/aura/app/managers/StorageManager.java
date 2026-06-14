package com.aura.app.managers;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.aura.app.AuraApplication;
import com.aura.app.supabase.SupabaseClient;
import com.aura.app.supabase.SupabaseConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/** Upload de fichiers vers Supabase Storage */
public class StorageManager {

    private static volatile StorageManager instance;
    private final OkHttpClient http;
    private final Executor     exec;

    public interface UploadCb {
        void ok(String publicUrl);
        void err(String msg);
    }

    private StorageManager() {
        http = SupabaseClient.getInstance().http();
        exec = Executors.newCachedThreadPool();
    }

    public static StorageManager getInstance() {
        if (instance == null) synchronized (StorageManager.class) {
            if (instance == null) instance = new StorageManager();
        }
        return instance;
    }

    public void uploadAvatar(String userId, Uri uri, UploadCb cb) {
        upload(SupabaseConfig.BUCKET_AVATARS, "avatars/" + userId + ".jpg", uri, cb);
    }

    public void uploadPostMedia(String userId, Uri uri, UploadCb cb) {
        String ext  = getExt(uri);
        String path = "posts/" + userId + "/" + UUID.randomUUID() + "." + ext;
        upload(SupabaseConfig.BUCKET_POSTS, path, uri, cb);
    }

    public void uploadMessageImage(String senderId, Uri uri, UploadCb cb) {
        String path = "messages/" + senderId + "/" + UUID.randomUUID() + ".jpg";
        upload(SupabaseConfig.BUCKET_MESSAGES, path, uri, cb);
    }

    private void upload(String bucket, String path, Uri uri, UploadCb cb) {
        exec.execute(() -> {
            try {
                InputStream is = AuraApplication.getAppContext()
                        .getContentResolver().openInputStream(uri);
                if (is == null) { cb.err("Impossible de lire le fichier."); return; }

                byte[] bytes = is.readAllBytes();
                is.close();

                String mime = AuraApplication.getAppContext()
                        .getContentResolver().getType(uri);
                if (mime == null) mime = "image/jpeg";

                Request r = new Request.Builder()
                        .url(SupabaseConfig.STORAGE_URL + "object/" + bucket + "/" + path)
                        .post(RequestBody.create(bytes, MediaType.parse(mime)))
                        .header("Content-Type", mime)
                        .header("x-upsert",     "true")
                        .build();

                Response resp = http.newCall(r).execute();
                if (!resp.isSuccessful()) {
                    String e = resp.body() != null ? resp.body().string() : "";
                    cb.err("Upload échoué : " + e);
                    return;
                }

                String url = SupabaseConfig.STORAGE_URL
                        + "object/public/" + bucket + "/" + path;
                cb.ok(url);

            } catch (IOException e) {
                cb.err("Erreur réseau : " + e.getMessage());
            }
        });
    }

    private String getExt(Uri uri) {
        String mime = AuraApplication.getAppContext().getContentResolver().getType(uri);
        if (mime == null) return "jpg";
        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
        return ext != null ? ext : "jpg";
    }
}
