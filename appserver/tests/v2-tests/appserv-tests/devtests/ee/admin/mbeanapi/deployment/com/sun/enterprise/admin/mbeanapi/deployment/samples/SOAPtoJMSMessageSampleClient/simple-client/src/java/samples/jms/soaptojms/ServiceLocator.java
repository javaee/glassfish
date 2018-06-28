/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2018 Oracle and/or its affiliates. All rights reserved.
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

package samples.jms.soaptojms;


import java.net.URL;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.jms.QueueConnectionFactory;
import javax.jms.Queue;
import javax.jms.TopicConnectionFactory;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;

import samples.jms.soaptojms.ServiceLocatorException;

/**
 *  This class is an implementation of the Service Locator pattern. It is
 *  used to looukup resources such as JMS Destinations, etc.
 */
public class ServiceLocator {

    private InitialContext ic;    

    public ServiceLocator() throws ServiceLocatorException  {
      try {
        ic = new InitialContext();       
      } catch (NamingException ne) {
            throw new ServiceLocatorException(ne);
      } catch (Exception e) {
            throw new ServiceLocatorException(e);
      } 
    }

    
   /**
     * This method helps in obtaining the topic factory
     * @return the factory for the factory to get topic connections from
     */
    public  TopicConnectionFactory getTopicConnectionFactory(String topicConnFactoryName) throws ServiceLocatorException {
      TopicConnectionFactory factory = null;
      try {
        factory = (TopicConnectionFactory) ic.lookup(topicConnFactoryName);        
      } catch (NamingException ne) {
          ne.printStackTrace();
          throw new ServiceLocatorException(ne);
      } catch (Exception e) {
          e.printStackTrace();
          throw new ServiceLocatorException(e);
      }
      return factory;
    }
    
   
    /**
     * This method obtains the topc itself for a caller
     * @return the Topic Destination to send messages to
     */
    public  Topic getTopic(String topicName) throws ServiceLocatorException {
      Topic topic = null;
      try {
          topic = (Topic)ic.lookup(topicName);
      } catch (NamingException ne) {
         throw new ServiceLocatorException(ne);
      } catch (Exception e) {
            throw new ServiceLocatorException(e);
      }
      return topic;
    }

}

