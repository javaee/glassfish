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

import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 * This is a Unit Test class provided solely for internal unit testing.
 *
 * @AUTHOR: Hemanth Puttaswamy
 */
public class UnitTest extends  Thread {
    static final int UNIT_TEST_MAIL_ALERT = 1; 

    static final int UNIT_TEST_MAIL_FILTER = 2; 

    private final int unitTestMode;

    private final String unitTestData;

    private final Object mailAlertOrFilter;

    /**
     * UnitTest class is used to test MailAlert and Filter facility.
     * unitTestMode can be to test Mail Alert or Mail Filter
     * unitTestData is a list of NameValue pairs seprated by ';' 
     * mailAlertOrFilter is the instance on which the test needs to be done.
     */
    UnitTest( int unitTestMode, String unitTestData, Object mailAlertOrFilter) {
        this.unitTestMode = unitTestMode;
        this.unitTestData = unitTestData;
        this.mailAlertOrFilter = mailAlertOrFilter;
    }

    public void run( ) {
        try { 
            // We need to sleep for 1 second to allow the complete 
            // initialization of the object which needs to be tested.
            sleep( 1000 );
        }
        catch( Exception e )
        {
            e.hashCode();  // silence FindBugs
        }
        StringTokenizer tokenizer = new StringTokenizer( unitTestData, ";" );
        switch( unitTestMode ) {
            case UNIT_TEST_MAIL_ALERT:  
                unitTestMailAlert( tokenizer );
                break;
 
            case UNIT_TEST_MAIL_FILTER:
                unitTestMailFilter( tokenizer );
                break;

            default:
                System.out.println( "INVALID OPTION FOR UNIT TEST OF ALERTS");
                break;
         }
    } 

    /**
     * Unit Tests Mail Alert class.
     */ 
    private void unitTestMailAlert(StringTokenizer tokenizer) {
        boolean testStatus = true;
        MailAlert mailAlert = (MailAlert) mailAlertOrFilter;
        while( tokenizer.hasMoreTokens( ) ) {
            String token = tokenizer.nextToken( );
            StringTokenizer nameAndValue = new StringTokenizer( token, "=" );
            String name = nameAndValue.nextToken( );
            String value = nameAndValue.nextToken( );  
            try {
                if( name.equals("subject" ) ) {
                    if( !value.equals(mailAlert.getSubject() ) ){
                        unitTestFailed( name, value, mailAlert.getSubject( ) );
                        testStatus = false;
                    }
                } else if( name.equals( "recipients" ) ) {
                    if( !value.equals(mailAlert.getRecipients() ) ){
                        unitTestFailed( name, value, mailAlert.getRecipients());
                        testStatus = false;
                    }
                } else if( name.equals( "mailSMTPHost" ) ) {
                    if( !value.equals(mailAlert.getMailSMTPHost() ) ){
                        unitTestFailed( name, value, 
                            mailAlert.getMailSMTPHost());
                        testStatus = false;
                    }
                } else if( name.equals( "fromAddress" ) ) {
                    if( !value.equals(mailAlert.getFromAddress() ) ){
                        unitTestFailed( name, value,mailAlert.getFromAddress());
                        testStatus = false;
                    }
                } else if( name.equals( "includeDiagnostics" ) ) {
                    if( !value.equals(
                        (new Boolean(
                            mailAlert.getIncludeDiagnostics())).toString() ) )
                    {
                        unitTestFailed( name, value, 
                        (new Boolean(
                            mailAlert.getIncludeDiagnostics())).toString() );
                        testStatus = false;
                    }
                }
            }catch( Exception e ) {
                System.out.println( "EXCEPTION IN UNIT TEST FOR MAIL ALERT " +
                    e );
                testStatus = false;
            }
        }
        if( testStatus ) {
            LogDomains.getAlertLogger().log( Level.SEVERE, 
                "Testing SEVERE alert.." );
            System.out.println( "UNIT TEST FOR MAIL ALERT PASSED..." );
        }
    }

    private void unitTestMailFilter(StringTokenizer tokenizer) {
        boolean testStatus = true;
        MailFilter mailFilter = (MailFilter) mailAlertOrFilter;
        while( tokenizer.hasMoreTokens( ) ) {
            String token = tokenizer.nextToken( );
            StringTokenizer nameAndValue = new StringTokenizer( token, "=" );
            String name = nameAndValue.nextToken( );
            String value = nameAndValue.nextToken( );  
            try {
                if( name.equals("filterWarningMessages" ) ) {
                    if( !value.equals(
                        (new Boolean(
                            mailFilter.getFilterWarningMessages())).toString()))
                    {
                        unitTestFailed( name, value, 
                            (new Boolean(
                            mailFilter.getFilterWarningMessages())).toString());
                        testStatus = false;
                    }
                } 
            }catch( Exception e ) {
                System.out.println( "EXCEPTION IN UNIT TEST FOR MAIL FILTER " +
                    e );
                testStatus = false;
            }
        }
        if( testStatus ) {
            System.out.println( "UNIT TEST FOR MAIL FILTER PASSED..." );
        }
    }

    private void unitTestFailed( String propertyName, String expectedValue,
        String realValue ) 
    {
        final String msg =  "UNIT TEST FAILED FOR ALERTS : " +
                            "\nPropertyName -> " + propertyName +
                            "\nexpectedValue -> " + expectedValue +
                            "\nrealValue -> " + realValue;
        System.out.println( msg );
        throw new RuntimeException( msg );
    }
}




