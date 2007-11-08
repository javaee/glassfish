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


package com.sun.persistence.cmp.ejbc;

import com.sun.ejb.codegen.EjbcContext;
import com.sun.ejb.codegen.GeneratorException;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.cmp.*;
import com.sun.persistence.cmp.PersistenceJarLoader;
import com.sun.persistence.utility.logging.Logger;
import com.sun.jdo.spi.persistence.support.ejb.ejbc.JDOCodeGeneratorHelper;

import java.util.Collection;
import java.util.Collections;

/**
 * This class implements {@link com.sun.ejb.codegen.CMPGenerator} which is the
 * contract between appserv-core and appserv-persistence module for code
 * genartion. It is instantiated using reflection by {@link
 * com.sun.ejb.codegen.CmpCompiler}. It uses mixin design pattern.
 * It extends {@link
 * com.sun.jdo.spi.persistence.support.ejb.ejbc.JDOCodeGenerator} for
 * the CMP 2.x functionality and delegates to {@link PersistenceJarLoader}
 * for the EJB 3.0 related functionality.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
/* TODO:
 * In future, when we have a separate
 * .par file for EJB 3.0 entities, this class will stop extending {@link
 * com.sun.jdo.spi.persistence.support.ejb.ejbc.JDOCodeGenerator} and
 * lots of if checks that we have in this class will vanish.
 * Since we will have two CodeGens, one for pre 3.0 entities and one for
 * 3.0 entities, we should also rename these classes.
 * Enhancer for 3.0 is supposed to be called from this class in addition
 * to model mapping.
 */
public class JDOCodeGenerator
        extends com.sun.jdo.spi.persistence.support.ejb.ejbc.JDOCodeGenerator {

    private final static I18NHelper i18NHelper = I18NHelper.getInstance(
            LogHelperCMP.class);

    private final static Logger logger = LogHelperCMP.getLogger();

    private EjbBundleDescriptor ejbBundleDescriptor;

    private PersistenceJarLoader pjl;

    /**
     * need a public no arg constructor as this is used in {@link
     * Class#newInstance()} by  {@link com.sun.ejb.codegen.CmpCompiler}.
     */
    public JDOCodeGenerator() {
    }

    /**
     * {@inheritDoc}
     */
    @Override public void init(
            EjbBundleDescriptor ejbBundleDescriptor, EjbcContext ejbcContext,
            String bundlePathName) throws GeneratorException {
        logger.info(i18NHelper.msg("MSG_JDOCodeGeneratorInit", // NOI18N
                JDOCodeGeneratorHelper.getModuleName(ejbBundleDescriptor)));

        this.ejbBundleDescriptor = ejbBundleDescriptor;
        if(ejbBundleDescriptor.containsCMPEntity()) {
            // do the pre EJB 3.0 related stuff here.
            super.init(ejbBundleDescriptor, ejbcContext, bundlePathName);
        }

        if(ejbBundleDescriptor.containsPersistenceEntity()) {
            // now do the EJB 3.0 specific task
            pjl = new PersistenceJarLoader();
            pjl.load(ejbBundleDescriptor);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public Collection cleanup() throws GeneratorException {
        logger.info(i18NHelper.msg("MSG_JDOCodeGeneratorCleanup", // NOI18N
                JDOCodeGeneratorHelper.getModuleName(ejbBundleDescriptor)));

        Collection result = null;
        if(ejbBundleDescriptor.containsCMPEntity()) {
            // do the pre EJB 3.0 related stuff here.
            result=super.cleanup();
        }
        if(ejbBundleDescriptor.containsPersistenceEntity()) {
            // now do the EJB 3.0 specific task
            pjl.unload();
        }
        // we have nothing to return for 3.0 beans until we integrate
        // enhancer with deployment. So as per the contract,
        // we return an empty collection and not a null collection if
        // we have only 3.0 beans.
        return result == null ? Collections.emptyList() : result;
    }
}
