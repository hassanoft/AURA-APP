package com.aura.app.utils;

import java.util.Calendar;

/**
 * Validation de l'âge lors de l'inscription.
 * Utilise java.util.Calendar pour compatibilité maximale (API 26+).
 */
public final class AgeValidator {

    public static final int MIN_AGE = 15;
    public static final int MAX_AGE = 100;

    public enum Result { VALID, TOO_YOUNG, TOO_OLD, INVALID }

    private AgeValidator() {}

    public static Result validate(int day, int month, int year) {
        try {
            Calendar birth = Calendar.getInstance();
            birth.setLenient(false);
            birth.set(year, month - 1, day);
            // Force le calcul pour détecter les dates invalides
            birth.getTime();

            Calendar today = Calendar.getInstance();
            if (birth.after(today)) return Result.INVALID;

            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--;

            if (age < MIN_AGE) return Result.TOO_YOUNG;
            if (age > MAX_AGE) return Result.TOO_OLD;
            return Result.VALID;

        } catch (Exception e) {
            return Result.INVALID;
        }
    }

    /** Formate en ISO-8601 : AAAA-MM-JJ */
    public static String toIso(int day, int month, int year) {
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    /** Calcule l'âge depuis une date ISO */
    public static int ageFrom(String iso) {
        try {
            String[] p = iso.split("-");
            int year  = Integer.parseInt(p[0]);
            int month = Integer.parseInt(p[1]);
            int day   = Integer.parseInt(p[2]);
            Calendar birth = Calendar.getInstance();
            birth.set(year, month - 1, day);
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--;
            return age;
        } catch (Exception e) {
            return 0;
        }
    }
}
