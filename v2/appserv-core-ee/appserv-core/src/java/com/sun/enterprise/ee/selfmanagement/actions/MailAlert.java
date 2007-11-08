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

/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
package com.sun.enterprise.ee.selfmanagement.actions;

import javax.management.NotificationListener;
import javax.management.Notification;
import javax.mail.internet.MimeMessage;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.MessagingException;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Date;
import java.util.Properties;

import com.sun.enterprise.deployment.MailConfiguration;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;


/**
 * Mail Alert utility action. The end user can use this as one 
 * of the predefined actions and configure a rule to dispatch
 * an mail alert upon the occurance of the associated event.
 *
 * @author Pankaj Jairath
 */
public class MailAlert implements MailAlertMBean, 
                                  NotificationListener
{
    static private String DEFAULT_MAIL_RESOURCE = "mail/_default";
    
    private static final String PKGNAME =
              "com.sun.enterprise.ee.selfmanagement.actions";
    
        /** Logger for self management service */
    private static Logger _logger =  null;
    
    /** Local Strings manager for the class */
    private static StringManager localStrings = null;
    
    private static String msgPrefix = null;
    
    static {
        _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
        localStrings = StringManager.getManager(PKGNAME);
        String instanceName = System.getProperty("com.sun.aas.instanceName");
        String clusterName = System.getProperty("com.sun.aas.clusterName");
        String domainName = System.getProperty("com.sun.aas.domainName");
        
        if ( clusterName != null) {
            msgPrefix = localStrings.getString("mailalert.event_occured_cluster",
                                               instanceName, clusterName,
                                               domainName);
        } else { 
            msgPrefix = localStrings.getString("mailalert.event_occured_instance", 
                                               instanceName, domainName);
        }    
    }
    
    /** Attribute : Subject */
    private String subject = null;

    /** Attribute : Message */
    private String message = null;

    /** Attribute : Recipients */
    private String recipients = null;

    /** Attribute : From */
    private String from = null;

    /** Attribute : MailResource Name */
    private String mailResource = null;
    
    /** Mail session */
    private Session mailSession = null;
    
   /* Creates a new instance of MailAlert */
    public MailAlert() {
        //mailResource = DEFAULT_MAIL_RESOURCE;
        
    }

   /**
    * Get Subject of the mal alert
    */
    public String getSubject() {
        return subject;
    }

   /**
    * Set Subject of the mal alert
    */
    public synchronized void setSubject(String value) {
        subject = value;
    }

   /**
    * Get Mesage for the alert
    */
    public String getMessage() {
        return message;
    }

   /**
    * Set Mesage for the alert
    */
    public synchronized void setMessage(String value) {
        message = value;
    }

   /**
    * Get Alert Reciptients
    */
    public String getRecipients() {
        return recipients;
    }

   /**
    * Set Alert Reciptients
    */
    public synchronized void setRecipients(String value) {
        recipients = value;
    }

   /**
    * Get From address
    */
    public String getFrom() {
        return from;
    }

   /**
    * Set From address
    */
    public synchronized void setFrom(String value) {
        from = value;
    }

     /**
    * Get mail resource used 
    */
    public String getMailResource() {
        return mailResource;
    }
   /**
    * Set User name of the sender account
    */
    public synchronized void setMailResource(String value) {
        mailResource = value;
        mailSession = null;
    }
     
    public synchronized void handleNotification(Notification event, 
                                                Object handback) {
        //formulate the message using the configured attributes
        //use the session from the mail resource to send this alert
        
        try {
            if (mailSession == null) {
                invokeSession(event);
            }

            if (recipients != null) {
                
                MimeMessage alert = new MimeMessage(mailSession);
                if (from != null) {
                    alert.setFrom(new InternetAddress(from));
                } else {
                    alert.setFrom();
                }
            
                if (subject != null) {
                    alert.setSubject(subject);
                } else {
                    alert.setSubject(localStrings.getString("mailalert.alert_subject"));
                }
                     
                alert.setSentDate(new Date());
                alert.setRecipients(Message.RecipientType.TO, 
                                    InternetAddress.parse(recipients,false));
                
                
                if (message != null) {
                    alert.setText(msgPrefix + message);
                } else {
                    alert.setText(msgPrefix + event.toString());
                }
        
                Transport.send(alert);
        
           } else {
                // log error cannot send message no recipients;
                _logger.log(Level.WARNING,"smgt.no_recipients", event.toString());
           }
        } catch (MessagingException ex) {
            _logger.log(Level.WARNING, "sgmt.util_error_messaging", new Object[] 
                        {event.toString(),ex.toString()} );
        } catch (NamingException ex) {
            System.out.println(ex);
            _logger.log(Level.WARNING, "sgmt.util_error_mailresource", 
                        new Object[] {event.toString(),mailResource});
        }catch (Throwable ex) {
            _logger.log(Level.WARNING,"sgmt.util_error_messaging",ex);
        }
    }
    
    /** Utility API */
    public void notification(Notification notif, Object callBack) {
        handleNotification(notif, callBack);
    }
    /**
     * Obtains the mail session from the configured mail
     * resource for this action
     *
     * @return Session
     *         Mail session to use for this action
     */
    private void invokeSession(Notification event) throws NamingException {        
        
        MailConfiguration mailConfiguration = null;
     
        if (mailResource != null) {
            InitialContext ic = new InitialContext();
          
            /* 
             *Currently an internal object is returned by ic lookup.
             *This jndi lookup needs to be fixed to return Session type.
             */                    
            //mailSession = (Session) ic.lookup(mailResource); 
            mailConfiguration = (MailConfiguration) ic.lookup(mailResource);
            mailSession = Session.getInstance(mailConfiguration.getMailProperties(),
                                              null);            
        } else {
            mailSession = Session.getDefaultInstance(new Properties());
        }    
    }
}

