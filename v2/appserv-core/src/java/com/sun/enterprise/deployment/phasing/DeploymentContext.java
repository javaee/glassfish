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
 * DeploymentContext.java
 *
 * Created on May 22, 2003, 2:26 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/DeploymentContext.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.deployment.Application;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

/**
 * This class is used to pass the common context needed by all deployment phases
 * @author Sandhya E
 */
public class DeploymentContext {
    
    /** 
     * Creates a new instance of DeploymentContext
     */
    public DeploymentContext() 
    {
    }
    
    /**
     * Set the config context as specified
     * @param configContext config context object
     */
    public void setConfigContext(ConfigContext configContext)
    {
        this.configContext = configContext;
    }
    
    /**
     * returns the stored configContext
     * @return configContext
     */
    public ConfigContext getConfigContext() 
    {
        return configContext;
    }

    public void addApplication(String appID, Application app) {
        if (app != null) {
            cachedApps.put(appID, app);    
        }
    }

    public Application getApplication(String appID) {
        return cachedApps.get(appID);
    }
 
    public Application removeApplication(String appID) {
        return cachedApps.remove(appID);
    }
 
     /**
      *Saves an app ref for retrieval during a later phase of the deployment.
      *@param appID the name of the application the reference refers to
      *@param ref the ApplicationRef to be saved
      */
     public SavedApplicationRefInfo saveAppRef(String appID, String targetName, ApplicationRef ref, 
             DeploymentRequest req) {
         
         SavedApplicationRefInfo result = null;
         synchronized(savedAppRefsByApp) {
             /*
              *Try to locate this app's HashMap of targets to refs.
              */
             HashMap<String,SavedApplicationRefInfo> refMapForApp = savedAppRefsByApp.get(appID);
             if (refMapForApp == null) {
                 refMapForApp = new HashMap<String,SavedApplicationRefInfo>();
                 savedAppRefsByApp.put(appID, refMapForApp);
             }
             result = new SavedApplicationRefInfo(ref, req);
             refMapForApp.put(targetName, result);
         }
         return result;
     }
     
     /**
      *Retrieves a saved app ref, if one was saved, for the specified target.
      *@param appID the name of the application to be checked for a reference 
      * to the specified target
      *@param target the name of the target of interest
      *@return a SavedApplicationRefInfo object if one was saved for this target; null otherwise
      */
     public SavedApplicationRefInfo getSavedAppRef(String appID, String targetName) {
         SavedApplicationRefInfo result = getSavedApplicationRefInfo(appID, targetName);
         return result;
     }
     
     private SavedApplicationRefInfo getSavedApplicationRefInfo(String appID, String targetName) {
         SavedApplicationRefInfo result = null;
         synchronized(savedAppRefsByApp) {
             /*
              *Try to find saved references for this application.
              */
             HashMap<String,SavedApplicationRefInfo> refMapForApp = savedAppRefsByApp.get(appID);
             if (refMapForApp != null) {
                result = refMapForApp.get(targetName);
             }
         }
         return result;         
     }

     /**
     * Removes the saved ApplicationRef for a given app and target.
     * <p>
     * As a side effect, if the application's map to targets becomes empty,
     * remove that map from the map of app IDs to saved result maps.
     * 
     * 
     * @param appID the name of the app
     * @param target the name of the target
     * @return ApplicationRef the corresponding saved app ref, if any; null otherwise
     */
     public SavedApplicationRefInfo removeSavedAppRef(String appID, String targetName) {
         SavedApplicationRefInfo result = null;
         synchronized(savedAppRefsByApp) {
             result = getSavedApplicationRefInfo(appID, targetName);
             if (result != null) {
                 HashMap<String,SavedApplicationRefInfo> refMapForApp= savedAppRefsByApp.get(appID);
                 refMapForApp.remove(targetName);
                 if (refMapForApp.isEmpty()) {
                     savedAppRefsByApp.remove(appID);
                 }
             }
         }
         return result;
     }
     
    /** config context */
    private ConfigContext configContext = null;
    
    // cache DOL object
    private Hashtable<String, Application> cachedApps = 
        new Hashtable<String, Application>();

    /** during redeployment saves non-defaulted values of pre-existing app refs */
    private HashMap<String,HashMap<String,SavedApplicationRefInfo>> savedAppRefsByApp = 
            new HashMap<String,HashMap<String,SavedApplicationRefInfo>>();
    
    /**
     *Records, for the redeployment of a single app, information about a single
     *app ref for that app on a single target.
     */
    public class SavedApplicationRefInfo {
        
        /** pointer to the original app ref */
        private ApplicationRef appRef;
        
        /** whether any app ref attributes are changing as a result of the redeployment */
        private boolean isChanging;
        
        /**
         * Creates a new instance of SavedApplicationRefInfo
         */
        public SavedApplicationRefInfo(ApplicationRef appRef, DeploymentRequest req) {
            this.appRef = appRef;
            isChanging = isAppRefChanging(req);
        }
        
        public ApplicationRef appRef() {
            return appRef;
        }
        
        public boolean isChanging() {
            return isChanging;
        }
        
        public String toString() {
            return "AppRefInfo: isChanging = " + isChanging + " app ref: " + appRef.toString();
        }
        
        /**
         *Determines, given the specified deployment request, whether the app
         *ref will be changing as a result of this redeployment.
         *@param req the DeploymentRequest object prepared for the current operation
         *@returns true if at least one app ref attribute value will changes as
         *a result of this redeployment.
         *@throws ConfigException in case of errors 
         */
        private boolean isAppRefChanging(
                DeploymentRequest req) {
            boolean result = true;

            String refVirtualServers = appRef.getVirtualServers();
            Properties optionalAttrs = req.getOptionalAttributes();
            String reqVirtualServers = null;
            if (optionalAttrs != null) {
                reqVirtualServers = (String) optionalAttrs.get(ServerTags.VIRTUAL_SERVERS);
            }
            result = (appRef.isEnabled() != req.isStartOnDeploy()) ||
                     ( ! commaStringsMatch(refVirtualServers, reqVirtualServers)
                     );
            return result;
        }
        
        /**
         *Returns whether two comma-separated lists contain the same elements.
         *The comma-separated strings match if they contain
         *exactly the same elements, regardless of order.
         *
         *@param a one String to compare
         *@param b the other String to compare
         *@return true if both strings specify the same list of elements; false otherwise
         */
        private boolean commaStringsMatch(String a, String b) {
            /*
             *Optimize the simple case if the strings are really identical.
             */
            if ((a == b) || (a != null && a.equals(b))) {
                return true;
            }

            /*
             *a cannot be null at this point.  If b is, they are not equal.
             */
            if (b == null) {
                return false;
            }

            /*
             *Use set operations to see conveniently if the strings specify the same elements.
             */
            Set<String> aSet = new HashSet<String>(Arrays.asList(a.split(",")));
            Set<String> bSet = new HashSet<String>(Arrays.asList(b.split(",")));
            return aSet.equals(bSet);
        }
    }
}
