package uk.nhs.ciao.dts;

public enum AddressType {
	/**
	 * SMTP addressing
	 * <p>
	 * Only the SMTP address fields are required
	 */
	SMTP,
	
	/**
	 * DTS name addressing
	 * <p>
	 * Only the DTS address fields are required
	 */
	DTS,
	
	/**
	 * Used by DTS Server when sending to DTS Client. All address fields will be completed
	 */
	ALL;
}
