/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.taglibs.standard.examples.taglib;

import java.io.IOException;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.jstl.core.*;

import org.apache.taglibs.standard.examples.beans.Customer;

/**
 * <p>Tag handler for &lt;customerFmt&gt;
 *
 * @author Pierre Delisle
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:20:39 $
 */
public class CustomerFmtTag extends TagSupport {
    
    //*********************************************************************
    // Instance variables
    
    /** Holds value of property customer. */
    private Customer customer;
    
    /** Holds value of property fmt. */
    private String fmt;
    
    //*********************************************************************
    // Constructors
    
    public CustomerFmtTag() {
        super();
        init();
    }
    
    private void init() {
        customer = null;
        fmt = null;
    }    
    
    //*********************************************************************
    // TagSupport methods
    
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        try {
            if (fmt.equalsIgnoreCase("short")) {
                out.println(customer.getFirstName() + " " +
                customer.getLastName());
            } else if (fmt.equalsIgnoreCase("long")) {
                out.println(customer.getFirstName() + " " +
                customer.getLastName() + " " + customer.getAddress());
            } else {
                out.println("invalid format");
            }
        } catch (IOException ex) {}
        
        return SKIP_BODY;
    }
    
    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        init();
    }
    
    //*********************************************************************
    // Accessors
    
    /**
     * Getter for property customer.
     * @return Value of property customer.
     */
    public Customer getCustomer() {
        return customer;
    }
    
    /**
     * Setter for property customer.
     * @param customer New value of property customer.
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    /**
     * Getter for property fmt.
     * @return Value of property fmt.
     */
    public String getFmt() {
        return fmt;
    }
    
    /**
     * Setter for property fmt.
     * @param fmt New value of property fmt.
     */
    public void setFmt(String fmt) {
        this.fmt = fmt;
    }    
}
