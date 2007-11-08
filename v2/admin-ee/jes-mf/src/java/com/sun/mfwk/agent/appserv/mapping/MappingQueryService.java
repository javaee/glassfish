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
package com.sun.mfwk.agent.appserv.mapping;

import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Provides mappings between Application Server and CMM mbeans. 
 */
public interface MappingQueryService {

    /**
     * Returns the environment property for the given key.
     *
     * @param  key  property key
     * @return  environment property for the given key
     */
    public Object getProperty(String key);

    /**
     * Returns a list of all AS Mbean elements defined found by this 
     * query service
     *
     * @return available AS mbean descriptors (xml elements)
     */
    NodeList getAS_Mbeans();
    
    /**
     * Returns a list of all AS Mbean elements that are children of 
     * AS_ObjectName
     *
     * @param  AS_ObjectName : App Server ObjectName
     *
     * @return AS mbean descriptor corresponding to AS_ObjectName (xml elements)
     */
    Element getAS_Mbean(String AS_ObjectName) 
            throws AS_ObjectNameNotFoundException;
    
    /**
     * Returns a list of all CMM MBean elements that map to AS_ObjectName
     *
     * @param  AS_ObjectName the AS Objectname whose CMM MBeans are returned
     *
     * @return available CMM mbean descriptors (xml elements)
     */
    NodeList getCMM_Mbeans(String AS_ObjectName) 
            throws AS_ObjectNameNotFoundException;
    
    /**
     * Returns a list of all CMM MBean elements of a given type that 
     * map to AS_ObjectName.
     *
     * @param  AS_ObjectName the AS Objectname whose CMM MBeans are returned
     *
     * @param  CMM_Type the typwe of cmm object. null means all types
     *
     * @return available CMM mbean descriptors (xml elements)
     */
    NodeList getCMM_Mbeans(String AS_ObjectName, String CMM_Type) 
            throws AS_ObjectNameNotFoundException;
    
    /**
     * Returns the CMM object name from the given mbean descriptor.
     * 
     * @param  CMM_MbeanDescriptor  xml element describing a cmm mbean
     */
    String getCMM_ObjectName(Element CMM_MbeanDescriptor);
    
    /**
     * Returns a map of all AS -> CMM attribute names. 
     * Key = AS Attribute Name, value = CMM attribute name
     *
     * @param  AS_ObjectName  AppServer ObjectName
     * @param  CMM_ObjectName  CMM ObjectName
     */
    Map getAttributeMappings(String AS_ObjectName, String CMM_ObjectName) 
        throws AS_ObjectNameNotFoundException, CMM_ObjectNameNotFoundException;
    
    /**
     * Returns a map of all AS -> CMM attribute names. 
     * Key = AS Attribute Name, value = CMM attribute name
     *
     * @param  CMM_MbeanDescriptor CMM MBean descriptor
     */
    Map getAttributeMappings(Element CMM_MbeanDescriptor);

    /**
     * Returns all default attribute names. 
     *
     * @param  CMM_MbeanDescriptor CMM MBean descriptor
     */
    NodeList getDefaultAttributeMappings(Element CMM_MbeanDescriptor);

    /**
     * Returns true if a handler is defined for this attribute.
     *
     * @param  CMM_MbeanDescriptor  CMM MBean descriptor
     * @param  asAttr  application server attribute
     * @param  cmmAttr  cmm attribute
     *
     * @return  true when a handler is defined
     */
    boolean isAttributeHandlerDefined(Element CMM_MbeanDescriptor, 
            String asAttr, String cmmAttr);

    /**
     * Returns a map of all handler properties.
     *
     * @param  CMM_MbeanDescriptor  CMM MBean descriptor
     * @param  asAttr  application server attribute
     * @param  cmmAttr  cmm attribute
     *
     * @return  a map containing all the handler properties
     */
    Map getHandlerAttributeMappings(Element CMM_MbeanDescriptor, String asAttr, 
            String cmmAttr);

    /**
     * Returns the handler class name.
     *
     * @param  CMM_MbeanDescriptor  CMM MBean descriptor
     * @param  asAttr  application server attribute
     * @param  cmmAttr  cmm attribute
     *
     * @return  handler class name
     */
    String getAttributeHandlerClass(Element CMM_MbeanDescriptor, String asAttr, 
            String cmmAttr);
}
