package org.jvnet.hk2.osgiadapter;

import static org.jvnet.hk2.osgiadapter.Logger.logger;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;

import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.MultiException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * An Hk2Loader that uses a Bundle to load classes 
 * 
 * @author mason.taube@oracle.com
 *
 */
public class OsgiHk2Loader implements HK2Loader {
	private final Bundle bundle;

	OsgiHk2Loader(Bundle bundle) {
		this.bundle = bundle;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<?> loadClass(final String className) throws MultiException {
		try {
			bundle.start();
		} catch (BundleException e1) {
			throw new MultiException(e1);
		}
		return (Class<?>) AccessController
				.doPrivileged(new PrivilegedAction() {
					public java.lang.Object run() {
						try {
							return bundle.loadClass(className);
						} catch (Throwable e) {
							logger.logp(
									Level.SEVERE,
									"OSGiModuleImpl",
									"loadClass",
									"Exception in module "
											+ bundle.toString() + " : "
											+ e.toString());
							throw new MultiException(e);
						}
					}
				});

	}
}