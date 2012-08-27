package org.glassfish.hk2.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DescriptorFileFinder {

	public static final String RESOURCE_BASE="META-INF/hk2-locator/";
	
	List<InputStream> findDescriptorFiles() throws IOException;

}
