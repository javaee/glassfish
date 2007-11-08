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

package com.sun.enterprise.admin.common;

import java.io.Serializable;
import java.util.Properties;
import java.util.Set;
import java.util.Iterator;

import com.sun.enterprise.admin.util.ArgChecker;
import com.sun.enterprise.admin.util.StringValidator;


import com.sun.enterprise.repository.ResourceProperty;
/**
    A class representing the <code> information </code> about a JCA resource adapter.

   @author  Ari Shapiro
    @version  1.0
*/

public class ResourceAdapterInfo implements Serializable {
  /**the JNDI name of the resource adapter*/
  private String	jndiName;
  /**the attributes of the resource adapter*/
  private Properties	attrs;

  /**these are the iAS specific attributes*/
  private static final String DEBUG_LEVEL = "debugLevel";
  private static final String MAX_POOL_SIZE = "maxPoolSize";
  private static final String STEADY_POOL_SIZE = "steadyPoolSize";
  private static final String MAX_WAIT = "maxWait";
  private static final String UNUSED_MAX_LIFE = "unusedMaxLife";
    
  public ResourceAdapterInfo (String jndiName) {    
  }
   
   
   private void convertAdapterProps(Set props) {
     if (props != null) {
       Iterator itr = props.iterator();
       while (itr.hasNext()) {
         ResourceProperty prop = (ResourceProperty)itr.next();
         attrs.setProperty(prop.getName(),prop.getValue().toString());
       }
     }
   }
   public String getJndiName()  {
      return jndiName;
   }


   public Properties getAttrs()  {
     return attrs;
   }

   public String toString() {
     return jndiName + " " + attrs;   
   }
}
