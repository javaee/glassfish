/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Node.java
 *
 * Created on March 31, 2005, 6:14 PM
 */


package com.sun.persistence.api.deployment;

import com.sun.xml.bind.ObjectLifeCycle;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * This is the super class for all JAXB generated value classes. It provides
 * following functionalities... a) visitor pattern support {@link #accept}, b)
 * reference to parent node. {@link #parent} JAXB unmarshaller does not call set
 * methdds while adding node. So this class implements ObjectLifeCycle interface
 * to receive notifications from JAXB marshaller/unmarshaller, so that it can
 * set up parent reference during unmarshalling process.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class DescriptorNode implements ObjectLifeCycle {

    /* parent of this node */
    private DescriptorNode parent;

    /**
     * @return parent of this node. null if this is the root node
     */
    public DescriptorNode parent() {
        return parent;
    }

    /**
     * @param node parent of this node.
     */
    // protected because it must be used in derived classes only
    protected void parent(DescriptorNode node) {
        parent = node;
    }

    /**
     * This method is as required by Visitor pattern.
     *
     * @param v the visitor whose appropriate visit method will be called.
     * @throws DeploymentException that is raised from the visit method.
     */
    public abstract void accept(Visitor v) throws DeploymentException;

    // ObjectLifeCycle interface implemention

    public void beforeMarshalling(Marshaller marshaller) {
    }

    public void afterMarshalling(Marshaller marshaller) {
    }

    public void beforeUnmarshalling(Unmarshaller unmarshaller, Object obj) {
    }

    public void afterUnmarshalling(Unmarshaller unmarshaller, Object obj) {
        parent(DescriptorNode.class.cast(obj));
    }

}