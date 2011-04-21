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
package org.glassfish.vmcluster.virtmgt.impl;

import org.glassfish.vmcluster.spi.PhysicalGroup;
import org.glassfish.vmcluster.spi.GroupManagement;
import org.glassfish.vmcluster.virtmgt.GroupAccess;
import org.glassfish.vmcluster.virtmgt.GroupsAccess;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Injector;
import org.jvnet.hk2.component.PostConstruct;

import java.util.HashMap;
import java.util.Map;

/**
 * Gateway to group access instances.
 * @author Jerome Dochez
 */
@Service
public class GroupsAccessImpl implements PostConstruct, GroupsAccess {

    final Map<String, GroupAccess> groups = new HashMap<String, GroupAccess>();

    @Inject
    GroupManagement groupMgt;

    @Inject
    Injector injector;

    @Override
    public Iterable<GroupAccess> groups() {
        return groups.values();
    }

    @Override
    public GroupAccess byName(String name) {
        return groups.get(name);
    }

    @Override
    public void postConstruct() {
        // all configured groups should be accessible as a group access instance.
        for (PhysicalGroup group : groupMgt) {
            groups.put(group.getName(), LocalGroupAccess.from(injector, group));
        }
    }
}
