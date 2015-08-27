package uk.nhs.ciao.spine.sds.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class MHSContractProperties {
	private String uniqueIdentifier;
	private String nhsMhsPersistduration;
	private String nhsMhsRetries;
	private String nhsMhsRetryInterval;
	
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}
	
	public void setUniqueIdentifier(final String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
	
	public String getNhsMhsPersistduration() {
		return nhsMhsPersistduration;
	}
	
	public void setNhsMhsPersistduration(final String nhsMhsPersistduration) {
		this.nhsMhsPersistduration = nhsMhsPersistduration;
	}
	
	public String getNhsMhsRetries() {
		return nhsMhsRetries;
	}
	
	public void setNhsMhsRetries(final String nhsMhsRetries) {
		this.nhsMhsRetries = nhsMhsRetries;
	}
	
	public String getNhsMhsRetryInterval() {
		return nhsMhsRetryInterval;
	}
	
	public void setNhsMhsRetryInterval(final String nhsMhsRetryInterval) {
		this.nhsMhsRetryInterval = nhsMhsRetryInterval;
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
		
		final MHSContractProperties other = (MHSContractProperties) obj;
		return Objects.equal(uniqueIdentifier, other.uniqueIdentifier);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("uniqueIdentifier", uniqueIdentifier)
				.add("nhsMhsPersistduration", nhsMhsPersistduration)
				.add("nhsMhsRetries", nhsMhsRetries)
				.add("nhsMhsRetryInterval", nhsMhsRetryInterval)
				.toString();
	}
}
