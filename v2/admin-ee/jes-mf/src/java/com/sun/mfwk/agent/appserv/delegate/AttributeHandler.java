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
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.delegate;

import java.util.Map;
import org.w3c.dom.Element;
import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import com.sun.mfwk.agent.appserv.mapping.MappingQueryService;

import java.io.IOException;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;

/**
 * Provides hook for specialized java coding to map application server
 * attribute to CMM attribute.
 *
 * All attribute handlers must implement this interface.
 */
public interface AttributeHandler {
    
   /**
    * This method is called by AbstractDelegate class. Implementation of 
    * this method may customize the AS value before being returned 
    * from the CMM mbean. 
    *
    * @param  peer  application server proxy object name or null if not defined
    * @param  attribute application server attribute or null if not defined
    * @param  mbs  mbean server connection to the application server
    *
    * @throws javax.management.AttributeNotFoundException if the attribute 
    *         is not found
    * @throws javax.management.MBeanException if a problem occurred
    * @throws javax.management.ReflectionException if a problem occurred
    *
    * @return  attribute value to be returned
    */
    Object handleAttribute(ObjectName peer, String attribute, 
            MBeanServerConnection mbs) throws HandlerException, AttributeNotFoundException, 
             MBeanException, ReflectionException, InstanceNotFoundException,
             IOException;
    
   /**
    * Returns XML element describing attrbute mappings for CMM mbean.
    *
    * @return  xml element describing attribute mappings
    */
    Element getCMM_MBeanDescriptor();

   /**
    * Sets the CMM mbean descriptor. 
    *
    * This method should not be called outside modeler pkg.
    * 
    * @param  descriptor  CMM mbean descriptor
    */
    void setCMM_MBeanDescriptor(Element descriptor);

   /**
    * Returns mapping service used by this handler. 
    *
    * @return   mapping service that returns attribute mapping info
    */
    MappingQueryService getMappingQueryService();

   /**
    * Sets the mapping query service.
    *
    * @param  mapping mapping query service
    */
    void setMappingQueryService(MappingQueryService mapping);

   /**
    * Returns handler properties.
    *
    * @return   properties defined for this handler
    */
    Map getHandlerProperties();

   /**
    * Sets the handler properties.
    *
    * @param  properties  handler properties
    */
    void setHandlerProperties(Map properties);
}
