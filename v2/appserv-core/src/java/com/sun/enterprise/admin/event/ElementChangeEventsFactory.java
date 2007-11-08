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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.event;

import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.lang.reflect.Method;

import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigChange;
import com.sun.enterprise.config.ConfigUpdate;
import com.sun.enterprise.config.impl.ConfigUpdateImpl;
import com.sun.enterprise.admin.target.TargetType;

/**
 *  ElementChangeEvent listener 
 */
public abstract class ElementChangeEventsFactory {
    
    /*
     * create ElementChange related event
     *
     * @param instanceName source instance name
     * @param targetName - destination target name (can be null)  
     * @param event_type - 
     * @param changeList
     * @param ctx
     */
    static ArrayList createEvents(
                String event_type, String instanceName,
                String element_type,
                ArrayList changeList, ConfigContext ctx, 
                ArrayList globalChangeList) throws Exception
    {

        int      action          = ElementChangeHelper.getActionCodeForChanges(changeList);
        if(action==ElementChangeEvent.ACTION_ELEMENT_UNDEFINED)
        {
            return null; //ignore wrong type
        }

        ArrayList events = null;
        AdminEvent event = null;
        String   element_xpath   = ElementChangeHelper.getElementXPath(changeList);
        String   targetName      = ElementChangeHelper.getConfigElementTargetName(element_xpath, ctx);
        String   element_id      = ElementChangeHelper.getConfigElementPrimaryKey(element_xpath); //

        // Special treatment for custom MBean
        // only if application-ref changed
        // we will replace "ApplicationDeployEvent" to "MBeanElementChangeEvent" 
        if (event_type.equals(ApplicationDeployEvent.eventType))
        {
            if(APPLICATION_REF_TYPE_NAME.equals(element_type))
            {
                try {
                    if(isMBeanReference(ctx, element_id, globalChangeList))
                    {
                       event_type="com.sun.enterprise.admin.event.MBeanElementChangeEvent";
                       //targetName = TargetType.DOMAIN.getName();
                       events =  buildSelfConstructingEvent(event_type, 
                                instanceName, element_type, changeList, ctx);
                        if(events!=null && 
                           (action==ElementChangeEvent.ACTION_ELEMENT_CREATE ||
                            action==ElementChangeEvent.ACTION_ELEMENT_DELETE ) )
                        {
                            
                            //add application dependent elements
                            String actionCode = 
                              (action==ElementChangeEvent.ACTION_ELEMENT_CREATE)?
                                BaseDeployEvent.DEPLOY:BaseDeployEvent.UNDEPLOY;
                            for(int i=0; i<events.size(); i++ )
                            {
                               AdminEvent ae = (AdminEvent)events.get(i);
                               String effective = ae.getEffectiveDestination();
                               DependencyResolver dr = 
                                       new DependencyResolver(ctx, effective);
                               List list = dr.resolveApplications(element_id, actionCode);
                               ae.addDependentConfigChange(list);
                            }
                        }
                    }
               } catch (Exception ce){
                   //something wrong - leave it for standard proceeding
                   events = null;
               }
           }
        }
            
        //currently it generates only ElementChangeEvents
        events =  buildSelfConstructingEvent(event_type, 
                instanceName, element_type, changeList, ctx);
        if(events!=null)
        {
//            if(MBEAN_TYPE_NAME.equals(element_type) &&
//               action==ElementChangeEvent.ACTION_ELEMENT_DELETE)
//            {
//                //this temporary solution for case of deletion of
//                //mbean only existing in remote instance
//                for(int i=0; i<events.size(); i++)
//                {
//                    AdminEvent ae = (AdminEvent)events.get(i);
//                    //broadcast to all
//                    ae.setTargetDestination("domain");
//                }
//            }

            return events;
        }

        boolean bMustSetChangeList = true; // config changes must be set into resulting event
        //redirect to custom event
        if(event_type.equals(SecurityServiceEvent.eventType))
        {
            event = (AdminEvent)createSecurityServiceEvent(instanceName, element_id, action);
        }
        else if(event_type.equals(AuditModuleEvent.eventType))
        {
            event = (AdminEvent)createAuditModuleEvent(instanceName, element_id, action);
        }
        else if(event_type.equals(AuthRealmEvent.eventType))
        {
            event = (AdminEvent)createAuthRealmEvent(instanceName, element_id, action);
        }
        else if(event_type.equals(LogLevelChangeEvent.eventType))
        {
            events = createLogLevelEvents(instanceName, element_id, action, changeList);
            bMustSetChangeList = false;
        }
        else if(event_type.equals(MonitoringLevelChangeEvent.eventType))
        {
            events = createMonitoringLevelEvents(instanceName, element_id, action, changeList);
            bMustSetChangeList = false;
        }
        else if(event_type.equals(ResourceDeployEvent.eventType))
        {
            event = (AdminEvent)createResourceDeployEvent(instanceName, element_type, element_id, action, changeList, targetName, ctx);
            bMustSetChangeList = false;
        }
        else if(event_type.equals(ModuleDeployEvent.eventType)  ||
               event_type.equals(ApplicationDeployEvent.eventType))
        {
            event = (AdminEvent)createModAppDeployEvent(instanceName, element_type, element_id, action, changeList, targetName, ctx);
            bMustSetChangeList = false;
        }
        else
        {
            return null; 
        }
           
       
       if( event!=null && (events==null || events.size()==0) )
       {
           events = new ArrayList();
           events.add(event);
       }
           
       if( events==null || events.size()==0 )
           return null;
        
       // set desctination target (can be null)
       setTargetDestinationAndConfigChange(events, targetName, changeList, bMustSetChangeList);

       //System.out.println("***********setTargetDestinationAndConfigChange->"+targetName);
       return events;
    }
    
//=============================================================================
    static private ArrayList buildSelfConstructingEvent(
                String event_type, 
                String instanceName,
                String element_type,
                ArrayList changeList, 
                ConfigContext ctx) throws Exception
    {
       try {
            //self-consrtructing event ?
            Class cl = Class.forName(event_type);
            Method m = cl.getMethod("getEventInstances", new Class[]{
                String.class, String.class, String.class,
                ArrayList.class, ConfigContext.class});
            return (ArrayList)m.invoke(null, new Object[]{event_type, instanceName, element_type, changeList, ctx});
        } catch (Exception e) { 
            if( !(e instanceof ClassNotFoundException) &&
                !(e instanceof NoSuchMethodException) )
                throw e;
        }
       return null;
    }

//=============================================================================
    static private boolean isMBeanReference(
                ConfigContext ctx,
                String element_id, 
                ArrayList globalChangeList)
    {
        String typeInDomain = null;
        try {
            typeInDomain = ApplicationHelper.getApplicationType(ctx, element_id);
        } catch(Exception e)
        {
        }
        if(typeInDomain!=null)
            return Applications.MBEAN.equals(typeInDomain);
        //here we are only if there is no MBean found in config context
        //let's check globalChangeList
        String toCompare = 
              "/" + ServerTags.MBEAN + 
              "[@" + ServerTags.NAME + "='" + element_id + "']";
        for(int i=0; i<globalChangeList.size(); i++)
        {
            ConfigChange change = (ConfigChange)globalChangeList.get(i);
            String xpath;
            if(change!=null &&
               (xpath=change.getXPath())!=null &&
               xpath.endsWith(toCompare))
               return true;
        }
        return false;
    }
                        
//=============================================================================
    static private void setTargetDestinationAndConfigChange(ArrayList events, String targetName, ArrayList changeList, boolean bSetChanges)
    {
        if(events!=null)
        {
            for(int i=0; i<events.size(); i++)
            {
                AdminEvent event = (AdminEvent)events.get(i); 
                event.setTargetDestination(targetName);
                if(bSetChanges && changeList!=null)
                    event.addConfigChange(changeList);
            }
        }
    }
    
//=============================================================================
    //ELEMENT CREATORS
    
    //******************************************
    // security-service 
    //******************************************
    static private SecurityServiceEvent createSecurityServiceEvent(String instanceName, String id, int action)
    {
        // create custom event
        if(action==ElementChangeEvent.ACTION_ELEMENT_CREATE)
            action = SecurityServiceEvent.ACTION_CREATE;
        else if(action==ElementChangeEvent.ACTION_ELEMENT_DELETE)
            action = SecurityServiceEvent.ACTION_DELETE;
        else if(action==ElementChangeEvent.ACTION_ELEMENT_UPDATE)
            action = SecurityServiceEvent.ACTION_UPDATE;
        return new SecurityServiceEvent(instanceName, action);
    }   
    
    //******************************************
    // Audit Module 
    //******************************************
    static private AuditModuleEvent createAuditModuleEvent(String instanceName, String id, int action)
    {
        // create custom event
        if(action==ElementChangeEvent.ACTION_ELEMENT_CREATE)
            action = AuditModuleEvent.ACTION_CREATE;
        else if(action==ElementChangeEvent.ACTION_ELEMENT_DELETE)
            action = AuditModuleEvent.ACTION_DELETE;
        else if(action==ElementChangeEvent.ACTION_ELEMENT_UPDATE)
            action = AuditModuleEvent.ACTION_UPDATE;
        return  new AuditModuleEvent(instanceName, id, action);
    }   
    
    //******************************************
    // Auth Realm 
    //******************************************
    static private AuthRealmEvent createAuthRealmEvent(String instanceName, String id, int action)
    {
        // create custom event
        if(action==ElementChangeEvent.ACTION_ELEMENT_CREATE)
            action = AuthRealmEvent.ACTION_CREATE;
        else if(action==ElementChangeEvent.ACTION_ELEMENT_DELETE)
            action = AuthRealmEvent.ACTION_DELETE;
        else if(action==ElementChangeEvent.ACTION_ELEMENT_UPDATE)
            action = AuthRealmEvent.ACTION_UPDATE;
        return  new AuthRealmEvent(instanceName, id, action);
    }   
    
    //******************************************
    // Log Level change event
    //******************************************
    static private ArrayList createLogLevelEvents(String instanceName, String id, int action, ArrayList changeList)
    {
        ArrayList events = new ArrayList();
        for(int i=0; i<changeList.size(); i++)
        {
            Object chg = changeList.get(i);

            if (ElementChangeHelper.isPropertyChange((ConfigChange)chg))
            {
                // constructs an event with property changed flag set to true
                LogLevelChangeEvent event=new LogLevelChangeEvent(instanceName);

                // sets the flag
                event.setPropertyChanged(true);

                // sets the property name
                String xpath = ((ConfigChange) chg).getXPath();
                event.setPropertyName( 
                    ElementChangeHelper.getConfigElementPrimaryKey(xpath) );

                // adds the config change objects
                event.addConfigChange((ConfigChange)chg);

                // adds the event to the event list
                events.add(event);
            }
        }

        if (action!=ElementChangeEvent.ACTION_ELEMENT_UPDATE)
            return events;

        for(int i=0; i<changeList.size(); i++)
        {
            Object chg = changeList.get(i);
            if(!(chg instanceof ConfigUpdate) || 
                ElementChangeHelper.isPropertyChange((ConfigChange)chg))
               continue; //what if property changed ? 
            ConfigUpdate update = (ConfigUpdate)chg;
            Set attrs = update.getAttributeSet();
            if (attrs != null) 
            {
                Iterator iter = attrs.iterator();
                while (iter.hasNext()) {
                    String compName = (String)iter.next();
                    String oldValue = update.getOldValue(compName);
                    String newValue = update.getNewValue(compName);
                    LogLevelChangeEvent event = new LogLevelChangeEvent(instanceName);
                    event.setModuleName(compName);
                    event.setOldLogLevel(oldValue);
                    event.setNewLogLevel(newValue);
                    events.add(event);
                    ConfigUpdate upd = new ConfigUpdateImpl(update.getXPath(), 
                            compName, oldValue,newValue);
//System.out.println("***********createLogLevelEvents."+compName +":"+oldValue+"->"+newValue+" target="+instanceName);
                    event.addConfigChange(upd);
                }
            }
        }
        return events;
    }   

    //******************************************
    // Monitoring Level Events change event
    //******************************************
    static private ArrayList createMonitoringLevelEvents(String instanceName, String id, int action, ArrayList changeList)
    {
        if (action!=ElementChangeEvent.ACTION_ELEMENT_UPDATE)
            return null;

        ArrayList events = new ArrayList();
        for(int i=0; i<changeList.size(); i++)
        {
            Object chg = changeList.get(i);
            if(!(chg instanceof ConfigUpdate) || 
                ElementChangeHelper.isPropertyChange((ConfigChange)chg))
               continue; //what if property changed ? 
            ConfigUpdate update = (ConfigUpdate)chg;
            Set attrs = update.getAttributeSet();
            if (attrs != null) 
            {
                Iterator iter = attrs.iterator();
                while (iter.hasNext()) {
                    String compName = (String)iter.next();
                    String oldValue = update.getOldValue(compName);
                    String newValue = update.getNewValue(compName);
                    MonitoringLevelChangeEvent event = new MonitoringLevelChangeEvent(instanceName);
                    event.setComponentName(compName);
                    event.setOldMonitoringLevel(oldValue);
                    event.setNewMonitoringLevel(newValue);
                    events.add(event);
                    ConfigUpdate upd = new ConfigUpdateImpl(update.getXPath(), 
                            compName, oldValue,newValue);
                    event.addConfigChange(upd);
                }
            }
        }
        return events;
    }   

    static final String RESOURCE_REF_TYPE_NAME = ServerTags.RESOURCE_REF;
    static final String APPLICATION_REF_TYPE_NAME = ServerTags.APPLICATION_REF;
    static final String MBEAN_TYPE_NAME = ServerTags.MBEAN;
    //******************************************
    // ResourceDeployment events
    //******************************************
    static private ResourceDeployEvent createResourceDeployEvent(String instanceName, 
            String elemType, String id, int action, ArrayList changeList, 
            String targetName, ConfigContext ctx)  throws Exception
    {
        ResourceDeployEvent rde = null;
        
        // for security map 
        
        if(("security-map").equals(elemType)){
        	String actionCode = null;
        	if(action==ElementChangeEvent.ACTION_ELEMENT_CREATE || 
        			action==ElementChangeEvent.ACTION_ELEMENT_DELETE){
        		
        	actionCode=BaseDeployEvent.REDEPLOY;
        	       	
        	String element_xpath = ElementChangeHelper.getElementXPath(changeList);
        	// get xpath corresponding to the jdbc-connection-pool
        	int endNewXpath = element_xpath.lastIndexOf("security-map");
        	String new_element_xpath= element_xpath.substring(0,endNewXpath-1);
        	id = ElementChangeHelper.getConfigElementPrimaryKey(new_element_xpath); 
        	}
        	
        	EventBuilder builder = new EventBuilder();
            if(actionCode!=null)
                rde = builder.createResourceDeployEvent(actionCode, id, 
                            ctx, changeList, targetName);
            return rde;
        }
        
        //************************************
        // 1. for references' changes
        //************************************
        if(RESOURCE_REF_TYPE_NAME.equals(elemType))
        {
            String actionCode = null;
            // "resource-ref" change proceeding 
            if(action==ElementChangeEvent.ACTION_ELEMENT_CREATE)
            {
                actionCode = BaseDeployEvent.ADD_REFERENCE;
            }
            else if(action==ElementChangeEvent.ACTION_ELEMENT_DELETE)
            {
                // adds reference remove event to the stack
                actionCode = BaseDeployEvent.REMOVE_REFERENCE;
            }
            else if(action==ElementChangeEvent.ACTION_ELEMENT_UPDATE)
            {
                Boolean bEnabled = null;
                if (/* changeList.size()==1 && */  
                     (bEnabled=ElementChangeHelper.findEnabledChange(changeList))!=null)
                {
                    actionCode = bEnabled.booleanValue()?BaseDeployEvent.ENABLE:BaseDeployEvent.DISABLE;
                }
                     
//                else
//                    actionCode = BaseDeployEvent.REDEPLOY;
            } 
            EventBuilder builder = new EventBuilder();
            if(actionCode!=null)
            {
                rde = builder.createResourceDeployEvent(actionCode, id, 
                            ctx, changeList, targetName);
            }
        } else 
//?? should we check all possible resources values
//?? skipped for now        
//??        if(RESOURCE_TYPE_NAME.equals(elemType))
        {
            //************************************
            // 2. for resources' changes
            //************************************
            String actionCode = null;
            // resource change proceeding 
            if(action==ElementChangeEvent.ACTION_ELEMENT_CREATE)
            {
//                actionCode = ResourceDeployEvent.DEPLOY;
            }
            else if(action==ElementChangeEvent.ACTION_ELEMENT_DELETE)
            {
//                actionCode = ResourceDeployEvent.UNDEPLOY;
            }
            else if(action==ElementChangeEvent.ACTION_ELEMENT_UPDATE)
            {
                Boolean bEnabled = null;
                if (changeList.size()==1 && (bEnabled = ElementChangeHelper.findEnabledChange(changeList))!=null) 
                {
                    actionCode = bEnabled.booleanValue()?BaseDeployEvent.ENABLE:BaseDeployEvent.DISABLE;
                }
                else
                    actionCode = BaseDeployEvent.REDEPLOY;
            } 
            EventBuilder builder = new EventBuilder();
            if(actionCode!=null)
                rde = builder.createResourceDeployEvent(actionCode, id, 
                            ctx, changeList, targetName);
        }
        return rde;
    }

    //******************************************
    // ModuleDeployment events
    //******************************************
    static private BaseDeployEvent createModAppDeployEvent(String instanceName, 
            String elemType, String id, int action, ArrayList changeList, 
            String targetName, ConfigContext ctx)  throws Exception
    {
        BaseDeployEvent bde = null;
        String actionCode = null;
        if(APPLICATION_REF_TYPE_NAME.equals(elemType))
        {
            //************************************
            // 1. for references' changes
            //************************************
            if(action==ElementChangeEvent.ACTION_ELEMENT_CREATE)
            {
//                actionCode = BaseDeployEvent.DEPLOY;
            }
            else if(action==ElementChangeEvent.ACTION_ELEMENT_DELETE)
            {
//                actionCode = BaseDeployEvent.UNDEPLOY;
            }
            else if(action==ElementChangeEvent.ACTION_ELEMENT_UPDATE)
            {
                Boolean bEnabled = null;
                if (/* changeList.size()==1 && */  
                     (bEnabled=ElementChangeHelper.findEnabledChange(changeList))!=null)
                {
                    actionCode = bEnabled.booleanValue()?BaseDeployEvent.ENABLE:BaseDeployEvent.DISABLE;
                }
                     
//                else
//                    actionCode = BaseDeployEvent.REDEPLOY;
            } 
            EventBuilder builder = new EventBuilder();
            if(actionCode!=null)
                bde = builder.createModAppDeployEvent(actionCode, id, 
                            ctx, changeList, targetName);
        }
        else
        {
            //****************************************
            // now let's try to find if enable updated 
            //****************************************
            // resource change proceeding 
            if(action==ElementChangeEvent.ACTION_ELEMENT_CREATE)
            {
//                    actionCode = BaseDeployEvent.DEPLOY;
            }
            else if(action==ElementChangeEvent.ACTION_ELEMENT_DELETE)
            {
//                    actionCode = BaseDeployEvent.UNDEPLOY;
            }
            else if(action==ElementChangeEvent.ACTION_ELEMENT_UPDATE)
            {
                Boolean bEnabled = null;
                if (changeList.size()==1 && (bEnabled = ElementChangeHelper.findEnabledChange(changeList))!=null) 
                {
                    actionCode = bEnabled.booleanValue()?BaseDeployEvent.ENABLE:BaseDeployEvent.DISABLE;
                }
                else
                    actionCode = BaseDeployEvent.REDEPLOY;
            } 
            EventBuilder builder = new EventBuilder();
            if(actionCode!=null)
                bde = builder.createModAppDeployEvent(actionCode, id, 
                            ctx, changeList, targetName);
        }
        return bde;
    } 

}
