package org.glassfish.hk2.bootstrap.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.glassfish.hk2.bootstrap.DescriptorFileFinder;

public class ClasspathDescriptorFileFinder implements
		DescriptorFileFinder {
	@Override
	public List<InputStream> findDescriptorFiles() throws IOException {
		ArrayList<InputStream> returnList = new ArrayList<InputStream>();
		Enumeration<URL> e = getClass().getClassLoader()
				.getResources(RESOURCE_NAME);

		for (; e.hasMoreElements();) {
			URL url = e.nextElement();
			returnList.add(url.openStream());
		}
		return returnList;
	}
}
