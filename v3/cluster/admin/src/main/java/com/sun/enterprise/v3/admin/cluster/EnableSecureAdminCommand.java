/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.SecureAdmin;
import com.sun.enterprise.security.ssl.SSLUtils;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;

/**
 * Records that secure admin is to be used and adjusts each admin listener
 * configuration in the domain to use secure admin.
 *
 * The command changes the admin-listener set-up within each separate
 * configuration as if by running
 * these commands:
 * <pre>
 * {@code
        ###
	### onEnable new protocol for secure admin
	###
	asadmin onEnable-protocol --securityenabled=true sec-admin-listener
	asadmin onEnable-http --default-virtual-server=__asadmin sec-admin-listener
	#asadmin onEnable-network-listener --listenerport 4849 --protocol sec-admin-listener sec-admin-listener
	asadmin onEnable-ssl --type network-listener --certname s1as --ssl2enabled=false --ssl3enabled=false --clientauthenabled=false sec-admin-listener
        asadmin set configs.config.server-config.network-config.protocols.protocol.sec-admin-listener.ssl.client-auth=want
	asadmin set configs.config.server-config.network-config.protocols.protocol.sec-admin-listener.ssl.classname=com.sun.enterprise.security.ssl.GlassfishSSLImpl
	asadmin set configs.config.server-config.security-service.message-security-config.HttpServlet.provider-config.GFConsoleAuthModule.property.restAuthURL=https://localhost:4848/management/sessions


	###
	### onEnable the port redirect config
	###
	asadmin onEnable-protocol --securityenabled=false admin-http-redirect
	asadmin onEnable-http-redirect --secure-redirect true admin-http-redirect
	#asadmin onEnable-http-redirect --secure-redirect true --redirect-port 4849 admin-http-redirect
	asadmin onEnable-protocol --securityenabled=false pu-protocol
	asadmin onEnable-protocol-finder --protocol pu-protocol --targetprotocol sec-admin-listener --classname com.sun.grizzly.config.HttpProtocolFinder http-finder
	asadmin onEnable-protocol-finder --protocol pu-protocol --targetprotocol admin-http-redirect --classname com.sun.grizzly.config.HttpProtocolFinder admin-http-redirect

	###
	### update the admin listener
	###
	asadmin set configs.config.server-config.network-config.network-listeners.network-listener.admin-listener.protocol=pu-protocol
 * }
 *
 *
 * @author Tim Quinn
 */
@Service(name = "enable-secure-admin")
@Scoped(PerLookup.class)
@I18n("enable.secure.admin.command")
@ExecuteOn(RuntimeType.ALL)
public class EnableSecureAdminCommand extends SecureAdminCommand {

    @Param(optional = true)
    public String adminalias;

    @Param(optional = true)
    public String instancealias;

    @Inject
    private SSLUtils sslUtils;

    private KeyStore keystore = null;

    @Override
    Iterator<Work<TopLevelContext>> secureAdminSteps() {
        return stepsIterator(secureAdminSteps);
    }

    @Override
    Iterator<Work<ConfigLevelContext>> perConfigSteps() {
        return stepsIterator(perConfigSteps);
    }

    /**
     * Iterator which returns array elements from front to back.
     * @param <T>
     * @param steps
     * @return
     */
    private <T  extends SecureAdminCommand.Context> Iterator<Work<T>> stepsIterator(Step<T>[] steps) {
        return new Iterator<Work<T>> () {
            private Step<T>[] steps;
            private int nextSlot;

            @Override
            public boolean hasNext() {
                return nextSlot < steps.length;
            }

            @Override
            public Work<T> next() {
                return steps[nextSlot++].enableWork();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            Iterator<Work<T>> init(Step<T>[] values) {
                this.steps = values;
                nextSlot  = 0;
                return this;
            }

        }.init(steps);
    }
    
    @Override
    protected boolean updateSecureAdminSettings(
            final SecureAdmin secureAdmin_w,
            final ActionReport actionReport) {
        /*
         * Apply the values for the aliases, if the user provided them on the
         * command invocation.
         */
        try {
            final List<String> badAliases = new ArrayList<String>();
            if (adminalias != null) {
                if ( ! validateAlias(adminalias)) {
                    badAliases.add(adminalias);
                } else {
                    secureAdmin_w.setDasAlias(adminalias);
                }
            }
            if (instancealias != null) {
                if ( ! validateAlias(instancealias)) {
                    badAliases.add(instancealias);
                } else {
                    secureAdmin_w.setInstanceAlias(instancealias);
                }
            }
            if (badAliases.size() > 0) {
                actionReport.failure(logger, Strings.get("enable.secure.admin.badAlias",
                        badAliases.size(), badAliases.toString()));
                return false;
            }
            return true;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected String transactionErrorMessageKey() {
        return "enable.secure.admin.errenable";
    }

    private synchronized KeyStore keyStore() throws IOException {
        if (keystore == null) {
            keystore = sslUtils.getKeyStore();
        }
        return keystore;
    }

    private boolean validateAlias(final String alias) throws IOException, KeyStoreException  {
        return keyStore().containsAlias(alias);
    }
}
