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

/**
 * User: Jerome Dochez
 * Date: Jul 12, 2008
 * Time: 1:27:53 AM
 */
@Service(name="list")
public class ListCommand extends V2DottedNameSupport implements AdminCommand {

    @Inject
    Habitat habitat;

    @Param(primary = true)
    String pattern="";

    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();

        // first let's get the parent for this pattern.
        TreeNode parentNode = getAliasedParent(habitat, pattern);
        Map<Dom, String> dottedNames = getAllDottedNodes(parentNode.node);

        // reset the pattern.
        pattern = parentNode.relativeName;

        Map<Dom, String> matchingNodes = getMatchingNodes(dottedNames, pattern);
        if (matchingNodes.isEmpty() && pattern.lastIndexOf('.')!=-1) {
            // it's possible the user is just looking for an attribute, let's remove the
            // last element from the pattern.
            matchingNodes = getMatchingNodes(dottedNames, pattern.substring(0, pattern.lastIndexOf(".")));
        }
        for (Map.Entry<Dom, String> node : matchingNodes.entrySet()) {
            ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setChildrenType("DottedName");
            part.setMessage(node.getValue());
        }
    }
}
