/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.enterprise.config.serverbeans.SystemPropertyBag;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.Dom;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Delete System Property Command
 * 
 * Removes one system property of the domain, configuration, cluster, or server 
 * instance, at a time
 * 
 * Usage: delete-system-property [--terse=false] [--echo=false] [--interactive=true] 
 * [--host localhost] [--port 4848|4849] [--secure|-s=true] [--user admin_user] [
 * --passwordfile file_name] [--target target(Default server)] property_name
 * 
 */
@Service(name="delete-system-property")
@Scoped(PerLookup.class)
@I18n("delete.system.property")
public class DeleteSystemProperty implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteSystemProperty.class);

    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Param(name="property_name", primary=true)
    String propName;
    
    @Inject
    Domain domain;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        SystemPropertyBag spb;
        if ("domain".equals(target))
            spb = domain;
        else
            spb = domain.getServerNamed(target); //this is ok for now  (config is not a target as far as v3 FCS is concerned -- take it up later)
        if (spb == null) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            String msg = localStrings.getLocalString("invalid.target.sys.props",
                    "Invalid target:{0}. Valid targets are ''domain'' and a server named ''server'' (default).", target);
            report.setMessage(msg);
            return;
        }
        if(!spb.containsProperty(propName)) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            String msg = localStrings.getLocalString("no.such.property",
                    "System Property named {0} does not exist at the given target {1}", propName, target);
            report.setMessage(msg);
            return;
        }
        if (definitions(propName) == 1) { //implying user is deleting the "last" definition of this property
            List<String> refs = new ArrayList<String>();
            Dom root = Dom.unwrap(domain);
            String sysPropName = SystemPropertyConstants.getPropertyAsValue(propName);
            listRefs(root, sysPropName, refs);
            if (!refs.isEmpty()) {
                //there are some references
                String msg = localStrings.getLocalString("cant.delete.referenced.property",
                        "System Property {0} is referenced by {1} in the configuration. Please remove the references first.", propName, Arrays.toString(refs.toArray()));
                report.setMessage(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }
        //now we are sure that the target exits in the config, just remove the given property
        try {
            ConfigSupport.apply(new SingleConfigCode<SystemPropertyBag>() {
                public Object run(SystemPropertyBag param) throws PropertyVetoException, TransactionFailure {
                    param.getSystemProperty().remove(param.getSystemProperty(propName));
                    return param;
                }
            }, spb);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            String msg = localStrings.getLocalString("delete.sysprops.ok",
                    "System Property named {0} deleted from given target {1}. Make sure you check its references.", propName, target);
            report.setMessage(msg);
        } catch (TransactionFailure tf) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tf);
        }
    }

    private int definitions(String propName) {
        //are there multiple <system-property> definitions for the given name?
        int defs = 0;
        SystemPropertyBag bag = domain;
        if (bag.containsProperty(propName))
            defs++;
        bag = domain.getServerNamed(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME); //this is deliberate, as V3 has single server
        if (bag != null && bag.containsProperty(propName))
            defs++;
        return defs;
    }

    private static void listRefs(Dom dom, String value, List<String> refs) {
        //this method is rather ugly, but it works. See 9340 which presents a compatibility issue
        //frankly, it makes no sense to do an extensive search of all references of <system-property> being deleted,
        //but that's what resolution of this issue demands. --- Kedar 10/5/2009
        for (String aname : dom.getAttributeNames()) {
            String raw = dom.rawAttribute(aname);
            if (raw != null && raw.equals(value)) {
                refs.add(dom.model.getTagName() + ":" + aname);
            }
        }
        for (String ename : dom.getElementNames()) {
            List<Dom> nodes = null;
            try {
                nodes = dom.nodeElements(ename);
            } catch(Exception e) {
                //ignore, in some situations, HK2 might throw ClassCastException here
            }
            if (nodes != null) {
                for (Dom node : nodes)
                    listRefs(node, value, refs);  //beware: recursive call ...
            }
        }
    }
}
