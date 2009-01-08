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

package com.sun.enterprise.deployment.node.ejb;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.node.LifecycleCallbackNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.EjbTagNames;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * This class handles all information pertinent to session beans
 *
 * @author  Jerome Dochez
 * @version 
 */
public class EjbSessionNode  extends InterfaceBasedEjbNode {
    
    private EjbSessionDescriptor descriptor;

    public EjbSessionNode() {
       super();
       // register sub XMLNodes

       registerElementHandler(new XMLElement(EjbTagNames.AROUND_INVOKE_METHOD), AroundInvokeNode.class, "addAroundInvokeDescriptor");       

       registerElementHandler(new XMLElement(EjbTagNames.POST_CONSTRUCT), LifecycleCallbackNode.class, "addPostConstructDescriptor");       

       registerElementHandler(new XMLElement(EjbTagNames.PRE_DESTROY), LifecycleCallbackNode.class, "addPreDestroyDescriptor");       

       registerElementHandler(new XMLElement(EjbTagNames.POST_ACTIVATE_METHOD), LifecycleCallbackNode.class, "addPostActivateDescriptor");       

       registerElementHandler(new XMLElement(EjbTagNames.PRE_PASSIVATE_METHOD), LifecycleCallbackNode.class, "addPrePassivateDescriptor");       

       registerElementHandler(new XMLElement(EjbTagNames.TIMEOUT_METHOD), MethodNode.class, "setEjbTimeoutMethod");       

       registerElementHandler(new XMLElement(EjbTagNames.INIT_METHOD), EjbInitNode.class, "addInitMethod");

       registerElementHandler(new XMLElement(EjbTagNames.REMOVE_METHOD), EjbRemoveNode.class, "addRemoveMethod");       
   }  
        
   /**
    * @return the descriptor instance to associate with this XMLNode
    */    
    public EjbDescriptor getEjbDescriptor() {
        
        if (descriptor==null) {
            descriptor = (EjbSessionDescriptor) DescriptorFactory.getDescriptor(getXMLPath());
            descriptor.setEjbBundleDescriptor((EjbBundleDescriptor) getParentNode().getDescriptor());
        }
        return descriptor;
    }    
    
    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value. 
     *  
     * @return the map with the element name as a key, the setter method as a value
     */    
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();
        table.put(EjbTagNames.SESSION_TYPE, "setSessionType");
        table.put(EjbTagNames.TRANSACTION_TYPE, "setTransactionType");
        return table;
    }
    
    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name for the root element of this xml fragment      
     * @param the descriptor to write
     * @return the DOM tree top node
     */    
    public Node writeDescriptor(Node parent, String nodeName, Descriptor descriptor) {
        if (! (descriptor instanceof EjbSessionDescriptor)) {
            throw new IllegalArgumentException(getClass() + " cannot handles descriptors of type " + descriptor.getClass());
        }    
        EjbSessionDescriptor ejbDesc = (EjbSessionDescriptor) descriptor;
        
        Node ejbNode = super.writeDescriptor(parent, nodeName, descriptor);
        writeDisplayableComponentInfo(ejbNode, descriptor);
        writeCommonHeaderEjbDescriptor(ejbNode, ejbDesc);
        appendTextChild(ejbNode, EjbTagNames.SESSION_TYPE, ejbDesc.getSessionType());                   

        MethodNode methodNode = new MethodNode();
        
        if( ejbDesc.isTimedObject() ) {
            methodNode.writeJavaMethodDescriptor
                (ejbNode, EjbTagNames.TIMEOUT_METHOD,
                 ejbDesc.getEjbTimeoutMethod());
        }

        if( ejbDesc.hasInitMethods() ) {
            EjbInitNode initNode = new EjbInitNode();
            for(EjbInitInfo next : ejbDesc.getInitMethods()) {
                initNode.writeDescriptor(ejbNode, 
                                         EjbTagNames.INIT_METHOD, next);
            }
        }

        if( ejbDesc.hasRemoveMethods() ) {
            EjbRemoveNode removeNode = new EjbRemoveNode();
            for(EjbRemovalInfo next : ejbDesc.getAllRemovalInfo()) {
                removeNode.writeDescriptor(ejbNode, 
                                           EjbTagNames.REMOVE_METHOD, next);
            }
        }


        appendTextChild(ejbNode, EjbTagNames.TRANSACTION_TYPE, ejbDesc.getTransactionType());                  

        //around-invoke-method
        writeAroundInvokeDescriptors(ejbNode, ejbDesc.getAroundInvokeDescriptors().iterator());

        // env-entry*
        writeEnvEntryDescriptors(ejbNode, ejbDesc.getEnvironmentProperties().iterator());
        
        // ejb-ref * and ejb-local-ref*
        writeEjbReferenceDescriptors(ejbNode, ejbDesc.getEjbReferenceDescriptors().iterator());

        // service-ref*
        writeServiceReferenceDescriptors(ejbNode, ejbDesc.getServiceReferenceDescriptors().iterator());
        
        // resource-ref*
        writeResourceRefDescriptors(ejbNode, ejbDesc.getResourceReferenceDescriptors().iterator());
        
        // resource-env-ref*
        writeResourceEnvRefDescriptors(ejbNode, ejbDesc.getJmsDestinationReferenceDescriptors().iterator());        

        // message-destination-ref*        
        writeMessageDestinationRefDescriptors(ejbNode, ejbDesc.getMessageDestinationReferenceDescriptors().iterator());

        // persistence-context-ref*
        writeEntityManagerReferenceDescriptors(ejbNode, ejbDesc.getEntityManagerReferenceDescriptors().iterator());
        
        // persistence-unit-ref*
        writeEntityManagerFactoryReferenceDescriptors(ejbNode, ejbDesc.getEntityManagerFactoryReferenceDescriptors().iterator());
        
        // post-construct
        writePostConstructDescriptors(ejbNode, ejbDesc.getPostConstructDescriptors().iterator());

        // pre-destroy
        writePreDestroyDescriptors(ejbNode, ejbDesc.getPreDestroyDescriptors().iterator());

        // post-activate-method
        writePostActivateDescriptors(ejbNode, ejbDesc.getPostActivateDescriptors().iterator());

        // pre-passivate-method
        writePrePassivateDescriptors(ejbNode, ejbDesc.getPrePassivateDescriptors().iterator());

        // security-role-ref*
        writeRoleReferenceDescriptors(ejbNode, ejbDesc.getRoleReferences().iterator());
        
        // security-identity
        writeSecurityIdentityDescriptor(ejbNode, ejbDesc);
        
        return ejbNode;
    }
}
