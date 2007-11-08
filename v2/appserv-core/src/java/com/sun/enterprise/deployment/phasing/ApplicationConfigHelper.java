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
 *   $Id: ApplicationConfigHelper.java,v 1.8 2007/03/27 19:19:31 hzhang_jn Exp $
 *   @author: alexkrav
 *
 *   $Log: ApplicationConfigHelper.java,v $
 *   Revision 1.8  2007/03/27 19:19:31  hzhang_jn
 *
 *   Incremental fix from deployment side for issue 2627. Minimize the
 *   definition of constants string.
 *   Ran deployment dev, QL.
 *
 *   Revision 1.7  2006/12/19 01:32:23  hzhang_jn
 *
 *   Fix issue 1739. Modify unique context root checking so an occupied context
 *   root at __asadmin virtual server would not cause any conflict if a user
 *   application wants to use the same context root.
 *   Reviewed by Tim. Passed QL, deployment dev tests.
 *
 *   Revision 1.6  2005/12/25 04:15:19  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.5  2005/08/14 00:37:27  qouyang
 *
 *
 *   Related Files:
 *
 *   Modified Files:
 *   M admin/mbeanapi-impl/src/java/com/sun/enterprise/management/support/UploadInfo.java
 *   M appserv-commons/src/java/com/sun/enterprise/deployment/annotation/introspection/AnnotationScanner.java
 *   M appserv-commons/src/java/com/sun/enterprise/deployment/annotation/introspection/ClassFile.java
 *   M appserv-commons/src/java/com/sun/enterprise/deployment/annotation/introspection/ConstantPoolInfo.java
 *   M appserv-commons/src/java/com/sun/enterprise/deployment/Application.java
 *   M appserv-commons/src/java/com/sun/enterprise/deployment/archivist/AppClientArchivist.java
 *   M appserv-commons/src/java/com/sun/enterprise/deployment/archivist/Archivist.java
 *   M appserv-core/src/java/com/sun/enterprise/deployment/backend/DeployableObjectType.java
 *   M appserv-core/src/java/com/sun/enterprise/deployment/backend/J2EEModuleExploder.java
 *   M appserv-core/src/java/com/sun/enterprise/deployment/client/DeployAction.java
 *   M appserv-core/src/java/com/sun/enterprise/deployment/phasing/ApplicationConfigHelper.java
 *   M appserv-core/src/java/com/sun/enterprise/deployment/phasing/DeploymentServiceUtils.java
 *   M appserv-core/src/java/com/sun/enterprise/deployment/phasing/PEDeploymentService.java
 *   M appserv-core/src/java/com/sun/enterprise/instance/DescriptorArchivist.java
 *   M appserv-core/src/java/com/sun/enterprise/loader/EJBClassPathUtils.java
 *   M appserv-core/src/java/com/sun/enterprise/server/ApplicationLoader.java
 *
 *   Added Files:
 *   A appserv-commons/src/java/com/sun/enterprise/deployment/annotation/introspection/CustomAnnotationScanner.java
 *   A appserv-commons/src/java/com/sun/enterprise/deployment/annotation/introspection/EjbComponentAnnotationScanner.java
 *   A appserv-commons/src/java/com/sun/enterprise/deployment/util/EjbComponentAnnotationDetector.java
 *
 *   Removed Files:
 *   R appserv-commons/src/java/com/sun/enterprise/deployment/util/DeploymentCommonUtils.java
 *
 *   Submitters:
 *   Hong Zhang (Reviewed by Qingqing Ouyang)
 *       R appserv-commons/src/java/com/sun/enterprise/deployment/util/DeploymentCommonUtils.java
 *       M appserv-core/src/java/com/sun/enterprise/deployment/phasing/DeploymentServiceUtils.java
 *       M appserv-core/src/java/com/sun/enterprise/deployment/phasing/ApplicationConfigHelper.java
 *
 *   Lloyd Chambers (Reviewed by Qingqing Ouyang)
 *       M admin/mbeanapi-impl/src/java/com/sun/enterprise/management/support/UploadInfo.java
 *
 *   Qingqing Ouyang (Reviewed by Hong Zhang)
 *       Everything else
 *
 *
 *   Description:
 *   1. Changes made to support deployment of application ear file without
 *      application.xml.
 *       * This checkin only supports archive file (not directory nor JSR88
 *         InputStream yet).
 *       * Use the new initiateUpload API on DeploymentMgr so that the original
 *         application name (if any) is preserved for easy identification of
 *         ear, war, and rar module type without accessing deployment descriptors.
 *       * Construct an Application object in memory if none exists, following
 *         rules defined in Java EE Platform spec section 8.4.2.
 *       * Enhanced deployment.annotation.introspect.* package to allow quick
 *         introspection of any jar file for EJB component annotations without
 *         loading the class.  This allows us to correctly identify the ejb
 *         module within an ear when there is no dd exists in neither the
 *         application nor the ejb submodule.
 *
 *   2. Added correct license for files under deployment.annotation.introspect
 *      package.
 *
 *   3. Write out the application.xml when the original app does not have any,
 *      otherwise copy to generated/xml directory.
 *
 *   4. Fixed ApplicationLoader to load from the generated/xml if exists
 *      (since application.xml is no longer guaranteed to be under the original
 *       application repository directory).
 *
 *   5. Remove unnecessary disk I/O for application.xml (Hong's change).
 *
 *   6. Removed unused methods from EJBClassPathUtils.
 *
 *   Tests Ran:
 *   1. PE QL
 *   2. PE CTS
 *   3. PE deployment devtests
 *   4. PE devtests/ejb/ejb30/hello
 *   5. New devtests on ear file containg ejb jar and library jar with no application
 *      (deploy, redeploy, server restart, ran appclient).
 *      devtests/deployment/descriptor_free_zone/earwithejb
 *      devtests/deployment/descriptor_free_zone/earwithwar
 *
 *   Revision 1.4  2005/08/09 01:29:41  hzhang_jn
 *
 *   Update logic in comparing context root: "foo" and "/foo" should be
 *   considered same context root.
 *   Reviewed by Qingqing, passed QL and deployment dev tests.
 *
 *   Revision 1.3  2005/08/02 15:00:28  hzhang_jn
 *
 *   Fix CR 6264984. Fixed a typo in context roots comparison.
 *   Reviewed by Qingqing, passed QL, deployment dev tests.
 *
 *   Revision 1.2  2005/06/27 21:29:16  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:53:54  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.2  2005/03/17 01:40:52  kravtch
 *   Signature changed for ApplicationConfigHelper.checkContextRootUniqueness(): it now accepts "targetName" instead of "serverName" (where targets could be only cluster or server), providing uniqueness chack for bot servers and clusters.
 *   Bug #6240955
 *   Tests ran: QLT/EE
 *
 *   Revision 1.1  2005/03/12 00:26:47  kravtch
 *   Unique contextRoots support is provided (getContextRoots/resetContextRoots/checkContextRootsUniqueness) without re-reading of application descriptor, storing it in the transientProperties of appplication config bean.
 *
 *   Tests: QLT/EE ok.
 *
 */

package com.sun.enterprise.deployment.phasing;

import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.J2eeApplication;

import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.serverbeans.PropertyResolver;

import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.TargetBuilder;

import com.sun.enterprise.deployment.backend.IASDeploymentException;
 
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
/**
 * This module provides helper methods for Application config element 
 * 
 * @author  alexkrav
 */
public class ApplicationConfigHelper
{        
    private static final String CONTEXT_ROOTS_PROPERTY_NAME = "context-roots";
    private static final StringManager _strMgr = 
        StringManager.getManager(ApplicationConfigHelper.class);

    private ConfigContext _configContext = null;

    /** Creates a new instance of EEApplicationsConfigMBean */
    public ApplicationConfigHelper(ConfigContext configContext) 
    {
        _configContext = configContext;
    } 
        
    private ConfigContext getConfigContext()
    {
        return _configContext;
    }
    
    /**
     * Get application context roots of standalone web-module or j2ee application
     *
     * @param appBean - config bean representing standalone web-module or j2ee application
     * @throws ConfigException if data in trancient property has wrong type
     * @returns String array of context roots, or null - if inapropriate bean type
     */
     public static String[] getAppContextRoots(ConfigBean appBean) throws ConfigException, IASDeploymentException 
    {
        if (appBean instanceof WebModule)
        {
            return new String[]{((WebModule)appBean).getContextRoot()};
        }
        if (appBean instanceof J2eeApplication)
        {
            Object roots = appBean.getTransientProperty(CONTEXT_ROOTS_PROPERTY_NAME);
            if(roots==null)
            {
                String appName = ((J2eeApplication)appBean).getName();

                //call deployment helper
                roots = DeploymentServiceUtils.getContextRootsForEmbeddedWebApp(appName);
                appBean.setTransientProperty(CONTEXT_ROOTS_PROPERTY_NAME, roots);
            }
            else
            {
                if(!(roots instanceof String[]))
                {
                    throw new ConfigException(_strMgr.getString("configRootsPropertyIsNotStringArray",
                        ((J2eeApplication)appBean).getName()));     
                }
            }
                
            return (String[])roots;
        }
        return null;
    }

     /**
     * Get application context roots of standalone web-module or j2ee application
     *
     * @param ctx - config context containing application elements
     * @param appName - name of application element
     * @throws ConfigException if data in trancient property has wrong type
     * @returns String array of context roots, or null - if inapropriate bean type
     */
     public static String[] getAppContextRoots(ConfigContext ctx, String appName) throws ConfigException, IASDeploymentException
    {
        ConfigBean appBean = ApplicationHelper.findApplication(ctx, appName);
        if(appBean==null)
        {
            throw new ConfigException(_strMgr.getString("applicationElementIsNotFoundForName", appName));
        }
        return getAppContextRoots(appBean);
    }

    /**
     * Resets config bean context-roots transient property value 
     * (ingnored if not j2ee-application)
     * 
     * @param appBean - config bean representing j2ee-application
     * @param bForceToSetActualValue - if this parameter false, correspondent   
     *    transient property will be just emptied, so reading of actual values from
     *    app desctriptor will be postponed until next "AppContextRoots" operation;
     * @throws ConfigException if data in trancient property has wrong type
     */
     public static void resetAppContextRoots( ConfigBean appBean, 
                        boolean bForceToSetActualValue) throws ConfigException, IASDeploymentException
    {
        if (appBean instanceof J2eeApplication)
        {
            appBean.setTransientProperty(CONTEXT_ROOTS_PROPERTY_NAME, null);
            if(bForceToSetActualValue)
                getAppContextRoots(appBean); //forces to read descriptor xml
        }
    }
   
    
    /**
     * Resets config bean context-roots transient property value 
     * (ingnored/no exception if not j2ee-application)
     * 
     * @param ctx - config context containing application elements
     * @param appName - name of application element
     * @param bForceToSetActualValue - if this parameter false, correspondent    
     *    transient property will be just emptied, so reading of actual values from
     *    app desctriptor will be postponed until next "AppContextRoots" operation;
     * @throws ConfigException if data in trancient property has wrong type
     */
    public static void resetAppContextRoots(ConfigContext ctx, String appName, 
                        boolean bForceToSetActualValue) throws ConfigException, IASDeploymentException
    {
        ConfigBean appBean = ApplicationHelper.findApplication(ctx, appName);
        if(appBean==null)
        {
            throw new ConfigException(_strMgr.getString("applicationElementIsNotFoundForName", appName));
        }
        resetAppContextRoots(appBean, bForceToSetActualValue);
    }
    
    
    /**
     * Check the uniqueness of application's context-roots
     * among applications deployed to the given server instance's virtual servers
     *
     * @param appId  - application name
     * @param targetName - server instance name or cluster name
     * @param virtualServerList coma separated list of virtual servers 
     *
     * @throws ConfigException
     * @returns conflicting context root and null is context roots are unique 
     *  inside of virtual servers.
     **/
    public static String checkContextRootUniqueness(ConfigContext ctx, String appId, String targetName, String virtualServerList) 
        throws ConfigException, IASDeploymentException
    {
        ConfigBean appBean = ApplicationHelper.findApplication(ctx, appId);
        if(appBean==null)
        {
            throw new ConfigException(_strMgr.getString("applicationElementIsNotFoundForName", appId));
        }
        
        //get context roots for testing element
        String[] ctxRoots = getAppContextRoots(appBean);
        if(ctxRoots==null || ctxRoots.length<=0)
        	return null;

        //create target by name
        final Target target = TargetBuilder.INSTANCE.createTarget(
            new TargetType[]{TargetType.CLUSTER, TargetType.SERVER, TargetType.DAS}, targetName, ctx);            
        
        //get apprefs for target 
        ApplicationRef[] refs = null;
        if (target.getType() == TargetType.CLUSTER ||
            target.getType() == TargetType.STANDALONE_CLUSTER) 
        {
            refs = ClusterHelper.getApplicationReferences(ctx, targetName);
        }
        else
        {
            refs = ServerHelper.getApplicationReferences(ctx, targetName);
        }
            
        if(refs==null)
            return null;
        
        for(int i=0; i<refs.length; i++)
        {
            if(isVSListsIntersected(refs[i].getVirtualServers(), virtualServerList))
            {
                ConfigBean appBeanToCompare = ApplicationHelper.findApplication(ctx, refs[i].getRef());
                if(appBeanToCompare==null)
                {
                    throw new ConfigException(_strMgr.getString("applicationElementIsNotFoundForName", refs[i].getRef()));
                }   
                if(((Object)appBeanToCompare)!=((Object)appBean))
                {
                    String commonElement = (String)getFirstCommonElement(getAppContextRoots(appBeanToCompare), ctxRoots);
                    if(commonElement!=null)
                        return commonElement;
                }
 
            }
        }
        return null;
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    // private methods
    
    /**
     * returns true if any of lists are empty or not intersected
     * vsList1 is from existing application-ref and vsList2 is from the 
     * the application being deployed
     **/
    private static boolean isVSListsIntersected(String vsList1, String vsList2)
    {
        if (vsList1==null || vsList1.length()==0 ) {
            return true;
        }

        if (vsList2 == null || vsList2.length()==0 ) {
            String[] arr = vsList1.split(",");
            if (arr.length == 1 && arr[0].equals(
                com.sun.enterprise.web.VirtualServer.ADMIN_VS)) {
                return false;
            }
            return true;
        }

        String[] arr1 = vsList1.split(",");
        String[] arr2 = vsList2.split(",");
        if(arr1.length==0 || arr2.length==0)
            return true;
        return (getFirstCommonElement(arr1, arr2)!=null);
            
    }
    
    /**
     * returns null if arrays have equal elements
     **/
    private static Object getFirstCommonElement(Object[] arr1, Object[] arr2)
    {
        if(arr1!=null && arr2!=null && arr1.length!=0 && arr2.length!=0)
        {
            for(int i=0; i<arr1.length; i++)
            {
                for(int j=0; j<arr2.length; j++)
                {
                    if( ((String)arr1[i]).startsWith("/") ) {
                        arr1[i] = ((String)arr1[i]).substring(1);
                    }
                    if( ((String)arr2[j]).startsWith("/") ) {
                        arr2[j] = ((String)arr2[j]).substring(1);
                    }
                    if(arr1[i].equals(arr2[j])) {
                        return arr1[i];
                    }
                }
            }
        }
        return null;
    }
    
}
