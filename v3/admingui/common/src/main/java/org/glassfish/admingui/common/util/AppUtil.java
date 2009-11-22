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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.management.ObjectName;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.deployment.client.DFDeploymentProperties;

/**
 *
 * @author anilam
 */
public class AppUtil {

    public static List getAllSniffers(AMXProxy app){
        Map<String, AMXProxy> modules = app.childrenMap("module");
        List sniffersList = new ArrayList();
        for(AMXProxy oneModule: modules.values()){
            List<String> sniffers = getSnifferListOfModule(oneModule);
            for(String oneSniffer : sniffers){
                if (! sniffersList.contains(oneSniffer)){
                    sniffersList.add(oneSniffer);
                }
            }
        }
        return sniffersList;
    }


    public static List<String> getSnifferListOfModule(AMXProxy module){
        List sniffersList = new ArrayList();
        Map<String, AMXProxy> engines = module.childrenMap("engine");
        for (String oneSniffer: engines.keySet()){
            String sniffer = oneSniffer;
            if (sniffersHide.contains(sniffer) )
                continue;
            sniffersList.add(sniffer);
        }
        Collections.sort(sniffersList);
        return sniffersList;
    }

    public static boolean isApplicationEnabled(AMXProxy application){
        Map<String, Object> attrs = application.attributesMap();
        String enabled = (String) attrs.get("Enabled");
        String appName = (String) attrs.get("Name");
        if ( "true".equals(enabled)){
           AMXProxy appRef = V3AMX.getInstance().getApplicationRef("server", appName);
           String appRefEnabled = (String) appRef.attributesMap().get("Enabled");
           return ("true".equals(appRefEnabled));
        }
        return false;
    }
    
    public static boolean isApplicationEnabled(String appObjectName){
        return isApplicationEnabled(V3AMX.objectNameToProxy(appObjectName));
    }

    public static boolean isLifecycle(ObjectName objName){
        AMXProxy amx = V3AMX.objectNameToProxy(objName.toString());
        String isLife = (String) V3AMX.getPropValue(amx, DFDeploymentProperties.IS_LIFECYCLE);
        return (isLife == null) ? false : true;
    }

    static final public List sniffersHide = new ArrayList();
    static {
        sniffersHide.add("security");
    }
}
