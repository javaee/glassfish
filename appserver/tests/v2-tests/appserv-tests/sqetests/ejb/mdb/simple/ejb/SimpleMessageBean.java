/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.ejb.mdb.simple.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.CreateException;
import javax.naming.*;
import javax.jms.*;
import java.util.*;

public class SimpleMessageBean implements MessageDrivenBean,
    MessageListener {

    private transient MessageDrivenContext mdc = null;
    private Context context;
    private TextMessage msg = null;
    private ArrayList messageList=new ArrayList();
    public javax.naming.Context jndiContext;
    private static int beancount=0;
    public static final String  TOPICCONFAC = "jms/TCFactory";
    public SimpleMessageBean() {
        beancount++;
        System.out.println("MESSAGE BEAN:["+beancount+"].SimpleMessageBean()");        
    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        System.out.println("In "
            + "MESSAGE BEAN:["+beancount+"].setMessageDrivenContext()");
	this.mdc = mdc;
         try {
            jndiContext=new javax.naming.InitialContext();
             }catch(Throwable e) {
          
          System.out.println(e.toString());
    }
    }

    public void ejbCreate() {
	System.out.println("MESSAGE BEAN: SimpleMessageBean.ejbCreate()");
    }

    public void onMessage(Message inMessage) {
        try {
            //inMessage.acknowledge();
            if (inMessage instanceof TextMessage) {
                msg = (TextMessage) inMessage;
                System.out.println("MESSAGE BEAN: Message received: "
                + msg.getText());
            } else {
                System.out.println("Message of wrong type: "
                + inMessage.getClass().getName());
            }
            messageList.add(msg);
            sendMessage(msg);
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (Throwable te) {
            te.printStackTrace();
        }
    }  // onMessage
    
    public void sendMessage(Message message) {
        System.out.println("MESSAGE BEAN: sendMessage back to appclient");        
        try{
            
            TopicConnectionFactory topicfactory=(TopicConnectionFactory)jndiContext.lookup(TOPICCONFAC);
            Topic topic=(Topic)jndiContext.lookup("java:comp/env/jms/SampleTopic");          
                        
            TopicConnection 
            
            connect = topicfactory.createTopicConnection();
            
            TopicSession session = connect.createTopicSession(false,0);
            
            TopicPublisher publisher=session.createPublisher(topic);
            Thread.sleep(3000);
            publisher.publish(message);                                
            System.out.println("<<Sent Message back to appclient >>");
            
        }catch(Throwable e) {
            System.out.println("!!!!MESSAGE BEAN: sendMessage Exception");
            e.printStackTrace();
        }
    }  
      
    

    public void ejbRemove() {
        System.out.println("In SimpleMessageBean.remove()");
    }
} // class
