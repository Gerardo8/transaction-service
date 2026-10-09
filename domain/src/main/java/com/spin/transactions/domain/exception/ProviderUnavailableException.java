package com.spin.transactions.domain.exception;

/**
 * Indicates that the service could not obtain a reliable response from the provider.
 */
public class ProviderUnavailableException extends RuntimeException {
    /** Stable code stored with the failed transaction. */
    private final String code;

    /**
     * Creates a provider failure retaining a stable code and underlying cause.
     *
     * @param code stable error code used when recording the failure
     * @param message diagnostic summary
     * @param cause underlying transport or circuit-breaker error, if present
     */
    public ProviderUnavailableException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Returns the stable provider failure code.
     *
     * @return provider failure code
     */
    public String code() {
        return code;
    }
}
