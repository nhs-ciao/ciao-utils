package uk.nhs.ciao.dts;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link ControlFile}
 */
public class ControlFileTest {
	@Test
	public void testCopyFromWithOverwrite() throws JAXBException {
		final ControlFile prototype = ControlFile.fromXml(getClass().getResourceAsStream("example-control-file-post.xml"));
		Assert.assertNotNull(prototype);
		
		final ControlFile controlFile = new ControlFile();
		final boolean overwrite = true;
		controlFile.copyFrom(prototype, overwrite);
		
		Assert.assertEquals(prototype, controlFile);
	}
	
	@Test
	public void testCopyFromWithoutOverwrite() throws JAXBException {
		final ControlFile prototype = ControlFile.fromXml(getClass().getResourceAsStream("example-control-file-post.xml"));
		Assert.assertNotNull(prototype);
		
		final ControlFile controlFile = new ControlFile();
		controlFile.setDTSId("original-DTS-ID");
		
		final boolean overwrite = false;
		controlFile.copyFrom(prototype, overwrite);
		
		// Result should be the prototype but with the original non-null values
		final ControlFile expected = prototype;
		prototype.setDTSId("original-DTS-ID");
		
		Assert.assertEquals(expected, controlFile);
	}
}
