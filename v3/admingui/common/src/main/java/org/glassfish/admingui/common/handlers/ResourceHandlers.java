/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

import java.util.Map;
import java.util.List;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.util.ArrayList;
import java.util.HashMap;
import org.glassfish.admingui.common.util.DeployUtil;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.TargetUtil;


/**
 *
 * @author Anissa Lam
 */
public class ResourceHandlers {

    /*
     * This handler takes in a list of rows, there should be 'Enabled' attribute in each row.
     * Get the resource-ref of this resource and do a logical And with this Enabled attribute
     * to get the real status
     */
    @Handler(id = "gf.getResourceRealStatus",
        input = {
            @HandlerInput(name = "endpoint", type = String.class),
            @HandlerInput(name = "rows", type = java.util.List.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = List.class)})
    public static void getResourceRealStatus(HandlerContext handlerCtx) {
        List<Map> rows = (List) handlerCtx.getInputValue("rows");
        String resourceRefEndPoint = (String) handlerCtx.getInputValue("endpoint");
        for (Map oneRow : rows) {
            String name = (String) oneRow.get("encodedName");
            String endpoint = resourceRefEndPoint + "/" +name;
            if (! RestApiHandlers.get(endpoint).isSuccess()){
                continue;            //The resource is only created on domain, no source-ref exists.
            }
            String enabledStr = DeployUtil.getTargetEnableInfo(name, false, false);
            List<String> targets = DeployUtil.getApplicationTarget(name, "resource-ref");
            List<String> targetUrls = new ArrayList<String>();
            for ( String target : targets) {
                if(TargetUtil.isCluster(target)) {
                    targetUrls.add(GuiUtil.getSessionValue("REST_URL")+"/clusters/cluster/"+target);
                } else {
                    targetUrls.add(GuiUtil.getSessionValue("REST_URL")+"/servers/server/"+target);
                }
            }
            oneRow.put("targetUrls", targetUrls);
            oneRow.put("enabled", enabledStr);
        }
        handlerCtx.setOutputValue("result", rows);
    }

    @Handler(id = "getJMSFactoriesTable",
        input = {
            @HandlerInput(name="endpoint", type = String.class, required = true)
        },
        output = {
        @HandlerOutput(name = "result", type = java.util.List.class)
    })
    public static void getJMSFactoriesTable(HandlerContext handlerCtx) throws Exception {
        String endpoint = (String)handlerCtx.getInputValue("endpoint");
        Map<String, String> children = RestApiHandlers.getChildMap(endpoint);

        List result = new ArrayList();

        for (Map.Entry<String, String> entry : children.entrySet()) {
            String poolName = entry.getKey();
            String poolUrl = entry.getValue();
            if (GuiUtil.isEmpty(poolName)) {
                continue;   //this is a required attribute, shouldn't happen.
            }
            Map<String, Object> poolEntity = RestApiHandlers.getEntityAttrs(poolUrl, "entity");
            if (poolEntity.get("resourceAdapterName").equals("jmsra")) {
                Map oneRow = new HashMap();
                oneRow.put("selected", false);
                oneRow.put("name", poolName);
                oneRow.put("jndiName", poolEntity.get("name"));
                oneRow.put("connectionDefinitionName", poolEntity.get("connectionDefinitionName"));
                oneRow.put("description",  poolEntity.get("description"));
                result.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("result", result);
    }
}