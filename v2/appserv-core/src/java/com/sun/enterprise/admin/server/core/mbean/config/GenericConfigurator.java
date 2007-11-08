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
    PROPRIETARY/CONFIDENTIAL. Use of this product is subject
    to license terms. Copyright ? 2001 Sun Microsystems, Inc.
        All rights reserved.
 
    $Id: GenericConfigurator.java,v 1.3 2005/12/25 04:14:29 tcfujii Exp $
 */

package com.sun.enterprise.admin.server.core.mbean.config;

import com.sun.enterprise.admin.util.HostAndPort;
import com.sun.enterprise.admin.util.Debug;
import com.sun.enterprise.admin.util.ArgChecker;
import com.sun.enterprise.admin.common.OperationProgress;
import com.sun.enterprise.admin.common.RequestID;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.*;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.server.core.mbean.meta.MBeanEasyConfig;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.server.core.mbean.config.naming.ConfigMBeanNamingInfo;
import com.sun.enterprise.admin.monitor.CommandMapper;
import com.sun.enterprise.admin.monitor.MonitorCommand;
import com.sun.enterprise.admin.monitor.MonitorGetCommand;
import com.sun.enterprise.admin.monitor.MonitorSetCommand;
import com.sun.enterprise.admin.monitor.MonitorListCommand;
import com.sun.enterprise.admin.event.MonitoringEvent;
import com.sun.enterprise.admin.event.AdminEventMulticaster;
import com.sun.enterprise.admin.event.AdminEventResult;

import com.sun.enterprise.admin.server.core.mbean.config.naming.ConfigMBeansNaming;

//JMX imports
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.DynamicMBean;
import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.JMRuntimeException;
import javax.management.ServiceNotFoundException;

import java.util.Set;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Transparent deliverer of get/set requests to proper MBeans, according
 * to dot-notated attribute name, which includes MBean name synonym in prefix part.
 * e.g name="server.ias1.orb.port" represents attribute "port" in
 * MBean named "ias:instance-name=ias1,component=orb"
 * <p>
 * ObjectName of this MBean is: ias:type=configurator
 */

public class GenericConfigurator extends AdminBase
{
    public static final Logger sLogger = Logger.getLogger(AdminConstants.kLoggerName);
    private final static String MSG_ATTR_NOT_FOUND      = "mbean.config.attribute_not_found";
    private final static String MSG_GETMBEANINFO_FAILED = "mbean.config.getmbeaninfo_failed";
    private final static String MSG_GET_ATTRIBUTE       = "mbean.config.get_attribute";
    private final static String MSG_GET_ATTRIBUTE_DEFAULT = "mbean.config.get_attribute_default";
    private final static String MSG_SET_ATTRIBUTE       = "mbean.config.set_attribute";
    private final static String MSG_LIST_NAMES_CONTS    = "mbean.config.list_names_continuations";
	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( GenericConfigurator.class );

    MBeanServer mServer = null;
    public GenericConfigurator() {
    }
    
    /**
     * Obtains the Dynamic MBean Attribute object(s) for given attribute pattern (in dotted notation).
     *
     * @param dottedName - The name of the attribute to be retrieved (in dotted notation)
     * @throws MBeanException - Wraps a java.lang.Exception thrown by the MBean's getter.
     * @throws ReflectionException - Wraps a java.lang.Exception thrown while trying to invoke the getter.
     * @throws MalformedNameException  - if MBean name incorrect
     */
    public AttributeList getGenericAttribute(String dottedName) throws
    InstanceNotFoundException, AttributeNotFoundException, MBeanException, ReflectionException,
    MalformedNameException, MalformedObjectNameException, IntrospectionException,MBeanConfigException
    {
        sLogger.log(Level.FINEST, MSG_GET_ATTRIBUTE, dottedName);
        if(mServer==null)
            mServer=MBeanServerFactory.getMBeanServer();
        dottedName = dottedName.trim();
        String attrName    = extractAttrNameFromDotted(dottedName);
        int prefixLen  = dottedName.length() - attrName.length();
        String namePrefix = null;
        if(prefixLen>0)
            namePrefix = dottedName.substring(0, prefixLen);
       
        ArrayList objectNames = getMBeanNamesFromDottedPattern(dottedName);
        
        AttributeList attrList = new AttributeList();
        for(int i=0; i<objectNames.size(); i++)
        {
            
            addAttributeToList(attrList, (ObjectName)objectNames.get(i), attrName);
        }
        if(namePrefix!=null && attrList!=null)
        {
            for(int i=0; i<attrList.size(); i++)
            {
                Attribute attr  = (Attribute)attrList.get(i);
                attrList.set(i, new Attribute(namePrefix+attr.getName(),attr.getValue()));
            }
        }
        return attrList;
    }
    
    /**
     * Obtains the Default MBean Attributes values 
     *
     * @param instanceName - The server instance name
     * @param mbeanType    - Type of mbean (from config naming)
     * @param attrNames    - Atring array of attribute names
     * @throws MBeanConfigException - if mbean creation failed.
     */
    public AttributeList getGenericAttributeDefaultValues(String instanceName, String mbeanType, String[] attrNames) throws MBeanConfigException
    {
        sLogger.log(Level.FINEST, MSG_GET_ATTRIBUTE_DEFAULT, new String[]{instanceName, mbeanType});
        ConfigMBeanNamingInfo info = new ConfigMBeanNamingInfo(mbeanType, new String[] {instanceName}, false);       
        ConfigMBeanBase mbean = info.constructConfigMBean();
        AttributeList attrList = new AttributeList();
        for(int i=0; i<attrNames.length; i++)
        {
            Object defaultValue;
            try 
            {
                defaultValue = mbean.getDefaultAttributeValue(attrNames[i]);
            }
            catch (AttributeNotFoundException e)
            {
                defaultValue = null;
            }
            catch (MBeanConfigException e)
            {
                defaultValue = null;
            }
            if(defaultValue!=null)
                attrList.add(new Attribute(attrNames[i], defaultValue));
        }
        return attrList;
    }
    

    /**
     */
    public String[] listGenericDottedNameContinuiations(String dottedName) throws MalformedNameException
    {
        sLogger.log(Level.FINEST, MSG_LIST_NAMES_CONTS, dottedName);
        dottedName = dottedName.trim();
        String instanceName    = extractInstanceNameFromDotted(dottedName);
        return ConfigMBeansNaming.findNameContinuation(instanceName, dottedName);
    }
    
    /**
     * Obtains the Dynamic MBean Attribute objects for given attribute patterns (in dotted notation).
     *
     * @param dottedNames - The arry of attribute names(patterns) to be retrieved (in dotted notation)
     * @throws MBeanException - Wraps a java.lang.Exception thrown by the MBean's getter.
     * @throws ReflectionException - Wraps a java.lang.Exception thrown while trying to invoke the getter.
     * @throws MalformedNameException  - if MBean name incorrect
     */
    public AttributeList getGenericAttributes(String[] dottedNames) throws
    InstanceNotFoundException,AttributeNotFoundException, MBeanException, ReflectionException,
    MalformedNameException, MalformedObjectNameException, IntrospectionException,MBeanConfigException
    {
        AttributeList list = new AttributeList();
        for(int i=0; i<dottedNames.length; i++)
        {
            list.addAll(getGenericAttribute(dottedNames[i]));
        }
        return list; 
    }
    
    /**
     * Set  Dynamic MBean attribute values for given attribute pattern (in dotted notation).
     *
     * @param dottedName - The name of the attribute to be set (in dotted notation)
     * @throws MBeanException - Wraps a java.lang.Exception thrown by the MBean's getter.
     * @throws ReflectionException - Wraps a java.lang.Exception thrown while trying to invoke the getter.
     * @throws MalformedNameException  - if MBean name incorrect
     */
    public AttributeList setGenericAttribute(String dottedName, Object value) throws
    MBeanConfigException,InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException,
    MBeanException, ReflectionException, MalformedNameException, MalformedObjectNameException, IntrospectionException
    {
        sLogger.log(Level.FINEST, MSG_SET_ATTRIBUTE, new Object[]{dottedName, value});
        if(mServer==null)
            mServer=MBeanServerFactory.getMBeanServer();
        String attrName    = extractAttrNameFromDotted(dottedName);
        
        ArrayList objectNames = getMBeanNamesFromDottedPattern(dottedName);
        
        AttributeList attrList = new AttributeList();
        for(int i=0; i<objectNames.size(); i++)
            setMatchedAttributesToValue(attrList, (ObjectName)objectNames.get(i), attrName,  value);
        return attrList;
    }
    
    
    /**
     * @return Returns the list of attributes that have been set. This method
     * doesnot throw AttributeNotFoundException for any attribute that is not
     * found. The caller has to obtain this information from the returned list
     * of attributes.
     */
    public AttributeList setGenericAttributes(AttributeList al)
    throws InvalidAttributeValueException,InstanceNotFoundException,InvalidAttributeValueException, MBeanConfigException,
    MBeanException, ReflectionException,  MalformedNameException, MalformedObjectNameException, IntrospectionException
    {
        AttributeList list = new AttributeList();
        Iterator it = al.iterator();
        while (it.hasNext())
        {
            Attribute attribute = (Attribute) it.next();
            String name = attribute.getName();
            Object value = attribute.getValue();
            try
            {
                list.addAll(setGenericAttribute(name, value));
            }
            catch (AttributeNotFoundException anfe)
            {
                sLogger.log(Level.FINE, MSG_ATTR_NOT_FOUND, name);
            }
        }
        return list;
    }
    
    /**
     * Method that returns the monitorable params as an attribute list. This
     * method will contact the running administered instance that is represented
     * in the name of the attribute and will return the values from various
     * monitoring data providers in the administered instance.
     * <p>
     * Note that this method gets the data as a whole. Returned list will have
     * all the attributes that represent the MBean attributes.
     * <p>
     * The semantics are similar to the generic get command so that
     * <tt>(server-name).application.(app-name).ejb-module.(mod-name).ejb.
     * (bean-name).pool.*</tt> will give all the monitorable attributes of
     * MBean corresponding to this name.
     * <p>
     * @return AttributeList that contains the Attribute instances. If there are
     * no attributes an empty AttributeList is returned, which means the
     * name does not correspond to any MBean or there are no attributes.
     * Never returns a null.
     * @param String representing the dotted name of the monitor data provider.
     */
    
    public AttributeList getMonitor(String dottedName) throws Exception
    {
        Name name = new Name(dottedName);
        String        instanceName = name.getNamePart(0).toString();
        
        // 1. create MonitorCommand
        CommandMapper cm = CommandMapper.getInstance(instanceName);
        MonitorGetCommand command  = cm.mapGetCommand(dottedName);  //throws InvalidDottedNameException
        // 2. create correspondent MonitoringEvent
        MonitoringEvent event = new MonitoringEvent(instanceName, dottedName, MonitoringEvent.GET_MONITOR_DATA, command);
        // 3. send/receive event to instance
        AdminEventResult result = AdminEventMulticaster.multicastEvent(event);
        // 4. analyse the result
        if(!result.getResultCode().equals(result.SUCCESS))
        {
            handleMonitoringError(result, instanceName, dottedName);
        }
        // 5. extract result list
        return (AttributeList)result.getAttribute(event.getEffectiveDestination(), MonitorCommand.MONITOR_RESULT);
    }
    
    public AttributeList getMonitor(String[] dottedNames) throws Exception
    {
        AttributeList list = new AttributeList();
        for(int i=0; i<dottedNames.length; i++)
        {
            list.addAll(getMonitor(dottedNames[i]));
        }
        return list;
    }

    /**
     * Returns the list of immediate children of a node that represents
     * a component in a hierarchy of nodes. Note that this method will return
     * the list of names of children only. The idea is to aid in reaching the
     * leaf nodes in the hierarchy (Semantics of unix list command). The name
     * of the node is represented by a dotted name.
     * @param String representing the dotted name of an (intermediate) node.
     * @return String[] each of whose elements represents the name of the
     * immediate child that would aid in forming a deeper path.
     * Returns an empty array if there are no children which essentially
     * means that one has reached the leaf node or the hierachy is invalid.
     * Never returns a null.
     */
    
    public String[] listMonitor(String dottedName) throws Exception
    {
        Name name = new Name(dottedName);
        String        instanceName = name.getNamePart(0).toString();
        
        // 1. create MonitorCommand
        CommandMapper cm = CommandMapper.getInstance(instanceName);
        MonitorListCommand command  = cm.mapListCommand(dottedName);  //throws InvalidDottedNameException
        // 2. create correspondent MonitoringEvent
        MonitoringEvent event = new MonitoringEvent(instanceName, dottedName, MonitoringEvent.LIST_MONITORABLE, command);
        // 3. send/receive event to instance
        AdminEventResult result = AdminEventMulticaster.multicastEvent(event);
        // 4. analyse the result
        if(!result.getResultCode().equals(result.SUCCESS))
        {
            handleMonitoringError(result, instanceName, dottedName);
        }
        // 5. extract result list
        return (String[])result.getAttribute(event.getEffectiveDestination(), MonitorCommand.MONITOR_RESULT);
    }
    
    public AttributeList setMonitor(AttributeList al) throws Exception
    {
        AttributeList list = new AttributeList();
        Iterator it = al.iterator();
        while (it.hasNext())
        {
            Attribute attribute = (Attribute) it.next();
            String name = attribute.getName();
            Object value = attribute.getValue();
            try
            {
                list.addAll(setMonitor(name, value));
            }
            catch (AttributeNotFoundException anfe)
            {
                sLogger.log(Level.FINE, MSG_ATTR_NOT_FOUND, name);
            }
        }
        
        return list;
    }
    
    public AttributeList setMonitor(String dottedName, Object value) throws Exception 
    {
        Name name = new Name(dottedName);
        String instanceName = name.getNamePart(0).toString();
        
        // 1. create MonitorCommand
        CommandMapper cm = CommandMapper.getInstance(instanceName);
        MonitorSetCommand command  = cm.mapSetCommand(dottedName, value);  //throws InvalidDottedNameException
        // 2. create correspondent MonitoringEvent
        MonitoringEvent event = new MonitoringEvent(instanceName, dottedName, MonitoringEvent.SET_MONITOR_DATA, command);
        // 3. send/receive event to instance
        AdminEventResult result = AdminEventMulticaster.multicastEvent(event);
        // 4. analyse the result
        if(!result.getResultCode().equals(result.SUCCESS))
        {
            handleMonitoringError(result, instanceName, dottedName);
        }
        // 5. extract result list
        AttributeList resultList = null;
        AttributeList tmp = (AttributeList)result.getAttribute(event.getEffectiveDestination(), MonitorCommand.MONITOR_RESULT);
        Iterator it = tmp.iterator();
        while (it.hasNext())
        {
            Attribute attribute = (Attribute) it.next();
            resultList = (AttributeList)attribute.getValue();
        }
        return resultList;
    }
   
    //******************************************************************************************************************
    private int setMatchedAttributesToValue(AttributeList attrList, ObjectName objName, String attrPattern, Object value ) throws
    InstanceNotFoundException,AttributeNotFoundException, InvalidAttributeValueException,
    MBeanException, ReflectionException, MalformedNameException, IntrospectionException
    {
        ArrayList attrNames = getAttrNames(objName, attrPattern);
        if(attrNames.size()==0)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.no_attributes_matched_to_pattern", attrPattern );
            throw new AttributeNotFoundException( msg );
        }
        for(int i=0; i<attrNames.size(); i++)
        {
            String name = (String)attrNames.get(i);
            Object obj = convertValueToAttributeType(objName, name, value);
            mServer.setAttribute(objName, new Attribute(name, obj));
            attrList.add(new Attribute(name, obj));
        }
        return attrNames.size(); //number of added elements
    }
    
    //***************************************************************************************************
    private void addAttributeToList(AttributeList attrList, ObjectName objName, String attrPattern ) throws
    InstanceNotFoundException,AttributeNotFoundException, MBeanException,
    ReflectionException, MalformedNameException, IntrospectionException
    {
        // only single "*"/"property.*" masks available now and they are implemented
        // in ConfigMBeanBase.getAttributes() with "empty" values
        AttributeList list = null;
        if(attrPattern.equals(""+Tokens.kWildCardChar))
        {
            list = mServer.getAttributes(objName, new String[]{""});
            if(list!=null)
                attrList.addAll(list);
        }
        else
            if(attrPattern.equals(ConfigAttributeName.PROPERTY_NAME_PREFIX+Tokens.kWildCardChar))
            {
                list = mServer.getAttributes(objName, new String[]{ConfigAttributeName.PROPERTY_NAME_PREFIX});
                if(list!=null)
                    attrList.addAll(list);
            }
            else
            {
                Object attValue = mServer.getAttribute(objName, attrPattern);
                attrList.add(new Attribute(attrPattern, attValue));
            }
        
/* //the following code maybe will be used for non-config mbeans        
        ArrayList attrNames = getAttrNames(objName, attrPattern);
        if(attrNames.size()==0)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.no_attributes_matched_to_pattern", attrPattern );
            throw new AttributeNotFoundException( msg );
        }
        for(int i=0; i<attrNames.size(); i++)
        {
            String name = (String)attrNames.get(i);
            Object attValue;
            if(attrNames.size()==1)
                attValue = mServer.getAttribute(objName, name);
            else
            {
                try
                {
                    attValue = mServer.getAttribute(objName, name);
                }
                catch (Exception e)
                {
                    attValue = null;
                }
            }
            attrList.add(new Attribute(name, attValue));
        }
        return attrNames.size(); //number of added elements
*/
    }

    /**
     * Handle monitoring error.
     * @param result monitoring result.
     * @param instance instance on which monitoring was attempted
     * @param compName component on which monitoring was attempted
     * @throws ServiceNotFoundException if failure was caused by inability to 
     *      connect to instance
     * @throws InstanceNotFoundException if mbean corresponding to the specified
     *      name could not be found
     * @throws AttributeNotFoundException if mbean attribute corresponding to
     *      specified name could not be found
     * @throws JMRuntimeException Other runtime error in monitoring
     */ 
    private void handleMonitoringError(AdminEventResult result, String instance,
            String compName)
            throws AttributeNotFoundException, InstanceNotFoundException,
            ServiceNotFoundException {
        String resultCode = result.getResultCode();
        if (AdminEventResult.TRANSMISSION_ERROR.equals(resultCode)) {
			String msg = localStrings.getString( "admin.server.core.mbean.config.failed_to_connect_instance", instance );
            throw new ServiceNotFoundException( msg );
        } else if (AdminEventResult.MBEAN_NOT_FOUND.equals(resultCode)) {
			String msg = localStrings.getString( "admin.server.core.mbean.config.unmatched_mbean", compName );
            throw new InstanceNotFoundException( msg );
        } else if (AdminEventResult.MBEAN_ATTR_NOT_FOUND.equals(resultCode)) {
			String msg = localStrings.getString( "admin.server.core.mbean.config.unmatched_attribute", compName );
            throw new AttributeNotFoundException( msg );
        } else if (!AdminEventResult.SUCCESS.equals(resultCode)) {
			String msg = localStrings.getString( "admin.server.core.mbean.config.other_monitoring_error", resultCode );
            throw new JMRuntimeException( msg );
        }
    }

    //***************************************************************************************************
    private ArrayList getAttrNames(ObjectName objName, String attrPattern ) throws
    InstanceNotFoundException, IntrospectionException, ReflectionException
    {
        ArrayList list = new ArrayList();
        if(attrPattern==null)
            return list;
        
        if(attrPattern.indexOf(Tokens.kWildCardChar)>=0 || attrPattern.indexOf(ObjectNames.kSingleMatchChar)>=0)
        {
            MBeanInfo mi = mServer.getMBeanInfo(objName);
            if(mi!=null)
            {
                MBeanAttributeInfo[] ai = mi.getAttributes();
                if(ai!=null)
                    for(int i=0; i<ai.length; i++)
                    {
                        String name = ai[i].getName();
                        try
                        {
                            if((new CombinedPatternMatcher(attrPattern, name)).matches())
                                list.add(ai[i].getName());
                        } catch (Throwable e)
                        {
                        }
                    }
            }
        }
        else
            list.add(attrPattern); //for now
        return list;
    }
    
    //***************************************************************************************************
    private String getAttrType(ObjectName objName, String attrName) throws
    InstanceNotFoundException, IntrospectionException, ReflectionException
    {
        return getAttrType(attrName, mServer.getMBeanInfo(objName));
    }
    
    //**?*************************************************************************************************
    private String getAttrType(String attrName, MBeanInfo mi)
    {
        if(attrName==null && mi==null)
            return null;
        
        MBeanAttributeInfo[] ai = mi.getAttributes();
        if(ai!=null)
            for(int i=0; i<ai.length; i++)
            {
                String name = ai[i].getName();
                if(attrName.equals(ai[i].getName()))
                    return ai[i].getType();
            }
        return null;
    }
    
    //***************************************************************************************************
    private ArrayList getTargetObjectNames(ObjectName objectNamePattern ) throws MalformedObjectNameException
    {
        ArrayList list = new ArrayList();
        
        if(objectNamePattern==null)
            return list;
        
        //  call to MBeanServer.queryNames()
        Set mNames = mServer.queryMBeans(objectNamePattern, null);
        Iterator it = mNames.iterator();
        while (it.hasNext())
        {
            list.add(it.next());
        }
        
        // because of lazy loading we can have no registered mbeans, in this case - add pattern
        // FIXME: add check is ObjectName -> non-wildcarded pattern
        // NOTE: it will be added only if no one registered bean with such pattern otherwise it will work as before
        if(list.size()==0)
        {
            list.add(objectNamePattern);
        }
        //    list.add(new ObjectName(objectNamePattern)); //for now
        return list;
    }
    
    //***************************************************************************************************
    String extractAttrNameFromDotted(String dottedStringName) throws MalformedNameException
    {
        int idx = dottedStringName.lastIndexOf(Tokens.kDelimiterChar);
        if(idx<=0 || idx==(dottedStringName.length()-1))
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.cannot_extract_attribute_name_from_dotted_notation", dottedStringName );
            throw new MalformedNameException( msg );
        }
        //property?
        if(dottedStringName.substring(0, idx+1).endsWith(ConfigAttributeName.PROPERTY_NAME_PREFIX))
        {
            int idx2 = idx-ConfigAttributeName.PROPERTY_NAME_PREFIX.length();
            if(dottedStringName.charAt(idx2)==Tokens.kDelimiterChar)
            {
                idx = idx2;
                if(idx<1) {
					String msg = localStrings.getString( "admin.server.core.mbean.config.cannot_extract_attribute_name_from_dotted_notation", dottedStringName );
                    throw new MalformedNameException( msg );
				}
            }
        }
        return dottedStringName.substring(idx+1);
    }
    
    //***************************************************************************************************
    String extractInstanceNameFromDotted(String dottedName) throws MalformedNameException
    {
        Name name = new Name(dottedName);
        return name.getNamePart(0).toString();
    }

    //***************************************************************************************************
    ArrayList getMBeanNamesFromDottedPattern(String dottedStringName) throws MBeanConfigException,MalformedNameException, MalformedObjectNameException
    {
        String attrName = extractAttrNameFromDotted(dottedStringName);
        int idx  = dottedStringName.length() - attrName.length() - 1;
        if(idx<1) {
			String msg = localStrings.getString( "admin.server.core.mbean.config.genericconfigurator_cannot_extract_attribute_name_from_dotted_notation", dottedStringName );
            throw new MalformedNameException( msg );
		}
       
        ConfigMBeanNamingInfo info = new ConfigMBeanNamingInfo(dottedStringName.substring(0,idx));
        ArrayList list = new ArrayList();
        list.add(info.getObjectName());
        return list;
    }
    
    //******************************************************************************************************************
    private Object convertValueToAttributeType(ObjectName objName, String attrName, Object value )
    {
        try
        {
            if(value instanceof String) //only for string now
            {
                String type = getAttrType(objName, attrName);
                return MBeanEasyConfig.convertStringValueToProperType((String)value, type);
            }
        }
        catch (Throwable t)
        {
        }
        return value; //no conversion if error
    }
    
    
    //***************************************************
    //static MBean attributes and opeartions descriptions
    /* For 8.0 since we are having the JMX 1.2 RI MBeanServer, it needs all
     * the mbeans to be JMX compliant mbeans. The earlier implementation was
     * rather lenient in that it was not checking for the MBean being compliant
     * per se. */
    
    private static final String[] mAttrs = new String[0];
    private static final String[] mOpers =
    {
        "getGenericAttribute(String name), INFO",
        "getGenericAttributes(String[] attributeNames),   INFO",
        "setGenericAttribute(String name, Object objValue),  ACTION_INFO",
        "setGenericAttributes(javax.management.AttributeList al),          ACTION_INFO",
        "getMonitor(String[] name), INFO",
        "setMonitor(javax.management.AttributeList al), ACTION",
        "listMonitor(String name), INFO",
        "listGenericDottedNameContinuiations(String dottedName), INFO",
    };
    
    /** Implementation of <code>getMBeanInfo()</code>
     * Uses helper class <code>MBeanEasyConfig</code> to construct whole MBeanXXXInfo tree.
     * @return <code>MBeanInfo</code> objects containing full MBean description.
     */
    public MBeanInfo getMBeanInfo()
    {
        
        try
        {
            return (new MBeanEasyConfig(getClass(), mAttrs, mOpers, null)).getMBeanInfo();
        }
        catch(Throwable e)
        {
            sLogger.log(Level.FINE, MSG_GETMBEANINFO_FAILED, e);
            return null;
        }
    }
    
    /** Every resource MBean should override this method to execute specific
     * operations on the MBean. This method is enhanced in 8.0. It was a no-op
     * in 7.0. In 8.0, it is modified to invoke the actual method through
     * reflection.
     * @since 8.0
     * @see javax.management.MBeanServer#invoke
     * @see #getImplementingClass
     */
    protected Class getImplementingClass() {
        return ( this.getClass() );
    }
    
    /** Reflection requires the implementing object.  */
    protected Object getImplementingMBean() {
        return ( this );
    }
 
}
