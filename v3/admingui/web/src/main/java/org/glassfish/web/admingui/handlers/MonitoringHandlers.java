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

package org.glassfish.web.admingui.handlers;

import org.glassfish.admingui.common.util.GuiUtil;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import org.glassfish.admingui.common.util.V3AMX;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;


import org.glassfish.admin.amx.base.Query;
import org.glassfish.admin.amx.core.AMXProxy;

/**
 *
 * @author Ana
 */
public class MonitoringHandlers {
    
    
  /*
     * This handler returns a list of statistical data for type and name of component.
     * Useful for populating table
     */
    @Handler(id="getStatsbyTypeName",
    input={
        @HandlerInput(name="type",   type=String.class, required=true),
        @HandlerInput(name="name",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="result",        type=List.class)})

        public static void getStatsbyTypeName(HandlerContext handlerCtx) {
        String type = (String) handlerCtx.getInputValue("type");
        String name = (String) handlerCtx.getInputValue("name");
        List result = new ArrayList();
        try {
            Query query = V3AMX.getInstance().getDomainRoot().getQueryMgr();
            Set amxproxy = (Set) query.queryTypeName(type, name);
            Iterator iter = amxproxy.iterator();
            while (iter.hasNext()) {
                Map<String, Object> monattrs = ((AMXProxy) iter.next()).attributesMap();
               for (String monName : monattrs.keySet()) {
                    if ((!monName.equals("Parent")) && (!monName.equals("Children"))) {
                        Map statMap = new HashMap();
                        statMap.put("Name", monName);
                        Object val = monattrs.get(monName);
                        if (val instanceof CompositeDataSupport) {
                            CompositeDataSupport cds = ((CompositeDataSupport) val);
                            CompositeType ctype = cds.getCompositeType();
                            Set attrSet = ctype.keySet();
                            Iterator citer = attrSet.iterator();
                            String cvalues = "";
                            while (citer.hasNext()) {
                                String oneEntry = (String) citer.next();
                                cvalues = cvalues + "\n" + oneEntry + " = " + cds.get(oneEntry) + ",";
                            }
                            val = cvalues;
                        } else if (val instanceof String[]) {
                            String values = "";
                            for (String s : (String[]) val) {
                                values = values + s + "\n";

                            }
                            val = values;
                        }
                        statMap.put("Value", val);
                        result.add(statMap);


                    }
                }
            }
            handlerCtx.setOutputValue("result", result);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }    
    
}
