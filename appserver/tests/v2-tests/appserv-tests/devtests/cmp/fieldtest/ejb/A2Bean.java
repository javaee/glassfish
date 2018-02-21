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

package fieldtest;

import javax.ejb.*;
import javax.naming.*;

/**
 * 2.0 bean. 
 * @author mvatkina
 */


public abstract class A2Bean implements javax.ejb.EntityBean {
    
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
        System.out.println("Debug: A2Bean ejbRemove");
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

    public void update() {

        setSqlDate(new java.sql.Date(getMyDate().getTime()));
        java.util.Date d = getMyDate();
        d.setTime(0);
        setMyDate(d);
        java.util.ArrayList c = getList();
        c.add(getName());
        setList(c);
    }

    public abstract java.lang.String getId1() ;
    public abstract void setId1(java.lang.String s) ;

    public abstract java.util.Date getIddate();
    public abstract void setIddate(java.util.Date d);

    public abstract java.lang.String getName() ;
    public abstract void setName(java.lang.String s) ;

    public abstract java.util.ArrayList getList();
    public abstract void setList(java.util.ArrayList l);

    public abstract java.util.Date getMyDate();
    public abstract void setMyDate(java.util.Date d);

    public abstract java.sql.Date getSqlDate() ;
    public abstract void setSqlDate(java.sql.Date d) ;

    public abstract byte[] getBlb() ;
    public abstract void setBlb(byte[] b) ;

    public A2PK ejbCreate(java.lang.String name) throws javax.ejb.CreateException {

        long now = System.currentTimeMillis();
        setId1(name);
        setName(name);
        setIddate(new java.util.Date(0));
        setMyDate(new java.util.Date(now));

        return null;
    }
    
    public void ejbPostCreate(java.lang.String name) throws javax.ejb.CreateException { 
        setBlb(new byte[]{1,2});
        setList(new java.util.ArrayList());
    }
    
}
