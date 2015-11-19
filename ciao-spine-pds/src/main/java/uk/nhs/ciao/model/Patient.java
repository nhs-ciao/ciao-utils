package uk.nhs.ciao.model;

import static uk.nhs.interoperability.payloads.util.Emptiables.isNullOrEmpty;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import uk.nhs.interoperability.payloads.HL7Date;
import uk.nhs.interoperability.payloads.commontypes.Address;
import uk.nhs.interoperability.payloads.commontypes.PersonName;
import uk.nhs.interoperability.payloads.commontypes.Telecom;
import uk.nhs.interoperability.payloads.util.Emptiable;
import uk.nhs.interoperability.payloads.vocabularies.generated.Sex;

/**
 * This is a general patient object, agnostic of any particular system or standard - this can
 * then be used to link together routes which require details about a patient
 * @author Adam Hatherly
 */
public class Patient implements Emptiable {
	private String nhsNumber;
	private PersonName name;
	private List<Address> address;
	private HL7Date dateOfBirth;
	private HL7Date dateOfDeath;
	private Sex gender;
	private List<Telecom> telecom;
	private String practiceCode;
	
	public String getNhsNumber() {
		return nhsNumber;
	}

	public void setNhsNumber(final String nhsNumber) {
		this.nhsNumber = nhsNumber;
	}

	public PersonName getName() {
		return name;
	}

	public void setName(final PersonName name) {
		this.name = name;
	}
	
	public HL7Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(final HL7Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public HL7Date getDateOfDeath() {
		return dateOfDeath;
	}

	public void setDateOfDeath(final HL7Date dateOfDeath) {
		this.dateOfDeath = dateOfDeath;
	}

	public List<Telecom> getTelecom() {
		return telecom;
	}

	public void setTelecom(final List<Telecom> telecom) {
		this.telecom = telecom;
	}

	public void addTelecom(final Telecom telecom) {
		if (isNullOrEmpty(telecom)) {
			return;
		}
		else if (this.telecom == null) {
			this.telecom = Lists.newArrayList();
		}
		this.telecom.add(telecom);
	}

	public String getPracticeCode() {
		return practiceCode;
	}

	public void setPracticeCode(final String practiceCode) {
		this.practiceCode = practiceCode;
	}

	public List<Address> getAddress() {
		return address;
	}

	public void setAddress(final List<Address> address) {
		this.address = address;
	}

	public void addAddress(final Address address) {
		if (isNullOrEmpty(address)) {
			return;
		}
		else if (this.address == null) {
			this.address = Lists.newArrayList();
		}
		this.address.add(address);
	}

	public Sex getGender() {
		return gender;
	}

	public void setGender(final Sex gender) {
		this.gender = gender;
	}

	@Override
	public boolean isEmpty() {
		return isNullOrEmpty(nhsNumber) &&
				isNullOrEmpty(name) &&
				isNullOrEmpty(address) &&
				isNullOrEmpty(dateOfBirth) &&
				isNullOrEmpty(dateOfDeath) &&
				gender == null &&
				isNullOrEmpty(telecom) &&
				isNullOrEmpty(practiceCode);
	}
	
	@Override
	public int hashCode() {
		return Strings.nullToEmpty(nhsNumber).hashCode();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Only the nhsNumber is used to test for equality
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		final Patient other = (Patient)obj;
		return Objects.equal(Strings.emptyToNull(nhsNumber), Strings.emptyToNull(other.nhsNumber));
	}
	
	@Override
	public String toString() {
		// Only adding nhsNumber
		return MoreObjects.toStringHelper(this)
			.add("nhsNumber", Strings.nullToEmpty(nhsNumber))
			.toString();
	}
	
	/*name.addGivenSimple("Adam");
	name.addFamilySimple("Hatherly");
	name.addPrefixSimple("Mr");
	name.setTextSimple("Mr Adam Hatherly");
	
	Identifier id = patient.addIdentifier();
	id.setLabelSimple("NHS Number");
	id.setSystemSimple("http://nhs.uk/fhir/nhsnumber");
	id.setValueSimple("1234567890");
	
	CodeableConcept gender = new CodeableConcept();
	gender.setTextSimple("Male");
	Coding coding = gender.addCoding();
	coding.setSystemSimple("http://hl7.org/fhir/vs/administrative-gender");
	coding.setCodeSimple("M");
	patient.setGender(gender);*/
}
