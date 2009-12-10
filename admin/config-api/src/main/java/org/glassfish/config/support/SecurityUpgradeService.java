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

package org.glassfish.config.support;

import com.sun.enterprise.config.serverbeans.JaccProvider;
import com.sun.enterprise.config.serverbeans.SecurityService;
import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;


/**
 *The only thing that needs to added Extra for SecurityService migration
 * is the addition of the new JACC provider. This would be required when
 * migrating from V2, for V3-Prelude it is already present.
 *
 * The rest of the security related upgrade is handled implicitly by the actions of the
 * upgrade service itself.
 * 
 */

@Service
public class SecurityUpgradeService implements ConfigurationUpgrade, PostConstruct {

    @Inject
    private Habitat habitat;
    public void postConstruct() {
        upgradeJACCProvider();
    }

    private void upgradeJACCProvider() {
        try {
            final SecurityService  securityService = habitat.getComponent(SecurityService.class);
            if (securityService == null) {
                return;
            }
            List<JaccProvider> jaccProviders = securityService.getJaccProvider();
            for (JaccProvider jacc : jaccProviders) {
                if ("com.sun.enterprise.security.jacc.provider.SimplePolicyConfigurationFactory".equals(jacc.getPolicyConfigurationFactoryProvider())) {
                    //simple policy provider already present
                    return;
                }
            }
            ConfigSupport.apply(new SingleConfigCode<SecurityService>() {
                @Override
                public Object run(SecurityService secServ) throws PropertyVetoException, TransactionFailure {
                    JaccProvider jacc = secServ.createChild(JaccProvider.class);
                    //add the simple provider to the domain's security service
                    jacc.setName("simple");
                    jacc.setPolicyConfigurationFactoryProvider("com.sun.enterprise.security.jacc.provider.SimplePolicyConfigurationFactory");
                    jacc.setPolicyProvider("com.sun.enterprise.security.jacc.provider.SimplePolicyProvider");
                    secServ.getJaccProvider().add(jacc);
                    return secServ;
                }
            }, securityService);
        } catch (TransactionFailure ex) {
            Logger.getAnonymousLogger().log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

    }

}
