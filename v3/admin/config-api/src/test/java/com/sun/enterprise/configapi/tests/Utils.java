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

package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.GlassFishDocument;
import com.sun.enterprise.module.bootstrap.Populator;
import com.sun.hk2.component.Holder;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.hk2.component.InhabitantsScanner;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utilities to create a configured Habitat and cache them
 *
 * @author Jerome Dochez
 */
public class Utils {

    final String habitatName = "default";
    final String inhabitantPath = "META-INF/inhabitants";

    Map<String, Habitat> habitats = new HashMap<String, Habitat>();
    static Utils instance;

    static {
        instance = new Utils();
    }

    public synchronized Habitat getHabitat(final String fileName) {

        // we cache the habitat per xml file
        if (habitats.containsKey(fileName))  {
           return habitats.get(fileName);
        }

        try {
            Holder<ClassLoader> holder = new Holder<ClassLoader>() {
                public ClassLoader get() {
                    return getClass().getClassLoader();
                }
            };

            Enumeration<URL> resources = getClass().getClassLoader().getResources(this.inhabitantPath + "/" + habitatName);
            if (resources == null) {
                System.out.println("Cannot find any inhabitant file in the classpath");
                return null;
            }

            final Habitat habitat = new Habitat();

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                InhabitantsScanner scanner = new InhabitantsScanner(resource.openConnection().getInputStream(), habitatName);
                InhabitantsParser inhabitantsParser = new InhabitantsParser(habitat);
                inhabitantsParser.parse(scanner, holder);
                ConfigParser configParser = new ConfigParser(habitat);

                (new Populator() {

                    public void run(ConfigParser parser) {
                        long now = System.currentTimeMillis();
                        URL url = getClass().getClassLoader().getResource(fileName + ".xml");
                        if (url!=null) {
                            DomDocument document = parser.parse(url, new GlassFishDocument(habitat));
                            Logger.getAnonymousLogger().fine("time to parse domain.xml : " + String.valueOf(System.currentTimeMillis() - now));
                        }
                    }
                }).run(configParser);
            }
            habitats.put(fileName, habitat);
            return habitat;
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
