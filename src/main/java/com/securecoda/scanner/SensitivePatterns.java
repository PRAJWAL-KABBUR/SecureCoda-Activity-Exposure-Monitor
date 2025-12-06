package com.securecoda.scanner;
import java.util.List;
import java.util.regex.Pattern;

public class SensitivePatterns {
    public static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}");
    public static final Pattern SSN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    public static final Pattern CREDIT_CARD = Pattern.compile("\\b(?:\\d[ -]*?){13,19}\\b");
    public static final Pattern PASSWORD = Pattern.compile("(?i)(password|pwd|pass|secret|key)\\s*[:=]\\s*\\S+");
    public static final Pattern API_KEY = Pattern.compile("(?i)(api[_-]?key|token|secret|bearer)[\"'=:\\s]+[a-zA-Z0-9._\\-]{8,}");
    public static final List<Pattern> ALL = List.of(EMAIL, SSN, CREDIT_CARD, PASSWORD, API_KEY);
}
