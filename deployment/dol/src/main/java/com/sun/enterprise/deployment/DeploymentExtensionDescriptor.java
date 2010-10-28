/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Vector;

/**
 * This class contains all the information for the deployment extension 
 * elements found in deployment descriptors since J2EE 1.4
 *
 * @author Jerome Dochez
 */
public class DeploymentExtensionDescriptor extends Observable implements Serializable {
    
    String nameSpace=null;
    boolean mustUnderstand=false;
    Vector elements;
    
    /** Creates a new instance of DeploymentExtensionDescriptor */
    public DeploymentExtensionDescriptor() {
        elements = new Vector();
    }
    
    /**
     * Sets the namespace for this deployment entension
     * @param the namespace
     */
    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
        changed();
    }
    
    /**
     * @return the namespace for this deployment extension
     */
    public String getNameSpace() {
        return nameSpace;
    }
    
    /**
     * Sets the mustUnderstand flag
     */
    public void setMustUnderstand(boolean mustUnderstand) {
        this.mustUnderstand = mustUnderstand;
        changed();
    }
    
    /**
     * @return true if this deployment extension must be understood
     */
    public boolean getMustUnderstand() {
        return mustUnderstand;
    }
    
    /**
     * Add a deployment extension element to his deployment extension
     * 
     * @param the new deployment extension
     */
    public void addElement(ExtensionElementDescriptor newElement) {
        elements.add(newElement);
        changed();
    }
    
    /**
     * @return an iterator on all the deployment extension elements
     */
    public Iterator elements() {
        return elements.iterator();
    }
    
    /**
     * @return a meaningful string about myself
     */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("namespace ").append(nameSpace);
        toStringBuffer.append("\nmustUnderstand = ").append(mustUnderstand);
        for(Iterator itr = elements();itr.hasNext();) {
            toStringBuffer.append("\nelement = ").append(itr.next());
        }
    }
    
    /**
     * notify our observers we have changed
     */
    private void changed() {
        setChanged();
        notifyObservers();
    }    
    
}
