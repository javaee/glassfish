/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.internal;

import java.net.URI;

import org.glassfish.hk2.xml.api.XmlRootCopy;
import org.glassfish.hk2.xml.api.XmlRootHandle;

/**
 * @author jwells
 *
 */
public class XmlRootHandleImpl<T> implements XmlRootHandle<T> {
    private final T root;
    private final Class<T> rootClass;
    private URI rootURI;
    private final boolean advertised;
    private final boolean advertisedInHub;
    
    /* package */ XmlRootHandleImpl(T root, Class<T> rootClass, URI rootURI, boolean advertised, boolean hub) {
        this.root = root;
        this.rootClass = rootClass;
        this.rootURI = rootURI;
        this.advertised = advertised;
        this.advertisedInHub = hub;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#getRoot()
     */
    @Override
    public T getRoot() {
        return root;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#getRootClass()
     */
    @Override
    public Class<T> getRootClass() {
        return rootClass;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#getURI()
     */
    @Override
    public URI getURI() {
        return rootURI;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#isAdvertisedInLocator()
     */
    @Override
    public boolean isAdvertisedInLocator() {
        return advertised;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#isAdvertisedInHub()
     */
    @Override
    public boolean isAdvertisedInHub() {
        return advertisedInHub;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#overlay(org.glassfish.hk2.xml.api.XmlRootHandle)
     */
    @Override
    public void overlay(XmlRootHandle<T> newRoot) {
        throw new AssertionError("overlay not yet implemented");
        
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#getXmlRootCopy()
     */
    @Override
    public XmlRootCopy<T> getXmlRootCopy() {
        throw new AssertionError("getXmlRootCopy not yet implemented");
    }
    
    @Override
    public String toString() {
        return "XmlRootHandleImpl(" + root + "," + rootClass.getName() + "," + rootURI + "," + System.identityHashCode(this) + ")";
    }

    

    

}
