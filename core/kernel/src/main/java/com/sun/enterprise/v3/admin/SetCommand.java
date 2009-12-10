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
package com.sun.enterprise.v3.admin;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import org.glassfish.api.I18n;
import org.glassfish.api.ActionReport;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.jvnet.hk2.config.types.Property;
import org.glassfish.api.admin.config.LegacyConfigurationUpgrade;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * User: Jerome Dochez
 * Date: Jul 11, 2008
 * Time: 4:39:05 AM
 */
@Service(name="set")
@Scoped(PerLookup.class)
@I18n("set")
public class SetCommand extends V2DottedNameSupport implements AdminCommand {

    @Inject
    Habitat habitat;

    @Inject
    Domain domain;

    @Inject
    ConfigSupport config;

    @Param(primary = true, multiple = true)
    String[] values;
    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(SetCommand.class);
    
    public void execute(AdminCommandContext context) {
        for (String value : values) {
            if (!set(context, value))
                return;
        }
    }

    private boolean set(AdminCommandContext context, String nameval) {

        int i = nameval.indexOf('=');
        if (i < 0) {
            //ail(context, "Invalid attribute " + nameval);
            fail(context, localStrings.getLocalString("admin.set.invalid.namevalue", "Invalid name value pair {0}. Missing expected equal sign.", nameval));
            return false;
        }
        String target = nameval.substring(0, i);
        String value = nameval.substring(i + 1);
        // so far I assume we always want to change one attribute so I am removing the
        // last element from the target pattern which is supposed to be the
        // attribute name
        int lastDotIndex = trueLastIndexOf(target, '.');
        if (lastDotIndex==-1) {
            // error.
            //fail(context, "Invalid attribute name " + target);
            fail(context, localStrings.getLocalString("admin.set.invalid.attributename", "Invalid attribute name {0}", target));
            return false;
        }
        String attrName = target.substring(lastDotIndex+1);
        String pattern =  target.substring(0, lastDotIndex);
        if (attrName.replace('_', '-').equals("jndi-name")) {
            //fail(context, "Cannot change a primary key\nChange of " + target + " is rejected.");
            fail(context, localStrings.getLocalString("admin.set.reject.keychange", "Cannot change a primary key\nChange of {0}", target));
            return false;
        }
        boolean isProperty = false;
        if ("property".equals(pattern.substring(pattern.lastIndexOf('.')+1))) {
            // we are looking for a property, let's look it it exists already...
            pattern = target.replaceAll("\\\\\\.", "\\.");
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
        String prefix="";
        if (!pattern.startsWith(parentNodes[0].relativeName)) {
            prefix= pattern.substring(0, pattern.indexOf(parentNodes[0].relativeName));
        }
        pattern = parentNodes[0].relativeName;
        String targetName = prefix + pattern;
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
                    //fail(context, "No configuration found for " + targetName);
                    fail(context, localStrings.getLocalString("admin.set.configuration.notfound", "No configuration found for {0}", targetName));
                    return false;
                }
                // need to find the right parent.
                Dom parentNode=null;
                for (Map.Entry<Dom,String> node : matchingNodes.entrySet()) {
                    if (node.getValue().equals(pattern)) {
                        parentNode = node.getKey();
                    }
                }
                if (parentNode==null) {
                    //fail(context, "No configuration found for " + targetName);
                    fail(context, localStrings.getLocalString("admin.set.configuration.notfound", "No configuration found for {0}", targetName));
                    return false;
                }

                // create and set the attribute.
                Map<String,String> attributes  = new HashMap<String, String>();
                attributes.put("value", value);
                attributes.put("name", attrName);
                try {
                    ConfigSupport.createAndSet((ConfigBean) parentNode, Property.class, attributes );
                    success(context, targetName, value);
                    runLegacyChecks(context);
                    return true;
                } catch (TransactionFailure transactionFailure) {
                    //fail(context, "Could not change the attributes: " +
                    //    transactionFailure.getMessage(), transactionFailure);
                    fail(context, localStrings.getLocalString("admin.set.attribute.change.failure", "Could not change the attributes: {0}",
                            transactionFailure.getMessage()),transactionFailure);
                    return false;
                }
            }
        }

        Map<ConfigBean, Map<String, String>> changes = new HashMap<ConfigBean, Map<String, String>>();

        boolean delPropertySuccess = false;
        boolean delProperty = false;
        Map<String, String> attrChanges = new HashMap<String, String>();
        if (isProperty) {
            attrName = "value";
            if ((value == null) || (value.length() == 0)) {
                delProperty = true;
            }
            attrChanges.put(attrName, value);                    
        }

        for (Map.Entry<Dom, String> node : matchingNodes.entrySet()) {
            final Dom targetNode = node.getKey();

            for (String name : targetNode.model.getAttributeNames()) {
                String finalDottedName = node.getValue()+"."  + name;
                if (matches(finalDottedName, pattern)) {
                    if (attrName.equals(name) ||
                            attrName.replace('_', '-').equals(name.replace('_', '-'))) {
                        
                        if (! isProperty) {
                           targetName = prefix + finalDottedName;

                            if (value!=null && value.length()>0) {
                                attrChanges.put(name, value);
                            } else {
                                attrChanges.put(name, null);
                            }
                        } else {
                            targetName = prefix + node.getValue();
                        }

                        if (delProperty) {
                            // delete property element
                            String str = node.getValue();
                            if (str.lastIndexOf('.') != -1) {
                                str = str.substring(str.lastIndexOf('.') + 1);
                            }
                            try {
                                if (str != null) {
                                    ConfigSupport.deleteChild((ConfigBean)targetNode.parent(), (ConfigBean)targetNode);
                                    delPropertySuccess = true;
                                }
                            } catch (IllegalArgumentException ie) {
                                //fail(context,
                                //    "Could not delete the property: " +
                                //    ie.getMessage(), ie);
                                fail(context, localStrings.getLocalString("admin.set.delete.property.failure", "Could not delete the property: {0}",
                                    ie.getMessage()), ie);
                                return false;
                            } catch (TransactionFailure transactionFailure) {
                                //fail(context,
                                //    "Could not change the attributes: " +
                                //        transactionFailure.getMessage(),
                                //    transactionFailure);
                                fail(context, localStrings.getLocalString("admin.set.attribute.change.failure", "Could not change the attributes: {0}",
                                    transactionFailure.getMessage()),transactionFailure);
                                return false;
                            }
                        } else {
                            changes.put((ConfigBean) node.getKey(), attrChanges);
                        }

                    }
                }
            }
        }
        if (!changes.isEmpty()) {
            try {
                config.apply(changes);
                success(context, targetName, value);
                runLegacyChecks(context);
            } catch (TransactionFailure transactionFailure) {
                //fail(context, "Could not change the attributes: " +
                //        transactionFailure.getMessage(), transactionFailure);
                fail(context, localStrings.getLocalString("admin.set.attribute.change.failure", "Could not change the attributes: {0}",
                    transactionFailure.getMessage()),transactionFailure);
                return false;
            }

        } else {
            if (delPropertySuccess) {
                success(context, targetName, value);
            } else {
                //fail(context, "No configuration found for " + targetName);
                fail(context, localStrings.getLocalString("admin.set.configuration.notfound", "No configuration found for {0}", targetName));
                return false;
            }
        }
        return true;
    }

    private void runLegacyChecks(AdminCommandContext context) {
        final Collection<LegacyConfigurationUpgrade> list = habitat.getAllByContract(LegacyConfigurationUpgrade.class);
        for (LegacyConfigurationUpgrade upgrade : list) {
            upgrade.execute(context);
        }
    }

    /**
     * Find the rightmost unescaped occurrence of specified character in target
     * string.
     *
     * XXX Doesn't correctly interpret escaped backslash characters, e.g. foo\\.bar
     * 
     * @param target string to search
     * @param ch a character
     * @return index index of last unescaped occurrence of specified character
     *   or -1 if there are no unescaped occurrences of this character.
     */
    private static int trueLastIndexOf(String target, char ch) {
        int i = target.lastIndexOf(ch);
        while(i > 0) {
            if(target.charAt(i-1) == '\\') {
                i = target.lastIndexOf(ch, i-1);
            } else {
                break;
            }
        }
        return i;
    }

    /**
     * Indicate in the action report that the command failed.
     */
    private static void fail(AdminCommandContext context, String msg) {
        fail(context, msg, null);
    }

    /**
     * Indicate in the action report that the command failed.
     */
    private static void fail(AdminCommandContext context, String msg,
                                                        Exception ex) {
        context.getActionReport().setActionExitCode(
                                        ActionReport.ExitCode.FAILURE);
        if (ex != null)
            context.getActionReport().setFailureCause(ex);
        context.getActionReport().setMessage(msg);
    }

    /**
     * Indicate in the action report that the command succeeded and
     * include the target property and it's value in the report
     */
    private void success(AdminCommandContext context, String target, String value) {
        context.getActionReport().setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ActionReport.MessagePart part = context.getActionReport().getTopMessagePart().addChild();
        part.setChildrenType("DottedName");
        part.setMessage(target + "=" + value);
    }
}
