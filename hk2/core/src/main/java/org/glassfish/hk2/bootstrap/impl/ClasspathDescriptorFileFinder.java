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
	private final ClassLoader classLoader;
	private String name;

	@Override
	public List<InputStream> findDescriptorFiles() throws IOException {
		ArrayList<InputStream> returnList = new ArrayList<InputStream>();
		Enumeration<URL> e = classLoader.getResources(RESOURCE_BASE+name);

		for (; e.hasMoreElements();) {
			URL url = e.nextElement();
			returnList.add(url.openStream());
		}
		return returnList;
	}
	
	public ClasspathDescriptorFileFinder (ClassLoader cl) {
		this(cl, "default");
	}
	
	public ClasspathDescriptorFileFinder (ClassLoader cl, String name) {
		this.classLoader = cl;
		this.name = name;
	}
	
	public ClasspathDescriptorFileFinder() {
		this(ClasspathDescriptorFileFinder.class.getClassLoader());
	}
}
