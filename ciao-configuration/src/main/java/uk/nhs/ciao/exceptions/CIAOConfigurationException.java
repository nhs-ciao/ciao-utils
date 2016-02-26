package uk.nhs.ciao.exceptions;

/**
 * Exceptions relating to CIAO Configuration
 * @author Adam Hatherly
 */
public class CIAOConfigurationException extends Exception {
	private static final long serialVersionUID = 5658266706837234405L;
	/**
	 * Initialise exception with a message
	 * @param message Message
	 */
	public CIAOConfigurationException(String message) {
		super(message);
	}
	/**
	 * Wrap an exception
	 * @param e
	 */
	public CIAOConfigurationException(Exception e) {
		super(e);
	}
	/**
	 * Initialise exception with a message and cause
	 * @param message Message
	 * @param cause Cause
	 */
	public CIAOConfigurationException(final String message, final Exception cause) {
		super(message, cause);
	}
}
