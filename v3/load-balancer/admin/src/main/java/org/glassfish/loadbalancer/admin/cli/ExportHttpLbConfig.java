/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.loadbalancer.admin.cli;

import java.io.File;
import java.util.Date;
import java.io.FileOutputStream;

import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.LoadBalancer;
import org.glassfish.api.admin.AdminCommandContext;

import org.glassfish.loadbalancer.admin.cli.reader.api.LoadbalancerReader;

import java.util.HashSet;
import java.util.List;
import org.glassfish.api.admin.AdminCommand;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import org.glassfish.api.ActionReport;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.loadbalancer.admin.cli.reader.impl.LoadbalancerReaderImpl;
import org.glassfish.loadbalancer.admin.cli.helper.LbConfigHelper;

/**
 * Export load-balancer xml
 * 
 * @author Kshitiz Saxena
 */
@Service(name = "export-http-lb-config")
@Scoped(PerLookup.class)
@I18n("export.http.lb.config")
public class ExportHttpLbConfig implements AdminCommand {

    @Param(name = "lbtargets", separator = ',', optional = true)
    List<String> target;
    @Param(name = "config", optional = true)
    String lbConfigName;
    @Param(name = "lbname", optional = true)
    String lbName;
    @Param(name = "file_name", primary = true)
    String fileName;
    @Param(name = "property", optional = true, separator = ':')
    Properties properties;
    @Inject
    Domain domain;
    @Inject
    ApplicationRegistry appRegistry;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        try {
            process();
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Throwable t) {
            String msg = LbLogUtil.getStringManager().getString("ExportHttpLbConfigFailed", t.getMessage());
            LbLogUtil.getLogger().log(Level.WARNING, msg);
            if (LbLogUtil.getLogger().isLoggable(Level.FINE)) {
                LbLogUtil.getLogger().log(Level.FINE, "Exception when exporting http lb config", t);
            }
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(t.getMessage());
            report.setFailureCause(t);
        }
    }

    public void process() throws Exception {
        File f = new File(fileName);
        LoadbalancerReader lbr = null;
        if (lbName != null && lbConfigName == null && target == null) {
            LoadBalancer lb = LbConfigHelper.getLoadBalancer(domain, lbName);
            lbr = LbConfigHelper.getLbReader(domain, appRegistry, lb.getLbConfigName());
        } else if (lbConfigName != null && lbName == null && target == null) {
            lbr = LbConfigHelper.getLbReader(domain, appRegistry, lbConfigName);
        } else if (target != null && lbName == null && lbConfigName == null){
            Set<String> clusters = new HashSet<String>();
            clusters.addAll(target);
            lbr = new LoadbalancerReaderImpl(domain, appRegistry, clusters, properties);
        } else {
            String msg = LbLogUtil.getStringManager().getString("ExportHttpLbConfigInvalidArgs");
            throw new Exception(msg);
        }

        FileOutputStream fo = null;

        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            fo = new FileOutputStream(f);
            String footer = LbLogUtil.getStringManager().getString("GeneratedFileFooter",
                    new Date().toString());
            LbConfigHelper.exportXml(lbr, fo);
        } finally {
            if (fo != null) {
                fo.close();
                fo = null;
            }
        }
    }
}
