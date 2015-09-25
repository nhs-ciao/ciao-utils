package uk.nhs.ciao.dts;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="DTSControl")
public class ControlFile {
	/**
	 * Version of Control File
	 */
	// 1.0
	@XmlElement(name="Version")
	private String version;
	
	/**
	 * Identifies the type of Address.
	 */
	@XmlElement(name="AddressType")
	private AddressType addressType;
	
	/**
	 * Identifies the type of Transfer
	 * <p>
	 * The OUT Transaction will have a data file and a control file.
	 */
	@XmlElement(name="MessageType")
	private MessageType messageType;
	
	/**
	 * Optional identifier for the workflow that the Data Transfer is part of, e.g. IOS.
	 * <p>
	 * This may be used for reporting and to define processing at the DTS Server. 
	 */
	// Max 32 characters
	@XmlElement(name="WorkflowId")
	private String workflowId;
	
	/**
	 * SMTP address of the originator.
	 */
	@XmlElement(name="From_ESMTP")
	private String fromESMTP;
	
	/**
	 * DTS address of the originator.
	 */
	@XmlElement(name="From_DTS")
	private String fromDTS;
	
	/**
	 * SMTP address of the recipient.
	 */
	@XmlElement(name="To_ESMTP")
	private String toESMTP;
	
	/**
	 * DTS address of the recipient.
	 */
	@XmlElement(name="To_DTS")
	private String toDTS;
	
	/**
	 * Subject of the Data Transfer as for SMTP email subject.
	 */
	@XmlElement(name="Subject")
	private String subject;
	
	/**
	 * Local Identifier of the Data Transfer.
	 * <p>
	 * This is specific to the Host Application sending via the Client.
	 * This will allow for correlation with DTS Ids
	 */
	// Max 255 characters
	@XmlElement(name="LocalId")
	private String localId;
	
	/**
	 * Each Data Transfer will be associated with a DTS Identifier as
	 * it passes through the DTS Server.
	 * <p>
	 * This will allow for correlation with local and partner ids.
	 * <p>
	 * Not applicable in the OUT Transaction
	 */
	// Max 100 characters
	@XmlElement(name="DTSId")
	private String DTSId;
	
	/**
	 * Optional Flag indicating processing to be performed at the DTS Server. 
	 * <p>
	 * For Future use.
	 */
	// Max 32 characters
	@XmlElement(name="ProcessId")
	private String processId;
	
	/**
	 * Optional flag to indicate that the data file can be compressed.
	 */
	@XmlElement(name="Compress")
	private Boolean compress;
	
	/**
	 * Optional flag to indicate that the Data File has been encrypted by the Host Application. 
	 */
	@XmlElement(name="Encrypted")
	private Boolean encrypted;
	
	/**
	 * Field to indicate that the Data file is compressed.
	 * <p>
	 * Not applicable in the OUT Transaction.
	 */
	@XmlElement(name="IsCompressed")
	private Boolean compressed;
	
	/**
	 * Field to be used for a checksum for the Data file. 
	 * <p>
	 * Not applicable in the OUT Transaction.
	 */
	@XmlElement(name="DataCheckSum")
	private String dataChecksum;
	
	/**
	 * If specified, the identifier (or SMTP message id) from received Data Transfers.
	 * <p>
	 * Not applicable in the OUT Transaction
	 */
	// Max 255 characters
	@XmlElement(name="PartnerId")
	private String partnerId;
	
	/**
	 * Not applicable in the OUT Transaction.
	 */
	@XmlElement(name="StatusRecord")
	private StatusRecord statusRecord;

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public AddressType getAddressType() {
		return addressType;
	}

	public void setAddressType(final AddressType addressType) {
		this.addressType = addressType;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(final MessageType messageType) {
		this.messageType = messageType;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(final String workflowId) {
		this.workflowId = workflowId;
	}

	public String getFromESMTP() {
		return fromESMTP;
	}

	public void setFromESMTP(final String fromESMTP) {
		this.fromESMTP = fromESMTP;
	}

	public String getFromDTS() {
		return fromDTS;
	}

	public void setFromDTS(final String fromDTS) {
		this.fromDTS = fromDTS;
	}

	public String getToESMTP() {
		return toESMTP;
	}

	public void setToESMTP(final String toESMTP) {
		this.toESMTP = toESMTP;
	}

	public String getToDTS() {
		return toDTS;
	}

	public void setToDTS(final String toDTS) {
		this.toDTS = toDTS;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	public String getLocalId() {
		return localId;
	}

	public void setLocalId(final String localId) {
		this.localId = localId;
	}

	public String getDTSId() {
		return DTSId;
	}

	public void setDTSId(final String dTSId) {
		DTSId = dTSId;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(final String processId) {
		this.processId = processId;
	}

	public boolean isCompress() {
		return compress == null ? false : compress;
	}

	public void setCompress(final boolean compress) {
		this.compress = compress;
	}

	public boolean getEncrypted() {
		return encrypted == null ? false : encrypted;
	}

	public void setEncrypted(final boolean encrypted) {
		this.encrypted = encrypted;
	}

	public boolean getCompressed() {
		return compressed == null ? false : compressed;
	}

	public void setCompressed(final boolean compressed) {
		this.compressed = compressed;
	}

	public String getDataChecksum() {
		return dataChecksum;
	}

	public void setDataChecksum(final String dataChecksum) {
		this.dataChecksum = dataChecksum;
	}

	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(final String partnerId) {
		this.partnerId = partnerId;
	}

	public StatusRecord getStatusRecord() {
		return statusRecord;
	}
	
	public void setStatusRecord(final StatusRecord statusRecord) {
		this.statusRecord = Preconditions.checkNotNull(statusRecord);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("version", version)
			.add("addressType", addressType)
			.add("messageType", messageType)
			.add("workflowId", workflowId)
			.add("fromESMTP", fromESMTP)
			.add("fromDTS", fromDTS)
			.add("toESMTP", toESMTP)
			.add("toDTS", toDTS)
			.add("subject", subject)
			.add("localId", localId)
			.add("DTSId", DTSId)
			.add("processId", processId)
			.add("compress", compress)
			.add("encrypted", encrypted)
			.add("compressed", compressed)
			.add("dataChecksum", dataChecksum)
			.add("partnerId", partnerId)
			.add("statusRecord", statusRecord)
			.toString();
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(version,
			addressType,
			messageType,
			workflowId,
			fromESMTP,
			fromDTS,
			toESMTP,
			toDTS,
			subject,
			localId,
			DTSId,
			processId,
			compress,
			encrypted,
			compressed,
			dataChecksum,
			partnerId,
			statusRecord);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		final ControlFile other = (ControlFile)obj;
		return Objects.equal(version, other.version) &&
			Objects.equal(addressType, other.addressType) &&
			Objects.equal(messageType, other.messageType) &&
			Objects.equal(workflowId, other.workflowId) &&
			Objects.equal(fromESMTP, other.fromESMTP) &&
			Objects.equal(fromDTS, other.fromDTS) &&
			Objects.equal(toESMTP, other.toESMTP) &&
			Objects.equal(toDTS, other.toDTS) &&
			Objects.equal(subject, other.subject) &&
			Objects.equal(localId, other.localId) &&
			Objects.equal(DTSId, other.DTSId) &&
			Objects.equal(processId, other.processId) &&
			Objects.equal(compress, other.compress) &&
			Objects.equal(encrypted, other.encrypted) &&
			Objects.equal(compressed, other.compressed) &&
			Objects.equal(dataChecksum, other.dataChecksum) &&
			Objects.equal(partnerId, other.partnerId) &&
			Objects.equal(statusRecord, other.statusRecord);
	}
	
	public String toXml() throws JAXBException {
		final StringWriter writer = new StringWriter();
		toXml(writer);
		return writer.toString();
	}
	
	public void toXml(final OutputStream out) throws JAXBException {
		JAXBFactory.createMarshaller().marshal(this, out);
	}
	
	public void toXml(final Writer writer) throws JAXBException {
		JAXBFactory.createMarshaller().marshal(this, writer);
	}
	
	public static ControlFile fromXml(final String text) throws JAXBException {
		final StringReader reader = new StringReader(text);
		return fromXml(reader);
	}
	
	public static ControlFile fromXml(final InputStream in) throws JAXBException {
		final Object result = JAXBFactory.createUnmarshaller().unmarshal(in);
		return (result instanceof ControlFile) ? (ControlFile)result : null;
	}
	
	public static ControlFile fromXml(final Reader reader) throws JAXBException {
		final Object result = JAXBFactory.createUnmarshaller().unmarshal(reader);
		return (result instanceof ControlFile) ? (ControlFile)result : null;
	}
}
