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
package com.sun.enterprise.jms;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.naming.*;
import javax.jms.*;
import com.sun.enterprise.util.FileUtil;
import com.sun.enterprise.ServerConfiguration;
import com.sun.enterprise.repository.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.messaging.jmq.jmsspi.JMSAdminFactory;
import com.sun.messaging.jmq.jmsspi.JMSAdmin;
import com.sun.logging.LogDomains;

import com.sun.appserv.server.util.Version;

/**
 * JMS Utilities.
 *
 */
public class IASJmsUtil {
    private IASJmsUtil() { /* disallow instantiation */ }
    
    private static final Logger _logger = LogDomains.getLogger(LogDomains.JMS_LOGGER);

    //Names of default JMS XA Connection Factories used by message bean container.
    public static final String MDB_CONTAINER_QUEUE_XACF =         "MDB_CONTAINER_QUEUE_CF__jmsxa_default";
    public static final String MDB_CONTAINER_TOPIC_XACF =         "MDB_CONTAINER_TOPIC_CF__jmsxa_default";
    public static final String MQ_DIR_NAME = "imq";

    private static final String DEFAULT_SERVER = "server";
    private static final String DEFAULT_MQ_INSTANCE = "imqbroker";

    public static final String DEFAULT_USER = "admin";
    public static final String DEFAULT_PASSWORD = "admin";
    public static final String DEFAULT_MAX_ACTIVE_CONSUMERS = "-1";
    public static final String MAX_ACTIVE_CONSUMERS_ATTRIBUTE = "MaxNumActiveConsumers";
    public static final String MAX_ACTIVE_CONSUMERS_PROPERTY = "maxNumActiveConsumers";

    private static final StringManager _sm = StringManager.getManager(IASJmsUtil.class); 

    private static final boolean debug = false;


    /**
     * Form the name of the internal XA Connection Factory 
     * for each javax.jms.ConnectionFactory
     */
    public static String getXAConnectionFactoryName(String 
                                                    connectionFactoryName) {
        return "JMSXA" + connectionFactoryName + "__jmsxa";
    }

    public static String getDestinationName(Destination destination) {
        String name = null;
        try {
            name = (destination instanceof javax.jms.Queue) ?
                ((javax.jms.Queue) destination).getQueueName() :
                ((javax.jms.Topic) destination).getTopicName();
        } catch(Exception e) {}
        return name;
    }

    public static void checkVersion(JMSAdmin ja) {
        float version = 0.0f;
        String vs = "?.?";

        try {
            vs = ja.getVersion();
            version = Float.parseFloat(vs);
        }
        catch (Exception e) {
            throw new RuntimeException("Error detected while parsing JMS " +
                "provider SPI version string (" + vs + ").");
        }

        if (version < 2.0 || version >= 3.0)
            throw new RuntimeException(
                "Incorrect JMS Provider SPI version detected (" +
                    vs + ")." +
                " Please make sure that you are using the correct" +
                " version of the JMS provider.");
    }

    /**
     * JMSAdminFactory is used to get a handle to the 
     * administration capabilities of a JMS provider.
     * Use Class.forName() to bootstrap an instance
     * in order to avoid compile-time dependencies on
     * implementation class names.
     */
    public static JMSAdminFactory getJMSAdminFactory() throws Exception {
        JMSAdminFactory jmsAdminFactory = null;

        Class jmsAdminFactoryClass = Class.forName("com.sun.messaging.jmq.admin.jmsspi.JMSAdminFactoryImpl");
        jmsAdminFactory = (JMSAdminFactory)jmsAdminFactoryClass.newInstance();

        checkVersion(jmsAdminFactory.getJMSAdmin());

        return jmsAdminFactory;
    }

    protected static JMSAdmin getJMSAdmin() throws Exception {
        JMSAdmin jmsAdmin = JmsProviderLifecycle.getJMSAdmin();
        if (jmsAdmin == null) {
            jmsAdmin = getJMSAdminFactory().getJMSAdmin();
        }
        return jmsAdmin;
    }

    /**
     * Validate a JMS Selector for syntactic correctness.
     * @throws Exception if system error contacting JMS Service
     * @throws JMSException if syntax error
     */
    public static void validateJMSSelector(String selector) 
        throws Exception, JMSException {

        getJMSAdmin().validateJMSSelector(selector);
    }

    /**
     * @return the ClientID property name or null
     */
    public static String clientIDPropertyName() throws Exception { 
        return getJMSAdmin().clientIDPropertyName();
    }

    /**
     * wrap a JMS standard XAQueue/TopicConnectionFactory or Queue/TopicConnectionFactory
     *
     * This method is used for foreign (non-built-in) JMS provider 
     *
     * @return a JMSXAConnectionFactory object
     * @throws JMSException if syntax error
     */
    public static Object wrapJMSConnectionFactoryObject(Object obj) 
        throws Exception, JMSException {

        return getJMSAdmin().wrapJMSConnectionFactoryObject(obj);
    }

    /**
     * Computes the instance name for the MQ broker.
     */
    public static String getBrokerInstanceName(String asDomain,
        String asInstance, JmsService js) {

        ElementProperty[] jmsProperties = js.getElementProperty();

        String instanceName = null;
        String suffix = null;

        if (jmsProperties != null) {
            for (int ii=0; ii < jmsProperties.length; ii++) {
                ElementProperty p = jmsProperties[ii];
                String name = p.getName();

                if (name.equals("instance-name"))
                    instanceName = p.getValue();
                if (name.equals("instance-name-suffix"))
                    suffix = p.getValue();
                if (name.equals("append-version") &&
                    Boolean.valueOf(p.getValue()).booleanValue()) {
                    suffix = Version.getMajorVersion() + "_" +
                        Version.getMinorVersion();
                }
            }
        }

        if (instanceName != null)
            return instanceName;

        if (asInstance.equals(DEFAULT_SERVER)) {
            instanceName = DEFAULT_MQ_INSTANCE;
        } else {
            instanceName = asDomain + "_" + asInstance;
        }  

        if (suffix != null)
            instanceName = instanceName + "_" + suffix;

        return instanceName;
    }

    /**
     * Getting default value for Maximum active Queue consumers.
     */
    public static String getDefaultMaxActiveConsumers () {
        return DEFAULT_MAX_ACTIVE_CONSUMERS;
    }

    /**
     * Getting Name Maximum active consumers property;
     */
    public static String getMaxActiveConsumersProperty () {
        return MAX_ACTIVE_CONSUMERS_PROPERTY;
    }

    /**
     * Getting Name Maximum active consumers attribute;
     */
    public static String getMaxActiveConsumersAttribute () {
        return MAX_ACTIVE_CONSUMERS_ATTRIBUTE;
    }
}
