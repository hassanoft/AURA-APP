package com.aura.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.aura.app.supabase.SupabaseConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/** Service WebSocket Supabase Realtime — messages et notifications temps réel */
public class RealtimeService extends Service {

    private static final String TAG = "RealtimeService";

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public RealtimeService getService() { return RealtimeService.this; }
    }

    @Override
    public IBinder onBind(Intent intent) { return binder; }

    // ─── Listener ─────────────────────────────────────────────────────────────

    public interface RealtimeCb {
        void onMessage(String convId, String payload);
        void onNotification(String payload);
    }

    private final List<RealtimeCb> listeners = new ArrayList<>();

    // ─── WebSocket ────────────────────────────────────────────────────────────

    private OkHttpClient wsClient;
    private WebSocket    ws;
    private boolean      connected;

    @Override
    public void onCreate() {
        super.onCreate();
        wsClient = new OkHttpClient.Builder()
                .pingInterval(25, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connect();
        return START_STICKY;
    }

    public void connect() {
        if (connected) return;
        String url = SupabaseConfig.REALTIME_URL
                + "?apikey=" + SupabaseConfig.SUPABASE_ANON_KEY;

        Request r = new Request.Builder()
                .url(url)
                .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .build();

        ws = wsClient.newWebSocket(r, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket socket, Response response) {
                connected = true;
                Log.d(TAG, "Connecté à Supabase Realtime");
                // Abonnement aux tables
                socket.send("{\"event\":\"phx_join\",\"topic\":\"realtime:public:messages\",\"payload\":{},\"ref\":\"1\"}");
                socket.send("{\"event\":\"phx_join\",\"topic\":\"realtime:public:notifications\",\"payload\":{},\"ref\":\"2\"}");
            }

            @Override
            public void onMessage(WebSocket socket, String text) {
                dispatch(text);
            }

            @Override
            public void onFailure(WebSocket socket, Throwable t, Response response) {
                connected = false;
                Log.w(TAG, "Déconnecté, reconnexion dans 5s…");
                // Reconnexion automatique
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .postDelayed(() -> connect(), 5000);
            }

            @Override
            public void onClosed(WebSocket socket, int code, String reason) {
                connected = false;
            }
        });
    }

    private void dispatch(String payload) {
        for (RealtimeCb cb : listeners) {
            if (payload.contains("\"table\":\"messages\"")) {
                cb.onMessage("", payload);
            } else if (payload.contains("\"table\":\"notifications\"")) {
                cb.onNotification(payload);
            }
        }
    }

    public void addListener(RealtimeCb cb)    { if (!listeners.contains(cb)) listeners.add(cb); }
    public void removeListener(RealtimeCb cb) { listeners.remove(cb); }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ws != null) ws.close(1000, "Service arrêté");
    }
}
