/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package com.sun.enterprise.v3.admin;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.Dom;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;

import java.util.Map;
import java.util.HashMap;

import java.util.*;

import com.sun.enterprise.v3.common.PropsFileActionReporter;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;

import com.sun.enterprise.config.serverbeans.Domain;

/**
 * User: Jerome Dochez
 * Date: Jul 12, 2008
 * Time: 1:27:53 AM
 */
@Service(name="list")
public class ListCommand extends V2DottedNameSupport implements AdminCommand {

    @Inject
    Domain domain;

    //How to define short option name?
    @Param(optional=true, defaultValue="false", shortName="m")
    Boolean monitor;

    @Param(primary = true)
    String pattern="";

    @Inject(optional=true)
    private MonitoringRuntimeDataRegistry mrdr;
    
    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();
        
        /* Issue 5918 Used in ManifestManager to keep output sorted */
        try {
            PropsFileActionReporter reporter = (PropsFileActionReporter) report;
            reporter.useMainChildrenAttribute(true);
        } catch(ClassCastException e) { 
            // ignore, this is not a manifest output
        }

        if (monitor) {
            listMonitorElements(report);
            return;
        }
        
        // first let's get the parent for this pattern.
         TreeNode[] parentNodes = getAliasedParent(domain, pattern);
        Map<Dom, String> dottedNames =  new HashMap<Dom, String>();
        for (TreeNode parentNode : parentNodes) {
               dottedNames.putAll(getAllDottedNodes(parentNode.node));
        }
        // reset the pattern.
        String prefix="";
        if (!pattern.startsWith(parentNodes[0].relativeName)) {
            prefix= pattern.substring(0, pattern.indexOf(parentNodes[0].relativeName));
        }
        pattern = parentNodes[0].relativeName;

        Map<Dom, String> matchingNodes = getMatchingNodes(dottedNames, pattern);
        if (matchingNodes.isEmpty() && pattern.lastIndexOf('.')!=-1) {
            // it's possible the user is just looking for an attribute, let's remove the
            // last element from the pattern.
            matchingNodes = getMatchingNodes(dottedNames, pattern.substring(0, pattern.lastIndexOf(".")));
        }
        List<Map.Entry> matchingNodesSorted = sortNodesByDottedName(matchingNodes);
        for (Map.Entry<Dom, String> node : matchingNodesSorted) {
            ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setChildrenType("DottedName");
            part.setMessage(prefix + (String)node.getValue());
        }
    }
    
    private void listMonitorElements(ActionReport report) {
        if ((pattern == null) || (pattern.equals(""))) {
            report.setActionExitCode(ExitCode.FAILURE);
            report.setMessage("match pattern is invalid or null");
            return;
        }

        if (mrdr==null) {
            report.setActionExitCode(ExitCode.FAILURE);
            report.setMessage("monitoring facility not installed");
            return;
        }

        //Grab the monitoring tree root from habitat and get the attributes using pattern
        org.glassfish.flashlight.datatree.TreeNode tn = mrdr.get("server");
        if (tn == null) {
            //No monitoring data, so nothing to list
            report.setActionExitCode(ExitCode.SUCCESS);
            return;
        }
        List<org.glassfish.flashlight.datatree.TreeNode> ltn = sortTreeNodesByCompletePathName(tn.getNodes(pattern));
        for (org.glassfish.flashlight.datatree.TreeNode tn1 : ltn) {
            if (tn1.hasChildNodes() ) {
                System.out.println(tn1.getCompletePathName());
                //report.setMessage(tn1.getCompletePathName() + " = " + tn1.getCompletePathName());
                ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(tn1.getCompletePathName());
            }
        }
        report.setActionExitCode(ExitCode.SUCCESS);
    }                                       
}
