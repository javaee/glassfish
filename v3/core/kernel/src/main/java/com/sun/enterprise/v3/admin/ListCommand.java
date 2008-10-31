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
            context.logger.severe("Sort failed in list command: " + e.toString());
            e.printStackTrace();
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
