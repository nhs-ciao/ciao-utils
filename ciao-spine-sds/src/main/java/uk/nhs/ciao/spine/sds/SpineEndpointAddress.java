package uk.nhs.ciao.spine.sds;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Details to identify / address an Accredited System end-point over spine
 * 
 * TODO: temporary copy to get the code started - remove!
 */
public class SpineEndpointAddress {
	/**
	 * Identifies the organisation associated with the Accredited System
	 */
	private String odsCode;

	private String service;
	
	private String action;
	
	/**
	 * Identifies the Accredited System
	 */
	private String asid;
	
	/**
	 * Identifies ContractProperties for Party + Interaction
	 */
	private String cpaId;
	
	/**
	 * Identifies the message handling service (MHS) responsible for sending
	 * messages to the Accredited System
	 */
	private String mhsPartyKey;

	public SpineEndpointAddress() {
		// NOOP
	}
	
	/**
	 * Copy constructor
	 */
	public SpineEndpointAddress(final SpineEndpointAddress copy) {
		odsCode = copy.odsCode;
		service = copy.service;
		action = copy.action;
		asid = copy.asid;
		cpaId = copy.cpaId;
		mhsPartyKey = copy.mhsPartyKey;
	}
	
	public String getOdsCode() {
		return odsCode;
	}
	
	public void setOdsCode(final String odsCode) {
		this.odsCode = odsCode;
	}

	public String getService() {
		return service;
	}
	
	public void setService(final String service) {
		this.service = service;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(final String action) {
		this.action = action;
	}
	
	public String getAsid() {
		return asid;
	}
	
	public void setAsid(final String asid) {
		this.asid = asid;
	}
	
	public String getCpaId() {
		return cpaId;
	}
	
	public void setCpaId(final String cpaId) {
		this.cpaId = cpaId;
	}
	
	public String getMhsPartyKey() {
		return mhsPartyKey;
	}
	
	public void setMhsPartyKey(final String mhsPartyKey) {
		this.mhsPartyKey = mhsPartyKey;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("odsCode", odsCode)
			.add("service", service)
			.add("action", action)
			.add("asid", asid)
			.add("cpaId", cpaId)
			.add("mhsPartyKey", mhsPartyKey)
			.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((asid == null) ? 0 : asid.hashCode());
		result = prime * result + ((cpaId == null) ? 0 : cpaId.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((mhsPartyKey == null) ? 0 : mhsPartyKey.hashCode());
		result = prime * result + ((odsCode == null) ? 0 : odsCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		final SpineEndpointAddress other = (SpineEndpointAddress) obj;
		return Objects.equal(action, other.action)
				&& Objects.equal(asid, other.asid)
				&& Objects.equal(cpaId, other.cpaId)
				&& Objects.equal(mhsPartyKey, other.mhsPartyKey)
				&& Objects.equal(odsCode, other.odsCode)
				&& Objects.equal(service, other.service);
	}	
}
