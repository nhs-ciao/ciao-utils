package uk.nhs.ciao.spine.sds.model;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

public class MessageHandlingService {
	private String uniqueIdentifier;
	private final Set<String> nhsMhsSvcIAs = Sets.newLinkedHashSet();
	private String nhsMHSPartyKey;
	private String nhsIDCode;
	private MHSContractProperties contractProperties;
	private String nhsDateApproved;
	
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}
	
	public void setUniqueIdentifier(final String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
	
	public Set<String> getNhsMhsSvcIAs() {
		return nhsMhsSvcIAs;
	}
	
	public void setNhsMhsSvcIAs(final Collection<String> nhsMhsSvcIAs) {
		this.nhsMhsSvcIAs.clear();
		if (nhsMhsSvcIAs != null) {
			this.nhsMhsSvcIAs.addAll(nhsMhsSvcIAs);
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
	
	public MHSContractProperties getContractProperties() {
		return contractProperties;
	}
	
	public void setContractProperties(final MHSContractProperties contractProperties) {
		this.contractProperties = contractProperties;
	}
	
	public String getNhsMhsCPAId() {
		return contractProperties == null ? null : contractProperties.getUniqueIdentifier();
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
		
		final MessageHandlingService other = (MessageHandlingService) obj;
		return Objects.equal(uniqueIdentifier, other.uniqueIdentifier);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("uniqueIdentifier", uniqueIdentifier)
				.add("nhsMhsSvcIAs", nhsMhsSvcIAs)
				.add("nhsMHSPartyKey", nhsMHSPartyKey)
				.add("nhsIDCode", nhsIDCode)
				.add("nhsMhsCPAId", contractProperties)
				.add("nhsDateApproved", nhsDateApproved)
				.toString();
	}
}
