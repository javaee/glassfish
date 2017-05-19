/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.admin.rest.cli;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.realm.RealmsManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.v3.common.ActionReporter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.config.types.Property;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * returns the list of targets
 *
 * @author ludovic Champenois
 */
@Service(name = "__list-group-names")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn({RuntimeType.DAS})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,
    CommandTarget.CLUSTER, CommandTarget.CONFIG,CommandTarget.CLUSTERED_INSTANCE})
@RestEndpoints({
    @RestEndpoint(configBean=AuthRealm.class,
        opType=RestEndpoint.OpType.GET, 
        path="list-group-names", 
        description="List Group Names",
        params={
            @RestParam(name="realmName", value="$parent")
        })
})
public class GetGroupNamesCommand implements AdminCommand {
    @Inject
    com.sun.enterprise.config.serverbeans.Domain domain;
    
    //TODO: for consistency with other commands dealing with realms
    //uncomment this below.
    //@Param(name="authrealmname")
    @Param
    String realmName;

    @Param
    String userName;


    @Param(name = "target", primary=true, optional = true, defaultValue =
    SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    private String target;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject
    private Configs configs;

    @Inject
    RealmsManager realmsManager;

    private static final LocalStringManagerImpl _localStrings =
	new LocalStringManagerImpl(GetGroupNamesCommand.class);

    @Override
    public void execute(AdminCommandContext context) {
        Config tmp = null;
        try {
            tmp = configs.getConfigByName(target);
        } catch (Exception ex) {
        }

        if (tmp != null) {
            config = tmp;
        }
        if (tmp == null) {
            Server targetServer = domain.getServerNamed(target);
            if (targetServer != null) {
                config = domain.getConfigNamed(targetServer.getConfigRef());
            }
            com.sun.enterprise.config.serverbeans.Cluster cluster = domain.getClusterNamed(target);
            if (cluster != null) {
                config = domain.getConfigNamed(cluster.getConfigRef());
            }
        }

        ActionReporter report = (ActionReporter) context.getActionReport();
        try {
            String[] list = getGroupNames(realmName, userName);
            List<String> ret = Arrays.asList(list);
            report.setActionExitCode(ExitCode.SUCCESS);
            Properties props = new Properties();
            props.put("groups", ret);
            report.setExtraProperties(props);
            report.setMessage("" + ret);
        } catch (NoSuchRealmException ex) {
            report.setFailureCause(ex);
            report.setActionExitCode(ExitCode.FAILURE);
        } catch (BadRealmException ex) {
            report.setFailureCause(ex);
            report.setActionExitCode(ExitCode.FAILURE);
        } catch (InvalidOperationException ex) {
            report.setFailureCause(ex);
            report.setActionExitCode(ExitCode.FAILURE);
        } catch (NoSuchUserException ex) {
            report.setFailureCause(ex);
            report.setActionExitCode(ExitCode.FAILURE);
        }
       
    }

    private String[] getGroupNames(String realmName, String userName)
            throws NoSuchRealmException, BadRealmException,
            InvalidOperationException, NoSuchUserException {
        //account for updates to file-realm contents from outside this config 
        //which are sharing the same keyfile
        realmsManager.refreshRealm(config.getName(), realmName);
        Realm r = realmsManager.getFromLoadedRealms(config.getName(), realmName);
        if (r != null) {
            return getGroupNames(r, userName);
        }
        List<AuthRealm> authRealmConfigs = config.getSecurityService().getAuthRealm();
        for (AuthRealm authRealm : authRealmConfigs) {
            if (realmName.equals(authRealm.getName())) {
                List<Property> propConfigs = authRealm.getProperty();
                Properties props = new Properties();
                for (Property p : propConfigs) {
                    String value = p.getValue();
                    props.setProperty(p.getName(), value);
                }
                r = Realm.instantiate(authRealm.getName(), authRealm.getClassname(), props, config.getName());
                return getGroupNames(r, userName);
            }
        }
        throw new NoSuchRealmException(
                _localStrings.getLocalString("NO_SUCH_REALM", "No Such Realm: {0}",
                new Object[] {realmName}));
    }

    private String[] getGroupNames(Realm r, String userName) 
            throws InvalidOperationException, NoSuchUserException {
        List<String> l = new ArrayList<String>();
        Enumeration<String> groupNames = r.getGroupNames(userName);
        while (groupNames.hasMoreElements()) {
            l.add(groupNames.nextElement());
        }
        return (String[]) l.toArray(new String[l.size()]);

    }

}
