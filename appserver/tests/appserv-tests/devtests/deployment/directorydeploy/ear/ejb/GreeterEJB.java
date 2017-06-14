/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2017 Oracle and/or its affiliates. All rights reserved.
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

package samples.ejb.stateless.simple.ejb;

import java.util.*; 
import java.io.*; 

/**
 * A simple stateless bean for the HelloWorld application. This bean implements one
 * business method as declared by the remote interface.
 */
public class GreeterEJB implements javax.ejb.SessionBean { 

    private  javax.ejb.SessionContext m_ctx = null; 
   
    /**
     * Sets the session context. Required by EJB spec.
     * @param ctx A SessionContext object.
     */
    public void setSessionContext(javax.ejb.SessionContext ctx) { 
        m_ctx = ctx; 
    } 

    /**
     * Creates a bean. Required by EJB spec.
     * @exception throws CreateException.
     */
    public void ejbCreate() throws javax.ejb.EJBException, javax.ejb.CreateException { 
        System.out.println("ejbCreate() on obj " + this); 
    } 

    /**
     * Removes the bean. Required by EJB spec.
     */
    public void ejbRemove() { 
        System.out.println("ejbRemove() on obj " + this); 
    } 

    /**
     * Loads the state of the bean from secondary storage. Required by EJB spec.
     */
    public void ejbActivate() { 
        System.out.println("ejbActivate() on obj " + this); 
    } 

    /**
     * Serializes the state of the bean to secondary storage. Required by EJB spec.
     */
    public void ejbPassivate() { 
        System.out.println("ejbPassivate() on obj " + this); 
    } 

    /**
     * Required by EJB spec.
     */
    public void Greeter() { 
    } 


    /**
     * Returns a greeting, based on the time of the day.
     * @return returns a greeting as a string.
     */
    public String getGreeting() { 
        String message = null; 
        Calendar calendar = new GregorianCalendar(); 
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY); 
        if(currentHour < 12) message = "morning"; 
        else { 
          if( (currentHour >= 12) && 
            (calendar.get(Calendar.HOUR_OF_DAY) < 18)) message = "afternoon"; 
          else message = "evening"; 
        } 
        return message; 
    } 
} 
