package com.viralforge.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PromptSanitizer {
    private static final int MAX_LENGTH = 500;
    private static final String[] DANGEROUS_PATTERNS = {
        "DROP TABLE", "DELETE FROM", "INSERT INTO", "UPDATE SET",
        "UNION SELECT", "OR 1=1", "EXEC", "EXECUTE",
        "<script>", "javascript:", "onclick=", "onerror=",
        "{{", "}}", "${", "interpolation"
    };

    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String sanitized = input.trim();

        // Remove potentially dangerous patterns
        for (String pattern : DANGEROUS_PATTERNS) {
            sanitized = sanitized.replaceAll("(?i)" + java.util.regex.Pattern.quote(pattern), "");
        }

        // Limit length to prevent token bloat
        if (sanitized.length() > MAX_LENGTH) {
            sanitized = sanitized.substring(0, MAX_LENGTH);
            log.warn("Input truncated from {} to {} characters", input.length(), MAX_LENGTH);
        }

        // Remove excessive special characters
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9\\s\\-_.,!?()'\"&]", " ");

        // Normalize whitespace
        sanitized = sanitized.replaceAll("\\s+", " ");

        return sanitized;
    }

    public static boolean isValid(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        if (input.length() > MAX_LENGTH) {
            return false;
        }

        for (String pattern : DANGEROUS_PATTERNS) {
            if (input.toUpperCase().contains(pattern.toUpperCase())) {
                log.warn("Dangerous pattern detected: {}", pattern);
                return false;
            }
        }

        return true;
    }
}
