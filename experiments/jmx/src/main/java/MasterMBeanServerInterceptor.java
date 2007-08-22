/*
 * @(#)file      MasterMBeanServerInterceptor.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.6
 * @(#)lastedit  04/04/21
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import com.sun.jmx.interceptor.DefaultMBeanServerInterceptor;
import com.sun.jmx.interceptor.MBeanServerInterceptor;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements a MasterMBeanServerInterceptor that receives all the requests
 * from the MBeanServer and forwards them to some sub-interceptors.
 * <p/>
 * This MasterMBeanServerInterceptor forwards all the requests to either:
 * <ul>
 * <li>the default MBeanServerInterceptor, or</li>
 * <li>the <i>other</i> MBeanServerInterceptor.</li>
 * </ul>
 * It decides to forward to one or the other depending on the value
 * of the <i>domain</i> part of the ObjectName.
 *
 * @see DefaultMBeanServerInterceptor
 */
public class MasterMBeanServerInterceptor implements MBeanServerInterceptor {

    // The default interceptor
    //
    private final MBeanServerInterceptor defaultInterceptor;

    // The other interceptor
    //
    private final MBeanServerInterceptor otherInterceptor;

    // The domain managed by the otherInterceptor
    //
    private final String otherDomain;

    /**
     * Construct a new MasterMBeanServerInterceptor.
     *
     * @param defaultInterceptor The default MBeanServerInterceptor.
     * @param otherDomain        The domain managed by the
     *                           <var>otherInterceptor</var>.
     * @parem otherInterceptor   The other MBeanServerInterceptor.
     */
    public MasterMBeanServerInterceptor(
        MBeanServerInterceptor defaultInterceptor,
        MBeanServerInterceptor otherInterceptor,
        String otherDomain) {
        this.defaultInterceptor = defaultInterceptor;
        this.otherInterceptor = otherInterceptor;
        this.otherDomain = otherDomain;
        checkInitialization();
    }

    /**
     * Returns the default MBeanServerInterceptor.
     */
    protected final synchronized MBeanServerInterceptor defaultInterceptor() {
        return defaultInterceptor;
    }

    /**
     * Returns the other MBeanServerInterceptor.
     */
    protected final synchronized MBeanServerInterceptor otherInterceptor() {
        return otherInterceptor;
    }

    /**
     * Check that this MasterMBeanServerInterceptor is correctly initialized.
     *
     * @throws IllegalArgumentException if the MasterMBeanServerInterceptor
     *                                  is not correctly initialized.
     */
    protected void checkInitialization() throws IllegalArgumentException {
        if (defaultInterceptor == null ||
            otherInterceptor == null ||
            otherDomain == null)
            throw new IllegalArgumentException("Null parameter");
    }

    /**
     * Returns the MBeanServerInterceptor managing the given ObjectName.
     * Practically, this method returns the <var>otherInterceptor</var>
     * iff the domain part of the given <var>name</var> is equal to the
     * the domain managed by the <var>otherInterceptor</var>.
     * Otherwise, it returns the default MBeanServerInterceptor.
     *
     * @param name The name of the MBean we want to access.
     * @return The MBeanServerInterceptor in which the MBean may be found.
     */
    private MBeanServerInterceptor getMBeanServerInterceptor(
        ObjectName name) {
        if (name == null) return defaultInterceptor();
        if (name.getDomain().equals(otherDomain))
            return otherInterceptor();
        return defaultInterceptor();
    }

    /**
     * Check whether an MBean with that name can be registered in the
     * MBeanServer.
     * <p/>
     * This method will reject any creation/registration of MBeans within
     * the domain managed by the <var>otherInterceptor</var>.
     *
     * @param name The ObjectName of the MBean we wish to register.
     *             This is the ObjectName that was passed to
     *             <code>createMBean()</code> or <code>registerMBean()</code>.
     * @throws InstanceAlreadyExistsException if an MBean of that name
     *                                        is already registered.
     * @throws MBeanRegistrationException     to simulate a registration
     *                                        failure.
     */
    protected void checkRegistration(ObjectName name)
        throws InstanceAlreadyExistsException, MBeanRegistrationException {
        if (name == null) return;
        if (name.getDomain().equals(otherDomain)) {
            final RuntimeException x =
                new UnsupportedOperationException(otherDomain +
                    ": Can't register an MBean in that domain.");
            throw new MBeanRegistrationException(x, "Registration failed.");
        }
    }

    public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanException, NotCompliantMBeanException, IOException {
        final MBeanServerInterceptor interceptor = getMBeanServerInterceptor(name);
        return interceptor.createMBean(className, name);
    }

    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException {
        final MBeanServerInterceptor interceptor = getMBeanServerInterceptor(name);
        return interceptor.createMBean(className, name, loaderName);
    }

    // Forward to the appropriate interceptor.
    //
    public final ObjectInstance createMBean(final String className,
                                            final ObjectName name,
                                            final Object params[],
                                            final String signature[])
        throws ReflectionException, InstanceAlreadyExistsException,
        MBeanException,
        NotCompliantMBeanException {

        checkRegistration(name);

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        return interceptor.createMBean(className, name, params, signature);
    }

    // Forward to the appropriate interceptor.
    //
    public final ObjectInstance createMBean(final String className,
                                            final ObjectName name,
                                            final ObjectName loaderName,
                                            final Object params[],
                                            final String signature[])
        throws ReflectionException, InstanceAlreadyExistsException,
        MBeanException,
        NotCompliantMBeanException, InstanceNotFoundException {

        checkRegistration(name);

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);

        return interceptor.createMBean(className, name, loaderName,
            params, signature);
    }

    // Forward to the appropriate interceptor.
    //
    public final ObjectInstance getObjectInstance(final ObjectName name)
        throws InstanceNotFoundException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);

        return interceptor.getObjectInstance(name);
    }

    // Forward to both interceptors and merge the results.
    //
    public final Set<ObjectInstance> queryMBeans(final ObjectName name,
                                                 final QueryExp query) {

        final MBeanServerInterceptor defaultInterceptor = defaultInterceptor();
        final MBeanServerInterceptor otherInterceptor = otherInterceptor();
        final Set<ObjectInstance> result = new HashSet<ObjectInstance>();
        result.addAll(defaultInterceptor.queryMBeans(name, query));
        result.addAll(otherInterceptor.queryMBeans(name, query));
        return debugSet(result);
    }

    // Forward to both interceptors and merge the results.
    //
    public final Set<ObjectName> queryNames(final ObjectName name,
                                            final QueryExp query) {

        final MBeanServerInterceptor defaultInterceptor = defaultInterceptor();
        final MBeanServerInterceptor otherInterceptor = otherInterceptor();
        final Set<ObjectName> result = new HashSet<ObjectName>();
        result.addAll(defaultInterceptor.queryNames(name, query));
        result.addAll(otherInterceptor.queryNames(name, query));
        return debugSet(result);
    }

    /**
     * Returns the default interceptor's default domain name.
     *
     * @return the default Interceptor's default domain name.
     */
    public final String getDefaultDomain() {
        final MBeanServerInterceptor defaultInterceptor = defaultInterceptor();
        return defaultInterceptor.getDefaultDomain();
    }

    // Forward to both interceptors and merge the results.
    // By default, this method calls getDomainsByDomains().
    //
    public String[] getDomains() {
        return getDomainsByDomains();
    }

    /**
     * This method gets the domains by successively asking the default
     * MBeanServerInterceptor and the other MBeanServerInterceptor in
     * the list to get its own domains. It then returns the merge of
     * all the obtained results.
     */
    protected final String[] getDomainsByDomains() {

        final MBeanServerInterceptor defaultInterceptor = defaultInterceptor();
        final MBeanServerInterceptor otherInterceptor = otherInterceptor();

        final String[] defaultDomains = defaultInterceptor.getDomains();
        final String[] otherDomains = otherInterceptor.getDomains();

        List<String> defaultDomainsList = Arrays.asList(defaultDomains);
        List<String> otherDomainsList = Arrays.asList(otherDomains);

        ArrayList<String> al = new ArrayList<String>();
        al.addAll(defaultDomainsList);
        al.addAll(otherDomainsList);

        return al.toArray(new String[al.size()]);
    }

    // Forward to both interceptors and merge the results.
    // By default, this method calls getMBeanCountByNames().
    //
    public Integer getMBeanCount() {
        return getMBeanCountByNames();
    }

    /**
     * This method counts the MBeans by successively asking the default
     * MBeanServerInterceptor and the other MBeanServerInterceptor in the list
     * to count its own MBeans. It then returns the sum of all the obtained
     * results. This method of counting may not be accurate if some MBeans
     * in one interceptor are "shadowed" by another interceptor. This is
     * why {@link #getMBeanCount()} calls by default
     * {@link #getMBeanCountByNames()}. If you wish to change this behavior
     * simply redefine {@link #getMBeanCount()} so that it calls this method:
     * <pre>
     *     public Integer getMBeanCount() {
     *         return getMBeanCountByCount();
     *     }
     * </pre>
     */
    protected final Integer getMBeanCountByCount() {

        final MBeanServerInterceptor defaultInterceptor = defaultInterceptor();
        final MBeanServerInterceptor otherInterceptor = otherInterceptor();
        int count = 0;
        final Integer defaultCount = defaultInterceptor.getMBeanCount();
        if (defaultCount != null) {
            if (defaultCount > 0) count += defaultCount;
        }
        final Integer otherCount = otherInterceptor.getMBeanCount();
        if (otherCount != null) {
            if (otherCount > 0) count += otherCount;
        }
        return count;
    }

    /**
     * This method counts the MBeans by calling
     * <code>queryNames(null,null)</code> and then counting the names in the
     * returned set. This method of counting is always accurate even if some
     * MBeans in one interceptor are "shadowed" by another interceptor.
     * This is why {@link #getMBeanCount()} calls by default
     * {@link #getMBeanCountByNames()}. If you wish to change this behavior
     * simply redefine {@link #getMBeanCount()} so that it calls the more
     * intuitive {@link #getMBeanCountByCount()} method:
     * <pre>
     *     public Integer getMBeanCount() {
     *         return getMBeanCountByCount();
     *     }
     * </pre>
     */
    protected Integer getMBeanCountByNames() {
        final Set names = queryNames(null, null);
        return (names == null ? 0 : names.size());
    }

    // Forwards to the defaultInterceptor, and if the object is not found
    // there, forwards to the otherInterceptor.
    //
    public final boolean isRegistered(final ObjectName name) {

        final MBeanServerInterceptor defaultInterceptor = defaultInterceptor();
        final MBeanServerInterceptor otherInterceptor = otherInterceptor();

        return defaultInterceptor.isRegistered(name) || otherInterceptor.isRegistered(name);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final boolean isInstanceOf(final ObjectName name,
                                      final String className)
        throws InstanceNotFoundException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        return interceptor.isInstanceOf(name, className);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final ObjectInstance registerMBean(final Object object,
                                              final ObjectName name)
        throws InstanceAlreadyExistsException,
        MBeanRegistrationException,
        NotCompliantMBeanException {

        checkRegistration(name);

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        return interceptor.registerMBean(object, name);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void addNotificationListener(final ObjectName name,
                                              final NotificationListener listener,
                                              final NotificationFilter filter,
                                              final Object handback)
        throws InstanceNotFoundException {

        final MBeanServerInterceptor interceptor = getMBeanServerInterceptor(name);
        interceptor.addNotificationListener(name, listener,
            filter, handback);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void addNotificationListener(final ObjectName name,
                                              final ObjectName listener,
                                              final NotificationFilter filter,
                                              final Object handback)
        throws InstanceNotFoundException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        interceptor.addNotificationListener(name, listener, filter, handback);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void removeNotificationListener(
        final ObjectName name,
        final NotificationListener listener)
        throws InstanceNotFoundException, ListenerNotFoundException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        interceptor.removeNotificationListener(name, listener);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void removeNotificationListener(
        final ObjectName name,
        final ObjectName listener)
        throws InstanceNotFoundException, ListenerNotFoundException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        interceptor.removeNotificationListener(name, listener);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void removeNotificationListener(
        final ObjectName name,
        final NotificationListener listener,
        final NotificationFilter filter,
        final Object handback)
        throws InstanceNotFoundException, ListenerNotFoundException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        interceptor.removeNotificationListener(name, listener,
            filter, handback);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void removeNotificationListener(
        final ObjectName name,
        final ObjectName listener,
        final NotificationFilter filter,
        final Object handback)
        throws InstanceNotFoundException, ListenerNotFoundException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        interceptor.removeNotificationListener(name, listener,
            filter, handback);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void unregisterMBean(final ObjectName name)
        throws InstanceNotFoundException, MBeanRegistrationException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        interceptor.unregisterMBean(name);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final Object getAttribute(final ObjectName name,
                                     final String attribute)
        throws MBeanException, AttributeNotFoundException,
        InstanceNotFoundException, ReflectionException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        return interceptor.getAttribute(name, attribute);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final AttributeList getAttributes(final ObjectName name,
                                             final String[] attributes)
        throws InstanceNotFoundException, ReflectionException {
        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        return interceptor.getAttributes(name, attributes);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void setAttribute(final ObjectName name,
                                   final Attribute attribute)
        throws InstanceNotFoundException, AttributeNotFoundException,
        InvalidAttributeValueException, MBeanException,
        ReflectionException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        interceptor.setAttribute(name, attribute);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final AttributeList setAttributes(final ObjectName name,
                                             final AttributeList attributes)
        throws InstanceNotFoundException, ReflectionException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        return interceptor.setAttributes(name, attributes);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final Object invoke(final ObjectName name,
                               final String operationName,
                               final Object params[],
                               final String signature[])
        throws InstanceNotFoundException, MBeanException, ReflectionException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        return interceptor.invoke(name, operationName, params, signature);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final MBeanInfo getMBeanInfo(final ObjectName name)
        throws InstanceNotFoundException,
        IntrospectionException,
        ReflectionException {
        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);
        return interceptor.getMBeanInfo(name);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final ClassLoader getClassLoader(final ObjectName loaderName)
        throws InstanceNotFoundException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(loaderName);

        return interceptor.getClassLoader(loaderName);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final ClassLoader getClassLoaderFor(final ObjectName name)
        throws InstanceNotFoundException {

        final MBeanServerInterceptor interceptor =
            getMBeanServerInterceptor(name);

        return interceptor.getClassLoaderFor(name);
    }

    // Debug the content of a Set
    //
    private static <T> Set<T> debugSet(Set<T> result) {
        if (isDebugOn() && result != null) {
            for (T n : result) {
                if (n instanceof ObjectInstance) {
                    final ObjectInstance o = (ObjectInstance) n;
                    debug("ObjectInstance", o.getObjectName() + " [" +
                        o.getClassName() + "] (" + o + ")");
                } else {
                    debug(n.getClass().getName(), n.toString());
                }
            }
        }
        return result;
    }

    //
    // Debug
    //

    private static final boolean debugOn = false;
    private static final String debugTag = "MasterMBeanServerInterceptor";

    private static boolean isDebugOn() {
        return debugOn;
    }

    private static void debug(String func, String info) {
        if (isDebugOn())
            System.out.println(debugTag + "::" + func + "::" + info);
    }
}
