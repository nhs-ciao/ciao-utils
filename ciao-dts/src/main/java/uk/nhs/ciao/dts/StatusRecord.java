package uk.nhs.ciao.dts;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.joda.time.LocalDateTime;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

@XmlAccessorType(XmlAccessType.FIELD)
public class StatusRecord {
	@XmlElement(name="DateTime")
	private LocalDateTime dateTime;

	@XmlElement(name="Event")
	private Event event;

	@XmlElement(name="Status")
	private Status status;

	/**
	 * 2 digit numeric code
	 */
	@XmlElement(name="StatusCode")
	private String statusCode;

	@XmlElement(name="Description")
	private String description;
	
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	
	public void setDateTime(final LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
	
	public Event getEvent() {
		return event;
	}
	
	public void setEvent(final Event event) {
		this.event = event;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(final Status status) {
		this.status = status;
	}
	
	public String getStatusCode() {
		return statusCode;
	}
	
	public void setStatusCode(final String statusCode) {
		this.statusCode = statusCode;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(final String description) {
		this.description = description;
	}
	
	public boolean isEmpty() {
		return dateTime == null &&
			event == null &&
			status == null &&
			Strings.isNullOrEmpty(statusCode) &&
			Strings.isNullOrEmpty(description);				
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("dateTime", dateTime)
			.add("event", event)
			.add("status", status)
			.add("statusCode", statusCode)
			.add("description", description)
			.toString();
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(
			dateTime,
			event,
			status,
			statusCode,
			description);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		final StatusRecord other = (StatusRecord)obj;
		return Objects.equal(dateTime, other.dateTime) &&
				Objects.equal(event, other.event) &&
				Objects.equal(status, other.status) &&
				Objects.equal(statusCode, other.statusCode) &&
				Objects.equal(description, other.description);
	}
}
