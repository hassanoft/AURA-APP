package com.aura.app.utils;

import java.security.SecureRandom;

/**
 * Génère les codes de confirmation e-mail AURA.
 * 6 caractères : A-Z, a-z, 1-9 (0 exclu pour éviter confusion avec O)
 * Exemples : A7mP2x  Y8Kq4R
 */
public final class CodeGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789";
    private static final int    LEN   = 6;

    private CodeGenerator() {}

    public static String generate() {
        SecureRandom   rng = new SecureRandom();
        StringBuilder  sb  = new StringBuilder(LEN);
        for (int i = 0; i < LEN; i++) sb.append(CHARS.charAt(rng.nextInt(CHARS.length())));
        return sb.toString();
    }
}
