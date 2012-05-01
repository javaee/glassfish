package org.jvnet.hk2.osgiadapter;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.bootstrap.PopulatorPostProcessor;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * A PopulatorPostProcessor that sets the HK2Loader prior to binding a descriptor
 * 
 * @author mason.taube@oracle.com
 *
 */
public class OsgiPopulatorPostProcessor implements
		PopulatorPostProcessor {
	private final HK2Loader hk2Loader;

	OsgiPopulatorPostProcessor(HK2Loader hk2Loader) {
		this.hk2Loader = hk2Loader;
	}

	@Override
	public List<DescriptorImpl> process(DescriptorImpl descriptorImpl) {
		descriptorImpl.setLoader(hk2Loader);

		List<DescriptorImpl> list = new ArrayList<DescriptorImpl>();

		list.add(descriptorImpl);
		return list;
	}
}