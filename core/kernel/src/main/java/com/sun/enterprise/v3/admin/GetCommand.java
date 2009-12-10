/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import java.util.*;
import org.jvnet.hk2.config.types.Property;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.Dom;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Proxy;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.glassfish.external.statistics.Statistic;
import org.glassfish.external.statistics.Stats;
import org.glassfish.external.statistics.impl.StatisticImpl;

/**
 * User: Jerome Dochez
 * Date: Jul 10, 2008
 * Time: 12:17:26 AM
 */
@Service(name="get")
@Scoped(PerLookup.class)
public class GetCommand extends V2DottedNameSupport implements AdminCommand {
    
    @Inject
    Domain domain;

    //How to define short option name?
    @Param(optional=true, defaultValue="false", shortName="m")
    Boolean monitor;

    @Param(primary = true)
    String pattern;

    @Inject(optional=true)
    private MonitoringRuntimeDataRegistry mrdr;

    private final String DOTTED_NAME = ".dotted-name";
    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(GetCommand.class);
    
    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();

        /* Issue 5918 Used in ManifestManager to keep output sorted */
        try {
            PropsFileActionReporter reporter = (PropsFileActionReporter) report;
            reporter.useMainChildrenAttribute(true);
        } catch(ClassCastException e) {
            // ignore this is not a manifest output.
        }
        
        if (monitor) {
            getMonitorAttributes(report);
            return;
        }

        //check for incomplete dotted name
        if(!pattern.equals("*")) {
            if (pattern.lastIndexOf(".") == -1 || pattern.lastIndexOf(".") == (pattern.length() - 1)) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                //report.setMessage("Missing expected dotted name part");
                report.setMessage(localStrings.getLocalString("missing.dotted.name", "Missing expected dotted name part"));
                return;
            }
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

        //No matches found - report the failure and return
        if (matchingNodes.isEmpty()) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            //report.setMessage("Dotted name path \"" + prefix + pattern + "\" not found.");
            report.setMessage(localStrings.getLocalString("admin.get.path.notfound", "Dotted name path {0} not found.", prefix + pattern));
            return;
        }

        List<Map.Entry> matchingNodesSorted = sortNodesByDottedName(matchingNodes);
        boolean foundMatch = false;
        for (Map.Entry<Dom, String> node : matchingNodesSorted) {
            // if we get more of these special cases, we should switch to a Renderer pattern
            if (Property.class.getName().equals(node.getKey().model.targetTypeName)) {
                 // special display for properties...
                if (matches(node.getValue(), pattern)) {
                    ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                    part.setChildrenType("DottedName");
                    part.setMessage(prefix + node.getValue() + "=" + encode(node.getKey().attribute("value")));
                    foundMatch = true;
                }
            }   else {
                Map<String, String> attributes = getNodeAttributes(node.getKey(), pattern);
                TreeMap<String, String> attributesSorted = new TreeMap(attributes);
                for (Map.Entry<String, String> name : attributesSorted.entrySet()) {
                    String finalDottedName = node.getValue()+"."+name.getKey();
                    if (matches(finalDottedName, pattern)) {
                        ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                        part.setChildrenType("DottedName");
                        part.setMessage(prefix + node.getValue() + "." + name.getKey() + "=" + name.getValue());
                        foundMatch = true;
                    }
                }
            }    
        }
        if (!foundMatch) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            //report.setMessage("No object found matching " + pattern);
            report.setMessage(localStrings.getLocalString("admin.get.path.notfound", "Dotted name path {0} not found.", prefix + pattern));

        }
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
    
    private void getMonitorAttributes(ActionReport report) {
        if ((pattern == null) || (pattern.equals(""))) {
            report.setActionExitCode(ExitCode.FAILURE);
            report.setMessage("match pattern is invalid or null");
            report.setMessage(localStrings.getLocalString("admin.get.invalid.pattern","Match pattern is invalid or null"));
            return;
        }

        if (mrdr==null) {
            report.setActionExitCode(ExitCode.FAILURE);
            //report.setMessage("monitoring facility not installed");
            report.setMessage(localStrings.getLocalString("admin.get.no.monitoring","Monitoring facility not installed"));
            return;
        }

        //Grab the monitoring tree root from habitat and get the attributes using pattern
        org.glassfish.flashlight.datatree.TreeNode tn = mrdr.get("server");
        if (tn == null) {
            //No monitoring data, so nothing to list
            report.setActionExitCode(ExitCode.SUCCESS);
            return;
        }

        TreeMap map = new TreeMap();
        List<org.glassfish.flashlight.datatree.TreeNode> ltn = tn.getNodes(pattern);
        boolean singleStat = false;

        if(ltn == null || ltn.isEmpty()) {
            org.glassfish.flashlight.datatree.TreeNode parent = tn.getPossibleParentNode(pattern);

            if(parent != null) {
                ltn = new ArrayList<org.glassfish.flashlight.datatree.TreeNode>(1);
                ltn.add(parent);
                singleStat = true;
            }
        }

        if(!singleStat)
            pattern = null; // signal to method call below

        for (org.glassfish.flashlight.datatree.TreeNode tn1 : sortTreeNodesByCompletePathName(ltn)) {
            if (!tn1.hasChildNodes()) {
                insertNameValuePairs(map, tn1, pattern);
            }
        }
        Iterator it = map.keySet().iterator();
        Object obj;
        while (it.hasNext()) {
          obj = it.next();
            ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setMessage(obj + " = " + map.get(obj));
        }
        report.setActionExitCode(ExitCode.SUCCESS);
    }

    private void insertNameValuePairs(
            TreeMap map, org.glassfish.flashlight.datatree.TreeNode tn1, String exactMatch){
        String name = tn1.getCompletePathName();
        Object value = tn1.getValue();
        if (tn1.getParent() != null) {
            map.put(tn1.getParent().getCompletePathName() + DOTTED_NAME,
                    tn1.getParent().getCompletePathName());
        }
        if (value instanceof Stats) {
            for (Statistic s: ((Stats)value).getStatistics()) {
                String statisticName = s.getName();
                if (statisticName != null) {
                    statisticName = s.getName().toLowerCase();
                }
                addStatisticInfo(s, name+"."+statisticName, map);
            }
        } else if (value instanceof Statistic) {
            addStatisticInfo(value, name, map);
        } else {
            map.put(name, value);
        }

        // IT 8985 bnevins
        // Hack to get single stats.  The code above above would take a lot of
        // time to unwind.  For development speed we just remove unwanted items
        // after the fact...

        if(exactMatch != null) {
            Object val = map.get(exactMatch);
            map.clear();

            if(val != null)
                map.put(exactMatch, val);
        }
    }

    private void addStatisticInfo(Object value, String name, TreeMap map) {
            Map<String,Object> statsMap;
            // Most likely we will get the proxy of the StatisticImpl,
            // reconvert that so you can access getStatisticAsMap method
            if (Proxy.isProxyClass(value.getClass())) {
                    statsMap = ((StatisticImpl)Proxy.getInvocationHandler(value)).getStaticAsMap();
            } else {
                statsMap = ((StatisticImpl)value).getStaticAsMap();
            }
            for (String attrName : statsMap.keySet()) {
                Object attrValue = statsMap.get(attrName);
                map.put(name + "-" + attrName, attrValue);
            }
    }
}
