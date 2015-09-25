package uk.nhs.ciao.dts;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.FallbackConverter;
import org.apache.camel.TypeConverter;
import org.apache.camel.spi.TypeConverterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;

/**
 * Camel type converters to convert to/from ControlFile.
 * <p>
 * The converters are automatically registered via Camel's type converter META-INF/services file:
 * <code>/META-INF/services/org/apache/camel/TypeConverter</code>
 */
@Converter
public final class ControlFileTypeConverter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ControlFileTypeConverter.class);
	
	private ControlFileTypeConverter() {
		// Suppress default constructor
	}
	
	/**
	 * Converts the specified input stream to a ControlFile
	 * <p>
	 * The InputStream is not closed by this method.
	 */
	@Converter
	public static ControlFile fromInputStream(final InputStream in) throws JAXBException {
		LOGGER.debug("fromInputStream()");
		
		return ControlFile.fromXml(in);
	}
	
	/**
	 * Encodes the envelope as an XML string
	 * 
	 * @throws Exception If the envelope could not be encoded
	 */
	@Converter
	public static String toString(final ControlFile controlFile) throws JAXBException {
		if (controlFile == null) {
			return null;
		}
		
		return controlFile.toXml();
	}
	
	/**
	 * Camel fallback converter to convert a value to ControlFile to a specified type either directly or via InputStream as an intermediate.
	 * <p>
	 * The type converter registry is used to convert the value to InputStream.
	 */
	@FallbackConverter
	public static <T> T convertToControlFile(final Class<T> type, final Exchange exchange, final Object value, final TypeConverterRegistry registry) throws JAXBException {
		if (!ControlFile.class.equals(type)) {
			// Only handle ControlFile conversions
			return null;
		} else if (value instanceof ControlFile) {
			// No conversion required
			return type.cast(value);
		}
		
		LOGGER.debug("convertToControlFile via (InputStream) from: {}", value.getClass());
		
		// Convert via InputStream
		final InputStream in = castOrConvert(InputStream.class, exchange, value, registry);
		try {
			return in == null ? null : type.cast(fromInputStream(in));
		} finally {
			// close the stream if it is an intermediate
			// does the stream always need to be closed? will camel close the stream when in == value?
			if (in != value) {
				Closeables.closeQuietly(in);
			}
		}
	}
	
	/**
	 * Camel fallback converter to convert a ControlFile to a specified type either directly or via String as an intermediate.
	 * <p>
	 * The type converter registry is used to convert the ControlFile to String
	 */
	@FallbackConverter
	public static <T> T convertFromControlFile(final Class<T> type, final Exchange exchange, final Object value, final TypeConverterRegistry registry) throws JAXBException {
		if (!(value instanceof ControlFile)) {
			// Only handle envelope conversions
			return null;
		} else if (ControlFile.class.isAssignableFrom(type)) {
			// No conversion required
			return type.cast(value);
		} else if (!canConvert(byte[].class, type, registry)) {
			// Can only support conversions via byte array as intermediate
			return null;
		}
		
		LOGGER.debug("convertFromControlFile via (String) to: {}", type);
		
		// Convert via String
		final String string = toString((ControlFile)value);		
		return castOrConvert(type, exchange, string, registry);
	}
	
	/**
	 * Helper method to coerce the specified value into the required type.
	 * <p>
	 * Value is cast if it is already of the correct kind, otherwise a camel type converter is tried. Null is
	 * returned if the value cannot be coerced.
	 */
	private static <T> T castOrConvert(final Class<T> toType, final Exchange exchange, final Object value, final TypeConverterRegistry registry) {
		final T result;
		
		if (toType.isInstance(value)) {
			result = toType.cast(value);
		} else {
			final TypeConverter typeConverter = registry.lookup(toType, value.getClass());
			result = typeConverter == null ? null : typeConverter.convertTo(toType, exchange, value);
		}
		
		return result;
	}
	
	/**
	 * Helper methods to test if a conversion can take place from one class to another - either via a direct cast, or
	 * via a registered Camel type converter.
	 */
	private static boolean canConvert(final Class<?> fromType, final Class<?> toType, final TypeConverterRegistry registry) {
		return toType.isAssignableFrom(fromType) || registry.lookup(toType, fromType) != null;
	}
}
