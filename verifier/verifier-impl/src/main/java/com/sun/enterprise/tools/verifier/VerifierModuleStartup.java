/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.enterprise.tools.verifier;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.tools.verifier.gui.MainFrame;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.internal.api.Globals;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.framework.BundleException;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.FrameworkEvent;

import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.io.IOException;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
public class VerifierModuleStartup implements ModuleStartup
{
    @Inject
    private Habitat habitat;

    @Inject
    private ModulesRegistry mr;

    @Inject
    PackageAdmin pa;

    // force initialization of Globals, as many appserver modules
    // use Globals.
    @Inject
    Globals globals;

    private StartupContext startupContext;
    private int failedCount;
    private ClassLoader oldCL;

    public void setStartupContext(StartupContext context)
    {
        this.startupContext = context;
    }

    public void start()
    {
        setTCL();
        try {
            registerFrameworkListener();
            String[] args = startupContext.getOriginalArguments();
            VerifierFrameworkContext verifierFrameworkContext =
                    new Initializer(args).getVerificationContext();

            // The reason for not injecting a Verifier in this class is that
            // Verifier is a PerLookup scoped object and this class is a
            // Singleton scoped service. So, injections does not make sense.
            Verifier verifier = habitat.getComponent(Verifier.class);
            try
            {
                verifier.init(verifierFrameworkContext);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
            }
            if (verifierFrameworkContext.isUsingGui()) {
                MainFrame mf = new MainFrame(
                        verifierFrameworkContext.getJarFileName(), true, verifier);
                mf.setSize(800, 600);
                mf.setVisible(true);
            } else {
                LocalStringManagerImpl smh = StringManagerHelper.getLocalStringsManager();
                try {
                    verifier.verify();
                } catch (Exception e) {
                    LogRecord logRecord = new LogRecord(Level.SEVERE,
                            smh.getLocalString(
                                    verifier.getClass().getName() +
                                    ".verifyFailed", // NOI18N
                                    "Could not verify successfully.")); // NOI18N
                    logRecord.setThrown(e);
                    verifierFrameworkContext.getResultManager().log(logRecord);
                }
                try
                {
                    verifier.generateReports();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                }
                failedCount = verifierFrameworkContext.getResultManager()
                        .getFailedCount() +
                        verifierFrameworkContext.getResultManager().getErrorCount();
            }
        } finally {
            unsetTCL();
        }

    }

    private void registerFrameworkListener()
    {
        final Bundle bundle = pa.getBundle(getClass());
        bundle.getBundleContext().addFrameworkListener(new FrameworkListener(){
            public void frameworkEvent(FrameworkEvent event)
            {
                final Bundle systemBundle = bundle.getBundleContext().getBundle(0);
                switch (event.getType()) {
                    case FrameworkEvent.STARTED :
                        try
                        {
                            systemBundle.stop();
                        }
                        catch (BundleException e)
                        {
                            throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                        }
                        System.out.println("Stopped " + systemBundle);
                        // We need to exit with proper status so that
                        // programs like Ant tasks can rely on exit status
                        // of verifier to determine status of verification.
                        System.exit(failedCount);
                        break;
                    // TODO(Sahoo): Exit in STOPPED event when we upgrade to OSGi R4.2
//                    case FrameworkEvent.STOPPED :
//                        System.exit(failedCount);
//                        break;
                    case FrameworkEvent.ERROR :
                        try
                        {
                            systemBundle.stop();
                            System.out.println("Stopped " + systemBundle);
                        }
                        catch (BundleException e)
                        {
                            throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                        }
                        System.exit(-1);
                }
            }
        });
    }

    public void stop()
    {
    }

    private void setTCL() {
        oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
    }

    private void unsetTCL() {
        Thread.currentThread().setContextClassLoader(oldCL);
    }
}
