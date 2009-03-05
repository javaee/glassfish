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

package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.KeepAlive;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.ConfigSupport;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyVetoException;

/**
 * HttpService related tests
 *
 * @author Jerome Dochez
 */
public class HttpServiceTest extends ConfigApiTest {


    public String getFileName() {
        return "DomainTest";
    }

    HttpService httpService = null;

    @Before
    public void setup() {
        httpService = getHabitat().getComponent(HttpService.class);
        assertTrue(httpService!=null);
    }

    @Test
    public void connectionTest() {
        logger.fine("Max connections = " + httpService.getKeepAlive().getMaxConnections());
        assertTrue(httpService.getKeepAlive().getMaxConnections().equals("250"));
    }

    @Test
    public void validTransaction() throws TransactionFailure {
        logger.fine("before..." +httpService.getKeepAlive().getThreadCount() );

        ConfigSupport.apply((new SingleConfigCode<HttpService>() {
            public Object run(HttpService okToChange) throws PropertyVetoException, TransactionFailure {
                    KeepAlive newKeepAlive = okToChange.createChild(KeepAlive.class);
                newKeepAlive.setMaxConnections(httpService.getKeepAlive().getMaxConnections());
                newKeepAlive.setThreadCount("3");
                newKeepAlive.setTimeoutInSeconds("65");
                okToChange.setKeepAlive(newKeepAlive);
                return newKeepAlive;
            }
        }), httpService);

        try {
            ConfigSupport.apply((new SingleConfigCode<KeepAlive>() {
                public Object run(KeepAlive param) throws PropertyVetoException, TransactionFailure {
                    param.setThreadCount("7");
                    throw new TransactionFailure("Sorry, changed my mind", null);
                }
            }), httpService.getKeepAlive());
        } catch(TransactionFailure e) {
            logger.fine("good, got my exception about changing my mind");
        }
        logger.fine("after..." +httpService.getKeepAlive().getThreadCount() );
        // let's try an invalid set
        try {
            httpService.getKeepAlive().setThreadCount("5");
        } catch (PropertyVetoException e) {
            logger.fine("excellent, we get the expected exception");
        }
        logger.fine("final..." +httpService.getKeepAlive().getThreadCount() );
        assertTrue(httpService.getKeepAlive().getThreadCount().equals("3"));
    }

    @Test(expected=TransactionFailure.class)
    public void invalidTransaction() throws TransactionFailure {

            ConfigSupport.apply((new SingleConfigCode<HttpService>() {
            public Object run(HttpService okToChange) throws PropertyVetoException, TransactionFailure {
                KeepAlive newKeepAlive = okToChange.createChild(KeepAlive.class);
                newKeepAlive.setMaxConnections("500");
                newKeepAlive.setThreadCount("5");
                newKeepAlive.setTimeoutInSeconds("65");
                okToChange.setKeepAlive(newKeepAlive);
                // this should fail
                okToChange.getHttpProtocol().setDefaultType("text/css");
                return newKeepAlive;
            }
        }), httpService);
        assertTrue(httpService.getKeepAlive().getThreadCount().equals("3"));
    }
}