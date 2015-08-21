package uk.nhs.ciao.spine.sds.query;

import uk.nhs.ciao.spine.sds.ldap.LdapConnection;
import uk.nhs.ciao.spine.sds.ldap.LdapFilter;
import uk.nhs.ciao.spine.sds.ldap.LdapQuery;
import uk.nhs.ciao.spine.sds.mapper.AccreditedSystemMapper;
import uk.nhs.ciao.spine.sds.model.AccreditedSystem;

public class AccreditedSystemQuery extends SDSQuery<AccreditedSystem> {
	private String uniqueIdentifier;
	private String nhsAsSvcIA;
	private String nhsMHSPartyKey;
	private String nhsIDCode;
	private String nhsDateApproved;
	
	public AccreditedSystemQuery(final LdapConnection connection) {
		super(connection, AccreditedSystemMapper.getInstance());
	}
	
	@Override
	public void reset() {
		uniqueIdentifier = null;
		nhsAsSvcIA = null;
		nhsMHSPartyKey = null;
		nhsIDCode = null;
		nhsDateApproved = null;
	}
	
	public AccreditedSystemQuery withUniqueIdentifier(final String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
		return this;
	}
	
	public AccreditedSystemQuery withNhsAsSvcIA(final String nhsAsSvcIA) {
		this.nhsAsSvcIA = nhsAsSvcIA;
		return this;
	}
	
	public AccreditedSystemQuery withNhsMHSPartyKey(final String nhsMHSPartyKey) {
		this.nhsMHSPartyKey = nhsMHSPartyKey;
		return this;
	}
	
	public AccreditedSystemQuery withNhsIDCode(final String nhsIDCode) {
		this.nhsIDCode = nhsIDCode;
		return this;
	}
	
	public AccreditedSystemQuery withNhsDateApproved(final String nhsDateApproved) {
		this.nhsDateApproved = nhsDateApproved;
		return this;
	}
	
	@Override
	protected LdapQuery getLdapQuery() {
		final String name = "ou=Services, o=nhs";
		
		final LdapFilter filter = new LdapFilter()
			.add("objectclass", "nhsAs")
			.add("uniqueIdentifier", uniqueIdentifier)
			.add("nhsAsSvcIA", nhsAsSvcIA)
			.add("nhsMHSPartyKey", nhsMHSPartyKey)
			.add("nhsIDCode", nhsIDCode)
			.add("nhsDateApproved", nhsDateApproved);
		
		final String[] attributeNames = new String[]{
				"uniqueIdentifier", "nhsAsSvcIA", "nhsMHSPartyKey", "nhsIDCode", "nhsDateApproved"};
		
		return new LdapQuery(name, filter, attributeNames);
	}
}