/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.test.jms.defaultcf.ejb;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.*;
import javax.naming.InitialContext;

/**
 *
 * @author LILIZHAO
 */
@Stateless(mappedName="SessionBeanDefault/remote")
public class SessionBeanDefault implements SessionBeanDefaultRemote {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Resource(name="myCF0", lookup="jms/__defaultConnectionFactory")
    private ConnectionFactory cf0;
    
    @Resource(name="myCF1", lookup="java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory cf1;
    
    @Resource(name="myCF2")
    private ConnectionFactory cf2;

    @Override
    public void sendMessage(String text) {
        Connection conn = null;
        Session session = null;
        try {
            InitialContext ic = new InitialContext();
            ConnectionFactory o1 = (ConnectionFactory) ic.lookup("java:comp/DefaultJMSConnectionFactory");
            ConnectionFactory o2 = (ConnectionFactory) ic.lookup("java:comp/env/jms/systemDefaultCF");
            if (o1 == null || o2 == null || cf0 == null || cf1 == null || cf2 == null)
                throw new RuntimeException("Failed to lookup up jms default connection factory.");
            conn = cf2.createConnection();
            conn.start();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            TextMessage msg = session.createTextMessage(text);
            MessageProducer p = session.createProducer(queue);
            p.send(msg);
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e) {
                    throw new EJBException(e);
                }
            }
            if (conn != null) {
                try {
                    
                    conn.close();
                } catch (Exception e) {
                    throw new EJBException(e);
                }
            }
        }
    }

    @Override
    public boolean checkMessage(String text) {
        Connection conn = null;
        Session session = null;
        try {
            conn = cf1.createConnection();
            conn.start();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer r = session.createConsumer(queue);
            Message msg = r.receive(30000L);
            if (msg instanceof TextMessage) {
                String content = ((TextMessage) msg).getText();
                if (text.equals(content))
                    return true;
            }
            return false;
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e) {
                    throw new EJBException(e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    throw new EJBException(e);
                }
            }
        }
    }
}
