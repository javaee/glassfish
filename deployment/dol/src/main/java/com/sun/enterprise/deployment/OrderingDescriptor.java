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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        if (wfs == null || wfs.size() == 1) {
            return;
        }
        
        // in the following, size >= 2
        int size = wfs.size();
        processOthers(wfs);
        enhanceOrderingData(wfs);

        WebFragmentDescriptor[] wfarr = wfs.toArray(new WebFragmentDescriptor[size]);

        int maxSwap = (size + 1) * size + 1;
        int numSwap = 0;
        boolean swap = false;
        boolean startCheck = false;
        int k = -1;

        while (numSwap < maxSwap) {
            if (swap) {
                k = 0;
                swap = false;
                startCheck = false;
            } else {
                k++;
                if (startCheck) {
                    break;
                }
                if (k == size - 1) {
                    k = 0;
                    startCheck = true;
                }
            }

            int i = 1;
            while (i < size - k) {
                if (isAfter(wfarr[k], wfarr[k + i])) {
                    OrderingDescriptor od = wfarr[k + i].getOrderingDescriptor();

                    // shift
                    if (od != null && od.getAfter() != null 
                            && od.getAfter().containsOthers()) {

                        // move k after k+i as  ..., k+1, ... , k+i, k, ...
                        WebFragmentDescriptor temp = wfarr[k];
                        for (int j = 0; j < i; j++) {
                            wfarr[k + j] = wfarr[k + j + 1];
                        }
                        wfarr[k + i] = temp;
                    } else {
                        // move k+i before k as  ..., k+i, k,  ... , k+i-1, ...
                        WebFragmentDescriptor temp = wfarr[k + i];
                        for (int j = k + i; j > k; j--) {
                            wfarr[j] = wfarr[j - 1];
                        }
                        wfarr[k] = temp;

                        k++;
                    }

                    numSwap++;
                    swap = true;

                } else {
                    i++;
                }
            } 
        }

        if (numSwap >= maxSwap) {
            throw new IllegalStateException(localStrings.getLocalString(
                    "enterprise.deployment.exceptioninvalidwebfragmentordering",
                    "The web fragment ordering is not valid and possibly has cycling conflicts."));
        }

        // copy the result back to original list
        if (wfs != null && wfs.size() > 1) {
            wfs.clear();
            for (WebFragmentDescriptor wf : wfarr) {
                wfs.add(wf);
            }
        }
    }

    /**
     * Processing &lt;others/&gt; in ordering first.
     */
    private static void processOthers(List<WebFragmentDescriptor> wfs) {
        int i = 0;
        //One can always insert the "before others" at the index 0 and no need for beforeOthersInd.
        //We keep this so that the order will be close to those in jsf-ri.
        int beforeOthersInd = 0; // the index to insert for "before others"
        int afterOthersInd = wfs.size(); // the largest index to process for "after others"
        while (i < afterOthersInd) {
            WebFragmentDescriptor wf = wfs.get(i);
            OrderingDescriptor od = wf.getOrderingDescriptor();

            if (od != null && od.getBefore() != null && od.getBefore().containsOthers()) {
                if (i > 0) {
                   wfs.remove(i);
                   wfs.add(beforeOthersInd, wf);
                }
                beforeOthersInd++;
                i++;
            } else if (i < (afterOthersInd - 1) &&
                    od != null && od.getAfter() != null && od.getAfter().containsOthers()) {
                wfs.remove(i);
                wfs.add(wf);
                afterOthersInd--;
            } else {
                i++;
            }
        }
    }

    /**
     * Update after/before knowledge of all OrderingDescriptors.
     * Basically, it does the following:
     * (a) add some symmetric data,
     * &nbsp;&nbsp; for instance, A is before B, then B should be after A
     * (b) add data from first level transitivity,
     * &nbsp;&nbsp; for instance, A is before B, B is before C, then A is before C
     * This method also detects some possible cycles and then throw exception.
     */
    private static void enhanceOrderingData(List<WebFragmentDescriptor> wfs) {
        Map<String, WebFragmentDescriptor> name2WfMap = new TreeMap<String, WebFragmentDescriptor>();
        for (WebFragmentDescriptor wf: wfs) {
            String name = wf.getName();
            if (name != null && name.length() > 0) {
                name2WfMap.put(name, wf);
            }
        }

        for (WebFragmentDescriptor wf: wfs) {
            String wfName = wf.getName();
            OrderingDescriptor od = wf.getOrderingDescriptor();

            if (od != null && wfName != null && wfName.length() > 0) {
                // if A is before B, then (i) B should be after A and (ii) A should be before B's before
                OrderingOrderingDescriptor before = od.getBefore();
                if (before != null) {
                    Set<String> beforeNamesSet = before.getNames();
                    String[] beforeNames = beforeNamesSet.toArray(new String[beforeNamesSet.size()]);
                    for (String beforeName : beforeNames) {
                        WebFragmentDescriptor otherWf = name2WfMap.get(beforeName);
                        OrderingDescriptor otherOd = otherWf.getOrderingDescriptor();
                        if (otherOd == null) {
                            otherOd = new OrderingDescriptor();
                            otherWf.setOrderingDescriptor(otherOd);
                        }
                        OrderingOrderingDescriptor otherAfter = otherOd.getAfter();
                        if (otherAfter == null) {
                            otherAfter = new OrderingOrderingDescriptor();
                            otherOd.setAfter(otherAfter);
                        }
                        otherAfter.addName(wfName);

                        OrderingOrderingDescriptor otherBefore = otherOd.getBefore();
                        if (otherBefore != null) {
                            before.getNames().addAll(otherBefore.getNames());

                            if (otherBefore.containsName(wfName)) {
                                throw new IllegalStateException(localStrings.getLocalString(
                                        "enterprise.deployment.exceptioncyclicdependenyinwebfragmentordering",
                                        "There is a cyclic dependencies on name [{0}] in web fragment relative ordering.",
                                        new Object[] { wfName }));
                            }
                        }
                    }
                }

                // if A is after B, then (i) B should be before A and (ii) A should be after B's after
                OrderingOrderingDescriptor after = od.getAfter();
                if (after != null) {
                    Set<String> afterNamesSet = after.getNames();
                    String[] afterNames = afterNamesSet.toArray(new String[afterNamesSet.size()]);
                    for (String afterName : afterNames) {
                        WebFragmentDescriptor otherWf = name2WfMap.get(afterName);
                        OrderingDescriptor otherOd = otherWf.getOrderingDescriptor();
                        if (otherOd == null) {
                            otherOd = new OrderingDescriptor();
                            otherWf.setOrderingDescriptor(otherOd);
                        }
                        OrderingOrderingDescriptor otherBefore = otherOd.getBefore();
                        if (otherBefore == null) {
                            otherBefore = new OrderingOrderingDescriptor();
                            otherOd.setBefore(otherBefore);
                        }
                        otherBefore.addName(wfName);

                        OrderingOrderingDescriptor otherAfter = otherOd.getAfter();
                        if (otherAfter != null) {
                            after.getNames().addAll(otherAfter.getNames());

                            if (otherAfter.containsName(wfName)) {
                                throw new IllegalStateException(localStrings.getLocalString(
                                        "enterprise.deployment.exceptioncyclicdependenyinwebfragmentordering",
                                        "There is a cyclic dependencies for name [{0}] in web fragment relative ordering.",
                                        new Object[] { wfName }));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This only checks the explicit ordering relationship without &lt;others/&gt;.
     */
    private static boolean isAfter(WebFragmentDescriptor wf1, WebFragmentDescriptor wf2) {
        String name1 = wf1.getName();
        String name2 = wf2.getName();
        OrderingDescriptor od1 = wf1.getOrderingDescriptor();
        OrderingDescriptor od2 = wf2.getOrderingDescriptor();
        return (od1 != null && od1.getAfter() != null && od1.getAfter().containsName(name2)) ||
                (od2 != null && od2.getBefore() != null && od2.getBefore().containsName(name1));
    }
}
