/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.cmp.readonly.ejb;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.appserv.ejb.ReadOnlyBeanNotifier;
import com.sun.appserv.ejb.ReadOnlyBeanHelper;

public abstract class StudentBean implements EntityBean {

    public abstract String getStudentId();
    public abstract void setStudentId(String studentId);

    public abstract String getName();
    public abstract void setName(String name);

    private EntityContext context;

   /**
     * Returns the Name of a student.
     */
    public String getNameTx() {
        return getName();
    }

    /**
     * Sets the Name of a student.
     */
    public void setName(String name, boolean notify) {
        // Only called for read-write version of Student.
        setName(name);
        if( notify ) {
            try {
                System.out.println("Notifying read-only bean of update to " +
                                   "read-mostly Student " + name);
                ReadOnlyBeanNotifier studentNotifier = 
                    ReadOnlyBeanHelper.getReadOnlyBeanNotifier
                    ("java:comp/env/ejb/ReadOnlyStudent");
                
                // Update read-only version
                studentNotifier.refresh(getStudentId());
            } catch(Exception e) {
                throw new EJBException(e);
            }
        }
    }

    public String ejbCreate(String studentId, String name) throws CreateException {
        setStudentId(studentId);
        setName(name);
        return studentId;
    }

    public void ejbPostCreate(String studentId, String name) throws CreateException {

    }

    public void ejbHomeTestLocalCreate(String pk) {

        StudentLocalHome localHome = (StudentLocalHome)
            context.getEJBLocalHome();

        boolean createSucceeded = false;
        try {
            localHome.create(pk, "mike");
            createSucceeded = true;
        } catch(EJBException ejbex) {
            //
        } catch(CreateException ce) {
            throw new EJBException("unexpected exception");
        }

        if( createSucceeded ) {
            throw new EJBException("cmp read-only bean create should " +
                                   " have thrown an exception");
        } else {
            System.out.println("Successfully caught exception while trying " +
                               " to do a create on a read-only cmp bean");
        }

    }


    public void ejbHomeTestFind(String pk) {

        StudentHome studentHome = (StudentHome)
            context.getEJBHome();

        try {
            Student student = studentHome.findByPrimaryKey(pk);

            long before = System.nanoTime();
            studentHome.findByRemoteStudent(student);
            long after = System.nanoTime();

            studentHome.findByRemoteStudent(student);
            long after2 = System.nanoTime();

            System.out.println("1st query of " + pk + " took " +
                               (after - before) + " nano secs");

            System.out.println("2nd query of " + pk + " took " +
                               (after2 - after) + " nano secs");
            
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
        
    }

    public void ejbHomeTestLocalFind(String pk) {

        StudentLocalHome localHome = (StudentLocalHome)
            context.getEJBLocalHome();

        try {
            StudentLocal student = localHome.findByPrimaryKey(pk);

            long before = System.nanoTime();
            localHome.findByLocalStudent(student);
            long after = System.nanoTime();

            localHome.findByLocalStudent(student);
            long after2 = System.nanoTime();

            System.out.println("1st query of " + pk + " took " +
                               (after - before) + " nano secs");

            System.out.println("2nd query of " + pk + " took " +
                               (after2 - after) + " nano secs");
            
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
        
    }

    public void ejbHomeTestLocalRemove(String pk) {
        
        
        StudentLocalHome localHome = (StudentLocalHome)
            context.getEJBLocalHome();

        boolean removeHomeSucceeded = false;
        try {
            localHome.remove(pk);
            removeHomeSucceeded = true;
        } catch(EJBException e) {
            //
        } catch(RemoveException re) {
            throw new EJBException("unexpected exception");
        }

        if( removeHomeSucceeded ) {
            throw new EJBException("cmp read-only bean Home remove should " +
                                   " have thrown an exception");
        } else {
            System.out.println("Successfully caught exception while trying " +
                               " to do a remove on a read-only cmp Home");
        }
        
        boolean removeSucceeded = false;
        try {
            StudentLocal student = localHome.findByPrimaryKey(pk);
            student.remove();
            removeSucceeded = true;
        } catch(EJBException e) {
            //
        } catch(FinderException fe) {
            throw new EJBException("unexpected exception");
        } catch(RemoveException re) {
            throw new EJBException("unexpected exception");
        }

        if( removeSucceeded ) {
            throw new EJBException("cmp read-only bean remove should " +
                                   " have thrown an exception");
        } else {
            System.out.println("Successfully caught exception while trying " +
                               " to do a remove on a read-only cmp ");
        }

        
    }
    

    public void ejbRemove() {
        System.out.println("StudentBean.ejbRemove called");
    }

    public void setEntityContext(EntityContext context) {
        this.context = context;       
    }

    public void unsetEntityContext() {     
        this.context = null;
    }

    public void ejbActivate() {     
    }

    public void ejbPassivate() {      
    }

    public void ejbLoad() {        
    }

    public void ejbStore() {        
    }
  

    
}
