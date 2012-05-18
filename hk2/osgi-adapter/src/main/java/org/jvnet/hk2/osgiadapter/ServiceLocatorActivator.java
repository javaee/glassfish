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

package org.jvnet.hk2.osgiadapter;

import static org.jvnet.hk2.osgiadapter.BundleEventType.valueOf;
import static org.jvnet.hk2.osgiadapter.Logger.logger;

import java.net.URL;
import java.util.logging.Level;

import com.sun.enterprise.module.bootstrap.Main;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.bootstrap.DescriptorFileFinder;
import org.glassfish.hk2.bootstrap.HK2Populator;
import org.glassfish.hk2.bootstrap.impl.URLDescriptorFileFinder;
import org.jvnet.hk2.external.generator.ServiceLocatorGeneratorImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;

import com.sun.enterprise.module.bootstrap.ModuleStartup;

/**
 * {@link org.osgi.framework.BundleActivator} that launches a ServiceLocator.
 * Responsible for registering bundles containing descriptors in the
 * ServiceLocator.
 * 
 * @author mason.taube@oracle.com
 */
public class ServiceLocatorActivator implements BundleActivator,
		SynchronousBundleListener {

	private BundleContext bundleContext;

	private ServiceLocator serviceLocator;
	private ServiceRegistration serviceLocatorRegistration;

	public ServiceLocatorActivator() {
		
	}
	
	ServiceLocatorActivator(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	private void destroyServiceLocator(ServiceLocator serviceLocator) {
		ServiceLocatorFactory.getInstance().destroy(serviceLocator.getName());

		serviceLocatorRegistration.unregister();
	}

	public void start(BundleContext context) throws Exception {
		this.bundleContext = context;

		logger.entering(getClass().getSimpleName(), "start",
				new Object[] { context });

		serviceLocator = ServiceLocatorFactory.getInstance().create(
				Main.DEFAULT_NAME, null, new ServiceLocatorGeneratorImpl());

		serviceLocatorRegistration = bundleContext.registerService(ServiceLocator.class.getName(),
				serviceLocator, null);

		context.addBundleListener(this);
	}

	public void stop(BundleContext context) throws Exception {
		// When OSGi framework shuts down, it shuts down all started bundles,
		// but the order is unspecified.
		// So, since we are going to shutdown the registry, it's better that we
		// stop startup service just incase it is still running.
		// Similarly, we can release the habitat.

		// Execute code in reverse order w.r.t. start()
		{
			ModuleStartup startupService = serviceLocator.getService(
					ModuleStartup.class);
			if (startupService != null) {
				try {
					logger.info("Stopping " + startupService);
					startupService.stop();
				} catch (Exception e) {
					logger.log(
							Level.WARNING,
							"HK2Main:stop():Exception while stopping ModuleStartup service.",
							e);
				}
			}
			destroyServiceLocator(serviceLocator);
		}
	}

	public void bundleChanged(BundleEvent event) {
		logger.logp(Level.FINE, getClass().getSimpleName(), "bundleChanged",
				"source= {0}, type= {1}", new Object[] { event.getSource(),
						valueOf(event.getType()) });

		// If a bundle is started, scan it for an hk2-descriptor file,
		// Then populate 
		switch (event.getType()) {
		case BundleEvent.STARTED:

			final Bundle bundle = event.getBundle();

			final URL url = bundle
					.getResource(DescriptorFileFinder.RESOURCE_NAME);

			if (url != null) {

				try {
					final HK2Loader hk2Loader = new OsgiHk2Loader(bundle);

					ServiceLocator sl = ServiceLocatorFactory.getInstance().create(
							Main.DEFAULT_NAME, null,
							new ServiceLocatorGeneratorImpl());
					HK2Populator.populate(
							sl,
							new URLDescriptorFileFinder(url),
							new OsgiPopulatorPostProcessor(hk2Loader));
					HK2Populator.populateConfig(sl);
				} catch (Exception e) {
					logger.log(
							Level.SEVERE,
							"Exception while binding HK2 service.",
							e);
				}
			}
			break;
		default:
			break;
		}
	}

}
