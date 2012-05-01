package org.glassfish.hk2.bootstrap;

import java.util.List;

import org.glassfish.hk2.utilities.DescriptorImpl;

public interface PopulatorPostProcessor {

	List<DescriptorImpl> process(DescriptorImpl descriptorImpl);

}
