/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.internal.api;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.internal.api.Init;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;

/**
 * Very sensitive class, anything stored here cannot be garbage collected
 *
 * @author Jerome Dochez
 */
@Service(name = "globals")
public class Globals implements Init {

    @Inject
    static volatile Habitat defaultHabitat;

    private static Object staticLock = new Object();
    
    // dochez : remove this once we can get rid of ConfigBeanUtilities class
    @Inject
    ConfigBeansUtilities utilities;

    public static Habitat getDefaultHabitat() {
        return defaultHabitat;
    }

    public static <T> T get(Class<T> type) {
        return defaultHabitat.getComponent(type);
    }

    public static void setDefaultHabitat(final Habitat habitat) {
        defaultHabitat = habitat;
    }

    public static Habitat getStaticHabitat() {
        if (defaultHabitat == null) {
            synchronized (staticLock) {
                if (defaultHabitat == null) {
                    ModulesRegistry registry = new StaticModulesRegistry(Globals.class.getClassLoader());
                    defaultHabitat = registry.createHabitat("default");
                }
            }
        }

        return defaultHabitat;
    }

}
