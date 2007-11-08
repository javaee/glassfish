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
 * ConfigBeanInterceptor.java
 *
 * Created on January 2, 2004, 2:48 PM
 */

package com.sun.enterprise.config.impl;

import com.sun.enterprise.config.pluggable.ConfigBeanInterceptor;
import com.sun.enterprise.config.ConfigBean;

//The RelativePathResolver is used to translate relative paths containing 
//embedded system properties (e.g. ${com.sun.aas.instanceRoot}/applications) 
//into absolute paths
import com.sun.enterprise.util.RelativePathResolver;

import java.io.Serializable;

/**
 *
 * @author  sridatta
 */
public class DefaultConfigBeanInterceptor implements ConfigBeanInterceptor, Serializable, Cloneable {
    
    /** Creates a new instance of ConfigBeanInterceptor */
    public DefaultConfigBeanInterceptor() {
    }
    
 
          /**
       * Resolve tokens for an element value that is a string
       * Broken into a separate method because called in numerous places
       */
      public Object resolveTokensForString(Object value) {
         if (isResolvingPaths() && value != null && value instanceof String) {
             return resolveStringTokens((String)value);
         }
         return value;
      }
         
      /**
       * General method to resolve tokens for string that are in attributes
       * and Text elements
       */
      public String resolveStringTokens(String string) {
          return RelativePathResolver.resolvePath(string);
      }

      public Object postGetValue(ConfigBean cb, String name, Object res) {
           return resolveTokensForString(res);
      }
      
         /**
         * The isResolvingPaths boolean when set to false causes getAttributeValue to 
         * return the raw unresolved attribute value. 
         */        
        private boolean _isResolvingPaths = true;
        
        public void setResolvingPaths(boolean isResolvingPaths) {
            _isResolvingPaths = isResolvingPaths;
        }
        
        public boolean isResolvingPaths() {
            return _isResolvingPaths;
        }    
        
        public String postGetAttributeValue(String name, String res) {
           if (isResolvingPaths()) {
                return resolveStringTokens(res);
            } else {
                return res;
            }
        }
        
        public Object[] postGetValues(String name, Object[] res) {
            
         // only needed in one place so don't call separate method
         if (isResolvingPaths()&& res != null && res instanceof String[]) {
             // loop through and resolve tokens
             for(int ii=0; ii < res.length; ii++) { 
                 res[ii]=resolveStringTokens((String)res[ii]);
             }
         }
         return res;
        }
        
        public Object preClone() {
          boolean orig = isResolvingPaths();
            setResolvingPaths(false);
            return Boolean.valueOf(orig);
        }
        
        public void postClone(Object o) {
            setResolvingPaths(((Boolean)o).booleanValue());
        }
        
        public Object clone() {
            ConfigBeanInterceptor cbiClone = new DefaultConfigBeanInterceptor();
            cbiClone.setResolvingPaths(this.isResolvingPaths());
            return cbiClone;
        }

        /**
         * NOTE: This method returns wrong values sometimes 
         * when a clone is in process
         */
        public String toString() {
            return "{resolvingPaths=" + isResolvingPaths() + "}";
        }
}
