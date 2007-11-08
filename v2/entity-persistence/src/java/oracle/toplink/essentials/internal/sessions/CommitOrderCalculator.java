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
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * This class calculates a commit order for a series of classes
 * based on the dependencies between them. It builds up a graph of
 * dependencies (CommitOrderDependencyNodes) then applies topological
 * sort to them to get an ordering.
 * This is a throwaway class, which exists only for the lifetime of
 * the calculation.
 *
 * The algorithm is descrbied in the method comment for orderCommits().
 * This class also includes static methods for quicksort, copied from
 * the standard libraries and adapted for these objects, since that
 * seemed like the easiest way to sort.
 */
public class CommitOrderCalculator {
    protected int currentTime;
    protected Vector nodes;
    protected Vector orderedDescriptors;
    protected AbstractSession session;

    /**
     *
     */
    public CommitOrderCalculator(AbstractSession session) {
        super();
        this.currentTime = 0;
        this.nodes = new Vector(1);
        this.session = session;
    }

    protected void addNode(ClassDescriptor d) {
        nodes.addElement(new CommitOrderDependencyNode(this, d, session));
    }

    public void addNodes(Vector descriptors) {
        Enumeration descriptorsEnum = descriptors.elements();
        while (descriptorsEnum.hasMoreElements()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptorsEnum.nextElement();
            addNode(descriptor);
        }
    }

    /*
     * Add to each node the dependent nodes
     */
    public void calculateMappingDependencies() {
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            CommitOrderDependencyNode node = (CommitOrderDependencyNode)e.nextElement();
            node.recordMappingDependencies();
        }
    }

    /*
     * Add to each node the dependent nodes
     */
    public void calculateSpecifiedDependencies() {
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            CommitOrderDependencyNode node = (CommitOrderDependencyNode)e.nextElement();
            node.recordSpecifiedDependencies();
        }
    }

    public void depthFirstSearch() {

        /*
         * Traverse the entire graph in breadth-first order. When finished, every node will have a
         * predecessor which indicates the node that came efore it in the search
         * It will also have a discovery time (the value of the counter when we first saw it) and
         * finishingTime (the value of the counter after we've visited all the adjacent nodes).
         * See Cormen, Leiserson and Rivest, Section 23.3, page 477 for a full explanation of the algorithm
         */

        //Setup
        for (Enumeration e = getNodes().elements(); e.hasMoreElements();) {
            CommitOrderDependencyNode node = (CommitOrderDependencyNode)e.nextElement();
            node.markNotVisited();
            node.setPredecessor(null);
        }
        currentTime = 0;

        //Execution
        for (Enumeration e = getNodes().elements(); e.hasMoreElements();) {
            CommitOrderDependencyNode node = (CommitOrderDependencyNode)e.nextElement();
            if (node.hasNotBeenVisited()) {
                node.visit();
            }
        }
    }

    /* Support for quicksort */
    /*
     * Implement the doCompare method.
     */
    private static int doCompare(Object o1, Object o2) {
        // I don't care if they're equal, and I want to sort largest first.
        int first;

        // I don't care if they're equal, and I want to sort largest first.
        int second;
        first = ((CommitOrderDependencyNode)o1).getFinishingTime();
        second = ((CommitOrderDependencyNode)o2).getFinishingTime();
        if (first >= second) {
            return 1;
        } else {
            return -1;
        }
    }

    /* Support for quicksort */
    /*
     * Implement the doCompare method.
     */
    private static int doCompare(CommitOrderDependencyNode o1, CommitOrderDependencyNode o2) {
        // I don't care if they're equal, and I want to sort largest first.
        int first;

        // I don't care if they're equal, and I want to sort largest first.
        int second;
        first = o1.getFinishingTime();
        second = o2.getFinishingTime();
        if (first >= second) {
            return 1;
        } else {
            return -1;
        }
    }

    public int getNextTime() {
        int result = currentTime;
        currentTime++;
        return result;
    }

    public Vector getNodes() {
        return nodes;
    }

    /*
     * Return the constraint ordered classes.
     */
    public Vector getOrderedClasses() {
        Vector orderedClasses = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(getOrderedDescriptors().size());
        for (Enumeration orderedDescriptorsEnum = getOrderedDescriptors().elements();
                 orderedDescriptorsEnum.hasMoreElements();) {
            orderedClasses.addElement(((ClassDescriptor)orderedDescriptorsEnum.nextElement()).getJavaClass());
        }

        return orderedClasses;
    }

    /*
     * Return the constraint ordered descriptors.
     */
    public Vector getOrderedDescriptors() {
        return orderedDescriptors;
    }

    public CommitOrderDependencyNode nodeFor(Class c) {
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            CommitOrderDependencyNode n = (CommitOrderDependencyNode)e.nextElement();
            if (n.getDescriptor().getJavaClass() == c) {
                return n;
            }
        }
        return null;
    }

    public CommitOrderDependencyNode nodeFor(ClassDescriptor d) {
        for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
            CommitOrderDependencyNode n = (CommitOrderDependencyNode)e.nextElement();
            if (n.getDescriptor() == d) {
                return n;
            }
        }
        return null;
    }

    /*
     * Calculate the commit order.
     * Do a depth first search on the graph, skipping nodes that we have
     * already visited or are in the process of visiting. Keep a counter
     * and note when we first encounter a node and when we finish visiting
     * it. Once we've visited everything, sort nodes by finishing time
     */
    public void orderCommits() {
        depthFirstSearch();

        Object[] nodeArray = new Object[nodes.size()];
        nodes.copyInto(nodeArray);

        quicksort(nodeArray);
        Vector result = new Vector(nodes.size());
        for (int i = 0; i < nodes.size(); i++) {
            CommitOrderDependencyNode node = (CommitOrderDependencyNode)nodeArray[i];
            result.addElement(node.getDescriptor());
        }
        this.orderedDescriptors = result;
    }

    /*
     * Preform a sort using the specified comparitor object.
     */
    private static void quicksort(Object[] arr) {
        quicksort(arr, 0, arr.length - 1);
    }

    /*
     * quicksort the array of objects.
     *
     * @param arr[] - an array of objects
     * @param left - the start index - from where to begin sorting
     * @param right - the last index.
     */
    private static void quicksort(Object[] arr, int left, int right) {
        int i;
        int last;

        if (left >= right) {/* do nothing if array contains fewer than two */
            return;/* two elements */
        }
        swap(arr, left, (left + right) / 2);
        last = left;
        for (i = left + 1; i <= right; i++) {
            if (doCompare(arr[i], arr[left]) < 0) {
                swap(arr, ++last, i);
            }
        }
        swap(arr, left, last);
        quicksort(arr, left, last - 1);
        quicksort(arr, last + 1, right);
    }

    private static void swap(Object[] arr, int i, int j) {
        Object tmp;

        tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}
