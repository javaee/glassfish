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

package org.glassfish.appclient.client.acc.agent;

import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;
import org.glassfish.appclient.client.AppClientFacade;
import org.glassfish.appclient.client.acc.UserError;

/**
 * Agent which prepares the ACC before the VM launches the selected main program.
 * <p>
 * This agent gathers processes agent arguments, supplied either by the
 * appclient script or the end-user (when entering a java command directly),
 * and processes those arguments.  The primary purpose is to:
 * <ol>
 * <li>identify the main class that the Java launcher has decided to start,
 * <li>create and initialize a new app client container instance, asking the
 * ACC to load and inject the indicated main class in the process <b>if and only if</b>
 * the main class is not the AppClientCommand class.
 * </ol>
 * Then the agent is done.  The java launcher and the VM see to it that the main class's
 * main method is invoked.
 *
 * @author tjquinn
 */
public class AppClientContainerAgent {

    private static Logger logger = Logger.getLogger(AppClientContainerAgent.class.getName());

    public static void premain(String agentArgsText, Instrumentation inst) {
        try {
            final long now = System.currentTimeMillis();

            /*
             * The agent prepares the ACC but does not launch the client.
             */
            AppClientFacade.prepareACC(agentArgsText,inst);
    
            logger.fine("AppClientContainerAgent finished after " + (System.currentTimeMillis() - now) + " ms");

        } catch (UserError ue) {
            ue.displayAndExit();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
