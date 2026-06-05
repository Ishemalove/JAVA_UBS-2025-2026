package rw.utility.billing.dto;

public final class ValidationPatterns {
    private ValidationPatterns() {}
    public static final String LOWERCASE_EMAIL = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$";
    public static final String PHONE = "^\\+?[0-9]{10,15}$";
    public static final String STRONG_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";
}
