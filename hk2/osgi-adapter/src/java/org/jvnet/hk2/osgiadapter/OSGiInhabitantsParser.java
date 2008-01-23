/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
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


package org.jvnet.hk2.osgiadapter;

import static org.jvnet.hk2.osgiadapter.Logger.logger;
import static com.sun.hk2.component.InhabitantsFile.CLASS_KEY;
import static com.sun.hk2.component.InhabitantsFile.INDEX_KEY;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.Inhabitant;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.BundleContext;
import com.sun.hk2.component.InhabitantsScanner;
import com.sun.hk2.component.Holder;
import com.sun.hk2.component.KeyValuePairParser;
import com.sun.hk2.component.LazyInhabitant;

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

    /*
     * TODO (Sahoo): Refactor this to be a subclass of hk2.InhabitantsParser
     */

    /**
     * When an HK2 service is defined with a name (using the name attribute
     * of {@link org.jvnet.hk2.annotations.Service} annotation, then
     * the corresponding OSGi service is registered with the following
     * property name.
     * See {@link org.osgi.framework.ServiceRegistration#setProperties}
     */
    public static final String SERVICE_NAME = "ServiceName";

    private final Habitat habitat;
    private BundleContext osgiCtx;

    public OSGiInhabitantsParser(Habitat habitat, BundleContext ctx) {
        super(habitat);
        this.habitat = habitat;
        this.osgiCtx = ctx;
    }

    public void parse(InhabitantsScanner scanner, Holder<ClassLoader> classLoader) throws IOException {
        for( KeyValuePairParser kvpp : scanner) {
            String className=null;
            List<String> contractNames = new ArrayList<String>();
            String serviceName = null;
            MultiMap<String,String> metadata=null;

            while(kvpp.hasNext()) {
                kvpp.parseNext();

                if(kvpp.getKey().equals(CLASS_KEY)) {
                    className = kvpp.getValue();
                    continue;
                }
                if(kvpp.getKey().equals(INDEX_KEY))
                    continue; // will process this after creating Inhabitant

                if(metadata==null)
                    metadata = new MultiMap<String,String>();
                metadata.add(kvpp.getKey(),kvpp.getValue());
            }

            Inhabitant i = new LazyInhabitant(habitat, classLoader, className,metadata);
            habitat.add(i);

            for (String v : kvpp.findAll(INDEX_KEY)) {
                // store index information to metadata
                if(metadata==null)
                    metadata = new MultiMap<String,String>();
                metadata.add(INDEX_KEY,v);

                // register inhabitant to the index
                int idx = v.indexOf(':');
                if(idx==-1) {
                    // no name
                    habitat.addIndex(i,v,null);
                    contractNames.add(v);
                } else {
                    // v=contract:name
                    String contract = v.substring(0, idx);
                    String name = v.substring(idx + 1);
                    habitat.addIndex(i, contract, name);
                    contractNames.add(name);
                    metadata.add(contract,name);
                    if (serviceName!=null) {
                        assert(serviceName.equals(name));
                    }
                    serviceName = name;
                }
            }
            registerOSGiService(i, contractNames, serviceName, metadata);
        }
    }

    /**
     * This method registers an equivalent service in OSGi's Service Registry.
     * The registered service would be registered with HK2 Service class name
     * in addition to all the contractNames.
     * @param i Inhabitant which is being exposed as service object
     * @param contractNames FQCN of all contract names of this service
     * @param serviceName Any additional name the service is known as. Used
     * to set {@link SERVICE_NAME} property in OSGi Service Registration.
     * @param metadata Additional metadata used to populate properties of
     * OSGi service registration. (not used for the moment)
     */
    private void registerOSGiService(
            Inhabitant i, List<String> contractNames, String serviceName,
            MultiMap<String, String> metadata) {
        contractNames.add(i.typeName());
        Properties props = new Properties();
        if (serviceName!=null) {
            props.setProperty(SERVICE_NAME, serviceName);
        }
        // TODO: Map metadata to properties
        ServiceRegistration reg = osgiCtx.registerService(
                contractNames.toArray(new String[0]),
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
