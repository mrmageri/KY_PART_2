package config;

/**
 * Signals that the user-supplied configuration is invalid.
 */
public class ConfigException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception with the given message.
     *
     * @param message error description
     */
    public ConfigException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param message error description
     * @param cause underlying cause
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}