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

package com.sun.enterprise.config.serverbeans.validation;

import java.util.Map;
import java.util.HashMap;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.ElementProperty;

/**
 * helper class with convenience util methods
 * for property changes validation 
*/

/* Class for attribute type Integer */

public abstract class PropertyHelper {
    
    static final String ELEMENT_PROPERTY = "ElementProperty";
    static final String PROP_NAME_ATTRNAME = "name";
    static final String PROP_VALUE_ATTRNAME = "value";

    /***********************************************************    
     *
     * this method can be called from a custom validator
     * during validatePropertyChanges
    ************************************************************/
    static public Map getPropertiesMap(ConfigBean parent) {
        HashMap map = new HashMap();
        if(parent!=null)
        {
            ElementProperty[] props = 
                    (ElementProperty[])parent.getValues(ELEMENT_PROPERTY);
            if(props!=null)
            {
                for(int i=0; i<props.length; i++)
                {
                    map.put(props[i].getName(), props[i].getValue());
                }
            }
        }    
        return map;
    }
   
    /********************************************************
     *
     * returns true is named property will be changed as result of change event
     *
     ********************************************************/
    static public boolean isPropertyChanged(ValidationContext propValCtx, String name) {
        ElementProperty prop = (ElementProperty)propValCtx.getTargetBean();
        if(!prop.getName().equals(name))
        {
            //check if name of property changing to checked one
            if(!propValCtx.isUPDATE() ||
               !PROP_NAME_ATTRNAME.equals(propValCtx.name) ||
               !name.equals(propValCtx.value))
            {
                //not related changes
                return false;
            }
        }
        
        // first get current properties as map
        Map map = getPropertiesMap(propValCtx.getParentBean());
        // save "old" value for tested property
        String oldValue = (String)map.get(name);
        // "apply" change over existing props
        addChangesToPropertiesMap(propValCtx, map);
        // get "new" value for tested property
        String newValue = (String)map.get(name);
        // now - compare
        if((oldValue==null && newValue==null) ||
           (oldValue!=null && oldValue.equals(newValue)) )
        {
            //we are trying to set prop to already existing value
            return false;
        }
        return true;
    }
    
    /********************************************************
     * returns map of element properties if changes would be accepted
     *
     *********************************************************/
    static public Map getFuturePropertiesMap(ValidationContext propValCtx) {
        Map map = getPropertiesMap(propValCtx.getParentBean());
        return addChangesToPropertiesMap(propValCtx, map);
    }

    /********************************************************
     *********************************************************/
    static private Map addChangesToPropertiesMap(ValidationContext propValCtx, Map map) {
        if(propValCtx.isADD() || propValCtx.isSET())
        {
            ElementProperty prop = (ElementProperty)propValCtx.getTargetBean();
            map.put(prop.getName(), prop.getValue());
        }
        else if(propValCtx.isUPDATE())
        {
            ElementProperty prop = (ElementProperty)propValCtx.getTargetBean();
            if(PROP_NAME_ATTRNAME.equals(propValCtx.name))
            {
                // when name of property is changing
                map.remove(prop.getName());
                map.put(propValCtx.value, prop.getValue());
            }
            else
            {
                // when value of property is changing
                map.put(prop.getName(), propValCtx.value);
            }
        }
        return map;
    }
    
}
