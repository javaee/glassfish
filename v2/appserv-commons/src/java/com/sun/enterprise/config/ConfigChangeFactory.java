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
 * ConfigChangeFactory.java
 *
 * Created on February 19, 2004, 9:41 AM
 */

package com.sun.enterprise.config;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.impl.ConfigAddImpl;
import com.sun.enterprise.config.impl.ConfigUpdateImpl;
import com.sun.enterprise.config.impl.ConfigSetImpl;
import com.sun.enterprise.config.impl.ConfigDeleteImpl;

import com.sun.enterprise.config.util.LoggerHelper;

/**
 * This factory has static methods to create 
 * Configchange objects from a configbean. 
 * It creates specific ConfigChange Interfaces
 * using the implementations from the impl package.
 *
 * This factory is used by the configcontext while
 * generating config change objects during its operation.
 *
 * This factory can also be used by other modules to
 * generate configChange objects that can be serialized
 * and reapplied in a different VM
 *
 * @author  sridatta
 */
public class ConfigChangeFactory {
  
    /**
     * creates a ConfigAdd object based on the parameters.
     * This method should be used only if the configbean
     * for the xpath already exists
     *
     * @return ConfigAdd object that can be applied to a different context.
     * @param ctx from where the config bean needs to be taken
     * @param xpath of the config bean to be used for creating configAdd
     * @throws ConfigException
     */
  public static ConfigAdd createConfigAdd(ConfigContext ctx, String xpath) throws ConfigException {
      try {
        return new ConfigAddImpl(ctx, xpath);
      } catch(ConfigException ce) {
          LoggerHelper.info("ConfigChangeFactory.createConfigAdd: Error creating config Add", ce);
          throw ce;
      }
  }
  
  /**
   * This method is to be used is the config add is to be created
   * for a config bean that is still not added to the tree
   * @param parentXpath
   * @param childXpath
   * @param name
   * @param cb
   * @return
   */
  public static ConfigAdd createConfigAdd(String parentXpath, 
                            String childXpath, 
                            String name, 
                            ConfigBean cb) {
    return new ConfigAddImpl(parentXpath, childXpath, name, cb);
  }
  
  /**
   * creates a ConfigUpdate given old and new value
   * @param xpath
   * @param attrName
   * @param oldValue
   * @param newValue
   * @return
   */
  public static ConfigUpdate createConfigUpdate(String xpath, 
                            String attrName, 
                            String oldValue, 
                            String newValue) {
       return new ConfigUpdateImpl(xpath, attrName, oldValue, newValue);
  }

  /**
   * creates a ConfigSet 
   * @return
   */
  public static ConfigSet createConfigSet(String parentXpath, 
                            String name, 
                            Object cb, 
                            Object[] cbArray) {
       return new ConfigSetImpl(parentXpath, name, cb, cbArray);
  }
  
  /**
   * creates a configdelete
   */
  public static ConfigDelete createConfigDelete(String xpath) {
      return new ConfigDeleteImpl(xpath);
  }
}
