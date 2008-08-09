package com.sun.enterprise.v3.admin;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;

import java.util.*;

import com.sun.enterprise.config.serverbeans.Domain;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.statistics.Counter;
import javax.management.j2ee.statistics.Statistic;
import org.glassfish.flashlight.datatree.MethodInvoker;

/**
 * User: Jerome Dochez
 * Date: Jul 10, 2008
 * Time: 12:17:26 AM
 */
@Service(name="get")
public class GetCommand extends V2DottedNameSupport implements AdminCommand {

    @Inject
    Domain domain;

    //How to define short option name?
    @Param(optional=true, defaultValue="false")
    Boolean monitor;

    @Param(primary = true)
    String pattern;

    @Inject
    private MonitoringRuntimeDataRegistry mrdr;
    
    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();

        if (monitor) {
            getMonitorAttributes(report);
            return;
        }
        

        // first let's get the parent for this pattern.
        TreeNode[] parentNodes = getAliasedParent(domain, pattern);
        Map<Dom, String> dottedNames =  new HashMap<Dom, String>();
        for (TreeNode parentNode : parentNodes) {
               dottedNames.putAll(getAllDottedNodes(parentNode.node));
        }

        // reset the pattern.
        pattern = parentNodes[0].relativeName;

        Map<Dom, String> matchingNodes = getMatchingNodes(dottedNames, pattern);
        if (matchingNodes.isEmpty() && pattern.lastIndexOf('.')!=-1) {
            // it's possible the user is just looking for an attribute, let's remove the
            // last element from the pattern.
            matchingNodes = getMatchingNodes(dottedNames, pattern.substring(0, pattern.lastIndexOf(".")));
        }
        for (Map.Entry<Dom, String> node : matchingNodes.entrySet()) {
            // if we get more of these special cases, we should switch to a Renderer pattern
            if (node.getKey().model.targetTypeName.equals("com.sun.enterprise.config.serverbeans.Property")) {
                 // special display for properties...
                ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setChildrenType("DottedName");
                part.setMessage(node.getValue() + "=" + node.getKey().attribute("value"));
            }   else {
                for (Map.Entry<String, String> name : getNodeAttributes(node.getKey(), pattern).entrySet()) {
                    String finalDottedName = node.getValue()+"."+name.getKey();
                    if (matches(finalDottedName, pattern)) {
                        ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                        part.setChildrenType("DottedName");
                        part.setMessage(node.getValue() + "." + name.getKey() + "=" + name.getValue());
                    }
                }
            }
        }
    }
    
    private void getMonitorAttributes(ActionReport report) {
        if ((pattern == null) || (pattern.equals(""))) {
            report.setActionExitCode(ExitCode.FAILURE);
            report.setMessage("match pattern is invalid or null");
        }

        //Grab the monitoring tree root from habitat and get the attributes using pattern
        org.glassfish.flashlight.datatree.TreeNode tn = mrdr.get("server");
        List<org.glassfish.flashlight.datatree.TreeNode> ltn = tn.getNodes(pattern);
        for (org.glassfish.flashlight.datatree.TreeNode tn1 : ltn) {
            System.out.println("[TN] node is instanceof  = " + tn1.getClass().getName());
            if ((! tn1.hasChildNodes()) && 
                    ((tn1 instanceof Statistic) || (tn1 instanceof MethodInvoker))) {
                //Counter c = (Counter)tn1;
                //System.out.println(tn1.getCompletePathName() + " = " + tn1.getValue());
                if (tn1 instanceof MethodInvoker) {
                    System.out.println("[TN] Inside: node is instanceof  = " + tn1.getClass().getName());
                }
                System.out.println(tn1.getCompletePathName() + " = " + tn1.getValue());
                //report.setMessage(tn1.getCompletePathName() + " = " + tn1.getValue());
                ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(tn1.getCompletePathName() + " = " + tn1.getValue());
            }
        }
        report.setActionExitCode(ExitCode.SUCCESS);
    }
}
