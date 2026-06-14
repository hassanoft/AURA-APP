package com.aura.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Utilitaires de formatage de dates et heures. */
public final class TimeUtils {

    private TimeUtils() {}

    /**
     * Retourne un libellé relatif lisible.
     * Ex : "Il y a 2 min", "Il y a 3h", "Hier", "12 jan."
     */
    public static String relative(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isEmpty()) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(isoTimestamp);
            if (date == null) return "";

            long diff = System.currentTimeMillis() - date.getTime();
            long mins  = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days  = TimeUnit.MILLISECONDS.toDays(diff);

            if (mins  < 1)   return "À l'instant";
            if (mins  < 60)  return "Il y a " + mins + " min";
            if (hours < 24)  return "Il y a " + hours + "h";
            if (days  < 2)   return "Hier";
            if (days  < 7)   return "Il y a " + days + " jours";

            SimpleDateFormat out = new SimpleDateFormat("d MMM", Locale.FRENCH);
            return out.format(date);

        } catch (ParseException e) {
            return "";
        }
    }

    /** Formate en "HH:mm" */
    public static String toTime(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isEmpty()) return "";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date d = in.parse(isoTimestamp);
            return d != null ? out.format(d) : "";
        } catch (ParseException e) {
            return "";
        }
    }
}
