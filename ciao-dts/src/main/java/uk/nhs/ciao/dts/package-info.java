@XmlJavaTypeAdapters({
	@XmlJavaTypeAdapter(value=TimestampAdapter.class, type=LocalDateTime.class),
    @XmlJavaTypeAdapter(value=YesNoAdapter.class, type=Boolean.class)
})
package uk.nhs.ciao.dts;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.joda.time.LocalDateTime;

import uk.nhs.ciao.dts.JAXBFactory.YesNoAdapter;
import uk.nhs.ciao.dts.JAXBFactory.TimestampAdapter;

