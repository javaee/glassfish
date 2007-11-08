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

/*
 * IasConnectorRoleMap.java
 *
 * Created on March 13, 2002, 1:56 PM
 */

package com.sun.enterprise.tools.common.properties;

import java.util.Vector;
import com.sun.enterprise.tools.common.dd.connector.RoleMap;
import com.sun.enterprise.tools.common.dd.connector.SunConnector;
import com.sun.enterprise.tools.common.dd.connector.MapElement;
import com.sun.enterprise.tools.common.dd.connector.Principal;
import com.sun.enterprise.tools.common.util.diagnostics.Reporter;

/**
 *
 * @author  vkraemer
 * @version 
 */
public class RoleMapElement {

    private RoleMap rm;
    
    //private SunConnector connectorDD = SunConnector.createGraph();
    
    /** Creates new IasConnectorRoleMap */
    public RoleMapElement(RoleMap rm) {
        this.rm = rm;
        //connectorDD.setRoleMap(rm);
    }

   public RoleMapElement(RoleMapElement rme) {
        this.rm = (RoleMap) rme.rm.clone();
        //connectorDD.setRoleMap(rm);
    }
   
   public RoleMap getRoleMap() {
       return rm;
   }
   
   public int getLength() {
       return rm.sizeMapElement();
   }
   
   public int getWidth() {
       return 4;
   }
   
   public Object getAttributeDetail(int row, int col) { 
       MapElement me = rm.getMapElement(row);
       if (col == 0)
           return me.getAttributeValue(MapElement.BACKEND_PRINCIPAL, 0, "user-name"); // , col); //NOI18N
       if (col == 1)
           return me.getAttributeValue(MapElement.BACKEND_PRINCIPAL, 0, "password");//, col); //NOI18N
       if (col == 2)
           return me.getAttributeValue(MapElement.BACKEND_PRINCIPAL, 0, "credential");//, col); //NOI18N
       if (col == 3) {
/*           
           String allPrincipals = ""; // NOI18N
           int rowCount = me.sizePrincipal();
           for (int i = 0; i < rowCount; i++) {
               Principal p = me.getPrincipal(i);
               allPrincipals += (String) p.getAttributeValue("user-name"); //NOI18N
               allPrincipals += ", "; // NOI18N
           }
           int len = allPrincipals.length();
           if (1 < len)
               allPrincipals = allPrincipals.substring(0,len-2);
           return allPrincipals;
 */
           Vector vec = new Vector();
           for (int i = 0; i < me.sizePrincipal(); i++) {
               Principal prin = me.getPrincipal(i);
               String[] element = new String[2];
               element[0] = prin.getAttributeValue("user-name");   //NOI18N
               element[1] = prin.getDescription();
               vec.add(element);
           }
           Reporter.info(new Integer(vec.size()));
           return vec;
       }
       return null;
   }
       
    public void setAttributeDetail(Object v, int r, int c) { 
        String input = null;
        if (c != 3)
            input = (String) v;
        while (r >= getLength()) {
            MapElement nme = new MapElement();
            nme.setBackendPrincipal(true);
            rm.addMapElement(nme);
            Reporter.info("add a MapElement");  //NOI18N
        }
        MapElement me = rm.getMapElement(r);
        if ((c != 3 &&(null == input || 0 == input.trim().length())) || (c == 3 && ((Vector)v).size() == 0)) {
            // test for need to delete
            if (r >= getLength())
                return;
            
            boolean rowHasValue = false;
            String value = null;
            if (c != 0) {
                value = me.getAttributeValue(MapElement.BACKEND_PRINCIPAL, 0, "user-name"); //NOI18N
                if (null != value && value.trim().length() > 0)
                    rowHasValue = true;
            }
            if (c != 1) {
                value = me.getAttributeValue(MapElement.BACKEND_PRINCIPAL, 0, "password"); //NOI18N
                if (null != value && value.trim().length() > 0)
                    rowHasValue = true;
            }
            if (c != 2) {
                value = me.getAttributeValue(MapElement.BACKEND_PRINCIPAL, 0, "credential"); //NOI18N
                if (null != value && value.trim().length() > 0)
                    rowHasValue = true;
            }
            if (c != 3) {
                if (0 != me.sizePrincipal())
                    rowHasValue = true;
            }
            
//            int otherDex = c - 1;
//            if (otherDex < 0)
//                otherDex = -otherDex;
//            String otherVal = ra.getAttributeValue(ResourceAdapter.PROPERTY, r, //NOI18N
//                intToAttribute(otherDex));
//            String otherVal = ra.getAttributeValue(ResourceAdapter.PROPERTY, r, //NOI18N
//                intToAttribute(otherDex));
            if (!rowHasValue) {
                Reporter.info("remove a MapElement");   //NOI18N
                rm.removeMapElement(me);
                return;
            }
//                ra.removePropertyElement(r);
//                return;
//            }
//            input = " ";
        }
        while (r >= getLength()) {
            Reporter.info("add a MapElement");   //NOI18N
            me = new MapElement();
            me.setBackendPrincipal(true);
            rm.addMapElement(me);
        }
       if (0 == c)
           me.setAttributeValue(MapElement.BACKEND_PRINCIPAL, 0, "user-name", (String) v); //NOI18N
       if (1 == c)
           me.setAttributeValue(MapElement.BACKEND_PRINCIPAL, 0, "password", (String) v); //NOI18N
       if (2 == c)
           me.setAttributeValue(MapElement.BACKEND_PRINCIPAL, 0, "credential",(String) v); //NOI18N
        if (3 == c) {
/*            
            java.util.StringTokenizer toker = 
                new java.util.StringTokenizer((String)v,","); // NOI18N
            Principal[] ps = new Principal[toker.countTokens()];
            int i = 0;
            while (toker.hasMoreTokens()) {
                String tok = toker.nextToken();
                Principal p = new Principal();
                p.setAttributeValue("user-name",tok); //NOI18N
                ps[i] = p;
                i++;
            }
            me.setPrincipal(ps);
 */
            Vector vec = (Vector)v;
            Reporter.info(new Integer(vec.size()));
            Principal[] principals = new Principal[vec.size()];
            for (int i = 0; i < vec.size(); i++) {
                Principal prin = new Principal();
                String[] principal = (String[])vec.elementAt(i);
//                Reporter.info("(" + principal[0] + ")   (" + principal[1] + ")");   //NOI18N
                prin.setAttributeValue("user-name", principal[0]);  //NOI18N
                prin.setDescription(principal[1]);
                principals[i] = prin;
            }
            me.setPrincipal(principals);
//            Reporter.info(new Integer(me.sizePrincipal()));
        }
            //ra.addPropertyElement(true);
        //System.out.println("SETAttributeDetail on " + ra.hashCode()); //NOI18N
        //ra.setPropertyElement(r,true);
        //ra.setAttributeValue(ResourceAdapter.PROPERTY, r, intToAttribute(otherDex), otherVal); //NOI18N
        //ra.setAttributeValue(ResourceAdapter.PROPERTY, r, intToAttribute(c), input); //NOI18N
    }
    
    public String getRoleMapDescription() {
        return rm.getDescription();
    }
        
    public void setRoleMapDescription(String desc) {
        rm.setDescription(desc);
    }    

    // For testing Do not expose.
   RoleMapElement(String args[]) {
        int rowCount = 0;
        if (null != args && args.length > 0) {
            try {
                rowCount = Integer.parseInt(args[0]);
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
        rm = new RoleMap();
        int unameVal = 100;
        for (int i = 0; i < rowCount; i++) {
            MapElement me = new MapElement();
            me.setBackendPrincipal(true);
            me.setAttributeValue(MapElement.BACKEND_PRINCIPAL, 0, "user-name", ""+i); //NOI18N
            me.setAttributeValue(MapElement.BACKEND_PRINCIPAL, 0, "password", ""+i); //NOI18N
            me.setAttributeValue(MapElement.BACKEND_PRINCIPAL, 0, "credential", ""+i); //NOI18N
            for (int j = 0; j < rowCount; j++) {
                Principal p = new Principal();
                p.setAttributeValue("user-name", ""+unameVal); //NOI18N
                unameVal++;
                me.addPrincipal(p);
            }
            rm.addMapElement(me);
            
            //ra.addPropertyElement(true);
            //ra.setAttributeValue(ResourceAdapter.PROPERTY,i,"name",""+i); //NOI18N
            //ra.setAttributeValue(ResourceAdapter.PROPERTY,i,"value",""+(rowCount - i)); //NOI18N
        }
        //connectorDD.setResourceAdapter(ra);
        //ra = (ResourceAdapter) ra.clone();
        SunConnector connectorDD = SunConnector.createGraph();
        connectorDD.setRoleMap(rm);
       
   }
   
    // For testing Do not expose.
   String dumpIt() {
       return rm.dumpBeanNode();
   }
}
