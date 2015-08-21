package uk.nhs.ciao.spine.sds.mapper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import uk.nhs.ciao.spine.sds.ldap.SearchResultMapper;
import uk.nhs.ciao.spine.sds.model.AccreditedSystem;

public class AccreditedSystemMapper extends SearchResultMapper<AccreditedSystem> {
	private static final AccreditedSystemMapper INSTANCE = new AccreditedSystemMapper();
	
	public static AccreditedSystemMapper getInstance() {
		return INSTANCE;
	}
	
	private AccreditedSystemMapper() {
		// Suppress default constructor
	}
	
	@Override
	public AccreditedSystem mapSearchResult(final SearchResult searchResult) throws NamingException {
		final AccreditedSystem accreditedSystem = new AccreditedSystem();
		
		final Attributes attributes = searchResult.getAttributes();
		accreditedSystem.setUniqueIdentifier(value(attributes, "uniqueIdentifier"));
		accreditedSystem.setNhsAsSvcIAs(values(attributes, "nhsAsSvcIA"));
		accreditedSystem.setNhsMHSPartyKey(value(attributes, "nhsMHSPartyKey"));
		accreditedSystem.setNhsIDCode(value(attributes, "nhsIDCode"));
		accreditedSystem.setNhsDateApproved(value(attributes, "nhsDateApproved"));
		
		return accreditedSystem;
	}
}