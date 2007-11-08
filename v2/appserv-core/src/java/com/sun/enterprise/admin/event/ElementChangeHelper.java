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

package com.sun.enterprise.admin.event;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigChange;
import com.sun.enterprise.config.ConfigAdd;
import com.sun.enterprise.config.ConfigSet;
import com.sun.enterprise.config.ConfigUpdate;
import com.sun.enterprise.config.ConfigDelete;
import com.sun.enterprise.config.serverbeans.ServerTags;

import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.meta.MBeanRegistryEntry;
import com.sun.enterprise.admin.meta.naming.MBeanNamingDescriptor;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.meta.MBeanMetaException;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import java.util.logging.Level;
import java.util.logging.Logger;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Config Element Change Helper - class providing ElementChangeEvent
 * related services (events constructing/ change lists filtering etc.
 * 
 */
public class ElementChangeHelper{

    /**
     * Event type
     */
    static final String eventType = ElementChangeHelper.class.getName();

    // i18n StringManager
    private static StringManager localStrings = StringManager.getManager( ElementChangeHelper.class );
    private static String PROPERTY_SUBSTR = "/"+ServerTags.ELEMENT_PROPERTY+"[";

    private static final Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
    
    //**********************************************************************
    public ElementChangeHelper() 
    {
    }

    //**********************************************************************
    static boolean isChangesToMerge(ConfigChange change1, ConfigChange change2)
    {
        String xpath1, xpath2;
        if( change1==null || change2==null ||
            (xpath1 = change1.getXPath())==null ||
            (xpath2 = change2.getXPath())==null ||
            !compareXPathes( xpath1, xpath2) )
            return false;
        if(isPropertyXPath(xpath2)) //optimize it later
            return true;
        return ( xpath1.equals(change2.getXPath()) &&
            change1.getClass().equals(change2.getClass()) );
    }
    
    //***************************************************************************
    static boolean checkChangeListForElement(ArrayList list)
    {
        if(list==null || list.size()<=0)
            return false;
        for(int i=1; i<list.size(); i++)
            if(!isChangesToMerge((ConfigChange)list.get(i-1), (ConfigChange)list.get(i))) 
               return false;
        return true;
    }
    //***************************************************************************
    static boolean isPropertyChange(ConfigChange change)
    {
        String xpath;
        return (change!=null &&
                (xpath = change.getXPath())!=null &&
                (xpath.indexOf(PROPERTY_SUBSTR))>=0 );
    }
    //***************************************************************************
    static boolean isPropertyXPath(String xpath)
    {
        return ((xpath != null) && ((xpath.indexOf(PROPERTY_SUBSTR))>=0));
    }
    //***************************************************************************
    // getting the primary key of config element
    static String getConfigElementPrimaryKey(String xpath)
    {
        if (xpath != null) {
            xpath = xpath.trim();
        }
        if((xpath!= null) && (xpath.endsWith("']")))
        {
            int idx = xpath.lastIndexOf('\'', xpath.length()-3);
            return xpath.substring(idx+1, xpath.length()-2);
        }
        return null;
    }    

    //***************************************************************************
    // getting the type of config element
    static String getConfigElementType(String xpath)
    {
        if(xpath.trim().endsWith("]"))
            xpath =  xpath.substring(0, xpath.lastIndexOf('['));
        return xpath.substring(0, xpath.lastIndexOf('/'));
    }    

    //***************************************************************************
    // get target name for event
    static String getConfigElementTargetName(String xpath, ConfigContext ctx)
    {
        String targetName = null;
        try {
            // Get target name for event
            TargetBuilder targetBuilder = new TargetBuilder();
            return targetBuilder.getTargetNameForXPath(xpath, ctx, true);
        } catch(Exception e)
        {
            //can not find target -> get default (domain)
        }
        return null;
    }
    //***************************************************************************
    static String getElementXPath(ArrayList changes)
    {
        return getElementXPath((ConfigChange)changes.get(0));
    }
    
    static String getElementXPath(ConfigChange change)
    {
        String xpath = change.getXPath();
        if(xpath==null)
            return xpath;
        int iMatch = xpath.indexOf(PROPERTY_SUBSTR);
        if(iMatch<0)
            return xpath;
        return xpath.substring(0, iMatch); 
    }
    //***************************************************************************
    static int getActionCodeForChanges(ArrayList changes)
    {
        return getActionCodeForChange((ConfigChange)changes.get(0));
    }
    
    static int getActionCodeForChange(ConfigChange change)
    {
        if(isPropertyXPath(change.getXPath()))
            return ElementChangeEvent.ACTION_ELEMENT_UPDATE;
        int action;
	//set action code
        if(change instanceof ConfigSet)
            action = ElementChangeEvent.ACTION_ELEMENT_CREATE;
        else if(change instanceof ConfigAdd)
            action = ElementChangeEvent.ACTION_ELEMENT_CREATE;
        else if(change instanceof ConfigUpdate)
            action = ElementChangeEvent.ACTION_ELEMENT_UPDATE;
        else if(change instanceof ConfigDelete)
            action = ElementChangeEvent.ACTION_ELEMENT_DELETE;
        else
            action = ElementChangeEvent.ACTION_ELEMENT_UNDEFINED;
        return action;
    }
    
    //***************************************************************************
    static private boolean compareXPathes(String xpath1, String xpath2)
    {
        int iProp;
        if((iProp = xpath1.indexOf(PROPERTY_SUBSTR))>0)
            xpath1 = xpath1.substring(0, iProp);
        if((iProp = xpath2.indexOf(PROPERTY_SUBSTR))>0)
            xpath2 = xpath2.substring(0, iProp);
        return xpath1.equals(xpath2); 
    }

    /* 
     * generate the array of ElementChangeEvents from given change list
     * @param changeList list of ConfigChanges to analyse
     * @param domainContext ConfigContext of domaiin.xml config file
     * @returns array of ElementChangeEvents
     * @throws IllegalArgumentException
     */
    public AdminEvent[] generateElementChangeEventsFromChangeList(String instanceName, ArrayList changeList, ConfigContext domainContext)
    {
        ArrayList merged = new ArrayList();
        ArrayList events = new ArrayList();
        ConfigChange change, lastChange = null;
        for(int i=0; i<changeList.size(); i++)
        {
            change = (ConfigChange)changeList.get(i);

            // check if dynamic reconfig is needed for this config change
            if (! isChangeDynamicReconfigNeeded(change)) {
		break;
	    }

            if(merged.isEmpty() || 
               isChangesToMerge(lastChange, change))
            {
                //add change to list
                merged.add(change);
                lastChange = change;
            }
            else
            {
                //here we are only if collected changes portion completed
                // it is time to create ElementChangeEvent 
                if(!merged.isEmpty())
                {
                    ArrayList new_events = createEventsForElementChange(instanceName, merged, domainContext, changeList);
                    if(new_events!=null && new_events.size()>0)
                        events.addAll(new_events);
                    //FIXME: - do we need treat empty answer as "restart is needed" sign?
                    merged.clear();
                    //add change to list
                    merged.add(change);
                    lastChange = change;
                }
            }
        }
        // last portion could be here
        if(!merged.isEmpty())
        {
            ArrayList new_events = createEventsForElementChange(instanceName, merged, domainContext, changeList);
            if(new_events!=null && new_events.size()>0)
                events.addAll(new_events);
            //FIXME: - do we need treat empty answer as "restart is needed" sign?
        }
        return (AdminEvent[])events.toArray(new AdminEvent[events.size()]);   
    }

    /* 
     * create ElementChangeEvents from given change list 
     *    (all changes should be related to the same element)
     * @param changeList list of ConfigChanges related to the same element
     * @param domainContext ConfigContext of domaiin.xml config file
     * @returns array of ElementChangeEvent (or null)
     * @throws IllegalArgumentException
     */
    public ArrayList createEventsForElementChange(String instanceName, ArrayList changeList, ConfigContext domainContext, ArrayList globalChangeList)
    {
       return createEventsForElementChange(instanceName, changeList, domainContext, true, globalChangeList);
    }
    
    private ArrayList createEventsForElementChange(String instanceName, ArrayList changeList, ConfigContext domainContext, boolean bCheckList, ArrayList globalChangeList)
    {
        
        if(bCheckList && !checkChangeListForElement(changeList))
        {
            String msg = localStrings.getString( "admin.event.wrong_configchange" );
            throw new IllegalArgumentException( msg );
        }

        String xpath0  = getElementXPath(changeList);
        if(xpath0==null)
            return null;
        // instantiate the proper Event
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 1. get event name

        MBeanRegistryEntry  entry = getRegistryEntry(xpath0);
        if(entry==null)
            return null;

        String              eventName = entry.getElementChangeEventName();
        if(eventName!=null && eventName.indexOf('.')<0)
            eventName = EVENT_PACKAGE_PATH_PREFIX + eventName;
        if(eventName==null)
            return null; //element is not a subject for ElementChangeEvent

        //  2. instantiate event
        try {
           return ElementChangeEventsFactory.createEvents(eventName, instanceName, 
                      entry.getName(), changeList, domainContext, globalChangeList);
        } catch(Exception e)
        {
            e.printStackTrace();
            String msg = localStrings.getString( "admin.event.cannot_create_eventforchange", eventName, xpath0 );
            throw new IllegalArgumentException( msg );
        }
    }

    
    String              lastXpath = null;
    MBeanRegistryEntry  lastEntry = null;
    static final public String EVENT_PACKAGE_PATH_PREFIX = "com.sun.enterprise.admin.event.";
    
    private synchronized MBeanRegistryEntry getRegistryEntry(String xpath)
    {
        if(xpath.equals(lastXpath))
            return lastEntry; 
        MBeanRegistry       registry = MBeanRegistryFactory.getAdminMBeanRegistry();
        MBeanRegistryEntry  entry = registry.findMBeanRegistryEntryByXPath(xpath);
        if(entry!=null)
        {
            lastXpath = xpath;
            lastEntry = entry;
        }
        return entry;
    }

    static public Set getXPathesForDynamicallyNotReconfigurableElements(ArrayList changeList)
    {
        HashSet xpathes = new HashSet();
        if(changeList==null)
            return xpathes;

        MBeanRegistry       registry = MBeanRegistryFactory.getAdminMBeanRegistry();
        String xpath_last = null;
        MBeanRegistryEntry  entry = null;
        boolean bLastEntryHasEvent = false;
        String propertyName = null;
        
        for(int i=0; i<changeList.size();i++)
        {
            ConfigChange change = (ConfigChange)changeList.get(i);
            int action = getActionCodeForChange(change);
            if(action==ElementChangeEvent.ACTION_ELEMENT_UNDEFINED)
                continue; //??? not add/set/update/delete
            String xpath = change.getXPath();
            // Check if this xpath belongs to one of the excluded 
            // XPaths. Excluded xpaths include lb-configs, lb-config
            // etc. Where restart checks do not apply.
            if (  (xpath == null) || isXPathExcludedForRestartCheck(xpath) ) {
                continue;
            }
            if(!xpath.equals(xpath_last))
            {
                xpath_last = xpath;
                if(isPropertyXPath(change.getXPath()))
                {
                    propertyName = getConfigElementPrimaryKey(xpath);
                    xpath = getElementXPath(change);
                }
                else
                {
                    propertyName = null;
                }
                entry = registry.findMBeanRegistryEntryByXPath(xpath);
                if(entry==null)
                {
                    xpathes.add(xpath);
                    continue;
                }
                bLastEntryHasEvent = (entry.getElementChangeEventName()!=null);
            }
            if(bLastEntryHasEvent)
                continue;
            // check whether creation reconfigurable
            if( (action==ElementChangeEvent.ACTION_ELEMENT_CREATE &&
                 entry.isElementCreationDynamicallyReconfigurable()))
            {
                xpathes.add(xpath);
                continue;
            }
            // check whether deletion reconfigurable
            if( (action==ElementChangeEvent.ACTION_ELEMENT_DELETE &&
                 entry.isElementDeletionDynamicallyReconfigurable()))
            {
                xpathes.add(xpath);
                continue;
            }
            // check properties changes
            if(propertyName!=null)
            {
                if(!entry.isPropertyDynamicallyReconfigurable(propertyName))
                {
                    xpathes.add(xpath);
                    continue;
                }
            }
            if ( !(change instanceof ConfigUpdate))
            {
                xpathes.add(xpath);
                continue;
            }
            //here we are only for attributes
            Set attrs = ((ConfigUpdate)change).getAttributeSet();
            Iterator iter  = attrs.iterator();
            while(iter.hasNext())
            {
                String attr = (String)iter.next();
                // check if dynamic reconfig is needed for the attribute, bug# 6509963
                try {
                    if (! entry.isAttributeDynamicReconfigNeeded(attr)) 
                    {
                        break;
                    }
                } catch (MBeanMetaException mme)
                {
                	//field not found in registry
                }

                if(!entry.isAttributeDynamicallyReconfigurable(attr))
                {
                    xpathes.add(xpath);
                    break;
                }
            }
        }
        return xpathes;
    }

    /**
     * Get a boolean value corresponding to the string value in xml.
     * @param strValue the string representing a boolean
     * @return true if the string is "true", "yes", "on" or "1"
     */
    public static boolean getBooleanValue(String strValue) {
        boolean retval = false;
        if (strValue == null) {
            return retval;
        }
        if (strValue.equalsIgnoreCase("true")
                || strValue.equalsIgnoreCase("yes")
                || strValue.equalsIgnoreCase("on")
                || strValue.equalsIgnoreCase("1")) {
            retval = true;
        }
        return retval;
    }
    
    /**
     * Checks if an XPath is excluded for restart required check.
     * Restart required check is skipped for lb-configs and lb-config
     * elements.
     *
     * @param xpath     XPath of the config change
     * 
     * @return boolean  true - if restart required check to be ignored, false
     *                  otherwise
     */
    private static boolean isXPathExcludedForRestartCheck(String xpath) 
    {
        if (xpath == null) {
            return false;
        }

        for(int excludeIdx =0; excludeIdx < restartExcludeXPaths.length; 
                excludeIdx++) {

            if ( xpath.startsWith(restartExcludeXPaths[excludeIdx]) ) {
                return true;
            }
        }
        return false;
    }

    /*
     * finds "enabled" update element in change-list and returns its new value (as Boolean)
     *  or null if not found
     */
    public static Boolean findEnabledChange(ArrayList changeList)
    {
        if(changeList==null)
            return null;
        for(int i=changeList.size()-1; i>=0; i--)
        {
            if ( changeList.get(i) instanceof ConfigUpdate) 
                {
                    ConfigUpdate update = (ConfigUpdate)changeList.get(i);
                    String enableStr = update.getNewValue(ServerTags.ENABLED);
                    if (enableStr != null) 
                        return new Boolean(ElementChangeHelper.getBooleanValue(enableStr));
                }
        }
        return null;
    }

    // remove enable config change from the config change list
    public static ConfigChange removeEnabledChange(ArrayList changeList)
    {
        if(changeList==null)
            return null;
        for(int i=changeList.size()-1; i>=0; i--)
        {
            if ( changeList.get(i) instanceof ConfigUpdate)
                {
                    ConfigUpdate update = (ConfigUpdate)changeList.get(i);
                    String enableStr = update.getNewValue(ServerTags.ENABLED);
                    if (enableStr != null) {
                        return (ConfigChange)changeList.remove(i);
                    }
                }
        }
        return null;
    }

    /**
     * Check if the given config change needs dynamic reconfiguration
     */
    private boolean isChangeDynamicReconfigNeeded(ConfigChange change) {
        boolean b = true;

        // if not update return
        if ( !(change instanceof ConfigUpdate)) return b;

        // get registry entry
        MBeanRegistryEntry  entry = getRegistryEntry(change.getXPath());
        if (entry == null) return b;

        // check if dynamic reconfig needed for the given attribute
        Set attrs = ((ConfigUpdate)change).getAttributeSet();
        if (attrs.isEmpty()) return b;

        Iterator<String> attrIter = attrs.iterator();
        String attrName = null;
        while(attrIter.hasNext()) {
            try {
                attrName = attrIter.next();
                b = entry.isAttributeDynamicReconfigNeeded(attrName);
                // if the dynamic reconfig needed is true for any of 
                // the attributes then return true
                if (b) return b; 
            } catch (MBeanMetaException mme)
            {
                logger.log(Level.INFO, "event.mbean_registry_entry.attribute.not.found", attrName);
            }
        }
        return b;
    }

    // PRIVATE VARIABLES 

    // The XPath prefixes are excluded during restart required check.
    // These XPaths belong to lb-configs and lb-config element

    private static String[] restartExcludeXPaths = { "/domain/lb-configs","/domain/load-balancers" };
}
