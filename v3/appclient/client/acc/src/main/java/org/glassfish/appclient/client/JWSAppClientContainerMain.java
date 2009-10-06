/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.appclient.client;

import com.sun.enterprise.glassfish.bootstrap.ASMainStatic;
import com.sun.enterprise.glassfish.bootstrap.MaskingClassLoader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.glassfish.appclient.client.acc.UserError;
import org.glassfish.appclient.client.jws.boot.ErrorDisplayDialog;
import org.glassfish.appclient.client.jws.boot.LaunchSecurityHelper;

/**
 *
 * @author tjquinn
 */
public class JWSAppClientContainerMain {

    public static final String SECURITY_CONFIG_PATH_PLACEHOLDER = "security.config.path";

    private static Logger logger = Logger.getLogger(JWSAppClientContainerMain.class.getName());

    /** localizable strings */
    private static final ResourceBundle rb =
        ResourceBundle.getBundle(
            JWSAppClientContainerMain.class.getPackage().getName().replaceAll("\\.", "/") + ".LocalStrings");



    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            final long now = System.currentTimeMillis();

            final String agentArgsText = System.getProperty("agent.args");
            LaunchSecurityHelper.setPermissions();
            insertMaskingLoader();

            AppClientFacade.prepareACC(agentArgsText, null);
            AppClientFacade.launch(args);

            logger.fine("JWSAppClientContainer finished after " + (System.currentTimeMillis() - now) + " ms");

        } catch (UserError ue) {
            ErrorDisplayDialog.showUserError(ue, rb);
        } catch (Throwable thr) {
            /*
             *Display the throwable and stack trace to System.err, then
             *display it to the user using the GUI dialog box.
             */
            System.err.println(rb.getString("jwsacc.errorLaunch"));
            System.err.println(thr.toString());
            thr.printStackTrace();
            ErrorDisplayDialog.showErrors(thr, rb);
            System.exit(1);
        }

    }

    private static void insertMaskingLoader() throws IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final String loaderConfig = System.getProperty("loader.config");
        StringReader sr = new StringReader(loaderConfig);
        final Properties props = new Properties();
        props.load(sr);

        final ClassLoader jwsLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader mcl = ASMainStatic.getMaskingClassLoader(
                jwsLoader.getParent(), props);

        final Field jwsLoaderParentField = ClassLoader.class.getDeclaredField("parent");
        jwsLoaderParentField.setAccessible(true);
        jwsLoaderParentField.set(jwsLoader, mcl);
    }

}
