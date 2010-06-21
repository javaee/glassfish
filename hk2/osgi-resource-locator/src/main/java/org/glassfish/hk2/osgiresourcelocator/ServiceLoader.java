/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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


package org.glassfish.hk2.osgiresourcelocator;

/**
 * This is a gateway to OSGi bundles as far as META-INF/services files are concerned.
 * Since META-INF/services files are not exportable, clients relying on Java SPI mechanism
 * can't discover all providers. This utility helps in such a situation. It provides a utility method
 * {@link #lookupProviderInstances} which can find META-INF/services being part of OSGi bundles.
 * This class has been carefully coded to avoid any reference to OSGi classes so that
 * it can be called in a non-OSGi environment as well. In such an environment,
 * it simply returns null.
 * In an OSGi environment, we expect the class to be initialized by the bundle activator.
 *
 * @see {@link #lookupProviderInstances}
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class ServiceLoader {
    /**
     * Singleton
     */
    private static ServiceLoader _me;

    /*package*/ ServiceLoader() {}

    public static synchronized void initialize(ServiceLoader singleton) {
        if (singleton == null) throw new NullPointerException("Did you intend to call reset()?");
        if (_me != null) throw new IllegalStateException("Already initialzed with [" + _me + "]");
        _me = singleton;
    }

    public static synchronized void reset() {
        if (_me == null) {
            throw new IllegalStateException("Not yet initialized");
        }
        _me = null;
    }

    /**
     * This method returns null if this class is not initialized yet.
     *
     * @param serviceClass Type of service requested
     * @param <T>
     * @return provider instances for the given service type
     */
    public static <T> Iterable<? extends T> lookupProviderInstances(Class<T> serviceClass) {
        if (_me == null) return null;
        return _me.lookupProviderInstances1(serviceClass);
    }

    /**
     * Calling this method is equivalent to calling {@link #lookupProviderClasses(Class, boolean)} with a true argument.
     * @see #lookupProviderClasses(Class, boolean)
     */
    public static <T> Iterable<Class> lookupProviderClasses(Class<T> serviceClass) {
        return lookupProviderClasses(serviceClass, true);
    }

    /**
     * Returns classes found in META-INF/services/serviceClass.getName() in OSGi bundles. This method searches for
     * such named resources in every OSGi bundle. For every resource found, it assumes that the file contains
     * a class name. It loads the class name mentioned in that file using the bundle containing the resource.
     * If onlyCompatible argument is true, it returns the class only if the loaded class is assignment compatible
     * with the service class.
     *
     * WARNING: Be extra careful while invoking this method with a false argument. It can result in ClassCastException
     * in your code. Only for special cases like JAXBContext, this method need to be called with false argument.
     *
     * @param serviceClass type of service requested
     * @param onlyCompatible indicates if only compatible classes to be returned.
     * @param <T>
     * @return classes corresponding to entries in META-INF/services file for the service class.
     */
    public static <T> Iterable<Class> lookupProviderClasses(Class<T> serviceClass, boolean onlyCompatible) {
        if (_me == null) return null;
        return _me.lookupProviderClasses1(serviceClass, onlyCompatible);
    }

    /*package*/ abstract <T> Iterable<? extends T> lookupProviderInstances1(Class<T> serviceType);
    /*package*/ abstract <T> Iterable<Class> lookupProviderClasses1(Class<T> serviceType, boolean onlyCompatible);
}
