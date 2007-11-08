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
package com.sun.mfwk.agent.appserv.modeler;

import java.util.Set;
import java.util.Map;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Constructor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.sun.mfwk.CMM_MBean;
import com.sun.mfwk.MfDelegate;
import com.sun.mfwk.MfDelegateFactory;
import com.sun.mfwk.MfObjectFactory;
import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import com.sun.mfwk.agent.appserv.delegate.DefaultDelegate;
import com.sun.mfwk.agent.appserv.delegate.AttributeHandler;
import com.sun.mfwk.agent.appserv.connection.ConnectionRegistry;
import com.sun.mfwk.agent.appserv.connection.TrustAnyConnectionFactory;
import com.sun.mfwk.agent.appserv.mapping.MappingQueryService;
import com.sun.mfwk.agent.appserv.util.Constants;
import com.sun.mfwk.agent.appserv.util.Utils;
import com.sun.mfwk.agent.appserv.logging.LogDomains;

import java.io.IOException;

/**
 * Factory class to create mbean from declarative xml file.
 */
class MBeanFactory {

    static Map map;

    /**
     * Creates a new instance of MBeanFactory
     * 
     * @param  ctx  context for process
     */
    MBeanFactory(ModelerContext ctx) {
        _context = ctx;
    }

    /**
     * Models a CMM mbean based on the given descriptor element and 
     * application server proxy object name. A CMM mbean may map to 
     * multiple application server mbean. In this method, the modeling
     * is done from application server mbean perspective. The delegate 
     * instrumented in this method may not have complete attribute mappings. 
     *
     * @param  asON  application server proxy object name
     * @param  CMM_MbeanDescriptor  xml element describing attribute mappings
     * @param  mapping  mapping service that returns attribute mapping info
     *
     * @return instrumented cmm mbean
     *
     * @throws Exception if a problem with processing
     */
    CMM_MBean model(ObjectName asON, Element CMM_MbeanDescriptor, 
            MappingQueryService mapping) throws Exception {

        if ((asON==null) || (CMM_MbeanDescriptor==null) || (mapping==null)) {
            throw new IllegalArgumentException();
        }

        CMM_MBean sourceMbean = null;

        MfObjectFactory objectFactory =
            MfObjectFactory.getObjectFactory(_context.getModuleName());

        // delegate factory
        if (map == null) {
            map = new Hashtable();
        }
        String mbeanName = CMM_MbeanDescriptor.getAttribute("name").trim();
        map.put(mbeanName, DefaultDelegate.class);
        MfDelegateFactory delegateFactory=objectFactory.getDelegateFactory(map);

        // mbean server connection
        MBeanServerConnection mbs = getConnection();

        // cmm object name
        String objectName = mapping.getCMM_ObjectName(CMM_MbeanDescriptor);
        String tokenizedON = 
            ObjectNameHelper.tokenizeON(asON, objectName, _context.getTokens());

        DefaultDelegate delegate = null;
        if (!objectFactory.isObjectCreated(tokenizedON)) {
            sourceMbean = objectFactory.createObject(tokenizedON);

            // create delegate
            delegate = (DefaultDelegate) 
                delegateFactory.createDelegate(mbs, sourceMbean, asON);

            _delegateReg.put(tokenizedON, delegate);

        } else {
            sourceMbean = objectFactory.getObject(tokenizedON);
            //delegate = (MfDelegate) delegateFactory.getDelegate(tokenizedON);
            delegate = (DefaultDelegate) _delegateReg.get(tokenizedON);
        }

        if ( (sourceMbean == null) || (delegate == null) ) {
            String msg = "MBean or the delegate must not be null";
            throw new IllegalStateException(msg);
        }

        // add attribute mapping in delegate
        Map attributes = mapping.getAttributeMappings(CMM_MbeanDescriptor);
        Set asAttributes = attributes.keySet();
        Iterator iter = asAttributes.iterator();
        while (iter.hasNext()) {
            String asAttr = (String) iter.next();
            String mfAttr = (String) attributes.get(asAttr);

            LogDomains.getLogger().finest("AS Attribute: " + asAttr); 
            LogDomains.getLogger().finest("MF Attribute: " + mfAttr); 
            LogDomains.getLogger().finest("AS Object Name: " + asON); 

            if ( (asAttr != null) && (mfAttr != null) ) {
                // add mapping
                try {
                    if (mapping.isAttributeHandlerDefined(CMM_MbeanDescriptor, 
                        asAttr, mfAttr)) {

                        // construct the attribute handler
                        AttributeHandler handler = buildAttrHandler(
                                CMM_MbeanDescriptor, mapping, asAttr, mfAttr);

                        // add mapping with handler
                        delegate.addMappingEntry(mfAttr, asAttr, asON, handler);

                    } else {

                        // add mapping without handler
                        delegate.addMappingEntry(mfAttr, asAttr, asON);
                    }
                } catch (Exception e) {
                    LogDomains.getLogger().log(Level.WARNING, 
                        "Error while adding mapping entry", e);
                }
            }
        }

        // set def capabilities mappings when dealing with a capabilities mbean
        if (isCapabilitiesType(tokenizedON)) {
            delegate.initCapabilitiesMappings();
        }

        // add default attribute mappings
        NodeList defAttr = 
            mapping.getDefaultAttributeMappings(CMM_MbeanDescriptor);

        if (defAttr != null) {
            int defSize = defAttr.getLength();

            for (int i=0; i<defSize; i++) {
                try {
                    Element e = (Element) defAttr.item(i);

                    String cmmAttr = e.getAttribute(CMM_NAME_TAG).trim();
                    String mappingAttr = e.getAttribute(MAPPING_TAG).trim();
                    String classAttr = e.getAttribute(MAPPING_CLASS_TAG);

                    LogDomains.getLogger().finest("CMM Attribute: " + cmmAttr); 
                    LogDomains.getLogger().finest("Mapping: " + mappingAttr); 
                    LogDomains.getLogger().finest("Mapping Class: "+classAttr); 

                    if ( (cmmAttr != null) && (mappingAttr != null) ) {

                        // add default mapping
                        Object type = convertType(mappingAttr, classAttr);

                        delegate.addDefaultMappingEntry(cmmAttr, type);

                    }
                } catch (Exception e) {
                    LogDomains.getLogger().log(Level.WARNING, 
                        "Error while adding default mapping entry", e);
                }
            }
        }

        // sets the name of the server instance
        String serverName = _context.getServerName();
        delegate.setServerName(serverName);

        // sets the name of the domain
        String domainName = _context.getDomainName();
        delegate.setDomainName(domainName);

        // return the cmm mbean
        return sourceMbean;
    }

    /**
     * Returns true if mbean type is CMM_Capabilities.
     *
     * @param  tokenizedON  tokenized cmm mbean object name
     *
     * @return  true when mbean type is CMM_Capabilities
     */
    private boolean isCapabilitiesType(String tokenizedON) {

        boolean tf = false;

        try {
            ObjectName on = new ObjectName(tokenizedON);
            String type = on.getKeyProperty(TYPE_KEY);

            // mbean type is CMM_Capabilities
            if ((type != null) && (CAPABILITIES_TYPE.equals(type))) {
                tf = true;
            }
        } catch (Exception e) {
            tf = false;
        }

        return tf;
    }

    /**
     * Converts the mapping string to default java types specified 
     * in the class argument.
     * 
     * @param  mappingAttr  mapping string
     * @param  classAttr  java class type
     *
     * @return  converted java types
     */
    private Object convertType(String mappingAttr, String classAttr) {

        if ( (classAttr ==null) || ("".equals(classAttr)) ) {
            return mappingAttr;
        }

        if ("java.lang.Double".equals(classAttr)) {
            return new Double(mappingAttr);
        } else if ("java.lang.Float".equals(classAttr)) {
            return new Float(mappingAttr);
        } else if ("java.lang.Integer".equals(classAttr)) {
            return new Integer(mappingAttr);
        } else if ("java.lang.Long".equals(classAttr)) {
            return new Long(mappingAttr);
        } else if ("java.lang.Short".equals(classAttr)) {
            return new Short(mappingAttr);
        } else if ("java.lang.Boolean".equals(classAttr)) {
            return new Boolean(mappingAttr);
        } else {
            return mappingAttr;
        }
    }

    /**
     * Starts the mbean processing. This method is called when 
     * a static xml template is used to create the cmm mbean.
     * This method instruments a complete delegate since the 
     * given mbean is expected to have all the mappings from 
     * CMM to AS.
     *
     * @param  mbean  xml element representing a cmm mbean 
     *                with all of its attribute mappings
     *
     * @return instrumented cmm mbean
     *
     * @throws Exception if a problem with processing
     */
    CMM_MBean create(Element mbean) throws Exception {

        if (mbean == null) {
            throw new IllegalArgumentException();
        }

        MfObjectFactory objectFactory =
            MfObjectFactory.getObjectFactory(_context.getModuleName());

        Hashtable map = new Hashtable();
        String mbeanName = mbean.getAttribute("name").trim();

        try {
            String delegateClassName = mbean.getAttribute("type").trim();
            Class c = (Class) Class.forName(delegateClassName).newInstance();
            map.put(mbeanName, c);

        } catch (ClassNotFoundException cnfe) {

            LogDomains.getLogger().fine("Error: " + cnfe.getMessage() 
                + ". Using default delegate class.");

            // use default delegate class
            map.put(mbeanName, DefaultDelegate.class);
        }

        // delegate factory
        MfDelegateFactory delegateFactory=objectFactory.getDelegateFactory(map);

        // jxm connection to the application server
        MBeanServerConnection mbs = getConnection();

        CMM_MBean sourceMbean = null;
        String objectName = ConfigReader.getMBeanObjectName(mbean, false);
        String tokenizedON = 
            ObjectNameHelper.tokenize(objectName, _context.getTokens());

        // if cmm mbean is not created
        if (!objectFactory.isObjectCreated(tokenizedON)) {
            sourceMbean = objectFactory.createObject(tokenizedON);

            // sets up the delegate
            String proxyObjectName=ConfigReader.getMBeanObjectName(mbean, true);
            String tokenizedPON = 
                ObjectNameHelper.tokenize(proxyObjectName,_context.getTokens());
            ObjectName pon = ObjectNameHelper.getObjectName(tokenizedPON, mbs);

            MfDelegate delegate = 
                delegateFactory.createDelegate(mbs, sourceMbean, pon);

            // instrument the delegate object from descriptor
            DelegateFactory df = 
                new DelegateFactory(mbean, delegate, _context, mbs);
            Object d = df.create();

            LogDomains.getLogger().fine("Created delegate: " + d);
        }

        return sourceMbean;
    }

    /**
     * Get mbean server connection.
     *
     * @return  mbean server connection
     * @throws  IOException  if an i/o error
     */
    private MBeanServerConnection getConnection() throws IOException {

        ConnectionRegistry reg = ConnectionRegistry.getInstance();
        String serverName = _context.getServerName();
        String domainName = _context.getDomainName();
        MBeanServerConnection mbs = reg.getConnection(serverName, domainName);

        return mbs;

        // trust any implementation
        //Map map = _context.getConnectorEnv();
        //return TrustAnyConnectionFactory.getConnection(map);
    }

    /**
     * Constructs attribute handler.
     */
    public static AttributeHandler buildAttrHandler(Element CMM_MbeanDescriptor,
            MappingQueryService mapping, String asAttr, String cmmAttr) 
            throws Exception {
        
        AttributeHandler handler = null;

        try {
            Map handlerProperties = mapping.getHandlerAttributeMappings(
                                        CMM_MbeanDescriptor, asAttr, cmmAttr);

            String handlerName = mapping.getAttributeHandlerClass(
                                        CMM_MbeanDescriptor, asAttr, cmmAttr);

            Class c = Class.forName(handlerName);
            handler = (AttributeHandler) c.newInstance();
            handler.setCMM_MBeanDescriptor(CMM_MbeanDescriptor);
            handler.setMappingQueryService(mapping);
            handler.setHandlerProperties(handlerProperties);

        } catch (Exception e) {

            LogDomains.getLogger().log(Level.WARNING, 
                    "Error while creating attribute handler", e);
            throw e;
        }

        return handler;
    }

    // ---- VARIABLES - PRIVATE ----------------------
    private static final String CMM_NAME_TAG       = "cmm-name";
    private static final String MAPPING_TAG        = "mapping";
    private static final String MAPPING_CLASS_TAG  = "mapping-class";
    private static final String TYPE_KEY           = "type";
    private static final String CAPABILITIES_TYPE  = "CMM_Capabilities";

    private ModelerContext _context        = null;
    private static Hashtable _delegateReg =
                                new Hashtable();
}
