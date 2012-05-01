package org.glassfish.hk2.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DescriptorFileFinder {

	public static final String RESOURCE_NAME="/META-INF/hk2-locator/default";
	
	List<InputStream> findDescriptorFiles() throws IOException;

}
