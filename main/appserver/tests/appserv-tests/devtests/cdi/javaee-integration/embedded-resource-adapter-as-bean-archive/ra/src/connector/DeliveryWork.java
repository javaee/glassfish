/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package connector;

import java.lang.reflect.Method;

import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.work.Work;

/**
 * 
 * @author Qingqing Ouyang
 */
public class DeliveryWork implements Work {

    private MessageEndpoint ep;
    private int num;
    private String op;
    private boolean keepCount;
    private static int counter = 0;

    public DeliveryWork(MessageEndpoint ep, int numOfMessages, String op) {
        this.ep = ep;
        this.num = numOfMessages;
        this.op = op;
        this.keepCount = false;
    }

    public DeliveryWork(MessageEndpoint ep, int numOfMessages, String op,
            boolean keepCount) {
        this.ep = ep;
        this.num = numOfMessages;
        this.op = op;
        this.keepCount = keepCount;
    }

    public void run() {

        debug("ENTER...");

        try {
            // Method onMessage = getOnMessageMethod();
            // ep.beforeDelivery(onMessage);

            if (!keepCount) {
                for (int i = 0; i < num; i++) {
                    String msgId = String.valueOf(i);
                    String msgBody = "This is message " + msgId;
                    String msg = msgId + ":" + msgBody + ":" + op;
                    ((MyMessageListener) ep).onMessage(msg);
                }
            } else {
                for (int i = 0; i < num; i++) {
                    String msgId = String.valueOf(i + counter);
                    String msgBody = "This is message " + msgId;
                    String msg = msgId + ":" + msgBody + ":" + op;
                    ((MyMessageListener) ep).onMessage(msg);
                }
                counter = counter + num;
            }

            // ep.afterDelivery();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        debug("LEAVE...");
    }

    public void release() {
    }

    public String toString() {
        return op;
    }

    private Method getOnMessageMethod() {

        Method onMessageMethod = null;
        try {
            Class msgListenerClass = connector.MyMessageListener.class;
            Class[] paramTypes = { java.lang.String.class };
            onMessageMethod = msgListenerClass.getMethod("onMessage",
                    paramTypes);

        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
        return onMessageMethod;
    }

    private void debug(String mesg) {
        System.out.println("DeliveryWork[" + op + "] --> " + mesg);
    }
}
