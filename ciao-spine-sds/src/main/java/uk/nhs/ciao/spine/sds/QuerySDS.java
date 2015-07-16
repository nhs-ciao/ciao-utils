package uk.nhs.ciao.spine.sds;

import java.util.Collection;
import java.util.Iterator;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;

import uk.nhs.ciao.camel.CamelApplication;

public class QuerySDS {

	public static String run(Exchange exchange, String query)  {
		ProducerTemplate template = exchange
			  .getContext().createProducerTemplate();
			 
			//Collection<?> results = 
			template.sendBody(
			    "ldap:ldapserver?base=ou=mygroup,ou=groups,ou=system",
			    "(member=uid=huntc,ou=users,ou=system)");
			Collection<?> results = (Collection<?>)
			template.sendBody("ldap:ldapserver?base=ou=mygroup,ou=groups,ou=system",
								ExchangePattern.InOut, 
								"(member=uid=huntc,ou=users,ou=system)");
			 
			if (results.size() > 0) {
			  // Extract what we need from the device's profile
			 
			  Iterator<?> resultIter = results.iterator();
			  SearchResult searchResult = (SearchResult) resultIter
			      .next();
			  /*Attributes attributes = searchResult
			      .getAttributes();
			  Attribute deviceCNAttr = attributes.get("cn");
			  String deviceCN = (String) deviceCNAttr.get();
			  */
			  System.out.println(searchResult.toString());
			}
			
			return null;
	}
	
}

