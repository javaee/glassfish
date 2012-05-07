package org.glassfish.hk2.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.bootstrap.impl.ClasspathDescriptorFileFinder;
import org.glassfish.hk2.utilities.DescriptorImpl;

public class HK2Populator {

	public static ServiceLocator populate(final ServiceLocator serviceLocator,
			final DescriptorFileFinder fileFinder,
			final PopulatorPostProcessor postProcessor) throws IOException {

		List<InputStream> descriptorFileInputStreams = fileFinder
				.findDescriptorFiles();

		DynamicConfigurationService dcs = serviceLocator
				.getService(DynamicConfigurationService.class);

		DynamicConfiguration config = dcs.createDynamicConfiguration();
		
		for (InputStream is : descriptorFileInputStreams) {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			try {
				DescriptorImpl descriptorImpl = new DescriptorImpl();
				
				boolean readOne = false;
				
				do {
				    readOne = descriptorImpl.readObject(br);

					if (readOne) {
						List<DescriptorImpl> descriptorImpls = postProcessor
								.process(descriptorImpl);

						for (Descriptor d : descriptorImpls) {
							config.bind(d);
						}
					} else {
						break;
					}
				} while (readOne);
				
			} finally {
				br.close();
			}
		}
		
		config.commit();
		return serviceLocator;
	}

	public static ServiceLocator populate(final ServiceLocator serviceLocator,
			final DescriptorFileFinder fileFinder) throws IOException {
		return populate(serviceLocator, fileFinder,
				new PopulatorPostProcessor() {

					@Override
					public List<DescriptorImpl> process(
							DescriptorImpl descriptorImpl) {
						ArrayList<DescriptorImpl> list = new ArrayList<DescriptorImpl>();
						list.add(descriptorImpl);
						return list;
					}
				});
	}

	public static ServiceLocator populate(final ServiceLocator serviceLocator)
			throws IOException {
		return populate(serviceLocator, new ClasspathDescriptorFileFinder());
	}

}
