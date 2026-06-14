package com.aura.app.utils;

import java.text.Normalizer;

/** Génère un @username à partir du prénom + nom. Ex: Hassan Sougue → hassansougue */
public final class UsernameGenerator {

    private UsernameGenerator() {}

    public static String generate(String first, String last) {
        String raw = (first.trim() + last.trim()).toLowerCase();
        // Supprime les accents
        raw = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        // Garde uniquement lettres et chiffres
        raw = raw.replaceAll("[^a-z0-9]", "");
        if (raw.length() > 20) raw = raw.substring(0, 20);
        return raw.isEmpty() ? "user" + System.currentTimeMillis() : raw;
    }
}
