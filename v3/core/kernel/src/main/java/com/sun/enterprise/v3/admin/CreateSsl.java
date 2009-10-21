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

import java.util.List;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.IiopService;
import com.sun.enterprise.config.serverbeans.SslClientConfig;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.Protocol;
import com.sun.grizzly.config.dom.Ssl;
import java.beans.PropertyVetoException;
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


/**
 * Create Ssl Command
 *
 * Usage: create-ssl --type [http-listener|iiop-listener|iiop-service]
 * --certname cert_name [--ssl2enabled=false] [--ssl2ciphers ssl2ciphers]
 * [--ssl3enabled=true] [--ssl3tlsciphers ssl3tlsciphers] [--tlsenabled=true]
 * [--tlsrollbackenabled=true] [--clientauthenabled=false]
 * [--target target(Default server)] [listener_id]
 *
 * domain.xml element example
 * <ssl cert-nickname="s1as" client-auth-enabled="false" ssl2-enabled="false"
 * ssl3-enabled="true" tls-enabled="true" tls-rollback-enabled="true"/>
 *
 * @author Nandini Ektare
 */

@Service(name="create-ssl")
@Scoped(PerLookup.class)
@I18n("create.ssl")
public class CreateSsl implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CreateSsl.class);

    @Param(name="certname")
    String certName;

    @Param(name="type", acceptableValues="network-listener, http-listener, iiop-listener, iiop-service")
    String type;

    @Param(name="ssl2enabled", optional=true, defaultValue="true")
    Boolean ssl2Enabled;

    @Param(name="ssl2ciphers", optional=true)
    String ssl2ciphers;

    @Param(name="ssl3enabled", optional=true, defaultValue="true")
    Boolean ssl3Enabled;

    @Param(name="ssl3tlsciphers", optional=true)
    String ssl3tlsciphers;

    @Param(name="tlsenabled", optional=true, defaultValue="true")
    Boolean tlsenabled;

    @Param(name="tlsrollbackenabled", optional=true, defaultValue="true")
    Boolean tlsrollbackenabled;

    @Param(name="clientauthenabled", optional=true, defaultValue="true")
    Boolean clientauthenabled;

    @Param(optional=true)
    String target;

    @Param(name="listener_id",primary=true,optional=true)
    String listenerId;

    @Inject
    Configs configs;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        if (!type.equals("iiop-service")) {
            if (listenerId == null) {
                report.setMessage(
                    localStrings.getLocalString(
                        "create.ssl.listenerid.missing",
                        "Listener id needs to be specified"));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        List <Config> configList = configs.getConfig();
        Config config = configList.get(0);

        if ("http-listener".equals(type) || "network-listener".equals(type))
            addSslToNetworkListener(config, report);
        else if ("iiop-listener".equals(type))
            addSslToIIOPListener(config, report);
        else if ("iiop-service".equals(type))
            addSslToIIOPService(config, report);
    }

    private void addSslToIIOPListener(Config config, ActionReport report) {
        IiopService iiopService = config.getIiopService();

        // ensure we have the specified listener
        IiopListener iiopListener = null;
        for (IiopListener listener  : iiopService.getIiopListener()) {
            if (listener.getId().equals(listenerId))
                iiopListener = listener;
        }

        if (iiopListener == null) {
            report.setMessage(
                localStrings.getLocalString("create.ssl.iiop.notfound",
                  "IIOP Listener named {0} to which this ssl element is " +
                  "being added does not exist.", listenerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (iiopListener.getSsl() != null) {
            report.setMessage(
                localStrings.getLocalString("create.ssl.iiop.alreadyExists",
                "IIOP Listener named {0} to which this ssl element is " +
                "being added already has an ssl element.", listenerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<IiopListener>() {

                public Object run(IiopListener param)
                throws PropertyVetoException, TransactionFailure {
                    Ssl newSsl = param.createChild(Ssl.class);
                    populateSslElement(newSsl);
                    param.setSsl(newSsl);
                    return newSsl;                }
            }, iiopListener);

        } catch(TransactionFailure e) {
            reportError(report, e);
        }
        reportSuccess(report);
    }

    private void addSslToIIOPService(Config config, ActionReport report) {
        IiopService iiopSvc = config.getIiopService();

        if (iiopSvc.getSslClientConfig() != null) {
            report.setMessage(
                localStrings.getLocalString(
                    "create.ssl.iiopsvc.alreadyExists", "IIOP Service " +
                    "already has been configured with SSL configuration."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<IiopService>() {
                public Object run(IiopService param) 
                throws PropertyVetoException, TransactionFailure {
                    SslClientConfig newSslClientCfg =
                        param.createChild(SslClientConfig.class);
                    Ssl newSsl = newSslClientCfg.createChild(Ssl.class);
                    populateSslElement(newSsl);
                    newSslClientCfg.setSsl(newSsl);
                    param.setSslClientConfig(newSslClientCfg);
                    return newSsl;
                }
            }, iiopSvc);

        } catch(TransactionFailure e) {
            reportError(report, e);
        }
    }

    private void addSslToNetworkListener(Config config, ActionReport report) {
        NetworkConfig netConfig = config.getNetworkConfig();

        // ensure we have the specified listener
        NetworkListener listener = netConfig.getNetworkListener(listenerId);

        if (listener == null) {
            report.setMessage(
                localStrings.getLocalString("create.ssl.http.notfound",
                  "Network Listener named {0} to which this ssl element is " +
                  "being added does not exist.", listenerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        Ssl ssl = listener.findHttpProtocol().getSsl();
        if (ssl != null) {
            report.setMessage(
                localStrings.getLocalString("create.ssl.http.alreadyExists",
                  "Network Listener named {0} to which this ssl element is " +
                  "being added already has an ssl element.", listenerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                public Object run(Protocol param) throws TransactionFailure {
                    Ssl newSsl = param.createChild(Ssl.class);
                    populateSslElement(newSsl);
                    param.setSsl(newSsl);
                    return newSsl;
                }
            }, listener.findHttpProtocol());

        } catch(TransactionFailure e) {
            reportError(report, e);
        }
        reportSuccess(report);
    }

    private void reportError(ActionReport report, TransactionFailure e) {
        report.setMessage(localStrings.getLocalString("create.ssl.fail",
                "Creation of Ssl in {0} failed", listenerId));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        report.setFailureCause(e);
    }

    private void reportSuccess(ActionReport report) {
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private void populateSslElement(Ssl newSsl) {
        newSsl.setCertNickname(certName);
        newSsl.setClientAuthEnabled(clientauthenabled.toString());
        newSsl.setSsl2Ciphers(ssl2ciphers);
        newSsl.setSsl2Enabled(ssl2Enabled.toString());
        newSsl.setSsl3Enabled(ssl3Enabled.toString());
        newSsl.setSsl3TlsCiphers(ssl3tlsciphers);
        newSsl.setTlsEnabled(tlsenabled.toString());
        newSsl.setTlsRollbackEnabled(tlsrollbackenabled.toString());
    }
}