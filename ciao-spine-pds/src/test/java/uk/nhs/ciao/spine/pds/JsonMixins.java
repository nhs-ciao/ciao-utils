package uk.nhs.ciao.spine.pds;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.nhs.ciao.model.Patient;
import uk.nhs.interoperability.payloads.DateValue;
import uk.nhs.interoperability.payloads.HL7Date;
import uk.nhs.interoperability.payloads.commontypes.Address;
import uk.nhs.interoperability.payloads.commontypes.DateRange;
import uk.nhs.interoperability.payloads.commontypes.PersonName;
import uk.nhs.interoperability.payloads.commontypes.Telecom;
import uk.nhs.interoperability.payloads.util.Emptiables;
import uk.nhs.interoperability.payloads.vocabularies.VocabularyEntry;
import uk.nhs.interoperability.payloads.vocabularies.generated.Sex;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.Maps;

public class JsonMixins {
	@JsonAutoDetect(
			creatorVisibility=Visibility.NONE,
			fieldVisibility=Visibility.NONE,
			getterVisibility=Visibility.NONE,
			isGetterVisibility=Visibility.NONE,
			setterVisibility=Visibility.NONE)
	interface DisabledAutoDetectMixin {
		// Tagging interface to import Jackson annotations - no additional methods required
	}
	
	interface PatientMixin extends DisabledAutoDetectMixin {
		@JsonProperty  String getNhsNumber();
		@JsonProperty  PersonName getName();	
		@JsonProperty  HL7Date getDateOfBirth();
		@JsonProperty  HL7Date getDateOfDeath();
		@JsonProperty  List<Telecom> getTelecom();
		@JsonProperty  String getPracticeCode();
		@JsonProperty  List<Address> getAddress();
		@JsonProperty  Sex getGender();
	}
		
	interface AddressMixin extends DisabledAutoDetectMixin {
		@JsonProperty List<String> getAddressLine();
		@JsonProperty String getCity();
		@JsonProperty String getPostcode();
		@JsonProperty String getAddressKey();
		@JsonProperty String getFullAddress();
		@JsonProperty String getAddressUse();
		@JsonProperty String getNullFlavour();
		@JsonProperty String getDescription();
		@JsonProperty DateRange getUseablePeriod();
	}	
	
	interface PersonNameMixin extends DisabledAutoDetectMixin {
		@JsonProperty String getTitle();
		@JsonProperty List<String> getGivenName();
		@JsonProperty String getFamilyName();
		@JsonProperty String getFullName();
		@JsonProperty String getNameType();
		@JsonProperty String getNullFlavour();
	}
	
	interface TelecomMixin extends DisabledAutoDetectMixin {
		@JsonProperty String getTelecom();
		@JsonProperty String getTelecomType();
	}
	
	static class DateValueDeserializer extends FromStringDeserializer<DateValue> {
		private static final long serialVersionUID = -5953739794572642066L;
		
		/**
		 * Creates a new deserializer instance for {@link DateValue}
		 */
		public DateValueDeserializer() {
			super(DateValue.class);
		}

		@Override
		protected DateValue _deserialize(final String value, final DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			return new DateValue(value);
		}	
	}
	
	static class HL7DateSerializer extends StdSerializer<HL7Date> {
		private static final long serialVersionUID = -8278187080926338731L;

		public HL7DateSerializer() {
			super(HL7Date.class);
		}
		
		@Override
		public boolean isEmpty(final SerializerProvider provider, final HL7Date value) {
			return Emptiables.isNullOrEmpty(value);
		}
		
		@Override
		public void serialize(final HL7Date value, final JsonGenerator gen,
				final SerializerProvider provider) throws IOException, JsonGenerationException {
			gen.writeString(value.asString());
		}
	}
	
	/**
	 * Jackson deserializer for {@link VocabularyEntry} enums.
	 * <p>
	 * The serialized value is {@link VocabularyEntry#getCode()}.
	 * <p>
	 * On construction, the set of known values is supplied and a code
	 * to instance lookup map is compiled. Additional mappings
	 * can be added later via {@link #addEntry(String, VocabularyEntry)}
	 * 
	 * @param <T> The concrete type of entry handled by this deserializer
	 */
	static class VocabularyEntryDeserializer<T extends VocabularyEntry> extends FromStringDeserializer<T> {
		private static final long serialVersionUID = 3689147032997601765L;
		private final HashMap<String, T> entriesByKey;
		private final boolean caseSensitive;
		
		/**
		 * Constructs a new case-sensitive deserializer
		 */
		public VocabularyEntryDeserializer(final Class<T> entryType, final T[] entries) {
			this(entryType, entries, true);
		}
		
		/**
		 * Constructs a new deserializer using the specified case-sensitivity
		 */
		public VocabularyEntryDeserializer(final Class<T> entryType, final T[] entries, final boolean caseSensitive) {
			super(entryType);
			
			this.entriesByKey = Maps.newHashMap();
			this.caseSensitive = caseSensitive;
			
			for (final T entry: entries) {
				addEntry(entry.getCode(), entry);
				addEntry(entry.getDisplayName(), entry);
			}
		}
		
		/**
		 * Adds a new code to instance entry mapping
		 */
		public final void addEntry(final String code, final T entry) {
			final String key = getKey(code);
			entriesByKey.put(key, entry);
		}
		
		/**
		 * Adds the specified code to instance entry mappings
		 */
		public void addEntries(final Map<String, T> entryMappings) {
			for (final Entry<String, T> entry: entryMappings.entrySet()) {
				addEntry(entry.getKey(), entry.getValue());
			}
		}
		
		@Override
		protected final T _deserialize(final String code, final DeserializationContext ctxt)
				throws IOException {
			final String key = getKey(code);
			final T entry = entriesByKey.get(key);
			if (entry == null) {
				throw new IllegalArgumentException("Unsupported code: " + code);
			}
			return entry;
		}
		
		/**
		 * Returns the lookup key to use for the specified code
		 */
		private String getKey(final String code) {
			return code == null || caseSensitive ? code : code.toLowerCase();
		}
	}
	
	/**
	 * Jackson serializer for {@link VocabularyEntry}
	 * <p>
	 * The serialized value is {@link VocabularyEntry#getCode()}
	 */
	static class VocabularyEntrySerializer extends StdScalarSerializer<VocabularyEntry> {
		private static final long serialVersionUID = 8712166069919187387L;

		public VocabularyEntrySerializer() {
			super(VocabularyEntry.class);
		}
		
		@Override
		public void serialize(final VocabularyEntry value, final JsonGenerator jgen,
				final SerializerProvider provider) throws IOException, JsonGenerationException {
			jgen.writeString(value.getCode());
		}

	}
	
	public static ObjectMapper createObjectMapper() {
		final ObjectMapper mapper = new ObjectMapper();
		final SimpleModule module = new SimpleModule();
		
		module.setMixInAnnotation(Patient.class, PatientMixin.class);
		module.setMixInAnnotation(Address.class, AddressMixin.class);
		module.setMixInAnnotation(PersonName.class, PersonNameMixin.class);
		module.setMixInAnnotation(Telecom.class, TelecomMixin.class);
		module.addDeserializer(DateValue.class, new DateValueDeserializer());
		module.addSerializer(new HL7DateSerializer());
		module.addAbstractTypeMapping(HL7Date.class, DateValue.class);
		
		module.addDeserializer(Sex.class, new VocabularyEntryDeserializer<Sex>(Sex.class,
				Sex.values(), false));
		module.addSerializer(VocabularyEntry.class, new VocabularyEntrySerializer());
		
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(Include.NON_NULL);
		return mapper;
	}
}
