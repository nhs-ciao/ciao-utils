package uk.nhs.ciao.spine.pds.hl7;

import java.util.Date;

import uk.nhs.ciao.spine.pds.model.SimpleTrace;
import uk.nhs.interoperability.payloads.DateValue;
import uk.nhs.interoperability.payloads.commontypes.SMSPPersonName;
import uk.nhs.interoperability.payloads.spine.SpineSOAP;
import uk.nhs.interoperability.payloads.spine.SpineSOAPBody;
import uk.nhs.interoperability.payloads.spine.SpineSOAPSimpleTraceBody;
import uk.nhs.interoperability.payloads.util.CDAUUID;
import uk.nhs.interoperability.payloads.vocabularies.generated.HL7StandardVersionCode;
import uk.nhs.interoperability.payloads.vocabularies.generated.ProcessingID;
import uk.nhs.interoperability.payloads.vocabularies.generated.ProcessingMode;
import uk.nhs.interoperability.payloads.vocabularies.internal.DatePrecision;
import uk.nhs.interoperability.payloads.vocabularies.internal.PersonNameType;

public class HL7PayloadBuilder {
	
	/**
	 * This method will validate the request parameters provided
	 * Get by NHS Number:
	 * REQ-PDSMS-4.5.1
	 * REQ-PDSMS-4.5.2
	 * REQ-PDSMS-4.5.3
	 * Get by search:
	 * REQ-PDSMS-4.6.1
	 * REQ-PDSMS-4.6.2
	 * @return true if request is valid
	 */
	public static boolean validateRequest() {
		// TODO: Implement validation
		return true;
	}
	
	private String senderASID;
	private String receiverASID;
	private String pdsURL;
	private String fromAddress;
	
	public void setSenderASID(final String senderASID) {
		this.senderASID = senderASID;
	}
	
	public void setReceiverASID(final String receiverASID) {
		this.receiverASID = receiverASID;
	}
	
	public void setPdsURL(final String pdsURL) {
		this.pdsURL = pdsURL;
	}
	
	public void setFromAddress(final String fromAddress) {
		this.fromAddress = fromAddress;
	}
	
	public String buildSimpleTrace(final SimpleTrace simpleTrace) throws Exception {
		final SpineSOAPBody body = createSOAPBody(simpleTrace, senderASID, receiverASID);
		return buildSOAPRequest(senderASID, receiverASID, pdsURL, fromAddress, body);
	}
	
	// TODO: Kept for backwards compatibility (for now)
	public String buildSimpleTrace(final String surname, final String gender, final String dateOfBirth,
			final String senderASID, final String receiverASID, final String pdsURL,
			final String fromAddress) throws Exception {
		
		final SimpleTrace query = new SimpleTrace();
		query.setSurname(surname);
		query.setGender(gender);
		query.setDateOfBirth(dateOfBirth);
		
		final SpineSOAPBody body = createSOAPBody(query, senderASID, receiverASID);
		return buildSOAPRequest(senderASID, receiverASID, pdsURL, fromAddress, body);
	}
	
	// TODO: Replace existing calls to buildSimpleTrace + params with the SimpleTraceQuery method - the static methods can then use member variables
	
	private static String buildSOAPRequest(final String senderASID, final String receiverASID,
			final String pdsURL, final String fromAddress, final SpineSOAPBody body) {
		final SpineSOAP template = createSpineSOAP(senderASID, receiverASID, pdsURL, fromAddress);
		template.setPayload(body);
		
		return template.serialise();
	}

	private static SpineSOAP createSpineSOAP(final String senderASID, final String receiverASID,
			final String pdsURL, final String fromAddress) {
		final SpineSOAP template = new SpineSOAP();

		template.setMessageID("uuid:"+CDAUUID.generateUUIDString());
		template.setAction("urn:nhs:names:services:pdsquery/QUPA_IN000005UK01");
		template.setTo(pdsURL);
		template.setFrom(fromAddress);
		template.setReceiverASID(receiverASID);
		template.setSenderASID(senderASID);
		template.setReplyAddress(fromAddress);
		
		return template;
	}

	private static SpineSOAPBody createSOAPBody(final SimpleTrace query, final String senderASID, final String receiverASID) {
		final SpineSOAPSimpleTraceBody body = new SpineSOAPSimpleTraceBody();
		
		// Transmission Wrapper Fields (Send Message Payload)
		//body.setTransmissionID(messageUUID);
		body.setTransmissionID(CDAUUID.generateUUIDString());
		body.setTransmissionCreationTime(new DateValue(new Date(), DatePrecision.Seconds));
		body.setTransmissionHL7VersionCode(HL7StandardVersionCode._V3NPfIT30);
		body.setTransmissionInteractionID("QUPA_IN000005UK01");
		body.setTransmissionProcessingCode(ProcessingID._Production);
		body.setTransmissionProcessingModeCode(ProcessingMode._Currentprocessing);
		body.setTransmissionReceiverASID(receiverASID);
		body.setTransmissionSenderASID(senderASID);
		
		// Control Act Wrapper Fields
		body.setControlActSenderASID(senderASID);
		
		// Actual Query Payload
		//body.setGender(Sex._Female.code);
		body.setGender(query.getGenderCode());
		
		//body.setDateOfBirth(new DateValue("19661111"));
		body.setDateOfBirth(query.getDateOfBirth());
		
		// Add provided parameters for query
		if (query.getSurname() != null) {
			body.setName(new SMSPPersonName()
								.setFamilyName(query.getSurname()));
			body.setNameType(PersonNameType.Legal.code);
		}
		//body.setPostcode("LS11AB");
		//body.setAddressUse(AddressType.Home.code);

		return body;
	}
}
