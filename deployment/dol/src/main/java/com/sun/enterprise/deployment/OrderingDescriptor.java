/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.deployment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * This represents the ordering resided in web-fragment.xml.
 *
 * @author Shing Wai Chan
 */

public class OrderingDescriptor extends Descriptor {
    private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(OrderingDescriptor.class);

    OrderingOrderingDescriptor after = null;
    
    OrderingOrderingDescriptor before = null;

    public OrderingOrderingDescriptor getAfter() {
        return after;
    }

    public void setAfter(OrderingOrderingDescriptor after) {
        this.after = after;
        validate();
    }

    public OrderingOrderingDescriptor getBefore() {
        return before;
    }

    public void setBefore(OrderingOrderingDescriptor before) {
        this.before = before;
        validate();
    }

    public void validate() {
        boolean valid = true;
        if (after != null && before != null) {
            if (after.containsOthers() && before.containsOthers()) {
                valid = false;
            }
            if (valid) {
                for (String name : after.getNames()) {
                    if (before.containsName(name)) {
                        valid = false;
                        break;
                    }
                }
            }
        }

        if (!valid) {
            throw new IllegalStateException(localStrings.getLocalString(
                    "enterprise.deployment.exceptioninvalidordering",
                    "The ordering is not valid as it contains the same name and/or others in both before and after."));
        }

    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (after != null) {
            builder.append("after: " + after + ", ");
        }
        if (before != null) {
            builder.append("before: " + before);
        }
        return builder.toString();
    }


    // ----- sorting logic

    public static void sort(List<WebFragmentDescriptor> wfs) {
        if (wfs == null || wfs.size() <= 1) {
            return;
        }

        // build the graph
        List<Node> graph = new ArrayList<Node>();
        Map<String, Node> name2NodeMap = new HashMap<String, Node>();

        // build the nodes
        Node othersNode = new Node(null);
        for (WebFragmentDescriptor wf : wfs) {
            Node wfNode = new Node(wf);
            String wfName = wf.getName();
            if (wfName != null && wfName.length() > 0) {
                name2NodeMap.put(wfName, wfNode);
            }
            graph.add(wfNode);
        }

        List<Node> remaining = new ArrayList<Node>(graph);
        boolean hasOthers = false;

        // build the edges
        // othersNode is not in the loop
        for (int i = 0; i < graph.size(); i++) {
            Node wfNode = graph.get(i);
            WebFragmentDescriptor wf = wfNode.getWebFragmentDescriptor();
            String wfName = wf.getName();
            OrderingDescriptor od = wf.getOrderingDescriptor();
            if (od != null) {
                OrderingOrderingDescriptor after = od.getAfter();
                if (after != null) {
                    if (after.containsOthers()) {
                        wfNode.getInNodes().add(othersNode);
                        othersNode.getOutNodes().add(wfNode);
                        remaining.remove(othersNode);
                        hasOthers = true;
                    }
                    for (String name : after.getNames()) {
                        Node nameNode = name2NodeMap.get(name);
                        wfNode.getInNodes().add(nameNode);
                        nameNode.getOutNodes().add(wfNode);
                        remaining.remove(nameNode);
                    }
                }

                OrderingOrderingDescriptor before = od.getBefore();
                if (before != null) {
                    if (before.containsOthers()) {
                        wfNode.getOutNodes().add(othersNode);
                        othersNode.getInNodes().add(wfNode);
                        remaining.remove(othersNode);
                        hasOthers = true;
                    }
                    for (String name : before.getNames()) {
                        Node nameNode = name2NodeMap.get(name);
                        wfNode.getOutNodes().add(nameNode);
                        nameNode.getInNodes().add(wfNode);
                        remaining.remove(nameNode);
                    }
                }

                boolean hasAfterOrdering = (after != null &&
                        (after.containsOthers() || after.getNames().size() > 0));
                boolean hasBeforeOrdering = (before != null &&
                        (before.containsOthers() || before.getNames().size() > 0));
                if (hasAfterOrdering || hasBeforeOrdering) {
                    remaining.remove(wfNode);
                }
            }
        }

        // add others to the end if necessary
        if (hasOthers) {
            graph.add(othersNode);
        }

        List<Node> subgraph = new ArrayList<Node>(graph);
        subgraph.removeAll(remaining);
        boolean hasRemaining = (remaining.size() > 0);

        List<Node> sortedNodes = topologicalSort(subgraph, hasRemaining);
        wfs.clear();
        boolean othersProcessed = false;
        for (Node node: sortedNodes) {
            WebFragmentDescriptor wf = node.getWebFragmentDescriptor();
            if (wf == null) {
                // others
                othersProcessed = true;
                for (Node rnode: remaining) {
                    wfs.add(rnode.getWebFragmentDescriptor());
                }
            } else {
                wfs.add(wf);
            }
        }

        if (!othersProcessed) {
            for (Node rnode: remaining) {
                wfs.add(rnode.getWebFragmentDescriptor());
            }
        }
    }

    /**
     * Note that this processing will modify the graph.
     * It is not intended for public.
     * @param graph
     * @param hasRemaining
     * @return a sorted list of Node
     */
    private static List<Node> topologicalSort(List<Node> graph, boolean hasRemaining) {
        List<Node> sortedNodes = new ArrayList<Node>();

        if (graph.size() == 0) {
            return sortedNodes;
        }

        Stack<Node> roots = new Stack<Node>();
        // find nodes without incoming edges
        for (Node node: graph) {
            if (node.getInNodes().size() == 0) {
                roots.push(node);
            }
        }
        
        if (roots.empty()) {
            // check if it is a circle with others and empty remaining
            if (isCircleWithOthersAndNoRemaining(graph, hasRemaining, sortedNodes)) {
                return sortedNodes;
            } else {
                throw new IllegalStateException(localStrings.getLocalString(
                        "enterprise.deployment.exceptioninvalidwebfragmentordering",
                        "The web fragment ordering is not valid and possibly has cycling conflicts."));
            }
        }

        while (!roots.empty()) {
            Node node = roots.pop();
            sortedNodes.add(node);
            // for each outcoming edges
            Iterator<Node> outNodesIter = node.getOutNodes().iterator();
            while (outNodesIter.hasNext()) {
                Node outNode = outNodesIter.next();
                // remove the outcoming edge
                outNodesIter.remove();
                // remove corresponding incoming edge from the outNode
                outNode.getInNodes().remove(node);

                // if no incoming edge
                if (outNode.getInNodes().size() == 0) {
                    roots.push(outNode);
                }
            }
        }

        boolean hasEdges = false;
        for (Node node: graph) {
            if (node.getInNodes().size() > 0 || node.getOutNodes().size() > 0) {
                hasEdges = true;
                break;
            }
        }
        if (hasEdges) {
            throw new IllegalStateException(localStrings.getLocalString(
                    "enterprise.deployment.exceptioninvalidwebfragmentordering",
                    "The web fragment ordering is not valid and possibly has cycling conflicts."));
        }
        return sortedNodes;
    }

    /**
     * This method will check whether the graph does not have remaining vertices
     * and is a circle with others. It return the sorted result in sortedNodes.
     * @param graph  the input graph
     * @param hasRemaining
     * @param sortedNodes  output sorted result if it is a circle with empty others
     * @return boolean indicating whether it is a circle with an empty others
     */
    private static boolean isCircleWithOthersAndNoRemaining(List<Node> graph,
            boolean hasRemaining, List<Node>sortedNodes) {

        boolean circleWithOthersAndNoRemaining = false;
        int size = graph.size();
        if (size == 0 || hasRemaining) {
            return circleWithOthersAndNoRemaining;
        }

        Node nextNode = graph.get(size - 1);
        if (nextNode.getWebFragmentDescriptor() == null) { // others
            Set<Node> set = new LinkedHashSet<Node>();
            int count = 0;
            while ((count < size) &&
                    nextNode.getOutNodes().size() == 1 &&
                    nextNode.getInNodes().size() == 1) {

                if (!set.add(nextNode)) {
                    break;
                }
                nextNode = nextNode.getOutNodes().iterator().next();
                count++;
            }

            if (set.size() == size) {
                circleWithOthersAndNoRemaining = true;
                Iterator<Node> iter = set.iterator();
                // exclude others
                if (iter.hasNext()) {
                    iter.next();
                }
                while (iter.hasNext()) {
                   sortedNodes.add(iter.next());
                }
            }
        }

        return circleWithOthersAndNoRemaining;
    }

    // for debug
    private static void print(WebFragmentDescriptor wf,
            String nullWfString, StringBuilder sb) {

        String wfName = null;
        if (wf != null) {
            wfName = wf.getName();
        } else {
            wfName = nullWfString;
        }
        sb.append(wfName);
    }

    private static class Node {
        private WebFragmentDescriptor webFragmentDescriptor = null;
        private Set<Node> inNodes = new LinkedHashSet<Node>();
        private Set<Node> outNodes = new LinkedHashSet<Node>();

        private Node(WebFragmentDescriptor wf) {
            webFragmentDescriptor = wf;
        }

        private WebFragmentDescriptor getWebFragmentDescriptor() {
            return webFragmentDescriptor;
        }

        private Set<Node> getInNodes() {
            return inNodes;
        }

        private Set<Node> getOutNodes() {
            return outNodes;
        }

        // for debug
        public String toString() {
            StringBuilder sb = new StringBuilder("{name=");
            print(webFragmentDescriptor, "@others", sb);

            sb.append(", inNodes=[");
            boolean first = true;
            for (Node n: inNodes) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                print(n.getWebFragmentDescriptor(), "@others", sb);
            }
            sb.append("]");

            sb.append(", outNodes=[");
            first = true;
            for (Node n: outNodes) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                print(n.getWebFragmentDescriptor(), "@others", sb);
            }
            sb.append("]}");
            return sb.toString();
        }
    }
}
