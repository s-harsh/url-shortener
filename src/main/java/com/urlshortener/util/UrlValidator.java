package com.urlshortener.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public final class UrlValidator {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final int MAX_URL_LENGTH = 2048;

    private UrlValidator() {}

    public static boolean isValid(String url) {
        if (url == null || url.isBlank() || url.length() > MAX_URL_LENGTH) return false;
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            return scheme != null
                    && ALLOWED_SCHEMES.contains(scheme.toLowerCase())
                    && uri.getHost() != null
                    && !uri.getHost().isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static boolean isSafeAlias(String alias) {
        return alias != null && alias.matches("^[a-zA-Z0-9-]{3,20}$");
    }
}
