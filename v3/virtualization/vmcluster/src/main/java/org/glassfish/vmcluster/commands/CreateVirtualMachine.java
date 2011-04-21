/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.vmcluster.commands;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.vmcluster.config.Template;
import org.glassfish.vmcluster.config.Virtualization;
import org.glassfish.vmcluster.runtime.VirtualClusters;
import org.glassfish.vmcluster.spi.*;
import org.glassfish.vmcluster.util.RuntimeContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

import java.util.concurrent.Future;

/**
 * Creates a new virtual machine on a target machine
 * @author Jerome Dochez
 */
@Service(name="create-vm")
@Scoped(PerLookup.class)
public class CreateVirtualMachine implements AdminCommand {

    @Param(name="template")
    String templateName;

    @Param(name="group", optional = true)
    String groupName=null;

    @Param(name="cluster")
    String clusterName;

    @Inject
    GroupManagement groups;

    @Inject
    VirtualClusters clusters;

    @Inject
    Domain domain;

    @Override
    public void execute(AdminCommandContext context) {

        Group group;
        if (groupName==null) {
            group = groups.iterator().next();
        } else {
            group = groups.byName(groupName);
        }
        Virtualization virt = group.getConfig().getVirtualization();
        Template template = virt.templateByName(templateName);

        Cluster cluster = domain.getClusterNamed(clusterName);
        if (template==null) {
            context.getActionReport().failure(RuntimeContext.logger, "Template not registered " + templateName);
            return;
        }
        try {
            // multi-threading bug possible here, need a better way to allocated tokens.
            Iterable<Future<VirtualMachine>> vm = group.allocate(template, clusters.byName(clusterName), 1);
            for (Future<VirtualMachine> future : vm) {
                try {
                    future.get();
                } catch(Exception e) {
                    throw new VirtException(e);

                }
            }
        } catch(VirtException e) {
            context.getActionReport().failure(RuntimeContext.logger, e.getMessage(), e);
        }
    }
}
