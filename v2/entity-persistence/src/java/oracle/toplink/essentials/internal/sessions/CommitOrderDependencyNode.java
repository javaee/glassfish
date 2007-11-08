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
package oracle.toplink.essentials.internal.sessions;

import java.util.*;
import oracle.toplink.essentials.descriptors.InheritancePolicy;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.localization.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * This wraps a descriptor with information required to compute an order for
 * dependencies. The algorithm is a simple topological sort.
 */
public class CommitOrderDependencyNode {
    protected CommitOrderCalculator owner;
    protected ClassDescriptor descriptor;
    protected AbstractSession session;

    // These are the descriptors to which we have 1:1 relationships
    protected Vector relatedNodes;
    protected CommitOrderDependencyNode predecessor;

    // Indicates the state of the traversal
    protected int traversalState;
    static public int NotVisited = 1;
    static public int InProgress = 2;
    static public int Visited = 3;

    // When we first saw this node in the traversal
    protected int discoveryTime;

    // When we finished visiting this node
    protected int finishingTime;

    public CommitOrderDependencyNode(CommitOrderCalculator calculator, ClassDescriptor descriptor, AbstractSession session) {
        this.owner = calculator;
        this.descriptor = descriptor;
        this.relatedNodes = new Vector();
        this.session = session;
    }

    public ClassDescriptor getDescriptor() {
        return descriptor;
    }

    public int getFinishingTime() {
        return finishingTime;
    }

    public CommitOrderCalculator getOwner() {
        return owner;
    }

    public CommitOrderDependencyNode getPredecessor() {
        return predecessor;
    }

    public Vector getRelatedNodes() {
        return relatedNodes;
    }

    public boolean hasBeenVisited() {
        return (traversalState == Visited);
    }

    public boolean hasNotBeenVisited() {
        return (traversalState == NotVisited);
    }

    public void markInProgress() {
        traversalState = InProgress;
    }

    public void markNotVisited() {
        traversalState = NotVisited;
    }

    public void markVisited() {
        traversalState = Visited;
    }

    /**
     * Add all owned classes for each descriptor through checking the mappings.
     * If I have a foreign mapping with a constraint dependency, then add it
     * If I'm related to a class, I'm related to all its subclasses and superclasses.
     * If my superclass is related to a class, I'm related to it.
     */
    public void recordMappingDependencies() {
        for (Enumeration mappings = getDescriptor().getMappings().elements();
                 mappings.hasMoreElements();) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.nextElement();
            if (mapping.isForeignReferenceMapping()) {
                if (((ForeignReferenceMapping)mapping).hasConstraintDependency()) {
                    Class ownedClass;
                    ClassDescriptor refDescriptor = ((ForeignReferenceMapping)mapping).getReferenceDescriptor();
                    if (refDescriptor == null) {
                        refDescriptor = session.getDescriptor(((ForeignReferenceMapping)mapping).getReferenceClass());
                    }
                    ownedClass = refDescriptor.getJavaClass();

                    if (ownedClass == null) {
                        throw oracle.toplink.essentials.exceptions.DescriptorException.referenceClassNotSpecified(mapping);
                    }
                    CommitOrderDependencyNode node = getOwner().nodeFor(ownedClass);
                    Vector ownedNodes = withAllSubclasses(node);

                    // I could remove duplicates here, but it's not that big a deal.
                    Helper.addAllToVector(relatedNodes, ownedNodes);
                } else if (((ForeignReferenceMapping)mapping).hasInverseConstraintDependency()) {
                    Class ownerClass;
                    ClassDescriptor refDescriptor = ((ForeignReferenceMapping)mapping).getReferenceDescriptor();
                    if (refDescriptor == null) {
                        refDescriptor = session.getDescriptor(((ForeignReferenceMapping)mapping).getReferenceClass());
                    }
                    ownerClass = refDescriptor.getJavaClass();
                    if (ownerClass == null) {
                        throw oracle.toplink.essentials.exceptions.DescriptorException.referenceClassNotSpecified(mapping);
                    }
                    CommitOrderDependencyNode ownerNode = getOwner().nodeFor(ownerClass);
                    Vector ownedNodes = withAllSubclasses(this);

                    // I could remove duplicates here, but it's not that big a deal.
                    Helper.addAllToVector(ownerNode.getRelatedNodes(), ownedNodes);
                }
            }
        }
    }

    /**
     * Add all owned classes for each descriptor through checking the mappings.
     * If I have a foreign mapping with a constraint dependency, then add it
     * If I'm related to a class, I'm related to all its subclasses and superclasses.
     * If my superclass is related to a class, I'm related to it.
     */
    public void recordSpecifiedDependencies() {
        for (Enumeration constraintsEnum = getDescriptor().getConstraintDependencies().elements();
                 constraintsEnum.hasMoreElements();) {
            Class ownedClass = (Class)constraintsEnum.nextElement();
            CommitOrderDependencyNode node = getOwner().nodeFor(ownedClass);
            Vector ownedNodes = withAllSubclasses(node);

            // I could remove duplicates here, but it's not that big a deal.
            Helper.addAllToVector(relatedNodes, ownedNodes);
        }
    }

    public void setDiscoveryTime(int time) {
        discoveryTime = time;
    }

    public void setFinishingTime(int time) {
        finishingTime = time;
    }

    public void setPredecessor(CommitOrderDependencyNode n) {
        predecessor = n;
    }

    public String toString() {
        if (descriptor == null) {
            return ToStringLocalization.buildMessage("empty_commit_order_dependency_node", (Object[])null);
        } else {
            Object[] args = { descriptor };
            return ToStringLocalization.buildMessage("node", args);
        }
    }

    public void visit() {
        //Visit this node as part of a topological sort
        int startTime;
        markInProgress();
        startTime = getOwner().getNextTime();
        setDiscoveryTime(startTime);

        for (Enumeration e = getRelatedNodes().elements(); e.hasMoreElements();) {
            CommitOrderDependencyNode node = (CommitOrderDependencyNode)e.nextElement();
            if (node.hasNotBeenVisited()) {
                node.setPredecessor(this);
                node.visit();
            }
            if (node.getPredecessor() == null) {
                node.setPredecessor(this);
            }
        }

        markVisited();
        setFinishingTime(getOwner().getNextTime());
    }

    // Return an enumeration of all mappings for my descriptor, including those inherited
    public Vector withAllSubclasses(CommitOrderDependencyNode node) {
        Vector results = new Vector();
        results.addElement(node);

        if (node.getDescriptor().hasInheritance()) {
            InheritancePolicy p = node.getDescriptor().getInheritancePolicy();

            // For bug 3019934 replace getChildDescriptors with getAllChildDescriptors.
            for (Enumeration e = p.getAllChildDescriptors().elements(); e.hasMoreElements();) {
                results.addElement(getOwner().nodeFor((ClassDescriptor)e.nextElement()));
            }
        }
        return results;
    }
}
