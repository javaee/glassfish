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

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SecureAdmin;
import java.beans.PropertyVetoException;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.RetryableException;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Provides common behavior for the enable and disable secure admin commands.
 * <p>
 * Those concrete classes implement the abstract methods, which this class
 * then invokes at the right times.
 * <p>
 * Concrete implementations should implement
 * <ul>
 * <li>{@link #updateSecureAdminSettings} - to adjust the secure-admin configuration
 * setting (whether secure-admin is enabled and, during enable-secure-admin,
 * the admin and instance alias values)
 * <li>{@link #updateAdminListenerConfig} - to adjust the admin-listener configuration
 * for a single Configuration in the domain. This method is invoked once for
 * each configuration in the domain because secure admin is a domain-wide
 * setting.
 * <el>
 *
 * @author Tim Quinn
 */
public abstract class SecureAdminCommand implements AdminCommand {

    @Inject
    protected Domain domain;

    @Inject
    protected Configs configs;

    /**
     * Updates the secure-admin settings in the configuration.
     * <p>
     * Note that changes to the Grizzly config for the admin listener occurs
     * in updateAdminListenerConfig, not in this method.
     *
     * @param secureAdmin_w
     */
    protected abstract void updateSecureAdminSettings(final SecureAdmin secureAdmin_w);

    protected abstract void updateAdminListenerConfig(
            final Transaction transaction,
            final Config config,
            final MessagePart partForThisConfig);

    protected abstract String transactionErrorMessageKey();

    protected void updateSecureAdminSettings(final SecureAdmin secureAdmin_w,
            final boolean newEnabledValue) {
        secureAdmin_w.setEnabled(Boolean.toString(newEnabledValue));
    }

    protected final SecureAdmin getWriteableSecureAdmin(final Transaction t, final Domain d) throws TransactionFailure {
        /*
         * Create the secure admin node if it is not already there.
         */
        SecureAdmin secureAdmin_w;
        SecureAdmin secureAdmin = d.getSecureAdmin();
        if (secureAdmin == null) {
            secureAdmin_w = d.createChild(SecureAdmin.class);
            d.setSecureAdmin(secureAdmin_w);
        } else {
            secureAdmin_w = t.enroll(secureAdmin);
        }
        return secureAdmin_w;
    }

    /**
     * Executes the particular xxx-secure-admin command (enable or disable).
     * @param context
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        try {
            ConfigSupport.apply(new SingleConfigCode<Domain>() {
                @Override
                public Object run(Domain d) throws PropertyVetoException, TransactionFailure {

                    // get the transaction
                    final Transaction t = Transaction.getTransaction(d);
                    if (t!=null) {

                        try {
                            final SecureAdmin secureAdmin_w = getWriteableSecureAdmin(t, d);

                            /*
                             * Delegate to the concrete implementation to
                             * adjust the secure-admin information.
                             */
                            updateSecureAdminSettings(secureAdmin_w);

                            for (Config c : configs.getConfig()) {
                                final MessagePart partForThisConfig = report.getTopMessagePart().addChild();

                                /*
                                 * Again, delegate to update the admin
                                 * listener configuration.
                                 */
                                updateAdminListenerConfig(t, c, partForThisConfig);
                            }

                            t.commit();
                        } catch (RetryableException ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    return Boolean.TRUE;
                }
            }, domain);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (TransactionFailure ex) {
            report.failure(context.getLogger(), Strings.get(transactionErrorMessageKey()), ex);
        }
    }
}
