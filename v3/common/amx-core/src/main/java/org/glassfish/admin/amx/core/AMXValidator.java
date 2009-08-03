/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * accompanied this code.  If applicable, add the following below the Licensep
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
package org.glassfish.admin.amx.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerInvocationHandler;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenType;
import javax.management.remote.JMXServiceURL;
import org.glassfish.admin.amx.annotation.Stability;
import org.glassfish.admin.amx.annotation.Taxonomy;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.base.Pathnames;
import org.glassfish.admin.amx.base.MBeanTrackerMBean;

import static org.glassfish.admin.amx.core.PathnameConstants.*;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import static org.glassfish.external.amx.AMX.*;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.jmx.JMXUtil;

/**
Validation of key behavioral requirements of AMX MBeans.
These tests do not validate any MBean-specific semantics, only general requirements for all AMX MBeans.
<p>
Note that all tests have to account for the possibility that an MBean can be unregistered while
the validation is in progress— that is not a test failure, since it is perfectly legal.
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
public final class AMXValidator
{
    private static void debug(final Object o)
    {
        System.out.println(o.toString());
    }

    private static final String NL = StringUtil.NEWLINE();

    private final MBeanServerConnection mMBeanServer;

    private final ProxyFactory mProxyFactory;

    private final DomainRoot mDomainRoot;
    
    // created if needed
    private MBeanTrackerMBean  mMBeanTracker;

    public AMXValidator(final MBeanServerConnection conn)
    {
        mMBeanServer = conn;

        mProxyFactory = ProxyFactory.getInstance(conn);
        mDomainRoot = mProxyFactory.getDomainRootProxy(false);
    }

    private static final class IllegalClassException extends Exception
    {
        private final Class<?> mClass;

        public IllegalClassException(final Class<?> clazz)
        {
            super("Class " + clazz.getName() + " not allowed for AMX MBeans");
            mClass = clazz;
        }

        public Class<?> clazz()
        {
            return mClass;
        }

        public String toString()
        {
            return super.getMessage();
        }

    }

    private static final class ValidationFailureException extends Exception
    {
        private final ObjectName mObjectName;

        public ValidationFailureException(final ObjectName objectName, final String msg)
        {
            super(msg);
            mObjectName = objectName;
        }

        public ValidationFailureException(final AMXProxy amx, final String msg)
        {
            this(amx.objectName(), msg);
        }

        public ObjectName objectName()
        {
            return mObjectName;
        }

        public String toString()
        {
            return getMessage() + ", " + mObjectName;
        }

    }

    private static final class Failures
    {
        private final ConcurrentMap<ObjectName, List<String>> mFailures = new ConcurrentHashMap<ObjectName, List<String>>();

        private AtomicInteger mNumTested = new AtomicInteger();

        public Failures()
        {
        }

        public int getNumTested()
        {
            return mNumTested.get();
        }

        public int getNumFailures()
        {
            return mFailures.keySet().size();
        }

        public Map<ObjectName, List<String>> getFailures()
        {
            return mFailures;
        }

        void result(final ObjectName objectName, final List<String> problems)
        {
            mNumTested.incrementAndGet();

            if (problems != null && problems.size() != 0)
            {
                mFailures.put(objectName, problems);
            }
        }

        public String toString()
        {
            final StringBuilder builder = new StringBuilder();

            for (final ObjectName badBoy : mFailures.keySet())
            {
                final List<String> failures = mFailures.get(badBoy);

                builder.append(badBoy + NL);
                builder.append(CollectionUtil.toString(failures, NL));
                builder.append(NL);
                builder.append(NL);
            }
            builder.append(mFailures.size() + " failures.");

            return builder.toString() + NL + mNumTested + " MBeans tested.";
        }

    }

    private String toString(final Throwable t)
    {
        return ExceptionUtil.toString(ExceptionUtil.getRootCause(t));
    }

    /** types that are not open types, but that we deem acceptable for a remote API */
    private static Set<Class> EXTRA_ALLOWED_TYPES = SetUtil.newTypedSet(
            (Class) JMXServiceURL.class,
            CompositeDataSupport.class 
    );

    private static boolean isAcceptableRemoteType(final Class<?> c)
    {
        if (c.isPrimitive() ||
            EXTRA_ALLOWED_TYPES.contains(c) ||
            OpenType.ALLOWED_CLASSNAMES_LIST.contains(c.getName()))
        {
            return true;
        }

        // quick checks for other common cases
        if (c.isArray() && isAcceptableRemoteType(c.getComponentType()))
        {
            return true;
        }

        return false;
    }

    /**
    "best effort"<p>
    Attributes that cannot be sent to generic clients are not allowed.
    More than OpenTypes are allowed eg messy stuff like JSR 77 Stats and Statistics.
     */
    private static void checkLegalForRemote(final Object value) throws IllegalClassException
    {
        if (value == null)
        {
            return;
        }
        final Class<?> clazz = value.getClass();
        if (isAcceptableRemoteType(clazz))
        {
            return;
        }

        // would these always be disallowed?
        if (clazz.isSynthetic() || clazz.isLocalClass() || clazz.isAnonymousClass() || clazz.isMemberClass())
        {
            throw new IllegalClassException(clazz);
        }

        if (clazz.isArray())
        {
            if (!isAcceptableRemoteType(clazz.getComponentType()))
            {
                final Object[] a = (Object[]) value;
                for (final Object o : a)
                {
                    checkLegalForRemote(o);
                }
            }
        }
        else if (Collection.class.isAssignableFrom(clazz))
        {
            final Collection<?> items = (Collection) value;
            for (final Object o : items)
            {
                checkLegalForRemote(o);
            }
        }
        else if (Map.class.isAssignableFrom(clazz))
        {
            final Map<?, ?> items = (Map) value;
            for (final Object key : items.keySet())
            {
                checkLegalForRemote(key);
                checkLegalForRemote(items.get(key));
            }
        }
        else
        {
            throw new IllegalClassException(clazz);
        }
    }

    boolean instanceNotFound(final Throwable t )
    {
        return ExceptionUtil.getRootCause(t) instanceof InstanceNotFoundException;
    }
    
    private void addToProblems( final String msg, final List<String> problems, final Throwable t )
    {
        // it's not an issue if the MBean went missing
        final Throwable rootCause = ExceptionUtil.getRootCause(t);
        if ( ! instanceNotFound(rootCause) )
        {
            problems.add( msg + rootCause.toString() );
        }
    }
    
    private void addToProblems( final List<String> problems, final Throwable t )
    {
        addToProblems( "", problems, t );
    }
    
    private List<String> _validate(final AMXProxy proxy)
    {
        //debug( "Validate: " + proxy.objectName() );
        final List<String> problems = new ArrayList<String>();
        final ObjectName objectName = proxy.objectName();

        try
        {
            validateObjectName(proxy);
        }
        catch (Throwable t)
        {
            addToProblems( problems, t);
        }

        List<String> temp = null;
        try
        {
            temp = validateMetadata(proxy);
            if (temp != null)
            {
                problems.addAll(temp);
            }
        }
        catch (Throwable t)
        {
            addToProblems( problems, t);
        }

        try
        {
            validateRequiredAttributes(proxy);
        }
        catch (Throwable t)
        {
            addToProblems( problems, t);
        }


        // test required attributes
        try
        {
            final String name = proxy.getName();
        }
        catch (Throwable t)
        {
            addToProblems( "Proxy access to 'Name' failed: ", problems, t);
        }

        try
        {
            final ObjectName parent = proxy.getParent();
        }
        catch (Throwable t)
        {
            addToProblems( "Proxy access to 'Parent' failed: ", problems, t);
        }
        try
        {
            final ObjectName[] children = proxy.getChildren();
        }
        catch (Throwable t)
        {
            addToProblems( "Proxy access to 'Children' failed: ", problems, t);
        }


        // test path resolution
        final Pathnames paths = mDomainRoot.getPathnames();
        try
        {
            final String path = proxy.path();
            final ObjectName actualObjectName = proxy.objectName();

            final ObjectName o = paths.resolvePath(path);
            if (o == null)
            {
                if ( proxy.valid() )   // could have been unregistered
                {
                    problems.add("Path " + path + " does not resolve to any ObjectName, should resolve to: " + actualObjectName);
                }
            }
            else if (!actualObjectName.equals(o))
            {
                problems.add("Path " + path + " does not resolve to ObjectName: " + actualObjectName);
            }
        }
        catch (Throwable t)
        {
            addToProblems( problems, t);
        }

        // test attributes
        final Set<String> attributeNames = proxy.extra().attributeNames();
        for (final String attrName : attributeNames)
        {
            try
            {
                final Object result = proxy.extra().getAttribute(attrName);

                checkLegalForRemote(result);
            }
            catch (final Throwable t)
            {
                addToProblems( "Attribute failed: '" + attrName + "': ", problems, t);
            }
        }

        List<String> tempProblems = null;
        try
        {
            validateChildren(proxy);
        }
        catch (Throwable t)
        {
            addToProblems( problems, t);
        }

        // test proxy methods
        try
        {
            final AMXProxy parent = proxy.parent();
            if (parent == null && !proxy.type().equals(Util.deduceType(DomainRoot.class)))
            {
                throw new Exception("Null parent");
            }

            final String nameProp = proxy.nameProp();
            final boolean valid = proxy.valid();
            final String path = proxy.path();
            final Extra extra = proxy.extra();

            final String interfaceName = extra.interfaceName();
            final MBeanInfo mbeanInfo = extra.mbeanInfo();
            final String group = extra.group();
            final Class<? extends AMXProxy> genericInterface = extra.genericInterface();
            final boolean invariantMBeanInfo = extra.isInvariantMBeanInfo();
            final boolean supportsAdoption = extra.supportsAdoption();
            final String[] subTypes = extra.subTypes();

            final Set<AMXProxy> childrenSet = proxy.childrenSet();
            final Map<String, Map<String, AMXProxy>> childrenMaps = proxy.childrenMaps();
            final Map<String, Object> attributesMap = proxy.attributesMap();
            final Set<String> attrNames = proxy.attributeNames();
            if (!attrNames.equals(attributesMap.keySet()))
            {
                throw new Exception("Attributes Map differs from attribute names");
            }

            for (final AMXProxy child : childrenSet)
            {
                if (child.extra().singleton())
                {
                    final String childType = child.type();
                    if (!child.objectName().equals(proxy.child(childType).objectName()))
                    {
                        throw new Exception("Child type " + childType + " cannot be found via child(type)");
                    }
                }
            }

            for (final String type : childrenMaps.keySet())
            {
                final Map<String, AMXProxy> m = proxy.childrenMap(type);
                if (m.keySet().size() == 0)
                {
                    throw new Exception("Child type " + type + " has nothing in Map");
                }
            }

        }
        catch (final Throwable t)
        {
            addToProblems( "General test failure: ", problems, t);
        }


        try
        {
            validateAMXConfig(proxy, problems);
        }
        catch (Throwable t)
        {
            addToProblems( "General test failure in validateAMXConfig: ", problems, t);
        }

        return problems;
    }

    private void fail(final ObjectName objectName, final String msg)
            throws ValidationFailureException
    {
        throw new ValidationFailureException(objectName, msg);
    }

    private void fail(final AMXProxy amx, final String msg)
            throws ValidationFailureException
    {
        throw new ValidationFailureException(amx, msg);
    }

    private void validateAMXConfig(final AMXProxy proxy, final List<String> problems)
    {
        if (!AMXConfigProxy.class.isAssignableFrom(proxy.extra().genericInterface()))
        {
            return;
        }

        // check default values support
        final AMXConfigProxy config = proxy.as(AMXConfigProxy.class);
        final Map<String, String> defaultValues = config.getDefaultValues(false);
        final Map<String, String> defaultValuesAMX = config.getDefaultValues(true);
        if (defaultValues.keySet().size() != defaultValuesAMX.keySet().size())
        {
            problems.add("Default values for AMX names differ in number from XML names: " + defaultValues.keySet().size() + " != " + defaultValuesAMX.keySet().size());
        }
        for (final String key : defaultValues.keySet())
        {
            final Object value = defaultValues.get(key);
            if (value == null)
            {
                problems.add("Default value of null for: " + key);
            }
            else if (!(value instanceof String))
            {
                problems.add("Default value is not a String for: " + key);
            }
        }

        final String[] subTypes = config.extra().subTypes();
        if (subTypes != null)
        {
            for (final String subType : subTypes)
            {
                final Map<String, String> subTypeDefaults = config.getDefaultValues(subType, false);
            }
        }
    }

    private static final Pattern TYPE_PATTERN = Pattern.compile(LEGAL_TYPE_PATTERN);

    private static final Pattern NAME_PATTERN = Pattern.compile(LEGAL_NAME_PATTERN);

    private void validateObjectName(final AMXProxy proxy)
            throws ValidationFailureException
    {
        final ObjectName objectName = proxy.objectName();

        final String type = objectName.getKeyProperty("type");
        if (type == null || type.length() == 0)
        {
            fail(objectName, "type property required in ObjectName");
        }
        if (!TYPE_PATTERN.matcher(type).matches())
        {
            fail(objectName, "Illegal type \"" + type + "\", does not match " + TYPE_PATTERN.pattern());
        }

        final String nameProp = objectName.getKeyProperty("name");
        if (nameProp != null)
        {
            if (nameProp.length() == 0)
            {
                fail(objectName, "name property of ObjectName may not be empty");
            }
            if (!NAME_PATTERN.matcher(nameProp).matches())
            {
                fail(objectName, "Illegal name \"" + nameProp + "\", does not match " + NAME_PATTERN.pattern());
            }
        }
        else
        {
            // no name property, it's by definition a singleton
            final String name = proxy.getName();
            if (!name.equals(NO_NAME))
            {
                fail(objectName, "getName() returned a non-empty name for a singleton: " + name);
            }
            if (!proxy.extra().singleton())
            {
                fail(objectName, "Metadata claims named (non-singleton), but no name property present in ObjectName");
            }
        }

        if (proxy.parent() != null)
        {
            if (!proxy.parentPath().equals(proxy.parent().path()))
            {
                fail(objectName, "Parent path of " + proxy.parentPath() + " does not match parent's path for " + proxy.parent().objectName());
            }
        }
    }

    /** verify that the children/parent relationship exists */
    private void validateChildren(final AMXProxy proxy)
            throws ValidationFailureException
    {
        final Set<String> attrNames = proxy.attributeNames();
        if (!attrNames.contains(ATTR_CHILDREN))
        {
            // must NOT supply Children
            try
            {
                final ObjectName[] children = proxy.getChildren();
                fail(proxy, "MBean has no Children attribute in its MBeanInfo, but supplies the attribute");
            }
            catch (Exception e)
            {
                // good, the Attribute must not exist
            }
        }
        else
        {
            // must supply Children
            try
            {
                final ObjectName[] children = proxy.getChildren();
                if (children == null)
                {
                    fail(proxy, "Children attribute must be non-null");
                }
                final Set<ObjectName> childrenSet = SetUtil.newSet(children);
                if ( childrenSet.size() != children.length )
                {
                    fail(proxy, "Children contains duplicates");
                }
                if ( childrenSet.contains(null) )
                {
                    fail(proxy, "Children contains null");
                }

                // verify that each child is non-null and references its parent
                for (final ObjectName childObjectName : children)
                {
                    if (childObjectName == null)
                    {
                        fail(proxy, "Child in Children array is null");
                    }
                    final AMXProxy child = mProxyFactory.getProxy(childObjectName);
                    if (!proxy.objectName().equals(child.parent().objectName()))
                    {
                        fail(proxy, "Child’s Parent of " + child.parent().objectName() +
                                    " does not match the actual parent of " + proxy.objectName());
                    }
                }

                // verify that the children types do not differ only by case-sensitivity
                final Set<String> caseSensitiveTypes = new HashSet<String>();
                final Set<String> caseInsensitiveTypes = new HashSet<String>();
                for (final ObjectName o : children)
                {
                    caseSensitiveTypes.add(Util.getTypeProp(o));
                    caseInsensitiveTypes.add(Util.getTypeProp(o).toLowerCase());
                }
                if (caseSensitiveTypes.size() != caseInsensitiveTypes.size())
                {
                    fail(proxy, "Children types must be case-insensitive");
                }
                
                // verify that the MBeanTracker agrees with the parent MBean
                final Set<ObjectName> tracked = getMBeanTracker().getChildrenOf(proxy.objectName()); 
                if ( childrenSet.size() != children.length )
                {
                    // try again, in case it's a timing issue
                    final Set<ObjectName> childrenSetNow = SetUtil.newSet( proxy.getChildren() );
                    if ( ! tracked.equals( childrenSetNow ) )
                    {
                        fail(proxy, "MBeanTracker has different MBeans than the MBean: {" + 
                            CollectionUtil.toString(tracked, ", ") + "} vs MBean having {" +
                            CollectionUtil.toString(childrenSetNow, ", ") + "}");
                    }
                }
            }
            catch (final Exception e)
            {
                if ( ! instanceNotFound(e) )
                {
                    fail(proxy, "MBean failed to supply Children attribute");
                }
            }

            // children of the same type must have the same MBeanInfo
            try
            {
                final Map<String, Map<String, AMXProxy>> maps = proxy.childrenMaps();

                for (final String type : maps.keySet())
                {
                    final Map<String, AMXProxy> siblings = maps.get(type);
                    if (siblings.keySet().size() > 1)
                    {
                        final Iterator<AMXProxy> iter = siblings.values().iterator();
                        final MBeanInfo mbeanInfo = iter.next().extra().mbeanInfo();
                        while (iter.hasNext())
                        {
                            final AMXProxy next = iter.next();
                            if (!mbeanInfo.equals(next.extra().mbeanInfo()))
                            {
                                fail(proxy, "Children of type " + type + " must  have the same MBeanInfo");
                            }
                        }
                    }
                }
            }
            catch (final Exception e)
            {
                if ( ! instanceNotFound(e) )
                {
                    fail(proxy, "MBean failed validating the MBeanInfo of children");
                }
            }
        }
    }
    
    private MBeanTrackerMBean getMBeanTracker() {
        if ( mMBeanTracker == null )
        {
            mMBeanTracker = MBeanServerInvocationHandler.newProxyInstance(
                mMBeanServer, MBeanTrackerMBean.MBEAN_TRACKER_OBJECT_NAME, MBeanTrackerMBean.class, false);
        }
        return mMBeanTracker;
    }   
    
    private static final class MetadataValidator
    {
        private final Descriptor mDescriptor;

        private final Set<String> mFieldNames;

        private final List<String> mProblems;

        public MetadataValidator(final Descriptor d, final List<String> problems)
        {
            mDescriptor = d;
            mFieldNames = SetUtil.newSet(d.getFieldNames());
            mProblems = problems;

            validateRemote();
        }
        
        // Descriptor fields must be remotable
        void validateRemote()
        {
            for (final String fieldName : mFieldNames)
            {
                try
                {
                    checkLegalForRemote(mDescriptor.getFieldValue(fieldName));
                }
                catch (final IllegalClassException e)
                {
                    mProblems.add("Descriptor field " + fieldName + " uses a remote-unfriendly class: " + e.clazz().getName());
                }
            }
        }

        void validateMetadataBoolean(final String fieldName)
        {
            if (mFieldNames.contains(fieldName))
            {
                final Object value = mDescriptor.getFieldValue(fieldName);
                if (value == null)
                {
                    mProblems.add("Descriptor field " + fieldName + " must not be null");
                }
                else if (!((value instanceof Boolean) || value.equals("true") || value.equals("false")))
                {
                    mProblems.add("Descriptor field " + fieldName + " must be set to 'true' or 'false', value is " + value);
                }
            }
        }

        void validateMetadataStringNonEmpty(final String fieldName)
        {
            if (mFieldNames.contains(fieldName))
            {
                final Object value = mDescriptor.getFieldValue(fieldName);
                if (value == null || (!(value instanceof String)) || ((String) value).length() == 0)
                {
                    mProblems.add("Descriptor field " + fieldName + " must be non-zero length String, value = " + value);
                }
            }
        }

        void validate(final String fieldName, final Class<?> clazz)
        {
            if (mFieldNames.contains(fieldName))
            {
                final Object value = mDescriptor.getFieldValue(fieldName);
                if (value == null || (!(clazz.isAssignableFrom(value.getClass()))))
                {
                    mProblems.add("Descriptor field " + fieldName + " must be of class " + clazz.getSimpleName());
                }
            }
        }

    }

    private List<String> validateMetadata(final AMXProxy proxy)
    {
        final List<String> problems = new ArrayList<String>();

        final MBeanInfo mbeanInfo = proxy.extra().mbeanInfo();
        final Descriptor d = mbeanInfo.getDescriptor();

        // verify that no extraneous field exist
        final Set<String> LEGAL_AMX_DESCRIPTORS = SetUtil.newStringSet(
                DESC_GENERIC_INTERFACE_NAME, DESC_IS_SINGLETON, DESC_IS_GLOBAL_SINGLETON, DESC_GROUP, DESC_SUPPORTS_ADOPTION, DESC_SUB_TYPES);
        for (final String fieldName : d.getFieldNames())
        {
            if (fieldName.startsWith(DESC_PREFIX) && !LEGAL_AMX_DESCRIPTORS.contains(fieldName))
            {
                problems.add("Illegal/unknown AMX metadata field: " + fieldName + " = " + d.getFieldValue(fieldName));
            }
        }

        final MetadataValidator val = new MetadataValidator(d, problems);
        // verify data types
        val.validateMetadataBoolean(DESC_IS_SINGLETON);
        val.validateMetadataBoolean(DESC_SUPPORTS_ADOPTION);
        val.validateMetadataBoolean(DESC_STD_IMMUTABLE_INFO);

        val.validateMetadataStringNonEmpty(DESC_STD_INTERFACE_NAME);
        val.validateMetadataStringNonEmpty(DESC_GENERIC_INTERFACE_NAME);
        val.validateMetadataStringNonEmpty(DESC_GROUP);

        val.validate(DESC_SUB_TYPES, String[].class);

        for (final MBeanAttributeInfo attrInfo : mbeanInfo.getAttributes())
        {
            new MetadataValidator(attrInfo.getDescriptor(), problems);
        }

        for (final MBeanOperationInfo opInfo : mbeanInfo.getOperations())
        {
            new MetadataValidator(opInfo.getDescriptor(), problems);
        }

        for (final MBeanConstructorInfo cosntructorInfo : mbeanInfo.getConstructors())
        {
            new MetadataValidator(cosntructorInfo.getDescriptor(), problems);
        }

        for (final MBeanNotificationInfo notifInfo : mbeanInfo.getNotifications())
        {
            new MetadataValidator(notifInfo.getDescriptor(), problems);
        }

        if ( proxy.extra().globalSingleton() )
        {
            final ObjectName objectName = proxy.objectName();
            //debug( "Global singleton type = " + Util.getTypeProp(objectName) );
            // don't use Query MBean, it might not exist
            final ObjectName pattern = Util.newObjectNamePattern( objectName.getDomain(), Util.makeTypeProp(Util.getTypeProp(objectName)) );
            try
            {
                final long start = System.currentTimeMillis();
                final Set<ObjectName>  instances = mMBeanServer.queryNames( pattern, null);
                final long elapsed = System.currentTimeMillis() - start;
                //debug( "Query time: " + elapsed);
                if ( instances.size() > 1 )
                {
                    problems.add( "Global singleton " + objectName +
                        " conflicts with other MBeans of the same type: " +
                        CollectionUtil.toString(instances, ", "));
                }
            }
            catch( final Exception e )
            {
                throw new RuntimeException(e);
            }
        }

        return problems;
    }

    private void validateRequiredAttributes(final AMXProxy proxy)
            throws ValidationFailureException
    {
        final ObjectName objectName = proxy.objectName();
        // verify that the required attributes are present
        final Map<String, MBeanAttributeInfo> infos = JMXUtil.attributeInfosToMap(proxy.extra().mbeanInfo().getAttributes());
        final Set<String> attrNames = infos.keySet();
        if (!attrNames.contains("Name"))
        {
            fail(objectName, "MBeanInfo does not contain Name attribute");
        }
        if (!attrNames.contains("Parent"))
        {
            fail(objectName, "MBeanInfo does not contain Parent attribute");
        }

        if (attrNames.contains("Children"))
        {
            // must contain a non-null list of children
            try
            {
                if (proxy.getChildren() == null)
                {
                    fail(objectName, "value of Children attribute must not be null");
                }
            }
            catch (final AMXException e)
            {
                throw e;
            }
            catch (final Exception e)
            {
                if ( ! instanceNotFound(e) )
                {
                    fail(objectName, "does not supply children correctly");
                }
            }
        }
        else
        {
            // must NOT contain children, we expect an exception
            try
            {
                proxy.getChildren();
                fail(objectName, "Children attribute is present, but not listed in MBeanInfo");
            }
            catch (final Exception e)
            {
                // good, this is expected
            }
        }
    }

    public static final class ValidationResult
    {
        private final String mDetails;

        private final int mNumTested;

        private final int mNumFailures;

        public ValidationResult(
                final int numTested,
                final int numFailures,
                final String details)
        {
            mNumTested = numTested;
            mNumFailures = numFailures;
            mDetails = details;
        }

        public String details()
        {
            return mDetails;
        }

        public int numTested()
        {
            return mNumTested;
        }

        public int numFailures()
        {
            return mNumFailures;
        }

        public String toString()
        {
            return details();
        }

    }
    
    private void unregisterNonCompliantMBean( final ObjectName objectName)
    {
        try {
            mMBeanServer.unregisterMBean(objectName);
            debug( "Unregistered non-compliant MBean " + objectName );
        }
        catch( final Exception ignore ) {
            debug( "Unable to unregister non-compliant MBean " + objectName );
        }
    }
    
    public ValidationResult validate(final ObjectName[] targets)
    {
        final long startMillis = System.currentTimeMillis();
        final Failures failures = new Failures();

        final DomainRoot dr = mDomainRoot;

        // list them in order
        for (final ObjectName objectName : targets)
        {
            List<String> problems = new ArrayList<String>();
            AMXProxy     amx = null;
            try
            {
                // certain failures prevent even the proxy from being created, a fatal error
                amx = mProxyFactory.getProxy(objectName);
            }
            catch( final Exception e )
            {
                if ( ! instanceNotFound(e) )
                {
                    debug( "Unable to create AMXProxy for " + objectName );
                    e.printStackTrace();
                    
                    final String msg = "Cannot create AMXProxy for MBean \"" + objectName + "\" -- MBean is  non-compliant, unregistering it.";
                    problems.add(msg);
                    
                    unregisterNonCompliantMBean(objectName);
                }
            }
            
            if ( amx != null )
            {
                try
                {
                    problems = _validate(amx);
                }
                catch( final Exception e )
                {
                    problems = new ArrayList<String>();
                    addToProblems( "Validation failure for MBean " + objectName + ", ", problems, e);
                }
            }

            failures.result(objectName, problems);
        }
        final long elapsedMillis = System.currentTimeMillis() - startMillis;

        final ValidationResult result = new ValidationResult(
                failures.getNumTested(),
                failures.getNumFailures(),
                failures.toString() );
        return result;
    }

    public ValidationResult validate(final ObjectName objectName)
    {
        return validate( new ObjectName[] { objectName } );
    }

    public ValidationResult validate()
    {
        final List<ObjectName> all = Util.toObjectNameList( mDomainRoot.getQueryMgr().queryAll() );

        return validate(CollectionUtil.toArray(all, ObjectName.class));
    }

}






































