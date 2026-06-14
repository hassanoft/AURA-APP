package com.aura.app.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

/** Hachage et validation des mots de passe (BCrypt). */
public final class PasswordUtils {

    private static final int COST = 12;
    private PasswordUtils() {}

    public static String hash(String plain) {
        return BCrypt.withDefaults().hashToString(COST, plain.toCharArray());
    }

    public static boolean verify(String plain, String hashed) {
        return BCrypt.verifyer().verify(plain.toCharArray(), hashed).verified;
    }

    /**
     * Valide le mot de passe selon les règles AURA.
     * @return null si valide, message d'erreur sinon
     */
    public static String validate(String p) {
        if (p == null || p.length() < 8)    return "Minimum 8 caractères requis.";
        if (!p.matches(".*[A-Z].*"))         return "Au moins une majuscule requise.";
        if (!p.matches(".*[a-z].*"))         return "Au moins une minuscule requise.";
        if (!p.matches(".*[0-9].*"))         return "Au moins un chiffre requis.";
        return null;
    }

    /** Score de force 0-4 */
    public static int strength(String p) {
        if (p == null || p.isEmpty()) return 0;
        int s = 0;
        if (p.length() >= 8)  s++;
        if (p.length() >= 12) s++;
        if (p.matches(".*[A-Z].*") && p.matches(".*[a-z].*")) s++;
        if (p.matches(".*[0-9].*")) s++;
        return Math.min(s, 4);
    }
}
