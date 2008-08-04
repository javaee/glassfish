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


package com.sun.enterprise.config.serverbeans;

import java.beans.PropertyVetoException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Injectable;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.DomDocument;

/**
 * Represents the {@literal <application-config>} child element of {@literal <application-ref>}.
 * <p>
 * The <code>type</code> attribute identifies the container type for which
 * the application configuration customizations apply.  The text value of the
 * <application-config> element holds CDATA-wrapped text which records the
 * application configuration customization in whatever format the container
 * engineer chooses.
 * <p>
 * As a temporary workaround, the customized configuration should be stored
 * as the <code>config</code> attribute, the value of which is encoded using
 * URLEncoder automatically by the ApplicationConfig implementation.  
 * This workaround should be removed and the config stored as
 * the text value once an AMX bug is fixed regarding getting the text value
 * of an element and once we find out how to be able to define the
 * ApplicationRef interface so we can get both the text
 * value of the ApplicationConfig children and also the List of ApplicationConfig
 * children elements themselves. 
 * 
 * @author tjquinn
 */
@Configured
@org.glassfish.api.amx.AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.ApplicationConfigConfig", nameHint="type" )
public interface ApplicationConfig extends ConfigBeanProxy, Injectable {

 
    
    /**
     * Reports the type value which holds the container type to which this
     * particular configuration customization applies.
     * 
     * @return the type for these customizations
     */
    @Attribute(required=true,key=true)
    public String getType();
    
    /**
     * Sets the type attribute value to the specified container type.
     * 
     * @param the container type for which these customizations apply
     * @throws java.beans.PropertyVetoException
     */
    public void setType(String value) throws PropertyVetoException;
    
    // XXX The following are a temporary workaround used to store the config as an attr instead of a CDATA-wrapped text value
    
    /**
     * Reports the configuration information already stored.
     * <p>
     * If, as suggested, the value was encoded before it was stored using
     * @{link setConfig} then the returned value should be decoded using
     * @{link java.net.URLDecoder} before use by the calling logic.
     * 
     * @return the config
     */
    @Attribute(required=true)
    public String getConfig();
    
    /**
     * Stores the config information.
     * <p>
     * The value stored should have already been encoded using
     * @{link java.net.URLEncoder} if it contains characters that might
     * interfere with the well-formedness of the containing domain.xml
     * XML document.
     * 
     * @param value the configuration information to be stored
     * @throws java.beans.PropertyVetoException
     */
     public void setConfig(String value) throws PropertyVetoException;
    
    /**
     * Returns the application configuration information as an object graph
     * of @Configured classes and/or interfaces.
     * <p>
     * If the class or interface XConfig implements or extends Configured then
     * you the following line returns the desired type:
     * <code>XConfig xc = appConfig.getConfigData();</code>
     * <p>
     * Make sure that you declare the variable of the type corresponding to the
     * top-level element in the application configuration.
     * 
     * @param habitat a valid Habitat that knows about the type that is
     * receiving the return value
     * 
     * @return object of type T as the root of an object graph corresponding to
     * the XML encoded in the config attribute value.
     * @throws ClassCastException if the result type to which you assign or cast
     * the return value does not match the type derived from the top-level
     * element of the application configuration information.
     */
    @DuckTyped
    public <T> T getConfigData(Habitat habitat);
    
    /**
     * Returns the configuration data in decoded form.
     * 
     * @return
     */
    @DuckTyped
    public String getFormattedConfig();
    
    /**
     * Encodes the value before storing it as the configuration data.
     * 
     * @param value
     */
    @DuckTyped
    public void setFormattedConfig(String value) throws PropertyVetoException;
    
    public class Duck {
       /** for encoding and decoding the config attribute contents */
        private static final String ENCODING = "UTF-8";

        private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        public static String getFormattedConfig(ApplicationConfig me) {
            try {
                return URLDecoder.decode(me.getConfig(), ENCODING);
            } catch (UnsupportedEncodingException e) {
                return me.getConfig(); // should not happen
            }
        }
        
        public static void setFormattedConfig(ApplicationConfig me, String c) throws PropertyVetoException {
            try {
                me.setConfig(URLEncoder.encode(c, ENCODING));
            } catch (UnsupportedEncodingException e) {
                me.setConfig(c); // should not happen
            }
        }
        /**
         * Returns the application configuration information as an object graph
         * of @Configured classes and/or interfaces.
         * <p>
         * If the class or interface XConfig implements or extends Configured then
         * you the following line returns the desired type:
         * <code>XConfig xc = appConfig.getConfigData();</code>
         * <p>
         * Make sure that you declare the variable of the type corresponding to the
         * top-level element in the application configuration.
         * @param habitat valid Habitat that knows about the config-api types to be parsed
         * @return object of type T as the root of an object graph corresponding to
         * the XML encoded in the config attribute value.
         * @throws ClassCastException if the result type to which you assign or cast
         * the return value does not match the type derived from the top-level
         * element of the application configuration information.
         */
        public static <T extends ConfigBeanProxy> T getConfigData(ApplicationConfig me, Habitat habitat) {
            try {
                String xmlData = URLDecoder.decode(me.getConfig(), ENCODING);
                ConfigParser parser = new ConfigParser(habitat);
                XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new StringReader(xmlData));
                DomDocument dom = parser.parse(reader);
                Object topLevelElement = dom.getRoot().get();
                return (T) topLevelElement;
            } catch (UnsupportedEncodingException uce) {
                throw new RuntimeException(uce); // should never happen
            } catch (XMLStreamException xse) {
                throw new RuntimeException(xse); // should never happen
            }
        }
   }
}
