/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2016 Oracle and/or its affiliates. All rights reserved.
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

import java.io.Serializable;
import java.util.Map;

import org.glassfish.hk2.utilities.general.GeneralUtilities;

/**
 * This contains the model for children who have a specific
 * parent, containing information such as the xml tag
 * name and type.  These are all strings or other simple
 * types so it can be hard-coded into the proxy at build
 * time
 * 
 * @author jwells
 *
 */
public class ParentedModel implements Serializable {
    private static final long serialVersionUID = -2480798409414987937L;
    
    private final Object lock = new Object();
    
    /** The interface of the child for which this is a parent */
    private String childInterface;
    private String childXmlTag;
    private ChildType childType;
    private String givenDefault;
    
    /** Set at runtime */
    private ClassLoader myLoader;
    private transient JAUtilities jaUtilities;
    
    /** Calculated lazily */
    private ModelImpl childModel;
    
    public ParentedModel() {
    }
    
    public ParentedModel(String childInterface, String childXmlTag, ChildType childType, String givenDefault) {
        this.childInterface = childInterface;
        this.childXmlTag = childXmlTag;
        this.childType = childType;
        this.givenDefault = givenDefault;
    }
    
    public String getChildInterface() {
        return childInterface;
    }
    
    public String getChildXmlTag() {
        return childXmlTag;
    }
    
    public ChildType getChildType() {
        return childType;
    }
    
    public String getGivenDefault() {
        return givenDefault;
    }
    
    public ModelImpl getChildModel() {
        synchronized (lock) {
            if (myLoader == null) {
                throw new IllegalStateException("Cannot call getChildModel before the classloader has been determined");
            }
            
            if (childModel != null) return childModel;
            
            Class<?> beanClass = GeneralUtilities.loadClass(myLoader, childInterface);
            if (beanClass == null) {
                throw new IllegalStateException("Interface " + childInterface + " could not be loaded by " + myLoader);
            }
            
            try {
                childModel = jaUtilities.getModel(beanClass);
            }
            catch (RuntimeException re) {
                throw new RuntimeException("Could not get model for " + beanClass.getName() + " in " + this, re);
            }
            
            return childModel;
        }
    }
    
    public void setRuntimeInformation(JAUtilities jaUtilities, ClassLoader myLoader) {
        synchronized (lock) {
            this.jaUtilities = jaUtilities;
            this.myLoader = myLoader;
        }
    }
    
    @Override
    public String toString() {
        return "ParentedModel(" + childInterface + "," + childXmlTag + "," + childType + "," + givenDefault + ")";
    }
}
