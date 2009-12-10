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

package org.glassfish.tests.utils;

import com.sun.enterprise.module.bootstrap.Populator;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.single.StaticModulesRegistry;

import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;

import java.net.URL;
import java.util.logging.Logger;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * Utilities to create a configured Habitat and cache them
 *
 * @author Jerome Dochez
 */
public class Utils {

    final static String habitatName = "default";
    final static String inhabitantPath = "META-INF/inhabitants";

    Map<String, Habitat> habitats = new HashMap<String, Habitat>();
    public static final Utils instance = new Utils();

    public synchronized Habitat getHabitat(ConfigApiTest test) {

        final String fileName = test.getFileName();
        // we cache the habitat per xml file

        if (habitats.containsKey(fileName))  {
           return habitats.get(fileName);
        }

        Habitat habitat = getNewHabitat(test);
        habitats.put(fileName, habitat);
        return habitat;
    }

    public static synchronized Habitat getNewHabitat(final ConfigApiTest test) {

        final Habitat habitat = getNewHabitat();

        final String fileName = test.getFileName();


        ConfigParser configParser = new ConfigParser(habitat);

        (new Populator() {

            public void run(ConfigParser parser) {
                long now = System.currentTimeMillis();
                URL url = getClass().getClassLoader().getResource(fileName + ".xml");
                if (url != null) {
                    try {
                        DomDocument document = parser.parse(url,  test.getDocument(habitat));
                        habitat.addComponent("document", document);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    Logger.getAnonymousLogger().fine("time to parse domain.xml : " + String.valueOf(System.currentTimeMillis() - now));
                }
            }
        }).run(configParser);
        return habitat;
    }

    public static Habitat getNewHabitat() {

        ModulesRegistry registry = new StaticModulesRegistry(Utils.class.getClassLoader());
        return registry.createHabitat("default");
    }
}
