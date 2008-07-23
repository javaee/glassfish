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

/**
 * User: Jerome Dochez
 * Date: Jul 10, 2008
 * Time: 12:17:26 AM
 */
@Service(name="get")
public class GetCommand extends V2DottedNameSupport implements AdminCommand {

    @Inject
    Domain domain;

    @Param(primary = true)
    String pattern="";
    
    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();

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
}
