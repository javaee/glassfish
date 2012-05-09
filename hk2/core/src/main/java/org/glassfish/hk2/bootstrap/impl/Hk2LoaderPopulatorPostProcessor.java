package org.glassfish.hk2.bootstrap.impl;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.bootstrap.PopulatorPostProcessor;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * A Simple PopulatorPostProcessor that uses the given classloader to override default
 * HK2Loader behavior 
 * 
 * @author mtaube
 *
 */
public class Hk2LoaderPopulatorPostProcessor implements PopulatorPostProcessor {

	final ClassLoader classLoader;
	
	@Override
	public List<DescriptorImpl> process(DescriptorImpl descriptorImpl) {
		List<DescriptorImpl> returnList = new ArrayList<DescriptorImpl> ();
		
		descriptorImpl.setLoader(new HK2Loader() {
			
			@Override
			public Class<?> loadClass(String className) throws MultiException {
				try {
					return classLoader.loadClass(className);
				} catch (ClassNotFoundException e) {
					throw new MultiException(e);
				}
		    }
		});
		
		returnList.add(descriptorImpl);
		return returnList;
	}

	public Hk2LoaderPopulatorPostProcessor(ClassLoader classLoader) {
		super();
		this.classLoader = classLoader;
	}

	
}
