package uk.nhs.itk.ciao.exceptions;

/**
 * Exceptions relating to CIAO Configuration
 * @author Adam Hatherly
 */
public class CIAOConfigurationException extends Exception {
	private static final long serialVersionUID = 5658266706837234405L;
	public CIAOConfigurationException(String message) {
		super(message);
	}
	public CIAOConfigurationException(Exception e) {
		super(e);
	}
	public CIAOConfigurationException(final String message, final Exception cause) {
		super(message, cause);
	}
}
