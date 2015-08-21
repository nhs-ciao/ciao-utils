package uk.nhs.ciao.spine.sds;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

public class AccreditedSystem {
	private String uniqueIdentifier;
	private final Set<String> nhsAsSvcIAs = Sets.newLinkedHashSet();
	private String nhsMHSPartyKey;
	private String nhsIDCode;
	private String nhsDateApproved;
	
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}
	
	public void setUniqueIdentifier(final String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
	
	public Set<String> getNhsAsSvcIAs() {
		return nhsAsSvcIAs;
	}
	
	public void setNhsAsSvcIAs(final Collection<String> nhsAsSvcIAs) {
		this.nhsAsSvcIAs.clear();
		if (nhsAsSvcIAs != null) {
			this.nhsAsSvcIAs.addAll(nhsAsSvcIAs);
		}
	}
	
	public String getNhsMHSPartyKey() {
		return nhsMHSPartyKey;
	}
	
	public void setNhsMHSPartyKey(final String nhsMHSPartyKey) {
		this.nhsMHSPartyKey = nhsMHSPartyKey;
	}
	
	public String getNhsIDCode() {
		return nhsIDCode;
	}
	
	public void setNhsIDCode(final String nhsIDCode) {
		this.nhsIDCode = nhsIDCode;
	}
	
	public String getNhsDateApproved() {
		return nhsDateApproved;
	}
	
	public void setNhsDateApproved(final String nhsDateApproved) {
		this.nhsDateApproved = nhsDateApproved;
	}
	
	@Override
	public int hashCode() {
		return uniqueIdentifier == null ? 0 : uniqueIdentifier.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		final AccreditedSystem other = (AccreditedSystem) obj;
		return Objects.equal(uniqueIdentifier, other.uniqueIdentifier);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("uniqueIdentifier", uniqueIdentifier)
				.add("nhsAsSvcIAs", nhsAsSvcIAs)
				.add("nhsMHSPartyKey", nhsMHSPartyKey)
				.add("nhsIDCode", nhsIDCode)
				.add("nhsDateApproved", nhsDateApproved)
				.toString();
	}
}
