package uk.nhs.ciao.spine.sds;

import uk.nhs.ciao.CIPRoutes;

public class SDSQueryRoute extends CIPRoutes {
	@Override
	public void configure() {
	    from("direct:ldapquery")
	        .log("***** Route is running!! *****");
	    super.configure();
	}

}
