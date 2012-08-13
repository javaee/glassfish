/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.hk2.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.bootstrap.impl.ClasspathDescriptorFileFinder;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import com.sun.enterprise.module.bootstrap.BootException;

/**
 * 
 * @author jwells, mason.taube@oracle.com
 *
 */
public class HK2Populator {

    /**
     * This method can be used to populate the service locator with files that
     * have been written out using the {@link DescriptorImpl} writeObject method.
     * 
     * @param serviceLocator The service locator to populate.  May not be null.
     * @param fileFinder An object that finds files in the environment.  May not be null.
     * @param postProcessors A post-processor that allows the environment to modify the set
     * of descriptors that are added to the system.  May be null, in which case the descriptors
     * read in are those that are used to populate the serviceLocator
     * @throws IOException In case of an error
     */
	public static void populate(final ServiceLocator serviceLocator,
			DescriptorFileFinder fileFinder,
			final Binder postProcessorBinder) throws IOException {

		if (postProcessorBinder != null) {
			ServiceLocatorUtilities.bind(serviceLocator, postProcessorBinder);
		}
		
		if (fileFinder == null) {
			fileFinder = serviceLocator.getService(DescriptorFileFinder.class);
		}
		List<InputStream> descriptorFileInputStreams = fileFinder
				.findDescriptorFiles();

		DynamicConfigurationService dcs = serviceLocator
				.getService(DynamicConfigurationService.class);

		DynamicConfiguration config = dcs.createDynamicConfiguration();

		List<PopulatorPostProcessor> postProcessors = serviceLocator.getAllServices(PopulatorPostProcessor.class);
		
		for (InputStream is : descriptorFileInputStreams) {

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			try {
				boolean readOne = false;

				do {
					DescriptorImpl descriptorImpl = new DescriptorImpl();

					readOne = descriptorImpl.readObject(br);

					if (readOne) {

						if (postProcessors != null) {
							for (PopulatorPostProcessor pp : postProcessors) {
								descriptorImpl = pp.process(descriptorImpl);

								if (descriptorImpl == null) {
									break;
								}
							}
							if (descriptorImpl != null) {
								config.bind(descriptorImpl);
							}
						}

					}
				} while (readOne);

			} finally {
				br.close();
			}
		}

		config.commit();

	}

	/**
	 * This method can be used to populate the service locator with files that
     * have been written out using the {@link DescriptorImpl} writeObject method,
     * looking in the classpath to locate these files
     * 
	 * @param serviceLocator The service locator to populate.  May not be null
	 * @throws IOException In case of an error
	 */
	public static void populate(final ServiceLocator serviceLocator)
			throws IOException {
		populate(serviceLocator, new ClasspathDescriptorFileFinder(), null);
	}

    public static void populateConfig(ServiceLocator serviceLocator) throws BootException {
        //Populate this serviceLocator with config data
        for (ConfigPopulator populator : serviceLocator.<ConfigPopulator>getAllServices(ConfigPopulator.class)) {
            populator.populateConfig(serviceLocator);
        }
    }
}
