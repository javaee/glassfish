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
 * EJBCompiler.java
 *
 * Created on February 23, 2002, 12:02 PM
 * 
 * @author  bnevins
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/backend/EJBCompiler.java,v $
 *
 */

package com.sun.enterprise.deployment.backend;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;

import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.zip.ZipItem;

import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.EjbModulesManager;
import com.sun.enterprise.instance.WebModulesManager;
import com.sun.enterprise.instance.ModulesManager;
import com.sun.enterprise.instance.UniqueIdGenerator;

import com.sun.enterprise.loader.EJBClassPathUtils;
import com.sun.ejb.codegen.IASEJBC;
import com.sun.ejb.codegen.IASEJBCTimes;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.loader.EJBClassLoader;


import com.sun.enterprise.security.acl.RoleMapper;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;
import com.sun.enterprise.security.application.EJBSecurityManager;

import java.io.IOException;
import org.xml.sax.SAXParseException;
import com.sun.enterprise.config.ConfigException;

/** 
 * This class is responsible mainly for executing ejbc. 
 * It also does the following:
 * <pre>
 *    - constructs the class loader for the deployed app   
 *    - loads the deployment descriptors
 *    - calls ejbc
 *    - sets the unique id
 *    - writes the deployment descriptors 
 *
 * classpath: Ejbc reads the target instance's classpath-prefix and
 * classpath-suffix via config context. It creates a class loader 
 * hierarchy based on these class paths, common class loader urls
 * ($INSTANCE/lib/classes:$INSTANCE/lib/*.jar:$INSTANCE/lib/*.zip) 
 * and the application's urls.
 *
 * BootStrap<-System<-Common<-Share<-Ejb<-Servlet<-Jasper [Admin Server]
 *            |
 *            |<-(classpath-prefix+classpath-suffix)<-Common<-Shared<-Ejb [EJBC]
 *
 * Note that System classpath is shared between the two hierarchy.  
 * According to the architect, end users are not expected to replace the
 * Application Server's jar (for example, appserv-rt.jar, etc). So, it is
 * sufficient to include the classpath-prefix and classpath-suffix in the 
 * ejbc class loader hierarchy. Any system level jar should be replaced 
 * accross all server instances.
 *
 * S1AS8 Change: 
 *   Deployment no longer needs to pick up class paths from target 
 *   server instance. Deployment class paths contains only the DAS's 
 *   class path plus the whole application paths.
 * 
 * rmic-options: Ejbc reads rmic options from server xml. If none found, it
 * gets the default from DTD.
 *
 * javac-options: Ejbc reads javac options from server xml. It supports 
 * the "-g" and "-O" options.
 * </pre>
 */
public class EJBCompiler 
{
    /**
     *Returns new instance of EJBCompiler, using the deployment request's classpath list as the classpath
     *to be used during compilation.
     *
     */
    public EJBCompiler
    (
        String              name, 
        File                srcDir, 
        File                stubsDir, 
        BaseManager         manager,
        DeploymentRequest   request,
        IASEJBCTimes        timing

    ) throws IASDeploymentException
    
    {
        this(name, srcDir, stubsDir, manager, request, timing, request.getCompleteClasspath());
    }

    /**
     *Returns new instance of EJBCompiler, accepting an alternate classpath List to be used during compilation.
     *<p>
     *This added constructor supports the fix for bug 4980750 so WebModuleDeployer can specify an adjusted
     *classpath for use during compilation.  
     */
    public EJBCompiler
        (
        String              name, 
        File                srcDir, 
        File                stubsDir, 
        BaseManager         manager,
        DeploymentRequest   request,
        IASEJBCTimes        timing,
        java.util.List                classpathForCompilation

    ) throws IASDeploymentException

    {
        assert StringUtils.ok(name);
        assert FileUtils.safeIsDirectory(srcDir);
        assert manager != null;
        assert request != null;
        assert timing != null;

        try 
        {
            this.name			= name; 
            this.request        = request;
            this.classpathForCompilation = classpathForCompilation;

            // context used during code gen
            this.ejbcContext    = new EjbcContextImpl();
            ejbcContext.setSrcDir(srcDir);
            ejbcContext.setStubsDir(stubsDir);
            ejbcContext.setTiming(timing);
            ejbcContext.setRmicOptions( manager.getRmicOptions() );
            ejbcContext.setJavacOptions( manager.getJavacOptions() );
            ejbcContext.setOptionalArguments(this.request.getOptionalArguments());
            ejbcContext.setDeploymentRequest(request);

            if(manager instanceof ModulesManager)
            {
                moduleManager	= (ModulesManager)manager;
                appManager		= null;
            }
            else if(manager instanceof AppsManager)
            {
                appManager		= (AppsManager)manager;
                moduleManager	= null;
            }
            else
                assert (false);
        }
        catch(Throwable t)
        {
            throw wrapException(t);
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    public ZipItem[] compile()	throws IASDeploymentException
    {
        ZipItem[] clientStubs = null;
        try
        {
            if(request.getNoEJBC())
            {
                _logger.info( "**********  EJBC Skipped! ************" );
                return new ZipItem[] {};
            }
            
            if(appManager != null)
            {
                clientStubs = preDeployApp();
            }
            else
            {
                clientStubs = preDeployModule();
            }

            Application app = ejbcContext.getDescriptor();
            if (app != null) 
            {
                for (Iterator iter = app.getEjbDescriptors().iterator();
			iter.hasNext();)
		{
		    //translate deployment descriptor to load PolicyConfiguration
		    EJBSecurityManager.loadPolicyConfiguration((EjbDescriptor)iter.next());
                }
            }
        }
        catch(Throwable t)
        {
            throw wrapException(t);
        } 
        return clientStubs;
    }

    /**
     * EJBC for an application. This is called from the deployment backend.
     * clientDstJar is the EAR that was deployed. It has been saved in
     * classLoadable format (i.e. all classes in all jars contained in the EAR
     * have been expanded out from the root of the EAR so that they can be
     * loaded by the JarClassLoader).
     *
     *
     * @return   the client stubs as zip entries; these entries are added to 
     *           the client jar
     * 
     * @exception  Exception   error while running ejbc
     */
    private ZipItem[] preDeployApp() throws Exception
    {
        ZipItem[] clientStubs = null;

        Application application = request.getDescriptor();

        String appRoot = ejbcContext.getSrcDir().getCanonicalPath();
	    setEjbClasspath(request.getModuleClasspath());
        ejbcContext.setDescriptor(application);

        /*
         *Bug 4980750 - See also the comments in WebModuleDeployer
         *for a discussion of the root cause of this bug.  Use the List of classpath entries set by the
         *constructor (either by default to preserve the old behavior or via an explicit argument passed from
         *the method that instantiated EJBCompiler to supply an alternate classpath List).
         */
        // converts all the class paths to an array
        String[] classPathUrls = new String[this.classpathForCompilation.size()];
        classPathUrls = (String[]) this.classpathForCompilation.toArray(classPathUrls);
        
        ejbcContext.setClasspathUrls(classPathUrls);

        // verify ejbc context
        verifyContext();

        // calls ejbc
        clientStubs = IASEJBC.ejbc(this.ejbcContext);

        UniqueIdGenerator uidGenerator = UniqueIdGenerator.getInstance();
        long uid = uidGenerator.getNextUniqueId();

        application.setUniqueId(uid);

        this.appManager.saveAppDescriptor(this.name, application, 
            request.getDeployedDirectory().getCanonicalPath(),
            request.getGeneratedXMLDirectory().getCanonicalPath(), false);

        return clientStubs;
    }

    /**
     * Sets the ejb portion of the class path in the ejbc context.
     *
     * @param  ejbClassPath  list of ejb class path urls for this application
     */
    private void setEjbClasspath(List ejbClassPath) {

        String[] ejbClassPathUrls = new String[ejbClassPath.size()];
        ejbClassPathUrls = (String[]) ejbClassPath.toArray(ejbClassPathUrls);
        this.ejbcContext.setEjbClasspathUrls(ejbClassPathUrls);
    }

    /**
     * EJBC for a stand alone ejb module.
     *
     * @return   the client stubs as zip entries; these entries are added to 
     *           the client jar
     *
     * @exception  Exception   error while running ejbc
     */
    private ZipItem[] preDeployModule() throws Exception
    {
        ZipItem[] clientStubs = null;

        Application application = request.getDescriptor();
        ejbcContext.setDescriptor(application);

        /*
         *Bug 4980750 - See also the comments in WebModuleDeployer
         *for a discussion of the root cause of this bug.  Use the List of classpath entries set by the
         *constructor (either by default to preserve the old behavior or via an explicit argument passed from
         *the method that instantiated EJBCompiler to supply an alternate classpath List).
         */
        // converts all the class paths to an array
        String[] classPathUrls = new String[this.classpathForCompilation.size()];
        classPathUrls = (String[]) this.classpathForCompilation.toArray(classPathUrls);
        
        ejbcContext.setClasspathUrls(classPathUrls);

        // verify ejbc context
        verifyContext();

        // calls ejbc
        clientStubs = IASEJBC.ejbc(this.ejbcContext);
        
        
        
        UniqueIdGenerator uidGenerator = UniqueIdGenerator.getInstance();
        long uid = uidGenerator.getNextUniqueId();
        
        for (Iterator itr = application.getEjbBundleDescriptors().iterator();
            itr.hasNext();)
        {
            
            // for stand alone ejb module, there will be
            // one EjbBundleDescriptor
            EjbBundleDescriptor bundleDesc = (EjbBundleDescriptor) itr.next();
            
            // sets the unique id of all the beans in this bundle
            bundleDesc.setUniqueId(uid);
        }
        this.moduleManager.saveAppDescriptor(this.name, application, 
            request.getDeployedDirectory().getCanonicalPath(),
            request.getGeneratedXMLDirectory().getCanonicalPath(), true);

        return clientStubs;
    }

    /**
     * Throws an exception if required elements of the ejbc context is null.
     */
    void verifyContext() throws IASDeploymentException
    {
        if ( (this.ejbcContext.getSrcDir() == null) 
                || (this.ejbcContext.getStubsDir() == null)
                || (this.ejbcContext.getDescriptor() == null)
                || (this.ejbcContext.getRmicOptions() == null)
                || (this.ejbcContext.getJavacOptions() == null) 
                || (this.ejbcContext.getTiming() == null)) 
        {
            // error - required element is missing
            String msg = localStrings.getString(
                "enterprise.deployment.backend.bad_ejbc_ctx");
            throw new IASDeploymentException(msg);
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    IASDeploymentException wrapException(Throwable t)
    {
        String msg = localStrings.getString(
                        "enterprise.deployment.backend.fatal_ejbc_error" );

        if(t instanceof java.rmi.RemoteException)
        {
            msg += localStrings.getString(
                    "enterprise.deployment.backend.ejbc_remoteexception", t );
        }

        return new IASDeploymentException(msg, t);
    }


    ///////////////////////////////////////////////////////////////////////////

    private String              name; 
    private ModulesManager      moduleManager;
    private AppsManager         appManager;
    private DeploymentRequest   request;
    private EjbcContextImpl     ejbcContext;
    
    /** Added to support fix for 4980750 so WebModuleDeployer can specify an altered classpath for use during compilation. */
    private java.util.List                classpathForCompilation;

    private static final Logger _logger = DeploymentLogger.get();
    private static StringManager localStrings =
            StringManager.getManager( EJBCompiler.class );
}
