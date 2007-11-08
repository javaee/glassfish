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
package com.sun.enterprise.ee.synchronization;

import java.io.FileOutputStream;
import java.io.PrintStream;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManagerBase;

import com.sun.enterprise.security.store.IdentityManager;
import com.sun.enterprise.admin.server.core.jmx.ssl.InstanceSyncWithDAS_TlsClientEnvSetter;
/**
 * Main driver class for server instance synchronization.
 *
 * Usage: SynchronizationMain <http-servlet-url> <synchronization-meta-xml>
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class SynchronizationMain {

    private static Logger _logger = Logger.getLogger(
            EELogDomains.SYNCHRONIZATION_LOGGER);

    private static StringManagerBase _strMgr = 
          StringManagerBase.getStringManager(_logger.getResourceBundleName());

    /**
     * Synchronizes a server instance cache with central repository.
     * Return code of zero means synchronization is complete without
     * any exception. The return code is non-zero when there is a 
     * failure in synchronization.
     *
     * @param  args  command line arguments passed in to this program
     */
    public static void main(String[] args) {

        int returnCode = 1;

               // read in parameters off the stdin if they exist
        try {
            IdentityManager.populateFromInputStreamQuietly();
        } catch (IOException e) {
            _logger.log(Level.WARNING, "failureOnReadingSecurityIdentity", e);
        }
        _logger.log(Level.INFO, IdentityManager.getFormatedContents());

        PrintStream printStream = null;
        try {
            try {
                String logFile = System.getProperty("com.sun.aas.defaultLogFile");
                printStream = new PrintStream(new FileOutputStream(logFile, true), true);
            } catch (Exception ex) {
                //ignore
            }
            String iRoot = System.getProperty(
                    SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);

            new InstanceSyncWithDAS_TlsClientEnvSetter().setup();
            
            String url = null;
            String xml = null;

            // only servlet url is specified
            if (args.length == 1) {
                // synchronization servlet url
                url = args[0];

                // use default meta xml file
                xml = SynchronizationDriverFactory.INSTANCE_CONFIG_URL;

            // both servlet url and meta xml is specified
            } else if (args.length == 2) {
                url = args[0];
                xml = args[1];

            // nothing is specified
            } else {
                xml = SynchronizationDriverFactory.INSTANCE_CONFIG_URL;
            }

            SynchronizationDriver sd = SynchronizationDriverFactory.
                                    getSynchronizationDriver(iRoot, xml, url);
            sd.synchronize();

            // synchronization is complete
            returnCode = 0;

        } catch (Throwable e) {
            try {
              String logFile = System.getProperty("com.sun.aas.defaultLogFile");
              printStream = 
                  new PrintStream(new FileOutputStream(logFile, true), true);
              e.printStackTrace(printStream);
              _logger.log(Level.WARNING, "synchronization.sync_fail", e);
            } catch (Exception ex) {
                //ignore
            } finally {
                if (printStream != null) printStream.close();
            }
        }

        System.exit(returnCode);
    }
}