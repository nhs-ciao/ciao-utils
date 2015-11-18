package uk.nhs.ciao.spine.pds.model;

import uk.nhs.interoperability.payloads.DateValue;
import uk.nhs.interoperability.payloads.vocabularies.generated.Sex;

public class SimpleTrace {
	private String surname;
	private Sex gender;
	private DateValue dateOfBirth;
	
	public String getSurname() {
		return surname;
	}
	
	public void setSurname(final String surname) {
		this.surname = surname;
	}
	
	public Sex getGender() {
		return gender;
	}
	
	public String getGenderCode() {
		return gender == null ? null : gender.getCode();
	}
	
	public void setGender(final String gender) {
		this.gender = gender == null ? null : Sex.getByCode(gender);
	}
	
	public void setGender(final Sex gender) {
		this.gender = gender;
	}
	
	public DateValue getDateOfBirth() {
		return dateOfBirth;
	}
	
	public void setDateOfBirth(final String dateOfBirth) {
		this.dateOfBirth = dateOfBirth == null ? null : new DateValue(dateOfBirth);
	}
	
	public void setDateOfBirth(final DateValue dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
}
