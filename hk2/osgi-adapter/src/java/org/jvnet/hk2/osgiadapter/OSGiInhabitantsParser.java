/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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


package org.jvnet.hk2.osgiadapter;

import static org.jvnet.hk2.osgiadapter.Logger.logger;
import static com.sun.hk2.component.InhabitantsFile.CLASS_KEY;
import static com.sun.hk2.component.InhabitantsFile.INDEX_KEY;

import com.sun.hk2.component.*;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.Inhabitant;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Parses <tt>/META-INF/inhabitants</tt> and
 * populate {@link org.jvnet.hk2.component.Habitat}.
 * and registers services in OSGi Service Registry.
 * 
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiInhabitantsParser extends com.sun.hk2.component.InhabitantsParser {

    /**
     * When an HK2 service is defined with a name (using the name attribute
     * of {@link org.jvnet.hk2.annotations.Service} annotation, then
     * the corresponding OSGi service is registered with the following
     * property name.
     * See {@link org.osgi.framework.ServiceRegistration#setProperties}
     */
    public static final String SERVICE_NAME = "ServiceName";

    private BundleContext osgiCtx;

    public OSGiInhabitantsParser(Habitat habitat, BundleContext ctx) {
        super(habitat);
        this.osgiCtx = ctx;
    }

    /**
     * This method registers an equivalent service in OSGi's Service Registry.
     * The registered service would be registered with HK2 Service class name
     * in addition to all the contractNames.
     * @param i Inhabitant which is being exposed as service object
     * @param parser the inhabitant metadata information
     */    
    @Override
    protected void add(Inhabitant i, InhabitantParser parser) {
        super.add(i, parser);
        List<String> fqcnContractNames = new ArrayList<String>();

        for (String contract : parser.getIndexes()) {
            int idx = contract.indexOf(':');
            if(idx==-1) {
                // no name
                fqcnContractNames.add(contract);
            } else {
                // v=contract:name
                fqcnContractNames.add(contract.substring(0, idx));
            }
        }
        fqcnContractNames.add(i.typeName());
        Properties props = new Properties();
        // todo : check what service name should be applied, watch out index can have
        // difference service name like for companionOf
        /**if (parser.getServiceName()!=null) {
            props.setProperty(SERVICE_NAME, parser.getServiceName());
        }  */
        // TODO: Map metadata to properties
        ServiceRegistration reg = osgiCtx.registerService(
                fqcnContractNames.toArray(new String[0]),
                new InhabitantServiceFactory(i),
                props);
        logger.logp(Level.INFO, "InhabitantsParser", "registerOSGiService",
                "reg = {0}", reg);
    }


    static class InhabitantServiceFactory implements ServiceFactory {
        private Inhabitant i;
        public InhabitantServiceFactory(Inhabitant i) {
            this.i = i;
        }

        public Object getService(Bundle bundle, ServiceRegistration registration) {
            return i.get();
        }

        public void ungetService(
                Bundle bundle, ServiceRegistration registration, Object service) {
            i = null;
        }
    }

}
