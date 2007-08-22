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
    }

    /**
     * Returns the default MBeanServerInterceptor.
     */
    protected final MBeanServerInterceptor defaultInterceptor() {
        return defaultInterceptor;
    }

    /**
     * Returns the other MBeanServerInterceptor.
     */
    protected final MBeanServerInterceptor otherInterceptor() {
        return otherInterceptor;
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
    private MBeanServerInterceptor choose(ObjectName name) {
        if (name == null) return defaultInterceptor;
        if (name.getDomain().equals(otherDomain)) return otherInterceptor;
        return defaultInterceptor;
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
    private void checkRegistration(ObjectName name)
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
        checkRegistration(name);
        return choose(name).createMBean(className, name);
    }

    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException {
        checkRegistration(name);
        return choose(name).createMBean(className, name, loaderName);
    }

    // Forward to the appropriate interceptor.
    //
    public final ObjectInstance createMBean(String className, ObjectName name, Object params[], String signature[])
        throws ReflectionException, InstanceAlreadyExistsException, MBeanException, NotCompliantMBeanException {

        checkRegistration(name);
        return choose(name).createMBean(className, name, params, signature);
    }

    // Forward to the appropriate interceptor.
    //
    public final ObjectInstance createMBean(final String className,
                                            final ObjectName name,
                                            final ObjectName loaderName,
                                            final Object params[],
                                            final String signature[])
        throws ReflectionException, InstanceAlreadyExistsException,
        MBeanException, NotCompliantMBeanException, InstanceNotFoundException {

        checkRegistration(name);
        return choose(name).createMBean(className, name, loaderName,
            params, signature);
    }

    // Forward to the appropriate interceptor.
    //
    public final ObjectInstance getObjectInstance(final ObjectName name) throws InstanceNotFoundException {
        return choose(name).getObjectInstance(name);
    }

    private <T> Set<T> union(Set<T> lhs, Set<T> rhs) {
        Set<T> result = new HashSet<T>();
        result.addAll(lhs);
        result.addAll(rhs);
        return result;
    }

    private <T> List<T> union(List<T> lhs, List<T> rhs) {
        List<T> result = new ArrayList<T>();
        result.addAll(lhs);
        result.addAll(rhs);
        return result;
    }

    // Forward to both interceptors and merge the results.
    //
    public final Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) {
        return union(
            defaultInterceptor.queryMBeans(name, query),
            otherInterceptor.queryMBeans(name, query)
        );
    }

    // Forward to both interceptors and merge the results.
    //
    public final Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
        return union(
            defaultInterceptor.queryNames(name, query),
            otherInterceptor.queryNames(name, query)
        );
    }

    /**
     * Returns the default interceptor's default domain name.
     *
     * @return the default Interceptor's default domain name.
     */
    public final String getDefaultDomain() {
        return defaultInterceptor.getDefaultDomain();
    }

    // Forward to both interceptors and merge the results.
    // By default, this method calls getDomainsByDomains().
    //
    public String[] getDomains() {
        return union(
            Arrays.asList(defaultInterceptor.getDomains()),
            Arrays.asList(otherInterceptor.getDomains())
        ).toArray(new String[0]);
    }

    // Forward to both interceptors and merge the results.
    // By default, this method calls getMBeanCountByNames().
    //
    public Integer getMBeanCount() {
        return normalize(defaultInterceptor.getMBeanCount())+normalize(otherInterceptor.getMBeanCount());
    }
    
    private int normalize(Integer i) {
        if(i==null || i<0)  return 0;
        return i;
    }

    // Forwards to the defaultInterceptor, and if the object is not found
    // there, forwards to the otherInterceptor.
    //
    public final boolean isRegistered(final ObjectName name) {
        return defaultInterceptor.isRegistered(name) || otherInterceptor.isRegistered(name);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException {
        return choose(name).isInstanceOf(name, className);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final ObjectInstance registerMBean(Object object, ObjectName name)
        throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {

        checkRegistration(name);
        return choose(name).registerMBean(object, name);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
        throws InstanceNotFoundException {
        choose(name).addNotificationListener(name, listener, filter, handback);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void addNotificationListener(final ObjectName name,
                                              final ObjectName listener,
                                              final NotificationFilter filter,
                                              final Object handback)
        throws InstanceNotFoundException {
        choose(name).addNotificationListener(name, listener, filter, handback);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void removeNotificationListener(
        final ObjectName name,
        final NotificationListener listener)
        throws InstanceNotFoundException, ListenerNotFoundException {

        choose(name).removeNotificationListener(name, listener);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void removeNotificationListener(
        final ObjectName name,
        final ObjectName listener)
        throws InstanceNotFoundException, ListenerNotFoundException {

        choose(name).removeNotificationListener(name, listener);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void removeNotificationListener(
        final ObjectName name,
        final NotificationListener listener,
        final NotificationFilter filter,
        final Object handback)
        throws InstanceNotFoundException, ListenerNotFoundException {

        choose(name).removeNotificationListener(name, listener, filter, handback);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void removeNotificationListener(
        final ObjectName name,
        final ObjectName listener,
        final NotificationFilter filter,
        final Object handback)
        throws InstanceNotFoundException, ListenerNotFoundException {

        choose(name).removeNotificationListener(name, listener, filter, handback);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void unregisterMBean(final ObjectName name)
        throws InstanceNotFoundException, MBeanRegistrationException {

        choose(name).unregisterMBean(name);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final Object getAttribute(final ObjectName name,
                                     final String attribute)
        throws MBeanException, AttributeNotFoundException,
        InstanceNotFoundException, ReflectionException {

        return choose(name).getAttribute(name, attribute);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final AttributeList getAttributes(final ObjectName name,
                                             final String[] attributes)
        throws InstanceNotFoundException, ReflectionException {
        return choose(name).getAttributes(name, attributes);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final void setAttribute(final ObjectName name,
                                   final Attribute attribute)
        throws InstanceNotFoundException, AttributeNotFoundException,
        InvalidAttributeValueException, MBeanException,
        ReflectionException {

        choose(name).setAttribute(name, attribute);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final AttributeList setAttributes(final ObjectName name,
                                             final AttributeList attributes)
        throws InstanceNotFoundException, ReflectionException {

        return choose(name).setAttributes(name, attributes);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final Object invoke(final ObjectName name,
                               final String operationName,
                               final Object params[],
                               final String signature[])
        throws InstanceNotFoundException, MBeanException, ReflectionException {

        return choose(name).invoke(name, operationName, params, signature);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
        return choose(name).getMBeanInfo(name);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final ClassLoader getClassLoader(final ObjectName loaderName) throws InstanceNotFoundException {
        return choose(loaderName).getClassLoader(loaderName);
    }

    // Forwards to the appropriate MBeanServerInterceptor.
    //
    public final ClassLoader getClassLoaderFor(ObjectName name) throws InstanceNotFoundException {
        return choose(name).getClassLoaderFor(name);
    }
}
