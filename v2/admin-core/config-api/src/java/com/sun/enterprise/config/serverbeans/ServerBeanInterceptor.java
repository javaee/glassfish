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

/*
 * ConfigBeanInterceptor.java
 *
 * Created on January 2, 2004, 2:48 PM
 */

package com.sun.enterprise.config.serverbeans;

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
public class ServerBeanInterceptor implements ConfigBeanInterceptor, Serializable, Cloneable {
    
    /** Creates a new instance of ConfigBeanInterceptor */
    public ServerBeanInterceptor() {
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
            return new Boolean(orig);
        }
        
        public void postClone(Object o) {
            setResolvingPaths(((Boolean)o).booleanValue());
        }
        
        public Object clone() {
            ConfigBeanInterceptor cbiClone = new ServerBeanInterceptor();
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
