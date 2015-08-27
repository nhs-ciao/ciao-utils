package uk.nhs.ciao.spine.sds.mapper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import uk.nhs.ciao.spine.sds.ldap.SearchResultMapper;
import uk.nhs.ciao.spine.sds.model.MHSContractProperties;
import uk.nhs.ciao.spine.sds.model.MessageHandlingService;

public class MessageHandlingServiceMapper extends SearchResultMapper<MessageHandlingService> {
	private static final MessageHandlingServiceMapper INSTANCE = new MessageHandlingServiceMapper();
	
	public static MessageHandlingServiceMapper getInstance() {
		return INSTANCE;
	}
	
	private MessageHandlingServiceMapper() {
		// Suppress default constructor
	}
	
	@Override
	public MessageHandlingService mapSearchResult(final SearchResult searchResult) throws NamingException {
		final MessageHandlingService messageHandlingService = new MessageHandlingService();
		
		final Attributes attributes = searchResult.getAttributes();
		messageHandlingService.setUniqueIdentifier(value(attributes, "uniqueIdentifier"));
		messageHandlingService.setNhsMhsSvcIAs(values(attributes, "nhsMhsSvcIA"));
		messageHandlingService.setNhsMHSPartyKey(value(attributes, "nhsMHSPartyKey"));
		messageHandlingService.setNhsIDCode(value(attributes, "nhsIDCode"));
		messageHandlingService.setContractProperties(contractProperties(attributes));
		messageHandlingService.setNhsDateApproved(value(attributes, "nhsDateApproved"));
		
		return messageHandlingService;
	}
	
	private MHSContractProperties contractProperties(final Attributes attributes) throws NamingException {
		final MHSContractProperties contractProperties = new MHSContractProperties();
		
		contractProperties.setUniqueIdentifier(value(attributes, "nhsMhsCPAId"));
		contractProperties.setNhsMhsPersistduration(value(attributes, "nhsMhsPersistduration"));
		contractProperties.setNhsMhsRetries(value(attributes, "nhsMhsRetries"));
		contractProperties.setNhsMhsRetryInterval(value(attributes, "nhsMhsRetryInterval"));
		
		return contractProperties;
	}
}