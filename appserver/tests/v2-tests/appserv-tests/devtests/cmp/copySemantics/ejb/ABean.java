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

package test;

import javax.ejb.*;
import javax.naming.*;

/**
 * @author mvatkina
 */


public abstract class ABean implements javax.ejb.EntityBean {
    
    private javax.ejb.EntityContext context;
    
    
    /**
     * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
     */
    public void setEntityContext(javax.ejb.EntityContext aContext) {
        context=aContext;
    }
    
    
    /**
     * @see javax.ejb.EntityBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    
    /**
     * @see javax.ejb.EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    
    /**
     * @see javax.ejb.EntityBean#ejbRemove()
     */
    public void ejbRemove() {
        System.out.println("Debug: ABean ejbRemove");
    }
    
    
    /**
     * @see javax.ejb.EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context=null;
    }
    
    
    /**
     * @see javax.ejb.EntityBean#ejbLoad()
     */
    public void ejbLoad() {
        
    }
    
    
    /**
     * @see javax.ejb.EntityBean#ejbStore()
     */
    public void ejbStore() {
        
    }
    
    public abstract Integer getId();
    public abstract void setId(Integer id);

    public abstract java.lang.String getName();
    public abstract void setName(java.lang.String name);

    public abstract java.util.Date getDate();
    public abstract void setDate(java.util.Date date);

    public abstract byte[] getBlb();
    public abstract void setBlb(byte[] b);

    public java.lang.Integer ejbCreate(Integer id, java.lang.String name, java.util.Date date, byte[] b) 
        throws javax.ejb.CreateException {

        setId(id);
        setName(name);
        setDate(date);
        setBlb(b);

        return null;
    }
    
    public void ejbPostCreate(Integer id, java.lang.String name, java.util.Date date, byte[] b) 
        throws javax.ejb.CreateException { }
    
    public void test() {
        java.util.Date d1 = getDate();
        System.out.println("Debug: ABean d1: " + d1);
        
        d1.setYear(2000);
        System.out.println("Debug: ABean d1 after setYear: " + d1);

        java.util.Date d2 = getDate();
        System.out.println("Debug: ABean d2: " + d2);
        if (d1.equals(d2))
            throw new EJBException("Same d1 and d2!");

        setDate(d1);
        d2.setMonth(2);
        System.out.println("Debug: ABean d2 after setMonth: " + d2);

        d1 = getDate();
        System.out.println("Debug: ABean d1: " + d1);
        if (d1.equals(d2)) 
            throw new EJBException("Same d1 and d2 after set!"); 

        setDate(null);
        if (getDate() != null)
            throw new EJBException("Date is not null after set!");

        byte[] b = getBlb();
        System.out.println("Debug: ABean b[0]: " + b[0]);
        b[0] = 90;

        byte[] b1 = getBlb(); 
        System.out.println("Debug: ABean b[0]: " + b[0]);
        System.out.println("Debug: ABean b1[0]: " + b1[0]);

        if (b[0] == b1[0])
            throw new EJBException("Same b and b1!"); 
 
        setBlb(b);
        b1[1] = 90; 
        System.out.println("Debug: ABean b[0]: " + b[0]);
        System.out.println("Debug: ABean b[1]: " + b[1]);
        System.out.println("Debug: ABean b1[0]: " + b1[0]);
        System.out.println("Debug: ABean b1[1]: " + b1[1]);

        b = getBlb();  

        System.out.println("Debug: ABean b[0]: " + b[0]);
        System.out.println("Debug: ABean b[1]: " + b[1]);
        System.out.println("Debug: ABean b1[0]: " + b1[0]);
        System.out.println("Debug: ABean b1[1]: " + b1[1]);

        if (b[1] == b1[1])
            throw new EJBException("Same b and b1 after set!"); 

        setBlb(null);
        if (getBlb() != null)
            throw new EJBException("Blob is not null after set!");

    }
}
