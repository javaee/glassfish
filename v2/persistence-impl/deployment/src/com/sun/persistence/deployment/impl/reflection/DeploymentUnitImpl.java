/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * DeploymentUnit.java
 *
 * Created on March 21, 2005, 3:50 PM
 */


package com.sun.persistence.deployment.impl.reflection;

import com.sun.persistence.api.deployment.DeploymentUnit;
import com.sun.persistence.api.deployment.JavaModel;
import com.sun.persistence.api.deployment.PersistenceJarDescriptor;
import com.sun.persistence.deployment.impl.LogHelperDeployment;
import com.sun.persistence.utility.logging.Logger;
import com.sun.org.apache.jdo.util.I18NHelper;

/**
 * This is an implementation of {@link DeploymentUnit}. Since it is used during
 * deployment, it knows about a class loader. It uses that class loader to
 * create a {@link JavaModel}.
 *
 * @author Sanjeeb Sahoo
 */
public class DeploymentUnitImpl implements DeploymentUnit {

    private PersistenceJarDescriptor pjar;

    private ClassLoader cl;

    private JavaModel javaModel;

    private final static I18NHelper i18NHelper = I18NHelper.getInstance(
            LogHelperDeployment.class);

    private final static Logger logger= LogHelperDeployment.getLogger();

    /**
     * Creates a new instance of DeploymentUnitImpl
     *
     * @param pjar the descriptor object graph
     * @param cl   the class loader for this deployment unit. If cl is {@code
     *             null} it sets {@code Thread.currentThread().getContextClassLoader()}
     *             as the classloader.
     */
    public DeploymentUnitImpl(PersistenceJarDescriptor pjar, ClassLoader cl) {
        this.pjar = pjar;
        if (cl == null) {
            logger.fine(i18NHelper.msg("MSG_DUInitWithNullCL"));
        } else {
            setClassLoader(cl);
        }
    }

    public PersistenceJarDescriptor getPersistenceJar() {
        return pjar;
    }

    public JavaModel getJavaModel() {
        return javaModel;
    }

    public ClassLoader getClassLoader() {
        return cl;
    }

    // This is a public method because the class loader used during deployment
    // is subsequently thrown away by app server and a new class loader is
    // created for the application which is used during runtime.
    // See PersistenceJarLoader which uses this method.
    public void setClassLoader(ClassLoader cl) {
        this.cl = cl;
        javaModel = new JavaModelImpl(cl);
    }
}

