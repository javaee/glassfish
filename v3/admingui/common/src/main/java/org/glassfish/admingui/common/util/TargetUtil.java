/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.util;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.glassfish.admingui.common.handlers.RestApiHandlers;
import org.glassfish.admingui.common.handlers.RestUtilHandlers;

/**
 *
 * @author anilam
 */
public class TargetUtil {

    public static boolean isCluster(String name){
        if (GuiUtil.isEmpty(name)){
            return false;
        }
        return getClusters().contains(name);
    }

    public static List getStandaloneInstances(){
        List<String> result = new ArrayList<String>();
        String endpoint = GuiUtil.getSessionValue("REST_URL") + "/list-instances" ;
        Map attrsMap = new HashMap();
        attrsMap.put("standaloneonly", "true");
        try{
            Map responseMap = RestApiHandlers.restRequest( endpoint , attrsMap, "get" , null);
            Map  dataMap = (Map) responseMap.get("data");
            Map<String, Object>  extraProps = (Map<String, Object>) dataMap.get("extraProperties");
            if (extraProps == null){
                return result;
            }
            List<Map<String, String>> props = (List<Map<String, String>>) extraProps.get("instanceList");            
            if (props == null){
                return result;
            }
            result = RestUtilHandlers.getListFromMapKey(props);
        }catch (Exception ex){
            GuiUtil.getLogger().severe("Error in getStandaloneInstances ; \nendpoint = " +endpoint + ", attrsMap=" + attrsMap);
        }

        return result;
    }

    public static List getClusters(){
        List clusters = new ArrayList();
        try{
            clusters.addAll(RestApiHandlers.getChildMap(GuiUtil.getSessionValue("REST_URL") + "/clusters/cluster").keySet());
        }catch (Exception ex){
            GuiUtil.getLogger().severe("Error in getClusters;");
            ex.printStackTrace();
        }
        return clusters;
    }


    public static String getTargetEndpoint(String target){
        try{
            String encodedName = URLEncoder.encode(target, "UTF-8");
            String endpoint = (String)GuiUtil.getSessionValue("REST_URL");
            if (target.equals("server")){
                endpoint = endpoint + "/servers/server/server";
            }else{
                List clusters = TargetUtil.getClusters();
                if (clusters.contains(target)){
                    endpoint = endpoint + "/clusters/cluster/" + encodedName;
                }else{
                    endpoint = endpoint + "/servers/server/" + encodedName;
                }
            }
            return endpoint;
        }catch(Exception ex){
            ex.printStackTrace();
            return "";
        }
    }

}
