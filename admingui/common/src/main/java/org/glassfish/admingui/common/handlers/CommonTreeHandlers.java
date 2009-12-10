/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admingui.common.handlers;
        
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.management.ObjectName;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admingui.common.tree.FilterTreeEvent;
import org.glassfish.admingui.common.util.AppUtil;
import org.glassfish.admingui.common.util.V3AMX;


/**
 *
 * @author anilam
 */
public class CommonTreeHandlers {
    
    /**
     *	<p> Default Constructor.</p>
     */
    public CommonTreeHandlers() {
    }
    
    /**
     *  <p> This handler filters out all apps that is not a lifecycle from the list of objName available
     *      through the event object, based on the object-type attribute.
     * The resulting list consists only of lifecycle module.
     */
    @Handler( id="filterOutNonLifecycle")
    public static Object filterOutNonLifecycle(HandlerContext context) {
        FilterTreeEvent event = (FilterTreeEvent) context.getEventObject();
        List<ObjectName> apps = (List<ObjectName>)event.getChildObjects();
        List result = new ArrayList();
        for(ObjectName oneApp :apps){
            if (AppUtil.isLifecycle(oneApp)){
                result.add(oneApp);
            }
        }
        return result;
    }

    /**
     *  <p> This handler filters out all the lifecycle from the list of objName available
     *      through the event object, based on the object-type attribute.
     * The resulting list consists all apps that is NOT a lifecycle.
     */
    @Handler( id="filterOutLifecycle")
    public static Object filterOutLifecycle(HandlerContext context) {
        FilterTreeEvent event = (FilterTreeEvent) context.getEventObject();
        List<ObjectName> apps = (List<ObjectName>)event.getChildObjects();
        List result = new ArrayList();
        if (apps != null){
            for(ObjectName oneApp :apps){
                if (! AppUtil.isLifecycle(oneApp)){
                    result.add(oneApp);
                }
            }
        }
        return result;
    }

    @Handler( id="filterOutNonJms")
    public static Object filterOutNonJms(HandlerContext context) {
        FilterTreeEvent event = (FilterTreeEvent) context.getEventObject();
        List<ObjectName> connectors = (List<ObjectName>)event.getChildObjects();
        List result = new ArrayList();
        if (connectors != null){
            for(ObjectName connector : connectors){
//                AMXProxy amx = V3AMX.objectNameToProxy(connector.toString());
//                Map props = V3AMX.getAttrsMap(connector);
                String raName = (String) V3AMX.getAttrsMap(connector).get("ResourceAdapterName");

                if ("jmsra".equals(raName)) {
                    result.add(connector);
                }
            }
        }
        return result;
    }
}