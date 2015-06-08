/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.hk2.xml.api;

import java.net.URI;

import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 *
 */
@Contract
public interface XmlService {
    /**
     * Unmarshalls the given URI using the jaxb annotated interface.  The resulting
     * JavaBean tree will be advertised in the ServiceLocator and in the Hub
     * 
     * @param uri The non-null URI whereby to find the xml corresponding to the class
     * @param jaxbAnnotatedClassOrInterface The non-null class corresonding to the Xml to be parsed
     * @return A non-null handle that can be used to get the unmarshalled data or perform
     * other tasks
     */
    public <T> XmlRootHandle<T> unmarshall(URI uri, Class<T> jaxbAnnotatedInterface);
    
    /**
     * Unmarshalls the given URI using the jaxb annotated interface
     * 
     * @param uri The non-null URI whereby to find the xml corresponding to the class
     * @param jaxbAnnotatedClassOrInterface The non-null interface corresponding to the Xml to be parsed
     * @param advertiseInRegistry if true the entire tree of parsed xml will be added to the
     * ServiceLocator
     * @param advertiseInHub if true the entire tree of parsed xml will be added to the
     * HK2 configuration Hub (as bean-like maps)
     * @return A non-null handle that can be used to get the unmarshalled data or perform
     * other tasks
     */
    public <T> XmlRootHandle<T> unmarshall(URI uri, Class<T> jaxbAnnotatedInterface,
            boolean advertiseInRegistry, boolean advertiseInHub);
    
    /**
     * This creates an empty handle (root will initially be null) corresponding to
     * the given interface class
     * 
     * @param jaxbAnnotationInterface The non-null interface class corresponding to
     * the XML to be parsed
     * @param advertiseInRegistry if true the entire tree of parsed xml will be added to the
     * ServiceLocator
     * @param advertiseInHub if true the entire tree of parsed xml will be added to the
     * HK2 configuration Hub (as bean-like maps)
     * @return A non-null handle that can be used to create a new root bean, but which
     * is not initially tied to any backing file or other input stream
     */
    public <T> XmlRootHandle<T> createEmptyHandle(Class<T> jaxbAnnotationInterface,
            boolean advertiseInRegistry, boolean advertiseInHub);
    
    /**
     * This creates an empty handle (root will initially be null) corresponding to
     * the given interface class
     * 
     * @param jaxbAnnotationInterface The non-null interface class corresponding to
     * the XML to be parsed
     * @return A non-null handle that can be used to create a new root bean, but which
     * is not initially tied to any backing file or other input stream
     */
    public <T> XmlRootHandle<T> createEmptyHandle(Class<T> jaxbAnnotationInterface);
    
    /**
     * This creates an instance of the given bean type
     * of with no fields of the bean filled
     * in.  Objects created with this API can be
     * used in the adder methods of the beans, and
     * will not be validated (but all setters and
     * getters and lookups will work properly)
     * 
     * @return An instance of the bean with
     * no properties set
     */
    public <T> T createBean(Class<T> beanInterface);

}
