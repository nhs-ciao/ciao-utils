package uk.nhs.ciao.spine.sds.query;

import uk.nhs.ciao.spine.sds.ldap.LdapConnection;
import uk.nhs.ciao.spine.sds.ldap.LdapFilter;
import uk.nhs.ciao.spine.sds.ldap.LdapQuery;
import uk.nhs.ciao.spine.sds.mapper.MessageHandlingServiceMapper;
import uk.nhs.ciao.spine.sds.model.MessageHandlingService;

public class MessageHandlingServiceQuery extends SDSQuery<MessageHandlingService> {
	private String uniqueIdentifier;
	private String nhsMhsSvcIA;
	private String nhsMhsCPAId;
	private String nhsIDCode;
	private String nhsMHSPartyKey;
	private String nhsDateApproved;
	
	public MessageHandlingServiceQuery(final LdapConnection connection) {
		super(connection, MessageHandlingServiceMapper.getInstance());
	}
	
	@Override
	public void reset() {
		this.uniqueIdentifier = null;
		this.nhsMhsSvcIA = null;
		this.nhsMhsCPAId = null;
		this.nhsIDCode = null;
		this.nhsMHSPartyKey = null;
		this.nhsDateApproved = null;
	}
	
	public MessageHandlingServiceQuery withUniqueIdentifier(final String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
		return this;
	}
	
	public MessageHandlingServiceQuery withNhsMhsSvcIA(final String nhsMhsSvcIA) {
		this.nhsMhsSvcIA = nhsMhsSvcIA;
		return this;
	}

	public MessageHandlingServiceQuery withNhsMhsCPAId(final String nhsMhsCPAId) {
		this.nhsMhsCPAId = nhsMhsCPAId;
		return this;
	}

	public MessageHandlingServiceQuery withNhsIDCode(final String nhsIDCode) {
		this.nhsIDCode = nhsIDCode;
		return this;
	}

	public MessageHandlingServiceQuery withNhsMHSPartyKey(final String nhsMHSPartyKey) {
		this.nhsMHSPartyKey = nhsMHSPartyKey;
		return this;
	}
	
	public MessageHandlingServiceQuery withNhsDateApproved(final String nhsDateApproved) {
		this.nhsDateApproved = nhsDateApproved;
		return this;
	}
	
	@Override
	protected LdapQuery getLdapQuery() {
		final String name = "ou=Services, o=nhs";
		
		final LdapFilter filter = new LdapFilter()
			.add("objectclass", "nhsMhs")
			.add("uniqueIdentifier", uniqueIdentifier)
			.add("nhsMhsSvcIA", nhsMhsSvcIA)
			.add("nhsMhsCPAId", nhsMhsCPAId)
			.add("nhsIDCode", nhsIDCode)
			.add("nhsMHSPartyKey", nhsMHSPartyKey)
			.add("nhsDateApproved", nhsDateApproved);
		
		final String[] attributeNames = new String[]{
				"uniqueIdentifier", "nhsMhsSvcIA", "nhsMhsCPAId", "nhsIDCode", "nhsMHSPartyKey", "nhsDateApproved"};
		
		return new LdapQuery(name, filter, attributeNames);
	}
}