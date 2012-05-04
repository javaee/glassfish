package org.glassfish.hk2.bootstrap.impl;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.hk2.bootstrap.PopulatorPostProcessor;
import org.glassfish.hk2.utilities.DescriptorImpl;

public class NullPopulatorPostProcessor implements PopulatorPostProcessor {

	@Override
	public List<DescriptorImpl> process(DescriptorImpl descriptorImpl) {
		List<DescriptorImpl> returnList = new ArrayList<DescriptorImpl> ();
		
		returnList.add(descriptorImpl);
		return returnList;
	}

}
