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

/*
 *   $Id: MBeanRegistry.java,v 1.12 2007/04/27 20:15:28 llc Exp $
 *   @author: alexkrav
 *
 *   $Log: MBeanRegistry.java,v $
 *   Revision 1.12  2007/04/27 20:15:28  llc
 *   After 15 days of waiting for code review, and multiple reminders/requests which resulted in no action,
 *   including cc'ing of manager multiple times, nothing has been done.
 *
 *
 *   QL passes:
 *   Mac OS X, quad core and dual core:  at least 10 times
 *   Solaris 10:  at least 3 times
 *   Windows:  twice on two different machines, two different developers
 *   Linux: not tested
 *
 *   Issue number:  1409
 *   Obtained from:
 *   Submitted by:  Lloyd Chambers
 *   Reviewed by:   see notes
 *
 *   Revision 1.11  2007/04/03 01:13:39  llc
 *   Issue number:  2752
 *   Obtained from:
 *   Submitted by:  Lloyd Chambers
 *   Reviewed by:   3 day timeout expired
 *
 *   Revision 1.10  2007/01/13 03:25:41  dpatil
 *   Backing out the checkin which is causing test failures, Ran EE QL on Linux, all earlier test failures passing, but saw 2 EJB Timer QL Failures but may be setup issue or other checkin introduced this failure
 *
 *   Revision 1.8  2006/05/16 18:31:38  kravtch
 *   Bug #6405118 (NullPointerException thrown from MBeanRegistry.instantiateAndRegisterConfigMBeans)
 *      - checks for no children elements are added;
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules: admin-core/admin;
 *   Tests: QLT/EE
 *
 *   Revision 1.7  2006/05/08 17:18:53  kravtch
 *   Bug #6423082 (request for admin infrastructure to support the config changes without DAS running (offline))
 *   Added infrastructure for offline execution under Config Validator for:
 *      - dottednames set/get operation
 *      - Add/remove jvm-options
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules: admin-core/admin; admin/validator;
 *
 *   Revision 1.6  2006/03/12 01:26:57  jluehe
 *   Renamed AS's org.apache.commons.* to com.sun.org.apache.commons.*, to avoid collisions with org.apache.commons.* packages bundled by webapps.
 *
 *   Tests run: QL, Servlet TCK
 *
 *   Revision 1.5  2005/12/25 03:47:37  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.4  2005/11/03 16:32:00  kravtch
 *   Bug #6325219
 *   Never-called constructor removed from MBeanRegistry.
 *   Submitted by: kravtch
 *   Reviewed by: Shreedhar
 *   Affected modules admin/core/admin
 *   Tests passed: QLT/EE + devtests
 *   -----
 *
 *   Revision 1.3  2005/08/16 22:19:31  kravtch
 *   M3: 1. ConfigMBeans: Support for generic getXXXNamesList() operation (request from management-rules).
 *       2. MBeanRegistry: support for getElementPrintName() to provide readable element's description for validator's messages
 *   Submitted by: kravtch
 *   Reviewed by: Shreedhar
 *   Affected modules admin-core/admin
 *   Tests passed: QLT/EE + devtests
 *
 *   Revision 1.2  2005/06/27 21:19:44  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.23  2004/11/14 07:04:21  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.22  2004/09/14 00:55:59  kravtch
 *   Patch added to MBeanRegistry: every time when new config element is addeded - config context of the root element will be propagated to its children
 *   Bug #6171788
 *   Reviewer: Sridatta
 *   Tests passed: QLT +EE
 *
 *   Revision 1.21  2004/04/21 18:36:38  kravtch
 *   Reviewer: Sridatta
 *   Mbean registration is added for new ConfigBean along with dottedNameRegistration
 *
 *   Revision 1.20  2004/04/05 16:44:05  kravtch
 *   admin/meta/AdminConfigEventListener: new configcontext listener's code
 *   This listener is for synchronization of ConfigBeans changes with both MBeans and dotted-name spaces.
 *   admin/meta/MBeanRegistry: added methods (adoptConfigBeanDelete/Add) implementing beans ajustment
 *   admin/config/BaseConfigMBean: calls from MBean's postRegister/unregister methods to dotted-name-manager is commented out.
 *
 *   Reviewer: Sridatta
 *   Tests Passed: QuickLook +  UnitTest
 *
 *   Revision 1.19  2004/02/20 03:56:14  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.16.4.3  2004/02/02 07:25:19  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.16.4.2  2003/12/23 01:51:46  kravtch
 *   Bug #4959186
 *   Reviewer: Sridatta
 *   Checked in PE8FCS_BRANCH
 *   (1) admin/admin-core/admin-cli/admin-gui/appserv-core/assembly-tool: switch to new domain name "ias:" -> "com.sun.appserv"
 *   (2) admin-core and admin-cli: switch to "dashed" attribute names
 *   (3) admin-core: support both "dashed"/"underscored" names for get/setAttributes
 *   (4) admin-gui: hook for reverse converting attribute names (temporary hack);
 *
 *   Revision 1.16.4.1  2003/12/01 21:52:39  kravtch
 *   Bug #4939964
 *   Reviewer: Sridatta
 *   admin.config.ManagedConfigBean.createChildByType() now analyzes registryEntries.AttributeInfo for each "empty"  valued attribute (similar to setAttribute(), but could not use MBeanAttributeInfo because MBean is not exists yet). If "emptyValueAllowed" field in registrEntry.AttributeInfo is not "true", then "empty" attribute will be ignored.
 *   Revision 1.16  2003/09/09 22:31:04  kravtch
 *   Bug: #4919291
 *   Reviewer: Sridatta
 *   commons-modeler's manageMBean is subclassed. Subclassing constructor removes requiered for commons-modeler "modeler_type" attribute.
 *
 *   Revision 1.15  2003/09/05 06:06:10  kravtch
 *   Bug #4915176
 *   Reviewer: Sridatta
 *      - new custom mbean - DomainMBean implemented public AttributeList getDefaultCustomProperties(String mbeanTypeName, AttributeList attributeList) to fulfill the reques;
 *      - descriptor file is updated (classname for this mbean added)
 *      - mandatory getDefaultCustomProperties() removed from all MBeanInfos;
 *      - CLI name for http-listener.ssl repared in descriptors file;
 *      - non-neccessary logs suppressed;
 *
 *   Revision 1.14  2003/09/04 05:53:49  kravtch
 *   bugs #4896268 and #4913653
 *   Reviewer: Sridatta
 *      -AuthRealmMbean's getFielRealm is chaged from creating of the new FileRealm object to gettting it from security pool - Realm.getInstance(name) with casting result to FileRealm.
 *   This approach will work only for PE because DAS and instance have the same auth-realms.
 *      -AdminContext expanded by two new methods getAdminMBeanResourcUrl() and getRuntimeMBeanResourcUrl() which used by MBeanRegistryFactory for initialization admin and runtime registries. So, they are become pluggable.
 *      -AdminContext also notifies MBeanRegistryFactory during its construction. So, AdminContext become "visible" to admin-core/admin classes.
 *      -Hardcoded output changed to appropriate logger calls.
 *
 *   Revision 1.13  2003/08/26 05:51:48  kravtch
 *   ignore tailing slashes in XPath
 *   for matching with xpath template
 *   Reviewer: Ramakanth
 *
 *   Revision 1.12  2003/08/16 21:33:05  kravtch
 *   Switch to logger for printing debug messages:
 *   System.out.printlns->_slogger.log(...)
 *
 *   Revision 1.11  2003/08/16 03:06:25  sridatta
 *   correcting object name for dottednameregistry
 *
 *   Revision 1.10  2003/08/15 23:08:28  kravtch
 *   DottedName Support (generation and call to manager)
 *   notifyRegisterMBean/UnregisterMBean are implemented;
 *   dotted name related opeartions are added to NaminDescriptor and NamingInfo
 *   removeChild support is added;
 *
 *   Revision 1.9  2003/08/07 00:41:06  kravtch
 *   - new DTD related changes;
 *   - properties support added;
 *   - getDefaultAttributeValue() implemented for config MBeans;
 *   - merge Jsr77 and config activity in runtime mbeans;
 *
 *   Revision 1.8  2003/07/29 18:59:35  kravtch
 *   MBeanRegistryEntry:
 *   	- support for toFormatString();
 *   	- instantiateMBean() method modified to instantiate runtime MBeans as well;
 *   MBeanRegistryFactory:
 *   	- fixed bug in getRuntimeRegistry();
 *   MBeanNamingInfo:
 *   	- less strict requirements for parm_list_array size in constructor (can be more then needed);
 *   BaseRuntimeMBean:
 *   	- exception ClassCastException("Managed resource is not a Jsr77ModelBean") handling;
 *   ManagedJsr77MdlBean:
 *   	- call managed bean bug fixed ( getDeclaredMethod()->getMethod())
 *   admin/dtds/runtime-mbeans-descriptors.xml - modified to represent new runtime mbeans;
 *
 *   Revision 1.7  2003/06/25 20:03:40  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin.meta;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;

// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import javax.management.MBeanServer;
import javax.management.DynamicMBean;
import javax.management.ObjectName;
import javax.management.Descriptor;
import javax.management.MalformedObjectNameException;

import com.sun.org.apache.commons.modeler.ManagedBean;
import com.sun.org.apache.commons.modeler.Registry;
import com.sun.org.apache.commons.modeler.AttributeInfo;

import com.sun.enterprise.admin.meta.naming.MBeanNamingDescriptor;
import com.sun.enterprise.admin.meta.naming.MBeanNamingInfo;
import com.sun.enterprise.admin.meta.naming.MBeanNamingException;
import com.sun.enterprise.admin.meta.naming.MBeansNaming;
import com.sun.enterprise.admin.meta.naming.Name;

import com.sun.enterprise.admin.BaseAdminMBean;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.ConfigContextEvent;

import com.sun.enterprise.util.FeatureAvailability;

/**
 * Provides naming support for Mbeans
 */
public class MBeanRegistry //extends MBeansNaming
{
    private final static String MSG_FINDREGISTRYENTRY_FAILED = "mbean.config.findregistryentry_failed";

    private final MBeanServer   mMBeanServer;
    
    public MBeanRegistry() {
        mMBeanServer    = FeatureAvailability.getInstance().getMBeanServer();
    }
    
    // Logging
    protected static final Logger _sLogger = LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    private volatile MBeanRegistryEntry[] entries = null;
    
    public String toString()
    {
        String str =  "MBeanRegistry("+entries.length+" entries):\n";
        for(int i=0; i<entries.length; i++)
        {
            str = str + entries[i].toString();
        }
        return str;
    }

    public String toFormatString()
    {
        String str =  "MBeanRegistry("+entries.length+" entries):\n";
        for(int i=0; i<entries.length; i++)
        {
            str = str + entries[i].toFormatString();
        }
        return str;
    }

    //**************************************************************************
    private class MyRegistry extends Registry
    {
        public MyRegistry()
        {
            super();
        }
        
    }

    
    //**************************************************************************
    private class MyManagedBean extends ManagedBean
    {
        public MyManagedBean(ManagedBean mb)
        {
            super();
            AttributeInfo attrs[] = mb.getAttributes();
            if(attrs.length>0 && "modelerType".equals(attrs[0].getName()))
            {
                attributes = new AttributeInfo[attrs.length-1];
                for(int i=1; i<attrs.length; i++)
                    attributes[i-1]=attrs[i];
            }
            else
                attributes = attrs;
            className       = mb.getClassName();
            constructors    = mb.getConstructors();
            description     = mb.getDescription();
            domain          = mb.getDomain();
            group           = mb.getGroup();
            name            = mb.getName();
            fields          = mb.getFields();
            notifications   = mb.getNotifications();
            operations      = mb.getOperations();
            type            = mb.getType();
        }
        
    }

  
    //**************************************************************************
    public void loadMBeanRegistry(InputStream stream) throws MBeanMetaException,MBeanNamingException,ClassNotFoundException
    {
        loadMBeanRegistry(stream, true);
    }
    //**************************************************************************
    public void loadMBeanRegistry(InputStream stream, boolean bMergeWithMbean) throws MBeanMetaException,MBeanNamingException,ClassNotFoundException
    {
        MyRegistry registry = new MyRegistry();
        try {
            registry.loadDescriptors("MbeansDescriptorsDOMSource", stream, null);
        } catch (Exception e)
        {
            throw new MBeanMetaException(e.getMessage());
        }
        // naming init
        String[] names = registry.findManagedBeans();
        entries = new MBeanRegistryEntry[names.length]; //loose the prev ?
        for(int i=0; i<names.length; i++)
        {
            entries[i] = new MBeanRegistryEntry(new MyManagedBean(registry.findManagedBean(names[i])), bMergeWithMbean);
        }
//        MBeansNaming.initMBeanNaming();
        
    }
    
    //**************************************************************************
    public MBeanRegistryEntry findMBeanRegistryEntry(ObjectName objectName)
    {
        try
        {
            Hashtable ht = objectName.getKeyPropertyList();
            ht.put(":",objectName.getDomain()); //add domain name pseudo pair
            
            for(int i=0; i<entries.length; i++)
            {
                if(entries[i]!=null && entries[i].isObjectNamePatternMatch(ht))
                    return entries[i];
            }
        }
        catch (Exception e)
        {
            _sLogger.log(Level.FINE, MSG_FINDREGISTRYENTRY_FAILED, e);
        }
        return null;
    }
    //**************************************************************************
    public MBeanRegistryEntry findMBeanRegistryEntryByType(String type)
    {
        for(int i=0; i<entries.length; i++)
        {
            if(entries[i]!=null && entries[i].getName().equals(type))
                return entries[i];
        }
        return null;
    }
    
    //**************************************************************************
    public MBeanRegistryEntry findMBeanRegistryEntryByXPath(String xpath)
    {
        if(xpath!=null)
            while(xpath.length()>1 && xpath.endsWith("/"))
                xpath = xpath.substring(0, xpath.length()-1);
        Object[] tokens = MBeanNamingDescriptor.getXPathTokens(xpath);
 
        for(int i=0; i<entries.length; i++)
        {
            if(entries[i].getNamingDescriptor().isXpathTokensMatch(tokens))
                return entries[i];
        }
        return null;
    }

    //**************************************************************************
    public MBeanRegistryEntry[] findMBeanRegistryEntriesByDottedName(String dottedName)
    {
        Name name;
        try {
             name = new Name(dottedName);
        } catch (Exception e)
        {
            return null;
        }
        ArrayList list = new ArrayList();
        for(int i=0; i<entries.length; i++)
        {
            if(entries[i].getNamingDescriptor().isDottedPatternMatch(name))
                list.add(entries[i]);
        }
        if(list.size()==0)
            return null;
        return (MBeanRegistryEntry[])list.toArray(new MBeanRegistryEntry[]{});
    }

    //**************************************************************************
    public MBeanRegistryEntry findMBeanRegistryEntryByXPathPattern(String patternPrefix)
    {
        // bring prefix into accord of inner naming descriptor's pattern style (double apostrophies)
        patternPrefix = MBeanNamingDescriptor.XPATH_TO_MASK(patternPrefix);
        int len = patternPrefix.length();
        String pattern;
        //enum entries to find matching pattern
        for(int i=0; i<entries.length; i++)
        {
            pattern = entries[i].namingDescriptor.getXPathPattern();
            if( pattern!= null &&
                pattern.startsWith(patternPrefix) &&
                (pattern.length()==len || pattern.indexOf('/', len)<0 ) )
               return entries[i];
        }
        return null;
    }

    //**************************************************************************
    public ObjectName getMbeanObjectName(String type, String[] location)
    {
        return getMbeanObjectName(findMBeanRegistryEntryByType(type), type, location);
    }
    
    //**************************************************************************
    private ObjectName getMbeanObjectName(MBeanRegistryEntry entry, String type, String[] location)
    {
 
        if(entry!=null)
        {
            try {
                MBeanNamingInfo namingInfo = new MBeanNamingInfo(entry.getNamingDescriptor(), type, location);
                if(namingInfo!=null)
                    return namingInfo.getObjectName();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    //**************************************************************************
    public BaseAdminMBean instantiateConfigMBean( ObjectName objectName, Object managedResource) throws Exception
    {
        return instantiateConfigMBean( objectName, managedResource, null);
    }
    
    //**************************************************************************
    public BaseAdminMBean instantiateConfigMBean( ObjectName objectName, Object managedResource, ConfigContext ctx) throws Exception
    {
        MBeanRegistryEntry entry = findMBeanRegistryEntry(objectName);
        if(entry!=null)
        {
            MBeanNamingDescriptor descr = entry.getNamingDescriptor();
            return entry.instantiateMBean(objectName, managedResource, ctx);
        }
        return null;
    }

    //**************************************************************************
    public BaseAdminMBean instantiateMBean( String type, String[] location) throws Exception
    {
        return instantiateMBean( type, location, null, null);
    }
    //**************************************************************************
    public BaseAdminMBean instantiateMBean( String type, String[] location, Object managedResource, ConfigContext ctx) throws Exception
    {
        return instantiateMBean( type, location, managedResource, ctx, false);
    }

    //**************************************************************************
    public MBeanNamingInfo getMBeanNamingInfo(ObjectName objectName) throws Exception
    {
        MBeanRegistryEntry entry = this.findMBeanRegistryEntry(objectName);
        return new MBeanNamingInfo(entry.getNamingDescriptor(), objectName);
    }
    //**************************************************************************
    // this method is called called during MBean Registration
    // it notifies the dotted name manager about new MBean appearing
    // registering correspondent dotted names 
    public void notifyRegisterMBean(ObjectName objectName, ConfigContext ctx)
    {
        try {
            MBeanNamingInfo namingInfo = getMBeanNamingInfo(objectName);
            ArrayList arr = getValidDottedNames(namingInfo);
            if(arr!=null)
                for(int i=0; i<arr.size(); i++) //enumerate all dotted names aliases 
                    addDottedName((String)arr.get(i), namingInfo.getObjectName());
            //FIXME: only if no XPath -> notifying Lloyd's MBean
        } catch (Exception e)
        {}
    }
    
    //**************************************************************************
    public void notifyUnregisterMBean(ObjectName objectName, ConfigContext ctx)
    {
        try {
            MBeanNamingInfo namingInfo = getMBeanNamingInfo(objectName);
            ConfigBean bean = null;
            try
            {
                bean = (ConfigBean)ConfigBeansFactory.getConfigBeanByXPath(ctx, namingInfo.getXPath());
            }
            catch (Exception e)
            {}
            if(bean==null)
               removeDottedName(objectName);
        } catch (Exception e)
        {}
    }
    
    //**************************************************************************
    public void generateAndRegisterAllDottedNames(ConfigContext ctx, String domainName) throws MBeanMetaException
    {
        ConfigBean bean = null;
        try
        {
            bean = (ConfigBean)ConfigBeansFactory.getConfigBeanByXPath(ctx, "/domain");
            registerDottedName(bean, domainName);
        }
        catch (Exception e)
        {
            throw new MBeanMetaException("DottedNamesRegistration: Could not get '/domain' ConfigMBean");
        }
    }

    /** 
     * This method can be used to register all config mbeans that are
     * available in the system. It recursively descends the configbean tree
     * and instantiates the mbean based on the descriptor file and
     * registers the mbeans. If a configbean is null, the mbean is skipped
     * and recursion is continued. Also, if the mbean is already created, then
     * the InstanceAlreadyException is ignored (or, alternatively, the
     * registration is not even attempted). No exception is thrown from the
     * recursive method and is a best effort solution.  The only exception 
     * thrown is when domain bean cannot be obtained.
     *
     * @param ctx ConfigContext used for creating config mbeans
     * @param domainName domainName used for registering the mbeans
     *
     * @exception MBeanMetaException. ConfigException is wrapped as 
     * MBeanMetaException when domain bean is not obtained and the 
     * ConfigException is also chained.
     *
     * @see generateAndRegisterAllDottedNames
     */
    public void instantiateAndRegisterAllConfigMBeans(
                                ConfigContext ctx, 
                                String domainName) 
                                throws MBeanMetaException
    {
        ConfigBean bean = null;
        try
        {
            bean = (ConfigBean)ConfigBeansFactory.getConfigBeanByXPath(ctx, 
                                                ServerXPathHelper.XPATH_DOMAIN);
        }
        catch (ConfigException e)
        {
            _sLogger.log(Level.FINEST, 
                "Exception during Instantiating All Config MBeans in MBeanRegistry", e);
            throw new MBeanMetaException
                ("ConfigException in getting Domain bean from config context", e);
        }  
        instantiateAndRegisterConfigMBeans(bean, domainName);
        _sLogger.log(Level.FINEST, 
            "Instantiated All Config MBeans in MBeanRegistry Successfully");
    }
    
    
    //****************************************************************************************************
    private String getFirstValidDottedName(MBeanNamingInfo namingInfo)
    {
        ArrayList arr = getValidDottedNames(namingInfo);
        if(arr!=null && arr.size()>0)
            return (String)arr.get(0);
        return null;
    }
    
    //****************************************************************************************************
    public static ArrayList getValidDottedNames(MBeanNamingInfo namingInfo)
    {
        return getValidDottedNames(namingInfo, null);
    }
    //****************************************************************************************************
    public static ArrayList getValidDottedNames(MBeanNamingInfo namingInfo, String idPrefix)
    {
        ArrayList arr = new ArrayList();
        try {
            String [] dottedNames = namingInfo.getDottedNames(idPrefix);
            for(int i=0; i<dottedNames.length; i++)    
            {
                if( dottedNames[i].length()>0 &&
                    !dottedNames[i].startsWith("@"))
                    arr.add(dottedNames[i]);
            }
        } catch (Exception e)
        {}
        return arr;
    }
    //****************************************************************************************************
    //  registers dotted name(s) for given ConfigBean 
    //  supports multiple aliases registration (not supported yet by dotted names framework)
    private void registerDottedName(ConfigBean bean, String domainName)
    {
        if(bean==null)
            return;
        String xpath = bean.getAbsoluteXPath("");
        try   
        {
            MBeanNamingInfo namingInfo = getNamingInfoForConfigBean(bean, domainName);
            ObjectName objectName = namingInfo.getObjectName();
            ArrayList arr = getValidDottedNames(namingInfo);
            if(arr!=null)
                for(int i=0; i<arr.size(); i++) //enum aliases
                {
                    // register dotted name/alias
                    addDottedName((String)arr.get(i), objectName);
                }
       }
        catch(Exception e)
        {
            if(xpath!=null && xpath.indexOf("/"+ServerTags.ELEMENT_PROPERTY+"[")==0)
            {
                _sLogger.log(Level.FINE,"---- Exception for xpath="+xpath, e);            
            }
        } 
        try 
        {
            ConfigBean[] beans = bean.getAllChildBeans();
            if(beans!=null)
            {
                for(int i=0; i<beans.length; i++)
                {
                    registerDottedName(beans[i], domainName);
                }
            }
        }
        catch(Exception e)
        {
        } 
    }

    //****************************************************************************************************
    
    /**
     * A recursive method to register all mbeans from the bean specified
     * Step 1, Get mbean for that bean. Step 2, Find all children for bean
     * Step 3, call this method for each bean
     *
     * If bean is null, return silently. This method is designed to instantiate
     * all possible mbeans without throwing any exceptions--a best effort
     * solution.
     */
    private void instantiateAndRegisterConfigMBeans(ConfigBean bean, String domainName)
    {
        if(bean == null) 
            return;
        String xpath = bean.getAbsoluteXPath("");
        ConfigContext ctx = bean.getConfigContext();
        try   
        {
            ObjectName objectName = getObjectNameForConfigBean(bean, domainName);
            if(objectName!=null)
            {
                if (! mMBeanServer.isRegistered(objectName)) 
                {
//register here
                    BaseAdminMBean mbean = instantiateConfigMBean(objectName, null, ctx) ;
                    registerMBean(mbean, objectName);
                }
            }    
        }
        catch(Exception e)
        {
            if(xpath!=null && xpath.indexOf("/"+ServerTags.ELEMENT_PROPERTY+"[")==0)
            {
                _sLogger.log(Level.FINEST,"---- Exception for xpath="+xpath, e);            
            }
        } 
        try 
        {
            ConfigBean[] beans = bean.getAllChildBeans();
            if(beans!=null)
            {
                for(int i=0; i<beans.length; i++)
                {
                    if(beans[i]!=null && beans[i].getConfigContext()==null)
                    { //temporary patch for bug #6171788
                        beans[i].setConfigContext(ctx);
                    }

                    try {
                        instantiateAndRegisterConfigMBeans(beans[i], domainName);
                    } catch(Exception e1) {
                        //ignore
                         _sLogger.log(Level.FINEST,
                            "Exception in instantiateAndRegisterConfigMBeans:", e1); 
                    }
                }
            }
        }
        catch(Exception e)
        {
             _sLogger.log(Level.FINEST,
                        "Exception in instantiateAndRegisterConfigMBeans:", e); 
        } 
    }
    
    /**
     * Note the check for isRegistered(). This is to prevent
     * calling of preRegister in registerMBean. There can 
     * potentially be infinite loop in the case where 
     * preRegister() goes queryNames or queryMBeans which inturn 
     * will call registerMBeans
     *
     * We need to silently move on when InstanceAlreadyException 
     * is thrown anyway. This does not change functionality in
     * that sense.
     */
    private void registerMBean(BaseAdminMBean mbean, ObjectName name) 
                                        throws Exception {
        if (!  mMBeanServer.isRegistered(name)) {
            mMBeanServer.registerMBean(mbean, name);
        } 
    }
    //****************************************************************************************************
    private static final ObjectName     REGISTRATION_MBEAN_OBJECTNAME = getDottedRegMBeanName();
    private static final String     REGISTRATION_OPERATION_NAME   = "add";
    private static final String     UNREGISTRATION_OPERATION_NAME = "remove";
    //****************************************************************************************************
    private static ObjectName getDottedRegMBeanName()
    {
        try {
            return new ObjectName("com.sun.appserv:name=dotted-name-registry,type=dotted-name-support"); //CHANGE_ME
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    //****************************************************************************************************
    private void addDottedName(String dottedName, ObjectName objectName) throws Exception
    {
        _sLogger.log(Level.FINE, "******regDottedName******* "+dottedName +" ObjectName="+objectName);
        mMBeanServer.invoke(REGISTRATION_MBEAN_OBJECTNAME, REGISTRATION_OPERATION_NAME, 
                       new Object[]{dottedName, objectName},
                       new String[]{dottedName.getClass().getName(), objectName.getClass().getName()});
    }
    //****************************************************************************************************
    private void removeDottedName(ObjectName objectName) throws Exception
    {
        _sLogger.log(Level.FINE, "******unregDottedName******* for ObjectName="+objectName);
        mMBeanServer.invoke(REGISTRATION_MBEAN_OBJECTNAME, UNREGISTRATION_OPERATION_NAME, 
                       new Object[]{objectName},
                       new String[]{objectName.getClass().getName()});
    }
    //**************************************************************************
    public BaseAdminMBean instantiateMBean( String type, String[] location, Object managedResource, ConfigContext ctx, boolean bRegister) throws Exception
    {
        MBeanRegistryEntry entry = findMBeanRegistryEntryByType(type);
        BaseAdminMBean mbean = null;
        if(entry!=null)
        {
            mbean =  entry.instantiateMBean(type, location, managedResource, location[0], ctx);
            if(bRegister && mbean!=null)
            {
                ObjectName objectName = getMbeanObjectName(entry, type, location);
                if(objectName!=null)
                {
                    registerMBean(mbean, objectName);
                }
            }
        }
        return mbean;
    }
/*    //SORT entries
    // BY xpath
    //**************************************************************************
    public void sortRegistryEntriesByXpath()
    {
        // veeery slooow
        ArrayList list = new ArrayList();
        for(int i=0; i<entries.length; i++)
        {
            for(int j=0; j<list.size(); j++)
            {
                if(entries[i].getNamingDescriptor().getXPathPattern().compareTo
                   (((MBeanRegistryEntry)list.get[j]).getNamingDescriptor().getXPathPattern())<0)
                {
                    list.add(j, entries[i]);
                    break;
                }
                else
                    if(j==list.size()-1)
                        list.add(entries[i]);
            }
        }
        entries = list.toArray(entries);
    }

 */
    
    public static final int SORT_BY_NAME  = 0;
    public static final int SORT_BY_XPATH = 1;
    public void sortRegistryEntries(int sortType )
    {
        // veeery slooow
        TreeMap map  = new TreeMap();
        String emptyKey = " ";
        String  key;
        for(int i=0; i<entries.length; i++)
        {
            if(sortType==SORT_BY_XPATH)
                key = entries[i].getNamingDescriptor().getXPathPattern();
            else
                key = entries[i].getName();
            if(key==null || key.length()==0)
            {
                key = emptyKey;
                emptyKey = emptyKey + " ";
            }    
            map.put(key, entries[i]);
        }
        entries = (MBeanRegistryEntry[])map.values().toArray((Object[])(new MBeanRegistryEntry[0]));
    }

    //****************************************************************************************************
    /*
     * this method should be called from ConfigContextListener
     * registers dotted-names correspondent to created ConfigBeans and its children
    * (question: should we register created mbeans or just call dottedname registration?)
     * @param bean added ConfigBean 
     * @param domainName ObjectName's domain name
     * @param domainName
     * 
     */
   //**************************************************************************************************** 
   public void adoptConfigBeanAdd(ConfigBean bean, String domainName)
   {
        _sLogger.log(Level.FINEST, "****** MBeanRegistry.adoptConfigBeanAddDelete->add element:"+bean);
        instantiateAndRegisterConfigMBeans(bean, domainName);
        registerDottedName(bean, domainName); //this one is already recursive
        return;
   }

   //****************************************************************************************************
    /*
     * this method should be called from ConfigContextListener
     * registers dotted-names and MBean correspondent to deleted ConfigBean and its children
    * (question: should we register created mbeans or just call dottedname registration?)
     * @param bean deleted ConfigBean 
     * @param domainName ObjectName's domain name
     * 
     */
   public void adoptConfigBeanDelete(ConfigBean bean, String domainName)
   {
        //delete only 
        // we need to unregister MBeans as well as unregister dotted name
        _sLogger.log(Level.FINEST, "****** MBeanRegistry.adoptConfigBeanAddDelete->delete element"+bean);
        ArrayList arr = new ArrayList();
        collectChildrenObjectNames(bean, domainName, arr);
        for(int i=0; i<arr.size(); i++)
        {
            ObjectName objectName = (ObjectName)arr.get(i);
            if(objectName!=null)
            {
                // first try to unregister dotted name
                try
                {
                    removeDottedName(objectName);
                }
                catch (Throwable t)
                {
                    //this is ok, some elements has no dotted name
                    _sLogger.fine("!!!!!!!!!!!!!! Can not unregister dotted name for MBean: "+objectName);
                }
                
                // now, try to unregister MBean
                try
                {
                    if( mMBeanServer.isRegistered(objectName))
                        mMBeanServer.unregisterMBean(objectName);
//                    else
//                        //just to unregister dotted name
//                        notifyUnregisterMBean(objectName, getConfigContext());
                }
                catch (Throwable t)
                {
                    //this is ok, some elements has no dotted name
                    _sLogger.fine("!!!!!!!!!!!!!! Can not unregister MBean: "+objectName);
                }
            }
        }
   }
    
   //****************************************************************************************************
   /* collects beans (own and all children) base on ConfigBeansXPathes
    * and adds them to provided arrayList
    *  
    * called recursively
    * @param bean ConfigBean to investigate
    * @param arr arraylist for accumulating of objectNames
    *
    */
   private void collectChildrenObjectNames(ConfigBean bean, String domainName, ArrayList arr)
   {
        try 
        {
            ConfigBean[] beans = bean.getAllChildBeans();
            if(beans!=null)
            {
                for(int i=0; i<beans.length; i++)
                {
                    collectChildrenObjectNames(beans[i], domainName, arr);
                }
            }
        }
        catch(Exception e)
        {
            //ignore exceptions (mabe there is no correspondent ObjectName
        } 
        try 
        {
            ObjectName name = getObjectNameForConfigBean(bean, domainName);
            if(name!=null)
            {
               arr.add(name);
               _sLogger.log(Level.FINEST, "******collectChildrenObjectNames.add-> ");
            }
        }
        catch(MalformedObjectNameException e)
        {
            String xpath = bean.getAbsoluteXPath("");
            _sLogger.log(Level.FINE, "Object name malformed for bean: "+ xpath, e);
        } 
   }
   
   /* ****************************************************************************************************
    * helper method - find MBeanNaminginfo for given ConfgBean
    * @param bean - ConfigBean 
    * @param domainName - domainName using to form MBeanNameinInfo (first tag in location) 
    */
   public MBeanNamingInfo getNamingInfoForConfigBean(ConfigBean bean, String domainName)
    {
        if(bean == null) 
            return null;
        try   
        {
            String xpath = bean.getAbsoluteXPath("");
            MBeanRegistryEntry entry = findMBeanRegistryEntryByXPath(xpath);
            MBeanNamingDescriptor descr = entry.getNamingDescriptor();
            String [] parms = descr.extractParmListFromXPath(xpath);
            if(parms!=null && parms.length>0 && parms[0]==null)
                parms[0] = domainName;
            return new MBeanNamingInfo(descr, descr.getType(), parms);
        }
        catch (Exception e)
        {
            // it is ok. not all config beans have entries in registry
            return null;
        }
   }
   /* ****************************************************************************************************
    * helper method - returns ObjectName correspondent to ConfigBean
    * @param bean - ConfigBean to find ObjectName
    * @param domainName - domainName using to form ObjectName 
    */
   public ObjectName getObjectNameForConfigBean(ConfigBean bean, String domainName) throws MalformedObjectNameException
    {
        MBeanNamingInfo namingInfo = getNamingInfoForConfigBean(bean, domainName);
        if(namingInfo!=null)
            return namingInfo.getObjectName();
        return null;
   }
   /** 
    *  returns element name description 
    *  like "custom resource myResource"
    */
   public String getConfigElementPrintName(String xpath, boolean bIncludingKey, boolean bReplaceRefByParentElem)
   {
       xpath = MBeanMetaHelper.cutAttributeTokenFromXPath(xpath.trim());
       MBeanRegistryEntry entry = findMBeanRegistryEntryByXPath(xpath);
       String printName;
       String elemName = MBeanMetaHelper.extractLastElemNameFromXPath(xpath);
       if(bReplaceRefByParentElem && elemName.endsWith("-ref"))
       {
          xpath = MBeanMetaHelper.cutLastElementFromXPath(xpath);
          elemName = MBeanMetaHelper.extractLastElemNameFromXPath(xpath);
       }
       if(entry==null ||
          (printName = entry.getElementPrintName())==null)
       {
           //generic name convertion to print name
           printName = elemName.replace('-', ' ');
       }
       String key = MBeanMetaHelper.getMultipleElementKeyValue(xpath);
       if(key!=null && bIncludingKey)
           printName = printName + " '" + key + "'";
       return printName;
   }
}
