package uk.nhs.ciao.spine.pds;

import java.io.IOException;

/**
 * Signals that an exception of some sort has occurred related to PDS.
 */
public class PDSException extends IOException {
	private static final long serialVersionUID = 2390382238506150678L;

	/**
	 * Constructs a new exception
	 */
	public PDSException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified message and cause
	 * 
	 * @param message The detail message
	 * @param cause The cause of the exception
	 */
	public PDSException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified message
	 * 
	 * @param message The detail message
	 */
	public PDSException(final String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified cause and a message derived from
	 * the cause.
	 * 
	 * @param cause The cause of the exception
	 */
	public PDSException(final Throwable cause) {
		super(cause);
	}
}
