package com.sun.enterprise.v3.admin;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.component.PerLookup;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Property;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * User: Jerome Dochez
 * Date: Jul 11, 2008
 * Time: 4:39:05 AM
 */
@Service(name="set")
@Scoped(PerLookup.class)
public class SetCommand extends V2DottedNameSupport implements AdminCommand {

    @Inject
    Domain domain;

    @Param(primary = true)
    String target;

    @Param(optional=true)
    String value;

    public void execute(AdminCommandContext context) {

        Pattern p = Pattern.compile("([^=]*)=(.*)");
        if (value==null) {
            // we should have something like A=some value
            Matcher m = p.matcher(target);
            if (m.matches()) {
                target=m.group(1);
                value = m.group(2);
            }  else {
                 context.getActionReport().setActionExitCode(ActionReport.ExitCode.FAILURE);
                context.getActionReport().setMessage("Invalid target " + target);
                return;
            }
        }
        // so far I assume we always want to change one attribute so I am removing the
        // last element from the target pattern which is supposed to be the
        // attribute name
        if (target.lastIndexOf('.')==-1) {
            // error.
            context.getActionReport().setActionExitCode(ActionReport.ExitCode.FAILURE);
            context.getActionReport().setMessage("Invalid target " + target);
            return;
        }
        String attrName = target.substring(target.lastIndexOf('.')+1);
        String pattern =  target.substring(0, target.lastIndexOf('.'));
        boolean isProperty = false;
         if ("property".equals(pattern.substring(pattern.lastIndexOf('.')+1))) {
             // we are looking for a property, let's look it it exists already...
             pattern = target;
             isProperty = true;
         }
        
        // now
        // first let's get the parent for this pattern.
        TreeNode[] parentNodes = getAliasedParent(domain, pattern);
        Map<Dom, String> dottedNames =  new HashMap<Dom, String>();
        for (TreeNode parentNode : parentNodes) {
               dottedNames.putAll(getAllDottedNodes(parentNode.node));
        }

        // reset the pattern.
        pattern = parentNodes[0].relativeName;
        Map<Dom, String> matchingNodes = getMatchingNodes(dottedNames,pattern );
        if (matchingNodes.isEmpty()) {
            // it's possible they are trying to create a property object.. lets check this.
            // strip out the property name
            pattern =  target.substring(0, target.lastIndexOf('.'));
            if (pattern.endsWith("property")) {
                pattern =  pattern.substring(0, pattern.lastIndexOf('.'));
                parentNodes = getAliasedParent(domain, pattern);
                pattern = parentNodes[0].relativeName;
                matchingNodes = getMatchingNodes(dottedNames, pattern);
                if (matchingNodes.isEmpty()) {
                    context.getActionReport().setActionExitCode(ActionReport.ExitCode.FAILURE);
                    context.getActionReport().setMessage("No configuration found for " + pattern);
                    return;
                }
                // need to find the right parent.
                Dom parentNode=null;
                for (Map.Entry<Dom,String> node : matchingNodes.entrySet()) {
                    if (node.getValue().equals(pattern)) {
                        parentNode = node.getKey();
                    }
                }
                if (parentNode==null) {
                    context.getActionReport().setActionExitCode(ActionReport.ExitCode.FAILURE);
                    context.getActionReport().setMessage("No configuration found for " + target);
                    return;
                }

                // create and set the attribute.
                Map<String,String> attributes  = new HashMap<String, String>();
                attributes.put("value", value);
                attributes.put("name", attrName);
                try {
                    ConfigSupport.createAndSet((ConfigBean) parentNode, Property.class, attributes );
                    context.getActionReport().setActionExitCode(ActionReport.ExitCode.SUCCESS);
                    return;
                } catch (TransactionFailure transactionFailure) {
                    context.getActionReport().setActionExitCode(ActionReport.ExitCode.FAILURE);
                    context.getActionReport().setFailureCause(transactionFailure);
                    context.getActionReport().setMessage("Could not change the attributes : "
                            + transactionFailure.getMessage());
                    return;
                }
            }

        }

        Map<ConfigBean, Map<String, String>> changes = new HashMap<ConfigBean, Map<String, String>>();

        Map<String, String> attrChanges = new HashMap<String, String>();
        if (isProperty) {
           attrName = "value";   
        }
        attrChanges.put(attrName, value);

        for (Map.Entry<Dom, String> node : matchingNodes.entrySet()) {
            final Dom targetNode = node.getKey();

            for (String name : targetNode.getAttributeNames()) {
                if (attrName.equals(name)) {
                    ActionReport.MessagePart part = context.getActionReport().getTopMessagePart().addChild();
                    part.setChildrenType("DottedName");
                    part.setMessage(node.getValue() + "." + name + "=" + value);
                    changes.put((ConfigBean) node.getKey(), attrChanges);                    
                }
            }
        }
        if (!changes.isEmpty()) {
            try {
                ConfigSupport.apply(changes);
                context.getActionReport().setActionExitCode(ActionReport.ExitCode.SUCCESS);                
            } catch (TransactionFailure transactionFailure) {
                context.getActionReport().setActionExitCode(ActionReport.ExitCode.FAILURE);
                context.getActionReport().setFailureCause(transactionFailure);
                context.getActionReport().setMessage("Could not change the attributes : "
                        + transactionFailure.getMessage());
            }

        } else {
            context.getActionReport().setActionExitCode(ActionReport.ExitCode.FAILURE);
            context.getActionReport().setMessage("No configuration found for " + pattern);
        }

    }
}
