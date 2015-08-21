package uk.nhs.ciao.spine.sds.mapper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import uk.nhs.ciao.spine.sds.ldap.SearchResultMapper;
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
		messageHandlingService.setNhsMhsCPAId(value(attributes, "nhsMhsCPAId"));
		messageHandlingService.setNhsDateApproved(value(attributes, "nhsDateApproved"));
		
		return messageHandlingService;
	}
}