package uk.nhs.ciao.spine.sds.ldap;

public class LdapFilter {
	private final StringBuilder filter = new StringBuilder();
	private int clauses;
	
	public LdapFilter add(final String name, final String value) {
		if (value != null) {
			clauses++;
			filter.append("(").append(name)
				.append("=").append(value).append(")");
		}
		
		return this;
	}
	
	@Override
	public String toString() {
		if (clauses > 1) {
			return new StringBuilder("(&")
				.append(filter)
				.append(")")
				.toString();
		}
		
		return filter.toString();
	}
}