/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.security.cli;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.*;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.security.auth.realm.ldap.LDAPRealm;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;
import java.util.List;
import java.beans.PropertyVetoException;

/**  A convenience command to configure LDAP for administration. There are several properties and attributes that
 *   user needs to remember and that's rather user unfriendly. That's why this command is being developed.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish V3
 */
@Service(name="setup-ldap-for-admin")
public class LDAPAdminAccessConfigurator implements AdminCommand {

    @Param (name="basedn", shortName="b", optional=false)
    public volatile String basedn;
    
    @Param(name="new admin realm name", optional=true, primary=true)
    public volatile String name = "ldap-admin-realm";  // the default value is ldap-admin-realm

    @Param(name="url", shortName="u", optional=true)
    public volatile String url = "ldap://localhost:389"; // the default ports for LDAP on localhost

    @Param(name="ping", shortName="p", optional=true, defaultValue="false")
    public volatile Boolean ping = Boolean.FALSE;

    @Inject
    Configs allConfigs;

    private final static String ADMIN_SERVER = "server"; //this needs to be at central place, oh well
    private static final StringManager lsm = StringManager.getManager(LDAPAdminAccessConfigurator.class);
    private static final String DIR_P    = "directory";
    private static final String BASEDN_P = "base-dn";
    private static final String JAAS_P   = "jaas-context";
    private static final String JAAS_V   = "ldapRealm";
    
    public void execute(AdminCommandContext context) {
        ActionReport rep = context.getActionReport();
        StringBuilder sb = new StringBuilder();
        if(url != null) {
            if (!url.startsWith("ldap://") && !url.startsWith("ldaps://")) {
                url += "ldap://" + url;        //it's ok to accept just host:port
            }
        }
        pingLDAP(sb);
        try {
            configure(sb);
            rep.setMessage(sb.toString());
            rep.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch(TransactionFailure tf) {
            rep.setMessage(tf.getMessage());
            rep.setActionExitCode(ActionReport.ExitCode.FAILURE);
        } catch (PropertyVetoException e) {
            rep.setMessage(e.getMessage());
            rep.setActionExitCode(ActionReport.ExitCode.FAILURE);        }
    }

    private void configure(StringBuilder sb) throws TransactionFailure, PropertyVetoException {
        Server s = ConfigBeansUtilities.getServerNamed(ADMIN_SERVER);
        String ac = s.getConfigRef();
        Config asc = null; //admin server config, that needs the configuration
        for (Config cfg : allConfigs.getConfig()) {
            if (cfg.getName().equals(ac)) {
                asc = cfg;
                break;
            }
        }
        //if (asc == null) --> no admin server config, we are in biig trouble, it's almost an assertion that this is non-null
        if (realmExists(asc)) {
            sb.append(lsm.getString("realm.exists", name)); //do nothing
            return;
        }
        //following things should happen transactionally - TODO replace SingleConfigCode by ConfigCode ...
        createIt(asc.getSecurityService(), sb);
        List<JmxConnector> cs = asc.getAdminService().getJmxConnector();
        JmxConnector sys = null;
        for (JmxConnector c : cs) {
            if ("system".equals(c.getName())) {
                sys = c;
                break;
            }
        }
        if (sys != null)
            configureJmxConnector(sys);
        configureAdminRealmProperty(asc.getAdminService());
    }

    private void configureAdminRealmProperty(AdminService as) throws PropertyVetoException, TransactionFailure {
        SingleConfigCode<AdminService> scc = new SingleConfigCode<AdminService>() {
            public Object run(AdminService as) {
                as.setAdminRealmName(name); 
                return true;
            }
        };
    }

    private void createIt(SecurityService ss, final StringBuilder sb) throws TransactionFailure {
        SingleConfigCode<SecurityService> scc = new SingleConfigCode<SecurityService>() {
            public Object run(SecurityService ss) throws PropertyVetoException, TransactionFailure {
                AuthRealm ldapr = createLDAPRealm(ss);
                ss.getAuthRealm().add(ldapr);
                appendNL(sb,lsm.getString("ldap.realm.setup", name));
                return true;
            }
        };
        ConfigSupport.apply(scc, ss);
    }

    private void configureJmxConnector(JmxConnector jc) throws PropertyVetoException, TransactionFailure {
        SingleConfigCode<JmxConnector> scc = new SingleConfigCode<JmxConnector>() {
            public Object run(JmxConnector jc) throws PropertyVetoException, TransactionFailure {
                jc.setAuthRealmName(name);
                return true;
            }
        };
        ConfigSupport.apply(scc, jc);
    }

    private AuthRealm createLDAPRealm(SecurityService ss) throws TransactionFailure, PropertyVetoException {
        AuthRealm ar = ss.createChild(AuthRealm.class);
        ar.setClassname(LDAPRealm.class.getName());
        ar.setName(name);
        List<Property> props = ar.getProperty();

        Property p = ar.createChild(Property.class);
        p.setName(DIR_P);
        p.setValue(url);
        props.add(p);

        p = ar.createChild(Property.class);
        p.setName(BASEDN_P);
        p.setValue(basedn);
        props.add(p);

        p = ar.createChild(Property.class);
        p.setName(JAAS_P);
        p.setValue(JAAS_V);
        props.add(p);
        
        return ar;
    }

    private void pingLDAP(StringBuilder sb) {
        if (!ping) {
            appendNL(sb,lsm.getString("ldap.noping", url));
            return;
        }
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, url);
        try {
            new InitialContext(env);
            appendNL(sb,lsm.getString("ldap.ok", url));
        } catch(Exception e) {
            appendNL(sb,lsm.getString("ldap.na", url, e.getClass().getName(), e.getMessage()));
        }
    }

    private boolean realmExists(Config cfg) {
        List<AuthRealm> realms = cfg.getSecurityService().getAuthRealm();
        for (AuthRealm ar : realms) {
            if (ar.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static void appendNL(StringBuilder sb, String s) {
        sb.append(s).append("%%%EOL%%%");
    }
}
