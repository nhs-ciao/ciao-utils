package uk.nhs.ciao.dts;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Strings;

class JAXBFactory {
	private static final JAXBContext CONTEXT;
	static {
		try {
			CONTEXT = JAXBContext.newInstance(ControlFile.class);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static JAXBContext getContext() {
		return CONTEXT;
	}

	public static Unmarshaller createUnmarshaller() throws JAXBException {
		final Unmarshaller unmarshaller = CONTEXT.createUnmarshaller();
		
		unmarshaller.setAdapter(TimestampAdapter.INSTANCE);
		unmarshaller.setAdapter(YesNoAdapter.INSTANCE);
		
		return unmarshaller;
	}
	
	public static Marshaller createMarshaller() throws JAXBException {
		final Marshaller marshaller = CONTEXT.createMarshaller();
		
		marshaller.setAdapter(TimestampAdapter.INSTANCE);
		marshaller.setAdapter(YesNoAdapter.INSTANCE);
		
		marshaller.setProperty("jaxb.formatted.output", true);
		
		return marshaller;
	}
	
	public static class TimestampAdapter extends XmlAdapter<String, LocalDateTime> {
		public static final TimestampAdapter INSTANCE = new TimestampAdapter();
		private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss")
				.withZone(DateTimeZone.getDefault());
		
		@Override
		public LocalDateTime unmarshal(final String value) throws Exception {
			return Strings.isNullOrEmpty(value) ? null : FORMATTER.parseLocalDateTime(value);
		}
		
		@Override
		public String marshal(final LocalDateTime dateTime) throws Exception {
			return dateTime == null ? null : FORMATTER.print(dateTime);
		}
	}
	
	public static class YesNoAdapter extends XmlAdapter<String, Boolean> {
		public static final YesNoAdapter INSTANCE = new YesNoAdapter();
		
		@Override
		public Boolean unmarshal(final String value) throws Exception {
			final Boolean result;
			
			if (Strings.isNullOrEmpty(value)) {
				result = null;
			} else if ("Y".equals(value)) {
				result = Boolean.TRUE;
			} else if ("N".equals(value)) {
				result = Boolean.TRUE;
			} else {
				throw new Exception("Unsupported YesNo value: " + value);
			}
			
			return result;
		}
		
		@Override
		public String marshal(final Boolean dateTime) throws Exception {
			return dateTime == null ? null : dateTime.booleanValue() ? "Y" : "N";
		}
	}
}
