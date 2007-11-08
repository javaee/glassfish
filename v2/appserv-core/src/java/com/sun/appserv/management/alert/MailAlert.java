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
package com.sun.appserv.management.alert;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.management.NotificationListener;
import javax.management.Notification;

/**
 * Class MailAlert sends out alerts through E-mail.
 *
 * @AUTHOR: Hemanth Puttaswamy
 */
public class MailAlert implements NotificationListener {
    private String subject; 
    
    private String recipients;

    private String  mailResourceName;

    private String  fromAddress;

    private InternetAddress fromSMTPAddress;

    private String mailSMTPHost;

    private boolean includeDiagnostics;

    Logger alertLogger = null;


    /**
     * Zero Arg constructor that will be used by the Alert Configuration 
     * framework.
     */
    public MailAlert( ) {
        alertLogger = LogDomains.getAlertLogger( ); 
        mailSMTPHost = "localhost";
        subject = "Alert from SJAS AppServer";
        setFromAddress("SJASAlert@sun.com");
        includeDiagnostics = false;
        if( alertLogger.isLoggable( Level.FINE ) ) {
            alertLogger.log( Level.FINE, "MailAlert instantiated" );
        }
    }

    /**
     * Set's the subjectLine for the E-mail Alert.
     */
    public void setSubject( String subject ) {
        this.subject = subject;
        if( alertLogger.isLoggable( Level.FINE ) ) {
            alertLogger.log( Level.FINE,
                "setSubject called with -> " + subject );
        }
      
    }


    /**
     * getter added for testing purpose.
     */
    String getSubject( ) { return subject; }

    /**
     * Set's the recipient e-mail addresses to recieve alerts. This is a
     * comma separated list of e-mail ids.
     */
    public void setRecipients( String recipients ) {
        this.recipients = recipients;
        if( alertLogger.isLoggable( Level.FINE ) ) {
            alertLogger.log( Level.FINE,
                "setRecipients called with -> " + recipients );
        }
    }

    /**
     * getter for testing purpose.
     */
    String getRecipients( ) { return recipients; }

    /**
     * Set's the SMTP Host used to send out the mail. This is usually a 
     * mail proxy.
     */
    public void setMailSMTPHost( String mailSMTPHost ) {
        this.mailSMTPHost = mailSMTPHost;
        if( alertLogger.isLoggable( Level.FINE ) ) {
            alertLogger.log( Level.FINE, 
                "setMailSMTPHost called with ->" + mailSMTPHost );
        }
    }

    /**
     * getter for Testing purpose.
     */
    String getMailSMTPHost( ) { return mailSMTPHost; }

    /**
     * A JNDI name that will be used to look up the MailResource.
     */
    public void setMailResourceName( String mailResourceName ) {
        //_REVISIT_: Currently not using it.
        this.mailResourceName = mailResourceName;
    }

    String getMailResourceName( ) { return mailResourceName; }

    /**
     * From Address for the e-mail alert. 
     */
    public void setFromAddress( String fromAddress ) {
        if( alertLogger.isLoggable( Level.FINE ) ) {
            alertLogger.log( Level.FINE, 
                "setFromAddress called with address ->" + fromAddress );
        }
        this.fromAddress = fromAddress;
        try {
            fromSMTPAddress = new InternetAddress( fromAddress );
        } catch( Exception e ) {
            alertLogger.log( Level.FINE, 
                "Exception in MailAlert.setFromAddress ->" + e );
            // _REVISIT_: Exception log and make sure there is no recursion
        }
    }

    String getFromAddress( ) { return fromAddress; }

    /**
     * If IncludeDiagnostics is true, then Diagnostics will be sent along with
     * the error. Note: Diagnostics may be available for some of the Logged
     * message alerts only.
     */
    public void setIncludeDiagnostics( boolean includeDiagnostics ) {
        this.includeDiagnostics = includeDiagnostics;
    }

    boolean getIncludeDiagnostics( ) { return includeDiagnostics; }

    /**
     *  javax.managament.NotificationHandler interface implementation.
     */
    public void handleNotification( Notification notification, Object handback)
    {
        try {
            Properties props = System.getProperties( );
            props.put("mail.smtp.host", mailSMTPHost );
            Session session = Session.getDefaultInstance( props, null );
            MimeMessage message = new MimeMessage(session);
            message.setFrom( fromSMTPAddress );
            message.setRecipients( Message.RecipientType.TO, recipients );
            message.setSubject( subject );
            message.setText( notification.toString() );
            Transport.send( message );
            
        } catch( Exception e ) {
            alertLogger.log( Level.FINE, 
                "Exception in MailAlert.handleNotification ->" + e );
            // _REVISIT_: Add the appropriate exception and make sure there
            // is no recursion here.
            // Add a new Key Value Pair to makesure that WARNING message here
            // doesn't result in an alert resulting in a loop
        }
    }


    /**
     *  These are Unit Test Method provided for internal testing only.
     */
    public void setUnitTestData( String unitTestData ) {
        alertLogger.log( Level.FINE, 
            "UnitTestData -> " + unitTestData );
        new UnitTest( 
            UnitTest.UNIT_TEST_MAIL_ALERT, unitTestData, this ).start();
    }
}




