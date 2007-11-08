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

import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.admin.event.v3.ElementChangeHelper;


//i18n import
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.v3.server.Globals;

/**
 * Config Element Change Event - emitted by DAS after comletion of
 * create/delete/update operation on domain.xml element (node)
 * only for those elements which have nonempty "ChangeListener" field
 * in correspondent descriptor entry.
 * Contains element(mbean) type, id (primary key value),  and action type.
 */
public class ElementChangeEvent extends AdminEvent {

    /**
     * Constant denoting action code 
     */
    public static final int ACTION_ELEMENT_UNDEFINED = 0;
    
    public static final int ACTION_ELEMENT_CREATE = 1;
    public static final int ACTION_ELEMENT_DELETE = 2;
    public static final int ACTION_ELEMENT_UPDATE = 3;

    /**
     * Event type
     */
    static final String eventType = ElementChangeEvent.class.getName();

    /**
     * Attributes
     */
    private int     change_action = ACTION_ELEMENT_UNDEFINED;
    private String  element_id = null;
    private String  element_type = null;  //type from mbean descritors file
    private String  element_path = null;  

    // i18n StringManager
    private static StringManager localStrings = StringManager.getManager( ElementChangeEvent.class );

    /* *****************************************************
     *  default ElementChange events factory
     * @param event_type - event type from mbean decriptor xml file
     * @param instanceName
     * @param changeList
     * @param ctx
     */
    public static ArrayList getEventInstances(
                String event_type, String instanceName, String elementType,
                ArrayList changeList, ConfigContext ctx) throws Exception
    {
        ElementChangeHelper helper = Globals.getGlobals().getDefaultHabitat().getComponent(ElementChangeHelper.class);
        
        int      action          = helper.getActionCodeForChanges(changeList);
        if(action==ElementChangeEvent.ACTION_ELEMENT_UNDEFINED)
            return null; //ignore wrong type
        String   element_xpath   = helper.getElementXPath(changeList);
        String   element_id      = helper.getConfigElementPrimaryKey(element_xpath); 

        AdminEvent event = null;
        if(event_type.equals(eventType))
        {
            //ElementChangeEvent ?
            event = new ElementChangeEvent(instanceName, event_type, action, element_id);
        }
        else
        {
            //let's try construct  
            try {
                Class cl = Class.forName(event_type);
                Constructor contr;
                try {
                    // unnamed element event ?
                    contr = cl.getConstructor(
                      new Class[]{String.class, Integer.TYPE});
                    event = (AdminEvent)contr.newInstance(
                      new Object[]{instanceName, new Integer(action)});
                } catch (Exception e) { 
                }
                
                if(event==null)
                {
                    //maybe named
                    contr = cl.getConstructor(
                        new Class[]{String.class, Integer.TYPE, String.class});
                    event = (AdminEvent)contr.newInstance(
                        new Object[]{instanceName, new Integer(action), element_id});
                }
                if(event!=null)
                    event.addConfigChange(changeList);
            } catch (Exception e) { 
                //throw e;
                return null; //?
            }
        }
        if(event!=null)
        {
            String targetName = helper.getConfigElementTargetName(element_xpath, ctx);
            event.setTargetDestination(targetName);
            ArrayList events = new ArrayList();
            events.add(event);
            return events;
        }
        return null;
    }
    
    //*****************************************************
/*    public String toString()
    {
        int s = 0;
        if (this.getConfigChangeList() != null ) {
            s = this.getConfigChangeList().size();
        }

        return "\nEvent type:" + eventType  +
                     "\nelement_id="+element_id +
                     "\nchange_action=" + change_action +
                     "\nchangeList.size()="+ s;
        
    }
*/
    //*****************************************************
    // CONSTRUCTORS
    //*****************************************************
    public ElementChangeEvent(String instance, String evtType, int actionCode, String elementId) 
    {
        super(evtType, instance);

        //set element's id(primary key value) [can be null]
        element_id = elementId;
        //validate and set action code
        setAction(actionCode);
    }

    //*****************************************************
    // OVERRIDINGS
    //*****************************************************
    /*
     * overriding the parent's class method
     *
     * Add specified changes to the event.
     * @param changeList the list of changes to add to this event
     */
    public void addConfigChange(ArrayList changeList) 
    {
        // only initial changes add allowed
        if(getConfigChangeList()!=null)
        { 
            String msg = localStrings.getString( "admin.event.wrong_configchange" );
            throw new IllegalArgumentException( msg );
        }
        /* commented for now to avoid double check
         if(ElementChangeHelpe.checkChangeListForElement(changeList);
        {
            String msg = localStrings.getString( "admin.event.wrong_configchange" );
            throw new IllegalArgumentException( msg );
        }*/
        super.addConfigChange(changeList);
    }

    //*************************************************************************
    // PUBLIC METHODS
    //*************************************************************************

    /**
     * get element's xpath
     */
    public String getElementXPath() 
    {
        ElementChangeHelper helper = Globals.getGlobals().getDefaultHabitat().getComponent(ElementChangeHelper.class);
        return helper.getElementXPath(this.getConfigChangeList());
    }

    /**
     * Get element's id(primary key value)
     */
    public String getElementId() 
    {
        return this.element_id;
    }

    /**
     * Get action type for this event.
     */
    public int getActionType() 
    {
        return change_action;
    }

    //*************************************************************************
    // PRIVATE METHODS
    //*************************************************************************
    
    /**
     * Set action to specified value. If action is not one of allowed,
     * then IllegalArgumentException is thrown.
     * @throws IllegalArgumentException if action is invalid
     */
    private void setAction(int action) 
    {
        boolean valid = false;
        if (action==ACTION_ELEMENT_CREATE ||
            action==ACTION_ELEMENT_DELETE ||
            action==ACTION_ELEMENT_UPDATE )
            valid = true;
        if (!valid) {
			String msg = localStrings.getString( "admin.event.invalid_action", ""+action );
            throw new IllegalArgumentException( msg );
        }
        this.change_action = action;
    }

}
