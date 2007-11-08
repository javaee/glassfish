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
package com.sun.enterprise.server.logging.diagnostics;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Diagnostics class contains list of causes, diagnostic checks and the uri
 * to search for the latest and greatest diagnostics information based on
 * a message id. 
 *
 * @author Hemanth Puttaswamy
 */
public class Diagnostics {
    String messageId; 
    private ArrayList causes;
    private ArrayList checks;
    private String uri;

    public Diagnostics( String messageId ) {
        this.messageId = messageId;
        causes = new ArrayList();
        checks = new ArrayList();
    }

    public void addCause( String cause ) {
        causes.add( cause );
    }

    public void addCheck( String check ) {
        checks.add( check );
    }

    public void setPossibleCauses( ArrayList list ) {
        causes = list;
    }

    public void setDiagnosticChecks( ArrayList list ) {
        checks = list;
    }
 
    public void setURI( String uri ) {
        this.uri = uri;
    } 

    public String getMessageId( ) {
        return messageId;
    }

    public ArrayList getPossibleCauses( ) {
        return causes;
    }

    public ArrayList getDiagnosticChecks( ) {
        return checks;
    }

    public String getURI( ) {
        return uri;
    }

    /**
     * A Simple Debug print method to print the contents of this class.
     */
    public void print( ) {
        System.out.println( "---------------------------------" );
        System.out.println( "Diagnostics for MessageId = " + messageId );
        Iterator iterator = null;
        if( causes != null ) {
            iterator = causes.iterator( );
            System.out.println( "Causes --> " );
            while( iterator.hasNext( ) ) {
                System.out.println( iterator.next( ) );
            }
        }
        if( checks != null ) { 
            iterator = checks.iterator( );
            System.out.println( "Checks --> " );
            while( iterator.hasNext( ) ) {
                System.out.println( iterator.next( ) );
            }
        }
        System.out.println( "URI = " + uri );
        System.out.println( "---------------------------------" );
    }
}

