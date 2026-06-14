package com.aura.app.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.aura.app.AuraApplication;
import com.aura.app.R;
import com.aura.app.activities.MainActivity;
import com.aura.app.models.Notification;

/**
 * Gestionnaire de notifications push locales AURA.
 *
 * <p>Affiche une notification système lorsqu'un événement survient
 * (like, commentaire, abonnement, mention, nouveau message).
 */
public final class NotificationHelper {

    private NotificationHelper() {}

    /**
     * Affiche une notification système pour un événement AURA.
     *
     * @param context Contexte applicatif
     * @param notif   Notification reçue depuis Supabase Realtime
     */
    public static void show(Context context, Notification notif) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notif.getId().hashCode(), intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, AuraApplication.NOTIF_CHANNEL_ID)
                .setSmallIcon(R.drawable.aura_logo)
                .setContentTitle("AURA")
                .setContentText(notif.getDisplayText())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        try {
            manager.notify(notif.getId().hashCode(), builder.build());
        } catch (SecurityException ignored) {
            // Permission POST_NOTIFICATIONS non accordée
        }
    }

    /**
     * Affiche une notification pour un nouveau message privé.
     *
     * @param context  Contexte applicatif
     * @param fromUser Nom d'utilisateur de l'expéditeur
     * @param message  Contenu du message
     * @param convId   ID de la conversation
     */
    public static void showNewMessage(Context context, String fromUser,
                                       String message, String convId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, convId.hashCode(), intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, AuraApplication.NOTIF_CHANNEL_ID)
                .setSmallIcon(R.drawable.aura_logo)
                .setContentTitle("@" + fromUser)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        try {
            manager.notify(convId.hashCode(), builder.build());
        } catch (SecurityException ignored) {}
    }

    /** Efface toutes les notifications AURA. */
    public static void clearAll(Context context) {
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.cancelAll();
    }
}
