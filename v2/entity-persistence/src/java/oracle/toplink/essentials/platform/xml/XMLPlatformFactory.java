/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.platform.xml;

import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedGetClassLoaderForClass;
import oracle.toplink.essentials.internal.security.PrivilegedNewInstanceFromClass;

public class XMLPlatformFactory {
    public static final String XML_PLATFORM_PROPERTY = "toplink.xml.platform";
    public static final String XDK_PLATFORM_CLASS_NAME = "oracle.toplink.essentials.platform.xml.xdk.XDKPlatform";
    public static final String JAXP_PLATFORM_CLASS_NAME = "oracle.toplink.essentials.platform.xml.jaxp.JAXPPlatform";
    private static XMLPlatformFactory instance;
    private Class xmlPlatformClass;

    private XMLPlatformFactory() {
        super();
    }

    /**
     * INTERNAL:
     * Return the singleton instance of XMLPlatformContext.
     * @return the the singleton instance of XMLPlatformContext.
     * @throws XMLPlatformException
     */
    public static XMLPlatformFactory getInstance() throws XMLPlatformException {
        if (null == instance) {
            instance = new XMLPlatformFactory();
        }
        return instance;
    }

    /**
     * INTERNAL:
     * Return the implementation class for the XMLPlatform.
     * @return the implementation class for the XMLPlatform.
     * @throws XMLPlatformException
     */
    public Class getXMLPlatformClass() throws XMLPlatformException {
        if (null != xmlPlatformClass) {
            return xmlPlatformClass;
        }

        String newXMLPlatformClassName = System.getProperty(XML_PLATFORM_PROPERTY);
        if (null == newXMLPlatformClassName) {
            newXMLPlatformClassName = JAXP_PLATFORM_CLASS_NAME;
        }

        try {
            ClassLoader classLoader = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try{
                    classLoader = (ClassLoader)AccessController.doPrivileged(new PrivilegedGetClassLoaderForClass(this.getClass()));
                } catch (PrivilegedActionException exc){
                    // will not be thrown
                }
            } else {
                classLoader = PrivilegedAccessHelper.getClassLoaderForClass(this.getClass());
            }
            // ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            // ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class newXMLPlatformClass = classLoader.loadClass(newXMLPlatformClassName);
            setXMLPlatformClass(newXMLPlatformClass);
            return xmlPlatformClass;
        } catch (ClassNotFoundException e) {
            throw XMLPlatformException.xmlPlatformClassNotFound(newXMLPlatformClassName, e);
        }
    }

    /**
     * PUBLIC:
     * Set the implementation of XMLPlatform.
     */
    public void setXMLPlatformClass(Class xmlPlatformClass) {
        this.xmlPlatformClass = xmlPlatformClass;
    }

    /**
     * INTERNAL:
     * Return the XMLPlatform based on the toplink.xml.platform System property.
     * @return an instance of XMLPlatform
     * @throws XMLPlatformException
     */
    public XMLPlatform getXMLPlatform() throws XMLPlatformException {
        try {
             if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return (XMLPlatform)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(getXMLPlatformClass()));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof InstantiationException) {
                        throw XMLPlatformException.xmlPlatformCouldNotInstantiate(getXMLPlatformClass().getName(), throwableException);
                    } else {
                        throw XMLPlatformException.xmlPlatformCouldNotInstantiate(getXMLPlatformClass().getName(), throwableException);
                    }
                }
            } else {
                return (XMLPlatform)PrivilegedAccessHelper.newInstanceFromClass(getXMLPlatformClass());
            }
        } catch (IllegalAccessException e) {
            throw XMLPlatformException.xmlPlatformCouldNotInstantiate(getXMLPlatformClass().getName(), e);
        } catch (InstantiationException e) {
            throw XMLPlatformException.xmlPlatformCouldNotInstantiate(getXMLPlatformClass().getName(), e);
        }
    }
}
