package org.glassfish.hk2.bootstrap.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.hk2.api.DescriptorFileFinder;

public class URLDescriptorFileFinder implements DescriptorFileFinder {
	private final URL url;

	public URLDescriptorFileFinder(URL url) {
		this.url = url;
	}

	@Override
	public List<InputStream> findDescriptorFiles() throws IOException {
		ArrayList<InputStream> returnList = new ArrayList<InputStream>();
		returnList.add(url.openStream());
		return returnList;
	}
}