/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package org.glassfish.ejb.embedded;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.embedded.Port;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.api.admin.ServerEnvironment;
import com.sun.enterprise.config.serverbeans.EjbContainer;
import com.sun.enterprise.config.serverbeans.Config;

import java.beans.PropertyVetoException;

/**
 * @author Jerome Dochez
 */
@Service(name="ejb")
public class EjbBuilder implements ContainerBuilder<EmbeddedEjbContainer> {

    @Inject
    Habitat habitat;    

    @Inject(optional=true)
    EjbContainer ejbConfig=null;

    @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    volatile EmbeddedEjbContainer instance=null;
    

    public synchronized EmbeddedEjbContainer create(org.glassfish.api.embedded.Server server) {
        if (instance==null) {
            instance =  new EmbeddedEjbContainer(this);
        }
        return instance;
    }

    EjbContainer getConfig() {
        if (ejbConfig==null) {
            try {
                ConfigSupport.apply(new SingleConfigCode<Config>() {
                    public Object run(Config c) throws PropertyVetoException, TransactionFailure {
                        EjbContainer ejb = c.createChild(EjbContainer.class);
                        c.setEjbContainer(ejb);
                        return ejb;
                    }
                }, config);
            } catch(TransactionFailure e) {
                e.printStackTrace();
            }
            ejbConfig = config.getEjbContainer();
        }
        return ejbConfig;
    }

}

