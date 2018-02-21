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
 * 1.1 bean. 
 * @author mvatkina
 */


public class A1Bean implements javax.ejb.EntityBean {
    
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
        System.out.println("Debug: A1Bean ejbRemove");
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
        sqldate = new java.sql.Date(mydate.getTime());
        mydate.setTime(0);
        list.add(name);
    }
    
    public java.lang.String getName() {
        return name;
    }

    public java.util.ArrayList getList() {
        return list;
    }

    public java.util.Date getMyDate() {
        return mydate;
    }

    public java.sql.Date getSqlDate() {
        return sqldate;
    }

    public byte[] getBlb() {
        return blb;
    }

    public java.lang.String id1;
    public java.util.Date iddate;

    public java.lang.String name;
    public java.util.Date mydate;
    public java.sql.Date sqldate;
    public byte[] blb;
    public java.util.ArrayList list;

    public A1PK ejbCreate(java.lang.String name) throws javax.ejb.CreateException {

        this.name = name;
        id1 = name;
        long now = System.currentTimeMillis();
        iddate = new java.util.Date(0);
        mydate = new java.util.Date(now);

        return null;
    }
    
    public void ejbPostCreate(java.lang.String name) throws javax.ejb.CreateException { 
        blb = new byte[]{1,2};
        list = new java.util.ArrayList();
    }
    
}
