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

import org.glassfish.api.embedded.EmbeddedContainer;
import org.glassfish.api.embedded.Port;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.beans.PropertyVetoException;

import com.sun.enterprise.config.serverbeans.EjbContainer;
import com.sun.enterprise.config.serverbeans.Config;

/**
 * @author Jerome Dochez
 */
public class EmbeddedEjbContainer implements EmbeddedContainer {

    final Habitat habitat;
    

    EmbeddedEjbContainer(EjbBuilder builder) {
        this.habitat = builder.habitat;
    }

    public void bind(Port port, String protocol) {

    }

    public List<Sniffer> getSniffers() {
        List<Sniffer> sniffers = new ArrayList<Sniffer>();
        addSniffer(sniffers, "Ejb");
        addSniffer(sniffers, "Security");
        addSniffer(sniffers, "jpa");
        addSniffer(sniffers, "jpaCompositeSniffer");
        addSniffer(sniffers, "ear");
        addSniffer(sniffers, "weld");
        return sniffers;

    }

    private void addSniffer(List<Sniffer> sniffers, String name) {
        Sniffer sniffer = habitat.getComponent(Sniffer.class, name);
        if (sniffer != null) {
            sniffers.add(sniffer);
        }
    }

    public void start() throws LifecycleException {
    }

    public void stop() throws LifecycleException {

    }

}
