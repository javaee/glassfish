package com.sun.enterprise.v3.admin;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.TransactionFailure;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import com.sun.enterprise.config.serverbeans.Domain;

import java.util.Map;
import java.util.HashMap;

/**
 * User: Jerome Dochez
 * Date: Jul 11, 2008
 * Time: 4:39:05 AM
 */
@Service(name="set")
public class SetCommand extends V2DottedNameSupport implements AdminCommand {

    @Inject
    Domain domain;

    @Param(primary = true)
    String target;

    @Param
    String value;

    public void execute(AdminCommandContext context) {

        // so far I assume we always want to change one attribute so I am removing the
        // last element from the target pattern which is supposed to be the
        // attribute name
        if (target.lastIndexOf('.')==-1) {
            // error.
            context.getActionReport().setActionExitCode(ActionReport.ExitCode.FAILURE);
            context.getActionReport().setMessage("Invalid target " + target);
            return;
        }
        final String attrName = target.substring(target.lastIndexOf('.')+1);
        Map<Dom, String> nodes = getAllDottedNodes(domain);
        Map<Dom, String> matchingNodes = getMatchingNodes(nodes, target.substring(0, target.lastIndexOf('.')));

        Map<ConfigBean, Map<String, String>> changes = new HashMap<ConfigBean, Map<String, String>>();

        Map<String, String> attrChanges = new HashMap<String, String>();
        attrChanges.put(attrName, value);

        for (Map.Entry<Dom, String> node : matchingNodes.entrySet()) {
            final Dom targetNode = node.getKey();
            for (Map.Entry<String, String> name : getNodeAttributes(targetNode, null).entrySet()) {
                String finalDottedName = node.getValue()+"."+name.getKey();
                if (matches(finalDottedName, target)) {
                    ActionReport.MessagePart part = context.getActionReport().getTopMessagePart().addChild();
                    part.setChildrenType("DottedName");
                    part.setMessage(node.getValue() + "." + name.getKey() + "=" + value);
                    changes.put((ConfigBean) node.getKey(), attrChanges);                    
                }
            }
        }
        if (!changes.isEmpty()) {
            try {
                ConfigSupport.apply(changes);
            } catch (TransactionFailure transactionFailure) {
                context.getActionReport().setActionExitCode(ActionReport.ExitCode.FAILURE);
                context.getActionReport().setFailureCause(transactionFailure);
                context.getActionReport().setMessage("Could not change the attributes : "
                        + transactionFailure.getMessage());
            }

        }
        context.getActionReport().setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
